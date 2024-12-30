package ru.nextupvamp.model.handlers;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogData;
import ru.nextupvamp.model.data.LogsStatistics;

@Component
@RequiredArgsConstructor
public class NginxLogsStatisticsGatherer implements LogsStatisticsGatherer {
    private static final double THE_95_TH_PERCENTILE = 0.95;

    private final NginxLogsHandler logsHandler;

    @SneakyThrows
    public LogsStatistics gatherStatisticsFromFile(Path file, Filters filters) {
        try (LogsReader logsReader = new LogsReader()) {
            if (file != null && Files.exists(file)) {
                return gatherData(logsReader.readFromFileAsStream(file, logsHandler), filters);
            }
        }

        throw new IllegalArgumentException();
    }

    @SneakyThrows
    public LogsStatistics gatherStatisticsFromUri(URI uri, Filters filters) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        try (LogsReader logsReader = new LogsReader()) {
            return gatherData(logsReader.readFromUriAsStream(uri, logsHandler), filters);
        }
    }

    private LogsStatistics gatherData(Stream<LogData> logDataStream, Filters filters) {
        List<Long> bytesSent = new ArrayList<>();
        Map<String, Integer> remoteAddresses = new HashMap<>();
        Map<String, Integer> remoteUsers = new HashMap<>();
        Map<String, Integer> requestMethods = new HashMap<>();
        Map<String, Integer> requestResources = new HashMap<>();
        Map<Short, Integer> statuses = new HashMap<>();
        AtomicInteger requestsAmount = new AtomicInteger(0);
        Map<ZonedDateTime, Integer> requestsOnDate = new HashMap<>();

        FilterPredicates filterPredicates = initPredicates(filters.fromTime(), filters.toTime(), filters.filterField(),
            filters.filterValueRegex());
        Predicate<LogData> dateTimePredicate = filterPredicates.dateTimePredicate();
        Predicate<LogData> fieldFilterPredicate = filterPredicates.fieldFilterPredicate();

        logDataStream.filter(dateTimePredicate)
            .filter(fieldFilterPredicate)
            .peek(ignored -> requestsAmount.incrementAndGet())
            .forEach(it -> {
                bytesSent.add(it.bytesSent());
                remoteAddresses.merge(it.remoteAddress(), 1, Integer::sum);
                remoteUsers.merge(it.remoteUser(), 1, Integer::sum);
                requestMethods.merge(it.requestMethod(), 1, Integer::sum);
                requestResources.merge(it.requestResource(), 1, Integer::sum);
                statuses.merge(it.status(), 1, Integer::sum);
                requestsOnDate.merge(it.timeLocal(), 1, Integer::sum);
            });

        return LogsStatistics.builder()
            .remoteAddresses(remoteAddresses)
            .remoteUsers(remoteUsers)
            .from(filters.fromTime())
            .to(filters.toTime())
            .requestsOnDate(requestsOnDate)
            .requestMethods(requestMethods)
            .requestResources(requestResources)
            .statuses(statuses)
            .requestsAmount(requestsAmount.get())
            .averageBytesSent(countAverageBytesSent(bytesSent))
            .p95BytesSent(count95pBytesSent(bytesSent))
            .build();
    }

    public static long countAverageBytesSent(List<Long> bytesSent) {
        if (bytesSent.isEmpty()) {
            return 0;
        }

        return bytesSent.stream().reduce(0L, Long::sum) / bytesSent.size();
    }

    public static long count95pBytesSent(List<Long> bytesSent) {
        if (bytesSent.isEmpty()) {
            return 0;
        }
        bytesSent.sort(Long::compareTo);

        return bytesSent.get((int) (bytesSent.size() * THE_95_TH_PERCENTILE));
    }

    private Predicate<LogData> buildDateTimePredicate(ZonedDateTime from, ZonedDateTime to) {
        return it -> {
            boolean before = Optional.ofNullable(from).map(it.timeLocal()::isBefore).orElse(false);
            boolean after = Optional.ofNullable(to).map(it.timeLocal()::isAfter).orElse(false);
            return !before && !after;
        };
    }

    private Predicate<LogData> buildFieldFilterPredicate(String filterField, Pattern filterValueRegex) {
        if (filterField == null || filterValueRegex == null) {
            return ignored -> true;
        }

        Function<LogData, String> method = switch (filterField) {
            case NginxLogsHandler.REMOTE_ADDRESS_GROUP -> LogData::remoteAddress;
            case NginxLogsHandler.USER_GROUP -> LogData::remoteUser;
            case NginxLogsHandler.METHOD_GROUP -> LogData::requestMethod;
            case NginxLogsHandler.RESOURCE_GROUP -> LogData::requestResource;
            case NginxLogsHandler.HTTP_GROUP -> LogData::requestHttpVersion;
            case NginxLogsHandler.STATUS_GROUP -> it -> String.valueOf(it.status());
            case NginxLogsHandler.REFERER_GROUP -> LogData::httpReferer;
            case NginxLogsHandler.USER_AGENT_GROUP -> LogData::httpUserAgent;
            default -> throw new IllegalArgumentException("Unknown filter field: " + filterField);
        };

        return it -> filterValueRegex.matcher(method.apply(it)).matches();
    }

    private FilterPredicates initPredicates(
        ZonedDateTime from,
        ZonedDateTime to,
        String filterField,
        String filterValueRegex
    ) {
        Pattern filterValuePattern = null;
        if (filterValueRegex != null && !filterValueRegex.isEmpty()) {
            filterValuePattern = Pattern.compile(filterValueRegex);
        }
        var dateTimePredicate = buildDateTimePredicate(from, to);
        var fieldFilterPredicate = buildFieldFilterPredicate(filterField, filterValuePattern);
        return new FilterPredicates(dateTimePredicate, fieldFilterPredicate);
    }
}

