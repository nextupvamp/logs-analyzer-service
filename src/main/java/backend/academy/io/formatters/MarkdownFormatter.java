package backend.academy.io.formatters;

public class MarkdownFormatter implements TextFormatter {
    public static final int MAX_HEADER_LEVEL = 6;
    public static final int MIN_HEADER_LEVEL = 1;
    public static final String MD_FILE_FORMAT = ".md";
    public static final String HEADER_SIGN = "#";
    public static final String MONOSPACED_QUOTE = "`";
    public static final char SPACE = ' ';
    public static final char COLUMN_SEPARATOR = '|';
    public static final String HEADER_SEPARATOR = " --- |";

    @Override
    public String toHeaderLine(String line, int level) {
        // 6 if greater than 6 and 1 if less then 1
        return HEADER_SIGN.repeat(Math.min(MAX_HEADER_LEVEL, Math.max(MIN_HEADER_LEVEL, level)))
            + SPACE + line;
    }

    @Override
    public String toTableHeader(String... columns) {
        String header = System.lineSeparator() + COLUMN_SEPARATOR
            + HEADER_SEPARATOR.repeat(columns.length);
        return toTableRow(columns) + header;
    }

    @Override
    public String toTableRow(String... columns) {
        StringBuilder sb = new StringBuilder();
        sb.append(COLUMN_SEPARATOR);
        for (String column : columns) {
            sb.append(SPACE).append(column).append(SPACE).append(COLUMN_SEPARATOR);
        }
        return sb.toString();
    }

    @Override
    public String getTableFooter() {
        return ""; // markdown doesn't have table footers
    }

    @Override
    public String toMonospaced(String line) {
        return MONOSPACED_QUOTE + line + MONOSPACED_QUOTE;
    }

    @Override
    public String getFileFormat() {
        return MD_FILE_FORMAT;
    }
}
