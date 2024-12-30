package ru.nextupvamp.data.statistics;

import ru.nextupvamp.data.PathsData;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
@SuppressWarnings("checkstyle:RecordComponentNumber")
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
