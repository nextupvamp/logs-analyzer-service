package backend.academy.handlers;

import backend.academy.data.ArgsData;
import backend.academy.data.PathsData;
import backend.academy.io.formatters.ADocFormatter;
import backend.academy.io.formatters.MarkdownFormatter;
import backend.academy.io.formatters.TextFormatter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;

@SuppressFBWarnings // suppress warnings concerned with user file paths input
public class ArgsHandler {
    public static final String PATH_ARG = "--path";
    public static final String FROM_ARG = "--from";
    public static final String TO_ARG = "--to";
    public static final String FORMAT_ARG = "--format";
    public static final String FILTER_FIELD_ARG = "--filter-field";
    public static final String FILTER_VALUE_ARG = "--filter-value";

    private final String[] args;
    private final List<Path> paths = new ArrayList<>();
    private final List<URI> uris = new ArrayList<>();
    private ZonedDateTime from;
    private ZonedDateTime to;
    private TextFormatter format;
    private String filterField;
    private Pattern filterValuePattern;

    public ArgsHandler(String[] args) {
        this.args = args;
    }

    @SuppressWarnings({"checkstyle:ModifiedControlVariable", "checkstyle:CyclomaticComplexity"})
    public ArgsData handle() {
        // the loop iterates through parameters ignoring args.
        // if it meets parameter on its way, an exception will be thrown
        for (int i = 0; i < args.length; ++i) {
            if (isParameter(args[i])) {
                i = switch (args[i]) {
                    case PATH_ARG -> getPaths(i);
                    case FROM_ARG -> getFromTime(i);
                    case TO_ARG -> getToTime(i);
                    case FORMAT_ARG -> getFormat(i);
                    case FILTER_FIELD_ARG -> getFilterField(i);
                    case FILTER_VALUE_ARG -> getFilterValuePattern(i);
                    default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
                };
            } else {
                throw new IllegalArgumentException("Excepted argument on argument position " + i);
            }
        }
        if (paths.isEmpty() && uris.isEmpty()) {
            throw new IllegalArgumentException("Missing paths");
        }
        if (filterField != null && filterValuePattern == null) {
            throw new IllegalArgumentException("Excepted filter value");
        }
        if (filterField == null && filterValuePattern != null) {
            throw new IllegalArgumentException("Excepted filter field");
        }
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("From date is after To date");
            }
            if (to.isBefore(from)) {
                throw new IllegalArgumentException("To date is before From date");
            }
        }
        if (format == null) {
            format = new MarkdownFormatter();
        }

        return ArgsData.builder()
            .paths(new PathsData(uris, paths))
            .from(from)
            .to(to)
            .format(format)
            .filterField(filterField)
            .filterValuePattern(filterValuePattern)
            .build();
    }

    /**
     * Iterates through paths until meet another arg or the end
     * and adds them into collection.
     *
     * @param pos position of <code>--path</code> arg
     * @return position of the last resolved path
     */
    @SneakyThrows
    public int getPaths(int pos) {
        int newPos = pos + 1;
        // loop starts from arg parameters
        for (; newPos < args.length; ++newPos) {
            String currentArg = args[newPos];
            // end loop if another arg
            if (isParameter(currentArg)) {
                break;
            }

            if (currentArg.matches("^(http|https|ftp|file)://.*")) {
                uris.add(URI.create(currentArg));
            } else {
                // if path doesn't contain any glob patterns
                // that mean we deal with a single file
                if (findFirstGlobSymbolIndex(currentArg) == -1) {
                    paths.add(Paths.get(currentArg));
                } else {
                    Path rootDir = extractRootDir(currentArg);
                    String pattern = extractPattern(currentArg);
                    Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"
                                + pattern);
                            Path relativePath = rootDir.relativize(file);
                            if (matcher.matches(relativePath)) {
                                paths.add(file.toAbsolutePath());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        }
        return newPos - 1; // returns pos of the last arg
    }

    public int getFromTime(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isParameter(args[newPos])) {
            from = ZonedDateTime.parse(args[newPos]);
        } else {
            throw new IllegalArgumentException("Excepted date after --from on argument position " + newPos);
        }

        return newPos;
    }

    public int getToTime(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isParameter(args[newPos])) {
            to = ZonedDateTime.parse(args[newPos]);
        } else {
            throw new IllegalArgumentException("Excepted date after --to on argument position " + newPos);
        }

        return newPos;
    }

    public int getFormat(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isParameter(args[newPos])) {
            format = switch (args[newPos]) {
                case "markdown" -> new MarkdownFormatter();
                case "adoc" -> new ADocFormatter();
                default -> throw new IllegalArgumentException("Unsupported format: " + args[newPos]);
            };
        } else {
            throw new IllegalArgumentException("Excepted format after --format on argument position " + newPos);
        }

        return newPos;
    }

    public int getFilterField(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isParameter(args[newPos])) {
            filterField = args[newPos];
        } else {
            throw new IllegalArgumentException(
                "Excepted filter field after --filter-field on argument position " + newPos);
        }

        return newPos;
    }

    public int getFilterValuePattern(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isParameter(args[newPos])) {
            filterValuePattern = Pattern.compile(args[newPos]);
        } else {
            throw new IllegalArgumentException(
                "Excepted filter value after --filter-value on argument position " + newPos);
        }

        return newPos;
    }

    private Path extractRootDir(String pathString) {
        int specialSymbolIndex = findFirstGlobSymbolIndex(pathString);

        if (specialSymbolIndex == -1) {
            return Paths.get(pathString);
        }

        int separatorBeforeSpecialSymbolIndex =
            getFirstSeparatorBeforeSpecialSymbolIndex(pathString, specialSymbolIndex);

        if (separatorBeforeSpecialSymbolIndex == -1) {
            return Paths.get(pathString);
        }

        String editedPathString = pathString.substring(0, separatorBeforeSpecialSymbolIndex);
        Path currentDirectory = Paths.get(".").toAbsolutePath();

        return Paths.get(currentDirectory.toString(), editedPathString).normalize();
    }

    private int getFirstSeparatorBeforeSpecialSymbolIndex(String pathString, int specialSymbolIndex) {
        int separatorBeforeSpecialSymbolIndex = -1;
        String separator = FileSystems.getDefault().getSeparator();
        for (int i = specialSymbolIndex; i >= 0; --i) {
            if (pathString.startsWith(separator, i)) {
                separatorBeforeSpecialSymbolIndex = i;
                break;
            }
        }
        return separatorBeforeSpecialSymbolIndex;
    }

    private String extractPattern(String pathString) {
        int specialSymbolIndex = findFirstGlobSymbolIndex(pathString);

        if (specialSymbolIndex == -1) {
            return pathString;
        }

        int separatorBeforeSpecialSymbolIndex =
            getFirstSeparatorBeforeSpecialSymbolIndex(pathString, specialSymbolIndex);

        if (separatorBeforeSpecialSymbolIndex == -1) {
            return pathString;
        }

        return pathString.substring(separatorBeforeSpecialSymbolIndex + 1);
    }

    private int findFirstGlobSymbolIndex(String pathString) {
        int specialSymbolIndex = -1;
        for (int i = 0; i != pathString.length(); ++i) {
            char ch = pathString.charAt(i);
            if (ch == '*' || ch == '?' || ch == '[' || ch == '{') {
                specialSymbolIndex = i;
                break;
            }
        }

        return specialSymbolIndex;
    }

    private boolean isParameter(String arg) {
        return arg.matches("^--.*");
    }
}
