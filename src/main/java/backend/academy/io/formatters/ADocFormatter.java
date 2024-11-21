package backend.academy.io.formatters;

public class ADocFormatter implements TextFormatter {
    public static final int MAX_HEADER_LEVEL = 6;
    public static final int MIN_HEADER_LEVEL = 1;
    public static final String TABLE_FOOTER = "|===";
    public static final String ADOC_FILE_FROMAT = ".adoc";
    public static final String MONOSPACED_QUOTE = "`";
    public static final char COLUMN_SEPARATOR = '|';
    public static final String HEADER_SIGN = "=";
    public static final char SPACE = ' ';
    public static final String TABLE_HEADER_START = "[cols=\"";
    public static final String TABLE_HEADER_END = "\"]";

    @Override
    public String toHeaderLine(String line, int level) {
        // if level is greater or smaller than max or min allowed value
        // it will be set to max or min value respectively
        return HEADER_SIGN.repeat(Math.min(MAX_HEADER_LEVEL, Math.max(MIN_HEADER_LEVEL, level)))
            + SPACE + line;
    }

    @Override
    public String toTableHeader(String... columns) {
        StringBuilder sb = new StringBuilder();
        sb.append(TABLE_HEADER_START);
        for (int i = 0; i != columns.length; ++i) {
            sb.append('1');
            if (i != columns.length - 1) {
                sb.append(',');
            }
        }
        sb.append(TABLE_HEADER_END).append(System.lineSeparator()).append(TABLE_FOOTER).append(System.lineSeparator());

        for (String column : columns) {
            sb.append(COLUMN_SEPARATOR).append(column);
        }
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String toTableRow(String... columns) {
        StringBuilder sb = new StringBuilder();
        for (String column : columns) {
            sb.append(COLUMN_SEPARATOR).append(column).append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public String getTableFooter() {
        return TABLE_FOOTER;
    }

    @Override
    public String toMonospaced(String line) {
        return MONOSPACED_QUOTE + line + MONOSPACED_QUOTE;
    }

    @Override
    public String getFileFormat() {
        return ADOC_FILE_FROMAT;
    }
}
