package ru.nextupvamp.io.formatters;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TextFormats {
    MARKDOWN("markdown", new MarkdownFormatter()),
    ADOC("adoc", new ADocFormatter());

    private final String format;
    private final TextFormatter formatter;
}
