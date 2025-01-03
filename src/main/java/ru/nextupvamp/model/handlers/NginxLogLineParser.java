package ru.nextupvamp.model.handlers;

import org.springframework.stereotype.Component;
import ru.nextupvamp.model.data.LogData;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NginxLogLineParser implements LogLineParser {
    private static final Pattern LOG_PATTERN = Pattern.compile("(?<address>\\S+) - (?<user>\\S+) \\[(?<time>.*)] "
        + "\"(?<method>GET|POST|HEAD|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH) (?<resource>\\S+) "
        + "(?<http>[^\"]+)\" (?<status>\\d{3}) (?<bytes>\\d+) \"(?<referer>[^\"]+)\" \"(?<userAgent>[^\"]+)\".*");
    private static final String LOG_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final Locale LOG_DATE_LOCALE = Locale.ENGLISH;
    static final String REMOTE_ADDRESS_GROUP = "address";
    static final String USER_GROUP = "user";
    static final String TIME_GROUP = "time";
    static final String METHOD_GROUP = "method";
    static final String RESOURCE_GROUP = "resource";
    static final String HTTP_GROUP = "http";
    static final String STATUS_GROUP = "status";
    static final String BYTES_GROUP = "bytes";
    static final String REFERER_GROUP = "referer";
    static final String USER_AGENT_GROUP = "userAgent";

    @Override
    public LogData parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return LogData.IGNORED;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(LOG_DATE_FORMAT, LOG_DATE_LOCALE);

        return LogData.builder()
            .remoteAddress(matcher.group(REMOTE_ADDRESS_GROUP))
            .remoteUser(matcher.group(USER_GROUP))
            .timeLocal(ZonedDateTime.parse(matcher.group(TIME_GROUP), dateTimeFormatter))
            .requestMethod(matcher.group(METHOD_GROUP))
            .requestResource(matcher.group(RESOURCE_GROUP))
            .requestHttpVersion(matcher.group(HTTP_GROUP))
            .status(Short.parseShort(matcher.group(STATUS_GROUP)))
            .bytesSent(Long.parseLong(matcher.group(BYTES_GROUP)))
            .httpReferer(matcher.group(REFERER_GROUP))
            .httpUserAgent(matcher.group(USER_AGENT_GROUP))
            .build();

    }
}
