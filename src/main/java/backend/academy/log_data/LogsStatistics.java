package backend.academy.log_data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@SuppressWarnings("checkstyle:RecordComponentNumber")
public record LogsStatistics(
    List<String> paths,
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
