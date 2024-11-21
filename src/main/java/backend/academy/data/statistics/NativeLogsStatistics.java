package backend.academy.data.statistics;

import backend.academy.data.PathsData;
import lombok.Builder;
import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record NativeLogsStatistics(
    PathsData paths,
    Map<String, Integer> remoteAddresses,
    Map<String, Integer> remoteUsers,
    ZonedDateTime from,
    ZonedDateTime to,
    Map<ZonedDateTime, Integer> requestsOnDate,
    Map<String, Integer> requestMethods,
    Map<String, Integer> requestResources,
    Map<Short, Integer> statuses
) {

}
