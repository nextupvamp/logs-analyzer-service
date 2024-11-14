package backend.academy.handlers.log_handlers;

import backend.academy.data.LogData;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NginxLogsHandler implements LogsHandler {
    public static final String LOG_REGEX = "(?<address>\\S+) - (?<user>\\S+) \\[(?<time>.*)] "
        + "\"(?<method>GET|POST|HEAD|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH) (?<resource>\\S+) "
        + "(?<http>[^\"]+)\" (?<status>\\d{3}) (?<bytes>\\d+) \"(?<referer>[^\"]+)\" \"(?<userAgent>[^\"]+)\".*";
    public static final Pattern LOG_PATTERN = Pattern.compile(LOG_REGEX);
    public static final String LOG_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    public static final Locale LOG_DATE_LOCALE = Locale.ENGLISH;
    public static final String REMOTE_ADDRESS_GROUP = "address";
    public static final String USER_GROUP = "user";
    public static final String TIME_GROUP = "time";
    public static final String METHOD_GROUP = "method";
    public static final String RESOURCE_GROUP = "resource";
    public static final String HTTP_GROUP = "http";
    public static final String STATUS_GROUP = "status";
    public static final String BYTES_GROUP = "bytes";
    public static final String REFERER_GROUP = "referer";
    public static final String USER_AGENT_GROUP = "userAgent";

    @Override
    public LogData parseLogLineData(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.matches()) {
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
        } else {
            throw new IllegalArgumentException("Invalid log format: " + line);
        }
    }
}
