package ru.nextupvamp.model.data;

import lombok.Builder;

import java.time.ZonedDateTime;

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
    public static final LogData IGNORED = LogData.builder().build();
}
