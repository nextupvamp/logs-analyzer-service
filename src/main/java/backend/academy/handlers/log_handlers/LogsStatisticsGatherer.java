package backend.academy.handlers.log_handlers;

import backend.academy.data.LogPaths;
import backend.academy.io.LogsReader;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import backend.academy.data.LogData;
import backend.academy.data.LogsStatistics;
import lombok.SneakyThrows;

public class LogsStatisticsGatherer {
    private final List<String> readPaths = new ArrayList<>();
    private final List<Long> bytesSent = new ArrayList<>();
    private final Map<String, Integer> remoteAddresses = new HashMap<>();
    private final Map<String, Integer> remoteUsers = new HashMap<>();
    private final Map<String, Integer> requestMethods = new HashMap<>();
    private final Map<String, Integer> requestResources = new HashMap<>();
    private final Map<Short, Integer> statuses = new HashMap<>();
    private final AtomicInteger requestsAmount = new AtomicInteger(0);
    private final ZonedDateTime from;
    private final ZonedDateTime to;
    private final LogPaths paths;
    private final LogsHandler logsHandler;
    private final String filterMethod;
    private final Pattern filterValueRegex;
    private final Map<ZonedDateTime, Integer> requestsOnDate = new HashMap<>();
    private Predicate<LogData> dateTimePredicate;
    private Predicate<LogData> fieldFilterPredicate;

    public LogsStatisticsGatherer(
        LogPaths paths,
        final ZonedDateTime from,
        final ZonedDateTime to,
        String filterMethod,
        Pattern filterValueRegex,
        LogsHandler logsHandler
    ) {
        this.from = from;
        this.to = to;
        this.filterMethod = filterMethod;
        this.filterValueRegex = filterValueRegex;
        this.paths = paths;
        this.logsHandler = logsHandler;
    }

    @SneakyThrows
    public LogsStatistics gatherStatistics() {
        initPredicates(from, to, filterMethod, filterValueRegex);

        try (LogsReader logsReader = new LogsReader()) {
            List<Path> files = paths.paths();
            List<URL> urls = paths.urls();

            if (files != null) {
                for (Path path : files) {
                    Path fileName = path.getFileName();
                    if (fileName != null) {
                        readPaths.add(fileName.toString());
                    }

                    gatherData(logsReader.readFromFileAsStream(path, logsHandler));
                }
            }

            if (urls != null) {
                for (URL url : urls) {
                    readPaths.add(url.getFile());
                    gatherData(logsReader.readFromUrlAsStream(url, logsHandler));
                }
            }
        }

        return new LogsStatistics(
            readPaths,
            remoteAddresses,
            remoteUsers,
            from,
            to,
            requestsOnDate,
            requestMethods,
            requestResources,
            statuses,
            requestsAmount.get(),
            countAverageBytesSent(bytesSent),
            count95pBytesSent(bytesSent)
        );
    }

    @SuppressWarnings({"checkstyle:IllegalIdentifierName", "checkstyle:LambdaParameterName"})
    private void gatherData(Stream<LogData> logDataStream) {
        logDataStream
            .filter(dateTimePredicate)
            .filter(fieldFilterPredicate)
            .peek(_ -> requestsAmount.incrementAndGet())
            .forEach(it -> {
                bytesSent.add(it.bytesSent());
                remoteAddresses.merge(it.remoteAddress(), 1, Integer::sum);
                remoteUsers.merge(it.remoteUser(), 1, Integer::sum);
                requestMethods.merge(it.requestMethod(), 1, Integer::sum);
                requestResources.merge(it.requestResource(), 1, Integer::sum);
                statuses.merge(it.status(), 1, Integer::sum);
                requestsOnDate.merge(it.timeLocal(), 1, Integer::sum);
            });
    }

    public static long countAverageBytesSent(List<Long> bytesSent) {
        if (bytesSent.isEmpty()) {
            return 0;
        }

        return bytesSent.stream().reduce(0L, Long::sum) / bytesSent.size();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static long count95pBytesSent(List<Long> bytesSent) {
        if (bytesSent.isEmpty()) {
            return 0;
        }
        bytesSent.sort(Long::compareTo);

        return bytesSent.get((int) (bytesSent.size() * 0.95));
    }

    private Predicate<LogData> buildDateTimePredicate(ZonedDateTime from, ZonedDateTime to) {
        return it -> {
            boolean before = true;
            boolean after = true;
            if (from != null) {
                after = !it.timeLocal().isBefore(from);
            }
            if (to != null) {
                before = !it.timeLocal().isAfter(to);
            }

            return before && after;
        };
    }

    @SuppressWarnings({"checkstyle:IllegalIdentifierName", "checkstyle:LambdaParameterName"})
    private Predicate<LogData> buildFieldFilterPredicate(String filterField, Pattern filterValueRegex) {
        if (filterField == null || filterValueRegex == null) {
            return _ -> true;
        }
        return switch (filterField) {
            case "address" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.remoteAddress());
                return matcher.matches();
            };
            case "user" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.remoteUser());
                return matcher.matches();
            };
            case "method" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.requestMethod());
                return matcher.matches();
            };
            case "resource" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.requestResource());
                return matcher.matches();
            };
            case "httpVersion" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.requestHttpVersion());
                return matcher.matches();
            };
            case "status" -> it -> {
                Matcher matcher = filterValueRegex.matcher(String.valueOf(it.status()));
                return matcher.matches();
            };
            case "referer" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.httpReferer());
                return matcher.matches();
            };
            case "userAgent" -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.httpUserAgent());
                return matcher.matches();
            };
            default -> throw new IllegalArgumentException("Unknown filter field: " + filterField);
        };
    }

    private void initPredicates(ZonedDateTime from, ZonedDateTime to, String filterField, Pattern filterValueRegex) {
        dateTimePredicate = buildDateTimePredicate(from, to);
        fieldFilterPredicate = buildFieldFilterPredicate(filterField, filterValueRegex);
    }
}

