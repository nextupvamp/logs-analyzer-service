package ru.nextupvamp.statistics.dto;

import ru.nextupvamp.parser.LogData;

import java.util.function.Predicate;

public record FilterPredicates(
        Predicate<LogData> dateTimePredicate,
        Predicate<LogData> fieldFilterPredicate) {
}
