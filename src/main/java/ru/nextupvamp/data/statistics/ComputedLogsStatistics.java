package ru.nextupvamp.data.statistics;

import lombok.Builder;

@Builder
public record ComputedLogsStatistics(
    int requestsAmount,
    long averageBytesSent,
    long p95BytesSent
) {
}
