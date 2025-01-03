package ru.nextupvamp.model.data;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record Filters(
        ZonedDateTime fromDate,
        ZonedDateTime toDate,
        Map<String, String> filterMap // filter field : filter value regex
) {
    public static final Filters EMPTY = Filters.builder().build();
}
