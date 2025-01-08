package ru.nextupvamp.model.data;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record LogsStatistics(
        int ignoredRows,
        Map<String, Integer> remoteAddresses,
        Map<String, Integer> remoteUsers,
        ZonedDateTime from,
        ZonedDateTime to,
        Map<ZonedDateTime, Integer> requestsOnDate,
        Map<String, Integer> requestMethods,
        Map<String, Integer> requestResources,
        Map<Short, Integer> statuses,
        int requestsAmount,
        long averageBytesSent,
        long p95BytesSent
) {
}
