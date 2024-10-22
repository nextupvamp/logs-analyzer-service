package backend.academy.io.formatters;

public class MarkdownFormatter implements TextFormatter {

    @Override
    public String toHeaderLine(String line, int level) {
        return "";
    }

    @Override
    public String toTableHeader(String key, String value) {
        return "";
    }

    @Override
    public String toTableRow(String key, String value) {
        return "";
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
