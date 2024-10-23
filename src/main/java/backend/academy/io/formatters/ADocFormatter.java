package backend.academy.io.formatters;

public class ADocFormatter implements TextFormatter {
    public static final int MAX_HEADER_LEVEL = 6;
    public static final int MIN_HEADER_LEVEL = 1;
    public static final String TABLE_BOUND = "|===";

    @Override
    public String toHeaderLine(String line, int level) {
        // 6 if greater than 6 and 1 if less then 1
        return "=".repeat(Math.min(MAX_HEADER_LEVEL, Math.max(MIN_HEADER_LEVEL, level)))
            + ' ' + line;
    }

    @Override
    public String toTableHeader(String... columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("[cols=\"");
        for (int i = 0; i != columns.length; ++i) {
            sb.append('1');
            if (i != columns.length - 1) {
                sb.append(',');
            }
        }
        sb.append("\"]").append(System.lineSeparator()).append(TABLE_BOUND).append(System.lineSeparator());

        for (String column : columns) {
            sb.append('|').append(column);
        }
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String toTableRow(String... columns) {
        StringBuilder sb = new StringBuilder();
        for (String column : columns) {
            sb.append('|').append(column).append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String getTableFooter() {
        return TABLE_BOUND;
    }

    @Override
    public String toMonospaced(String line) {
        return "`" + line + "`";
    }

    @Override
    public String getFileFormat() {
        return ".adoc";
    }
}
