package backend.academy.handlers;

import backend.academy.data.ArgsData;
import backend.academy.data.LogsStatistics;
import backend.academy.data.PathsData;
import backend.academy.handlers.log_handlers.LogsStatisticsGatherer;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import backend.academy.io.LogsReaderTest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogsStatisticsGathererTest {
    public static final Path DATA_SOURCE;

    static {
        try {
            DATA_SOURCE =
                Path.of(Objects.requireNonNull(
                    LogsStatisticsGathererTest.class.getClassLoader().getResource("test_logs.txt")).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final List<Path> PATHS = List.of(DATA_SOURCE);

    @Test
    public void testFromToDate() {
        ZonedDateTime from = ZonedDateTime.parse("2015-05-17T08:05:00Z");
        ZonedDateTime to = ZonedDateTime.parse("2015-05-17T08:05:30Z");
        PathsData logPaths = new PathsData(null, PATHS);
        LogsStatisticsGatherer logsStatisticsGatherer = LogsStatisticsGatherer.builder()
            .paths(logPaths)
            .from(from)
            .to(to)
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertTrue(!zdt.isBefore(from) && !zdt.isAfter(to));
        }
    }

    @Test
    public void testFromDate() {
        ZonedDateTime from = ZonedDateTime.parse("2015-05-17T08:05:00Z");
        PathsData logPaths = new PathsData(null, PATHS);
        LogsStatisticsGatherer logsStatisticsGatherer = LogsStatisticsGatherer.builder()
            .paths(logPaths)
            .from(from)
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertFalse(zdt.isBefore(from));
        }
    }

    @Test
    public void testToDate() {
        ZonedDateTime to = ZonedDateTime.parse("2015-05-17T08:05:30Z");
        PathsData logPaths = new PathsData(null, PATHS);
        LogsStatisticsGatherer logsStatisticsGatherer = LogsStatisticsGatherer.builder()
            .paths(logPaths)
            .to(to)
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertFalse(zdt.isAfter(to));
        }
    }

    @Test
    @SneakyThrows
    public void testNoDate() {
        PathsData logPaths = new PathsData(null, PATHS);
        InputStream is = Files.newInputStream(logPaths.paths().getFirst());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        long lines = bufferedReader.lines().count();
        LogsStatisticsGatherer logsStatisticsGatherer = LogsStatisticsGatherer.builder()
            .paths(logPaths)
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        assertEquals(lines, logsStatistics.requestsAmount());
    }

    @Test
    public void testCompute95p() {
        List<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().collect(Collectors.toList());
        long p95 = LogsStatisticsGatherer.count95pBytesSent(list);

        assertEquals(96, p95);
    }

    @Test
    public void testComputeAverage() {
        List<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().toList();

        assertEquals(50, LogsStatisticsGatherer.countAverageBytesSent(list));
    }

    @Test
    public void testCounters() {
        String[] args = {
            "--path",
            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt"
        };
        ArgsHandler argsHandler = new ArgsHandler(args);
        ArgsData argsData = argsHandler.handle();
        LogsStatisticsGatherer logsStatisticsGatherer = LogsStatisticsGatherer.builder()
            .paths(argsData.paths())
            .from(argsData.from())
            .to(argsData.to())
            .filterField(argsData.filterField())
            .filterValuePattern(argsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics ls = logsStatisticsGatherer.gatherStatistics();

        assertEquals(30, ls.requestsAmount()); // there are 30 lines in logs1.txt
        assertEquals(30, ls.requestMethods().get("GET")); // all requests are GET
        assertEquals(16, ls.statuses().get((short) 304)); // 16 304 statuses
        // i've counted it by my hands
    }

    @Test
    public void testFieldValueFilter() {
        String[] args = {
            "--path",
            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt",
            "--filter-field",
            "status",
            "--filter-value",
            "30.*"
        };

        ArgsHandler argsHandler = new ArgsHandler(args);
        ArgsData argsData = argsHandler.handle();
        LogsStatisticsGatherer logsStatisticsGatherer = LogsStatisticsGatherer.builder()
            .paths(argsData.paths())
            .from(argsData.from())
            .to(argsData.to())
            .filterField(argsData.filterField())
            .filterValuePattern(argsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics ls = logsStatisticsGatherer.gatherStatistics();

        assertEquals(16, ls.requestsAmount()); // there are 16 30* statuses
    }
}
