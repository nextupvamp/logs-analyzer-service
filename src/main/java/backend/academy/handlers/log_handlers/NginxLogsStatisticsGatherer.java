package backend.academy.handlers.log_handlers;

import backend.academy.data.LogData;
import backend.academy.data.LogsStatistics;
import backend.academy.data.PathsData;
import backend.academy.io.LogsReader;
import java.net.URI;
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
import lombok.Builder;
import lombok.SneakyThrows;

public class NginxLogsStatisticsGatherer {
    private final List<Path> readPaths = new ArrayList<>();
    private final List<URI> readUris = new ArrayList<>();
    private final List<Long> bytesSent = new ArrayList<>();
    private final Map<String, Integer> remoteAddresses = new HashMap<>();
    private final Map<String, Integer> remoteUsers = new HashMap<>();
    private final Map<String, Integer> requestMethods = new HashMap<>();
    private final Map<String, Integer> requestResources = new HashMap<>();
    private final Map<Short, Integer> statuses = new HashMap<>();
    private final AtomicInteger requestsAmount = new AtomicInteger(0);
    private final Map<ZonedDateTime, Integer> requestsOnDate = new HashMap<>();

    private final ZonedDateTime from;
    private final ZonedDateTime to;
    private final PathsData paths;
    private final NginxLogsHandler logsHandler;
    private final String filterField;
    private final Pattern filterValuePattern;
    private Predicate<LogData> dateTimePredicate;
    private Predicate<LogData> fieldFilterPredicate;

    @Builder
    private NginxLogsStatisticsGatherer(
        PathsData paths,
        final ZonedDateTime from,
        final ZonedDateTime to,
        String filterField,
        Pattern filterValuePattern,
        NginxLogsHandler logsHandler
    ) {
        this.from = from;
        this.to = to;
        this.filterField = filterField;
        this.filterValuePattern = filterValuePattern;
        this.paths = paths;
        this.logsHandler = logsHandler;
    }

    @SneakyThrows
    public LogsStatistics gatherStatistics() {
        initPredicates(from, to, filterField, filterValuePattern);

        try (LogsReader logsReader = new LogsReader()) {
            List<Path> files = paths.paths();
            List<URI> uris = paths.uris();

            if (files != null) {
                gatherDataFromFiles(files, logsReader);
            }

            if (uris != null) {
                gatherDataFromUrls(uris, logsReader);
            }
        }

        return LogsStatistics.builder()
            .paths(new PathsData(readUris, readPaths))
            .remoteAddresses(remoteAddresses)
            .remoteUsers(remoteUsers)
            .from(from)
            .to(to)
            .requestsOnDate(requestsOnDate)
            .requestMethods(requestMethods)
            .requestResources(requestResources)
            .statuses(statuses)
            .requestsAmount(requestsAmount.get())
            .averageBytesSent(countAverageBytesSent(bytesSent))
            .p95BytesSent(count95pBytesSent(bytesSent))
            .build();
    }

    private void gatherDataFromUrls(List<URI> uris, LogsReader logsReader) {
        for (URI uri : uris) {
            readUris.add(uri);
            gatherData(logsReader.readFromUriAsStream(uri, logsHandler));
        }
    }

    private void gatherDataFromFiles(List<Path> files, LogsReader logsReader) {
        for (Path path : files) {
            Path fileName = path.getFileName();
            if (fileName != null) {
                readPaths.add(fileName);
            }

            gatherData(logsReader.readFromFileAsStream(path, logsHandler));
        }
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
            case NginxLogsHandler.REMOTE_ADDRESS_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.remoteAddress());
                return matcher.matches();
            };
            case NginxLogsHandler.USER_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.remoteUser());
                return matcher.matches();
            };
            case NginxLogsHandler.METHOD_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.requestMethod());
                return matcher.matches();
            };
            case NginxLogsHandler.RESOURCE_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.requestResource());
                return matcher.matches();
            };
            case NginxLogsHandler.HTTP_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.requestHttpVersion());
                return matcher.matches();
            };
            case NginxLogsHandler.STATUS_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(String.valueOf(it.status()));
                return matcher.matches();
            };
            case NginxLogsHandler.REFERER_GROUP -> it -> {
                Matcher matcher = filterValueRegex.matcher(it.httpReferer());
                return matcher.matches();
            };
            case NginxLogsHandler.USER_AGENT_GROUP -> it -> {
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

