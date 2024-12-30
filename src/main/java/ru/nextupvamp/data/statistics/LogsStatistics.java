package ru.nextupvamp.data.statistics;

public record LogsStatistics(
    NativeLogsStatistics nativeLogsStatistics,
    ComputedLogsStatistics computedLogsStatistic
) {
}
