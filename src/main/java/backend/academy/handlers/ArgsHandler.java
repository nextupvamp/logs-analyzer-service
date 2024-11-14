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
        // the loop iterates through keys ignoring parameters.
        // if it meets parameter on its way, an exception will be thrown
        for (int i = 0; i < args.length; ++i) {
            if (isKey(args[i])) {
                i = switch (args[i]) {
                    case "--path" -> getPaths(i);
                    case "--from" -> getFromTime(i);
                    case "--to" -> getToTime(i);
                    case "--format" -> getFormat(i);
                    case "--filter-field" -> getFilterField(i);
                    case "--filter-value" -> getFilterValuePattern(i);
                    default -> throw new IllegalArgumentException("Unknown key: " + args[i]);
                };
            } else {
                throw new IllegalArgumentException("Excepted key on argument position " + i);
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
     * Iterates through paths until meet another key or the end
     * and adds them into collection.
     *
     * @param pos position of <code>--path</code> key
     * @return position of the last resolved path
     */
    @SneakyThrows
    public int getPaths(int pos) {
        int newPos = pos + 1;
        // loop starts from key parameters
        for (; newPos < args.length; ++newPos) {
            String currentArg = args[newPos];
            // end loop if another key
            if (isKey(currentArg)) {
                break;
            }

            if (currentArg.matches("^(http|https|ftp|file)://.*")) {
                uris.add(URI.create(currentArg));
            } else {
                // if path doesn't contain any glob patterns
                // that mean we deal with a single file
                if (findSpecialSymbolIndex(currentArg) == -1) {
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

    /**
     * Tries to parse a zoned date time right after
     * the <code>--from</code> key.
     *
     * @param pos position of <code>--from</code> key
     * @return position of parsed zoned date time
     */
    public int getFromTime(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isKey(args[newPos])) {
            from = ZonedDateTime.parse(args[newPos]);
        } else {
            throw new IllegalArgumentException("Excepted date after --from on argument position " + newPos);
        }

        return newPos;
    }

    /**
     * Tries to parse a zoned date time right after
     * the <code>--to</code> key.
     *
     * @param pos position of <code>--to</code> key
     * @return position of parsed zoned date time
     */
    public int getToTime(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isKey(args[newPos])) {
            to = ZonedDateTime.parse(args[newPos]);
        } else {
            throw new IllegalArgumentException("Excepted date after --to on argument position " + newPos);
        }

        return newPos;
    }

    /**
     * Tries to parse a format right after
     * the <code>--format</code> key.
     *
     * @param pos position of <code>--format</code> key
     * @return position of parsed format
     */
    public int getFormat(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isKey(args[newPos])) {
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

    /**
     * Tries to parse a filter field right after
     * the <code>--filter-field</code> key.
     *
     * @param pos position of <code>--filter-field</code> key
     * @return position of parsed filter field
     */
    public int getFilterField(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isKey(args[newPos])) {
            filterField = args[newPos];
        } else {
            throw new IllegalArgumentException(
                "Excepted filter field after --filter-field on argument position " + newPos);
        }

        return newPos;
    }

    /**
     * Tries to parse a filter value right after
     * the <code>--filter-value</code> key.
     *
     * @param pos position of <code>--filter-value</code> key
     * @return position of parsed filter value
     */
    public int getFilterValuePattern(int pos) {
        int newPos = pos + 1;
        if (newPos < args.length && !isKey(args[newPos])) {
            filterValuePattern = Pattern.compile(args[newPos]);
        } else {
            throw new IllegalArgumentException(
                "Excepted filter value after --filter-value on argument position " + newPos);
        }

        return newPos;
    }

    /**
     * Finds the last file in the path without any
     * pattern symbols and extracts path of this
     * file from the original path <code>pathString</code>.
     *
     * @param pathString original path
     * @return path of the last file without any pattern symbols
     */
    private Path extractRootDir(String pathString) {
        int specialSymbolIndex = findSpecialSymbolIndex(pathString);

        if (specialSymbolIndex == -1) {
            return Paths.get(pathString);
        }

        int separatorBeforeSpecialSymbolIndex = getSeparatorBeforeSpecialSymbolIndex(pathString, specialSymbolIndex);

        if (separatorBeforeSpecialSymbolIndex == -1) {
            return Paths.get(pathString);
        }

        String editedPathString = pathString.substring(0, separatorBeforeSpecialSymbolIndex);
        Path currentDirectory = Paths.get(".").toAbsolutePath();

        return Paths.get(currentDirectory.toString(), editedPathString).normalize();
    }

    /**
     * Finds the index of file separator before
     * glob-pattern symbol.
     *
     * @param pathString         full file path
     * @param specialSymbolIndex index of pattern symbol
     * @return index of special pattern symbol
     */
    private int getSeparatorBeforeSpecialSymbolIndex(String pathString, int specialSymbolIndex) {
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

    /**
     * Extracts glob pattern from filepath. The method
     * finds the first file with any pattern symbols
     * and truncates everything before it in the path.
     *
     * @param pathString file path
     * @return glob file pattern
     */
    private String extractPattern(String pathString) {
        int specialSymbolIndex = findSpecialSymbolIndex(pathString);

        if (specialSymbolIndex == -1) {
            return pathString;
        }

        int separatorBeforeSpecialSymbolIndex = getSeparatorBeforeSpecialSymbolIndex(pathString, specialSymbolIndex);

        if (separatorBeforeSpecialSymbolIndex == -1) {
            return pathString;
        }

        return pathString.substring(separatorBeforeSpecialSymbolIndex + 1);
    }

    /**
     * Finds an index of the first glob pattern
     * symbol.
     *
     * @param pathString file path
     * @return index of the first glob pattern symbol
     */
    private int findSpecialSymbolIndex(String pathString) {
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

    /**
     * Checks if argument is the key.
     *
     * @param arg argument
     * @return <code>true</code> if an argument is the key,
     *     <code>false</code> if it isn't
     */
    private boolean isKey(String arg) {
        return arg.matches("^--.*");
    }
}
