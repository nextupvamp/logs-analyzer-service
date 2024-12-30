package ru.nextupvamp.model.data;

import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record Filters(
    ZonedDateTime fromTime,
    ZonedDateTime toTime,
    String filterField,
    String filterValueRegex
) {
}
