package ru.nextupvamp.statistics.dto;

import java.time.ZonedDateTime;
import java.util.Map;

public record StatisticsDto(
        int ignoredRows,
        Map<String, Integer> remoteAddresses,
        Map<String, Integer> remoteUsers,
        ZonedDateTime fromDate,
        ZonedDateTime toDate,
        Map<ZonedDateTime, Integer> requestsOnDate,
        Map<String, Integer> requestMethods,
        Map<String, Integer> requestResources,
        Map<Short, Integer> statuses,
        int requestsAmount,
        double averageBytesSent,
        long p95BytesSent
) {
}
