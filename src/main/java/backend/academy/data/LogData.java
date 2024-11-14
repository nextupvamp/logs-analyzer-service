package backend.academy.data;

import java.time.ZonedDateTime;
import lombok.Builder;

@SuppressWarnings("checkstyle:RecordComponentNumber")
@Builder
public record LogData(
    String remoteAddress,
    String remoteUser,
    ZonedDateTime timeLocal,
    String requestMethod,
    String requestResource,
    String requestHttpVersion,
    short status,
    long bytesSent,
    String httpReferer,
    String httpUserAgent
) {
}
