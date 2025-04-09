package ru.nextupvamp.model.handlers;

import ru.nextupvamp.dto.LogData;

import java.util.function.Predicate;

public record FilterPredicates(
        Predicate<LogData> dateTimePredicate,
        Predicate<LogData> fieldFilterPredicate) {
}
