package backend.academy.handlers.log_handlers;

import backend.academy.log_data.LogData;
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

    @Override
    public LogData parseLogLineData(String line) {
        return null;
    }
}
