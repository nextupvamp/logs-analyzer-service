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
    public String toTableHeader(String key, String value) {
        return toTableRow(key, value) + "\n| --- | --- |";
    }

    @Override
    public String toTableRow(String key, String value) {
        return "| " + key + " | " + value + " |";
    }

    @Override
    public String getTableFooter() {
        return "";
    }

    @Override
    public String toMonospaced(String line) {
        return "";
    }
}
