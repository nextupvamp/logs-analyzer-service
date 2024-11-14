package backend.academy.data;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;

@SuppressWarnings("checkstyle:RecordComponentNumber")
@Builder
public record LogsStatistics(
    PathsData paths,
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
