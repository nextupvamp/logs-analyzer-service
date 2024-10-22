package backend.academy.log_data;

import java.time.ZonedDateTime;

@SuppressWarnings("checkstyle:RecordComponentNumber")
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
