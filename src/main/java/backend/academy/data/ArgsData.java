package backend.academy.data;

import backend.academy.io.formatters.TextFormatter;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public record ArgsData(
    PathsData paths,
    ZonedDateTime from,
    ZonedDateTime to,
    TextFormatter format,
    String filterField,
    Pattern filterValuePattern
) {
}
