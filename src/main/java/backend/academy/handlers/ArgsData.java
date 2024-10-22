package backend.academy.handlers;

import backend.academy.io.formatters.TextFormatter;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public record ArgsData(
    LogPaths paths,
    ZonedDateTime from,
    ZonedDateTime to,
    TextFormatter format,
    String filterField,
    Pattern filterValuePattern
) {
}
