package backend.academy.data.statistics;

public record LogsStatistics(
    NativeLogsStatistics nativeLogsStatistics,
    ComputedLogsStatistics computedLogsStatistic
) {
}
