package backend.academy.io.formatters;

public class MarkdownFormatter implements TextFormatter {
    public static final int MAX_HEADER_LEVEL = 6;
    public static final int MIN_HEADER_LEVEL = 1;

    @Override
    public String toHeaderLine(String line, int level) {
        // 6 if greater than 6 and 1 if less then 1
        return "#".repeat(Math.min(MAX_HEADER_LEVEL, Math.max(MIN_HEADER_LEVEL, level)))
            + ' ' + line;
    }

    @Override
    public String toTableHeader(String... columns) {
        String header = System.lineSeparator() + '|'
            + " --- |".repeat(columns.length);
        return toTableRow(columns) + header;
    }

    @Override
    public String toTableRow(String... columns) {
        StringBuilder sb = new StringBuilder();
        sb.append('|');
        for (String column : columns) {
            sb.append(' ').append(column).append(' ').append('|');
        }
        return sb.toString();
    }

    @Override
    public String getTableFooter() {
        return "";
    }

    @Override
    public String toMonospaced(String line) {
        return "`" + line + "`";
    }

    @Override
    public String getFileFormat() {
        return ".md";
    }
}
