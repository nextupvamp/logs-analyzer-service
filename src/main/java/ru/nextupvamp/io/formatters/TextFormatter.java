package ru.nextupvamp.io.formatters;

public interface TextFormatter {
    String toHeaderLine(String line, int level);

    String toTableHeader(String... columns);

    String toTableRow(String... columns);

    String getTableFooter();

    String toMonospaced(String line);

    String getFileFormat();
}
