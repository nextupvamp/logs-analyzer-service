package backend.academy.io.formatters;

public interface TextFormatter {
    String toHeaderLine(String line, int level);

    String toTableHeader(String key, String value);

    String toTableRow(String key, String value);

    String getTableFooter();

    String toMonospaced(String line);
}
