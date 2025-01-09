package ru.nextupvamp.model.handlers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

public class NginxLogsStatisticsGathererTest {
    public static final Path DATA_SOURCE;
    public static final LogLineParser PARSER = new NginxLogLineParser();
    public static final LogsStatisticsGatherer GATHERER = new NginxLogsStatisticsGatherer(PARSER);

    static {
        try {
            DATA_SOURCE =
                    Path.of(Objects.requireNonNull(
                            NginxLogsStatisticsGathererTest.class.getClassLoader().getResource("test_logs.txt")).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFromToDate() {
        ZonedDateTime fromDate = ZonedDateTime.parse("2015-05-17T08:05:00Z");
        ZonedDateTime toDate = ZonedDateTime.parse("2015-05-17T08:05:30Z");
        Filters filters = Filters.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, filters);

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertTrue(!zdt.isBefore(fromDate) && !zdt.isAfter(toDate));
        }
    }

    @Test
    public void testFromDate() {
        ZonedDateTime fromDate = ZonedDateTime.parse("2015-05-17T08:05:00Z");
        Filters filters = Filters.builder()
                .fromDate(fromDate)
                .build();
        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, filters);

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertFalse(zdt.isBefore(fromDate));
        }
    }

    @Test
    public void testToDate() {
        ZonedDateTime toDate = ZonedDateTime.parse("2015-05-17T08:05:30Z");
        Filters filters = Filters.builder()
                .toDate(toDate)
                .build();
        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, filters);

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertFalse(zdt.isAfter(toDate));
        }
    }

    @Test
    @SneakyThrows
    public void testNoDate() {
        InputStream is = Files.newInputStream(DATA_SOURCE);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        long lines = bufferedReader.lines().count();

        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, Filters.EMPTY);

        assertEquals(lines, logsStatistics.requestsAmount());
    }

    @Test
    public void testCompute95p() {
        List<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().collect(Collectors.toList());
        long p95 = new NginxLogsStatisticsGatherer(PARSER).count95pBytesSent(list);

        assertEquals(96, p95);
    }

    @Test
    public void testComputeAverage() {
        List<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().toList();

        assertEquals(50, new NginxLogsStatisticsGatherer(PARSER).countAverageBytesSent(list));
    }

    @Test
    public void testCounters() {
        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, Filters.EMPTY);

        assertAll(
                () -> assertEquals(33, logsStatistics.requestsAmount()), // there are 30 lines in logs1.txt
                () -> assertEquals(30, logsStatistics.requestMethods().get("GET")), // all requests are GET
                () -> assertEquals(16, logsStatistics.statuses().get((short) 304)) // 16 304 statuses
        );
        // I've counted it by myself
    }

    @Test
    public void testFieldValueFilter() {
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("status", "30.*");

        Filters filters = Filters.builder()
                .filterMap(filterMap)
                .build();
        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, filters);

        assertEquals(16, logsStatistics.requestsAmount());
    }

    @Test
    public void testMultipleFieldValueFilter() {
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("status", "4.*");
        filterMap.put("method", "GET");

        Filters filters = Filters.builder()
                .filterMap(filterMap)
                .build();
        LogsStatistics logsStatistics = GATHERER.gatherStatisticsFromFile(DATA_SOURCE, filters);

        assertEquals(13, logsStatistics.requestsAmount()); // also hand counted
    }
}
