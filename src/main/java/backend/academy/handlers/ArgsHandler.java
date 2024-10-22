package backend.academy.handlers;

public class ArgsHandler {
    private final String[] args;
    private final List<Path> paths = new ArrayList<>();
    private final List<URL> urls = new ArrayList<>();
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
        if (paths.isEmpty() && urls.isEmpty()) {
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

        return new ArgsData(
            new LogPaths(urls, paths),
            from,
            to,
            format,
            filterField,
            filterValuePattern
        );
    }

}
