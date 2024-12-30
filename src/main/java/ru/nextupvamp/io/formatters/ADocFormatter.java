package ru.nextupvamp.io.formatters;

public class ADocFormatter implements TextFormatter {
    private static final int MAX_HEADER_LEVEL = 6;
    private static final int MIN_HEADER_LEVEL = 1;
    private static final String TABLE_FOOTER = "|===";
    private static final String ADOC_FILE_FROMAT = ".adoc";
    private static final String MONOSPACED_QUOTE = "`";
    private static final char COLUMN_SEPARATOR = '|';
    private static final String HEADER_SIGN = "=";
    private static final char SPACE = ' ';
    private static final String TABLE_HEADER_START = "[cols=\"";
    private static final String TABLE_HEADER_END = "\"]";

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
