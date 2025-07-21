package ru.nextupvamp.statistics.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.nextupvamp.parser.LogData;
import ru.nextupvamp.parser.LogLineParser;
import ru.nextupvamp.parser.LogsStreamReader;
import ru.nextupvamp.parser.NginxLogLineParser;
import ru.nextupvamp.resource.domain.ResourceFilters;
import ru.nextupvamp.statistics.dto.FilterPredicates;
import ru.nextupvamp.statistics.dto.StatisticsDto;
import ru.nextupvamp.util.StatisticsCalculator;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NginxLogsStatisticsGatherer implements LogsStatisticsGatherer {

    private final LogLineParser logsHandler;

    @Override
    @SneakyThrows
    public StatisticsDto gatherStatisticsFromFile(Path file, ResourceFilters filters) {
        if (file == null || Files.notExists(file)) {
            throw new IllegalArgumentException("File does not exist");
        }
        try (LogsStreamReader logsStreamReader = new LogsStreamReader()) {
            return gatherData(logsStreamReader.readFromFileAsStream(file, logsHandler), filters);
        }
    }

    @Override
    @SneakyThrows
    public StatisticsDto gatherStatisticsFromUri(URI uri, ResourceFilters filters) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        try (LogsStreamReader logsStreamReader = new LogsStreamReader()) {
            return gatherData(logsStreamReader.readFromUriAsStream(uri, logsHandler), filters);
        }
    }

    private StatisticsDto gatherData(Stream<LogData> logDataStream, ResourceFilters filters) {
        Queue<Long> bytesSent = new ConcurrentLinkedQueue<>();
        Map<String, Integer> remoteAddresses = new ConcurrentHashMap<>();
        Map<String, Integer> remoteUsers = new ConcurrentHashMap<>();
        Map<String, Integer> requestMethods = new ConcurrentHashMap<>();
        Map<String, Integer> requestResources = new ConcurrentHashMap<>();
        Map<Short, Integer> statuses = new ConcurrentHashMap<>();
        AtomicInteger requestsAmount = new AtomicInteger(0);
        AtomicInteger ignoredRows = new AtomicInteger(0);
        Map<ZonedDateTime, Integer> requestsOnDate = new ConcurrentHashMap<>();

        FilterPredicates filterPredicates;
        if (filters != null) {
            filterPredicates = initPredicates(filters.fromDate(), filters.toDate(), filters.filterMap());
        } else {
            filterPredicates = initPredicates(null, null, null);
        }
        Predicate<LogData> dateTimePredicate = filterPredicates.dateTimePredicate();
        Predicate<LogData> fieldFilterPredicate = filterPredicates.fieldFilterPredicate();

        logDataStream.filter(dateTimePredicate)
                .parallel()
                .filter(fieldFilterPredicate)
                .forEach(it -> {
                    if (it == LogData.IGNORED) {
                        ignoredRows.incrementAndGet();
                    } else {
                        requestsAmount.incrementAndGet();
                        bytesSent.add(it.bytesSent());
                        remoteAddresses.merge(it.remoteAddress(), 1, Integer::sum);
                        remoteUsers.merge(it.remoteUser(), 1, Integer::sum);
                        requestMethods.merge(it.requestMethod(), 1, Integer::sum);
                        requestResources.merge(it.requestResource(), 1, Integer::sum);
                        statuses.merge(it.status(), 1, Integer::sum);
                        requestsOnDate.merge(it.timeLocal(), 1, Integer::sum);
                    }
                });

        ZonedDateTime from = null;
        ZonedDateTime to = null;
        if (filters != null) {
            from = filters.fromDate();
            to = filters.toDate();
        }
        return new StatisticsDto(
                ignoredRows.get(),
                remoteAddresses,
                remoteUsers,
                from,
                to,
                requestsOnDate,
                requestMethods,
                requestResources,
                statuses,
                requestsAmount.get(),
                StatisticsCalculator.calculateAverage(bytesSent),
                StatisticsCalculator.calculate95Percentile(bytesSent)
        );
    }

    private FilterPredicates initPredicates(
            ZonedDateTime from,
            ZonedDateTime to,
            Map<String, String> filterMap
    ) {
        Map<String, Pattern> compiledFilterMap = new HashMap<>();
        if (filterMap != null) {
            filterMap.forEach((field, value) -> {
                Pattern filterValuePattern = null;
                if (value != null && !value.isEmpty()) {
                    filterValuePattern = Pattern.compile(value);
                }
                compiledFilterMap.put(field, filterValuePattern);
            });
        }

        var dateTimePredicate = buildDateTimePredicate(from, to);
        var fieldFilterPredicates = buildFieldFilterPredicate(compiledFilterMap);
        return new FilterPredicates(dateTimePredicate, fieldFilterPredicates);
    }

    private Predicate<LogData> buildDateTimePredicate(ZonedDateTime from, ZonedDateTime to) {
        return it -> {
            boolean before = Optional.ofNullable(from).map(it.timeLocal()::isBefore).orElse(false);
            boolean after = Optional.ofNullable(to).map(it.timeLocal()::isAfter).orElse(false);
            return !before && !after;
        };
    }

    private Predicate<LogData> buildFieldFilterPredicate(Map<String, Pattern> compiledFilterMap) {
        List<Predicate<LogData>> predicateList = new ArrayList<>();

        compiledFilterMap.forEach((field, value) -> {
            if (field == null || value == null) {
                predicateList.add(ignored -> true);
                return;
            }

            Function<LogData, String> method = switch (field) {
                case NginxLogLineParser.REMOTE_ADDRESS_GROUP -> LogData::remoteAddress;
                case NginxLogLineParser.USER_GROUP -> LogData::remoteUser;
                case NginxLogLineParser.METHOD_GROUP -> LogData::requestMethod;
                case NginxLogLineParser.RESOURCE_GROUP -> LogData::requestResource;
                case NginxLogLineParser.HTTP_GROUP -> LogData::requestHttpVersion;
                case NginxLogLineParser.STATUS_GROUP -> it -> String.valueOf(it.status());
                case NginxLogLineParser.REFERER_GROUP -> LogData::httpReferer;
                case NginxLogLineParser.USER_AGENT_GROUP -> LogData::httpUserAgent;
                default -> throw new IllegalArgumentException("Unknown filter field: " + field);
            };

            predicateList.add(it -> value.matcher(method.apply(it)).matches());
        });

        return predicateList.stream().reduce(Predicate::and).orElse(ignored -> true);
    }
}

