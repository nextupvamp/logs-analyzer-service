package backend.academy.handlers;

import backend.academy.data.LogsStatistics;
import backend.academy.data.ArgsData;
import backend.academy.data.LogPaths;
import backend.academy.handlers.log_handlers.LogsStatisticsGatherer;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import backend.academy.io.LogsReaderTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogsStatisticsGathererTest {
    public static final URL DATA_SOURCE =
        LogsStatisticsGathererTest.class.getClassLoader().getResource("test_logs.txt");
    public static final List<URL> URLS;

    static {
        assert DATA_SOURCE != null;
        URLS = List.of(DATA_SOURCE);
    }

    @Test
    public void testFromToDate() {
        ZonedDateTime from = ZonedDateTime.parse("2015-05-17T08:05:00Z");
        ZonedDateTime to = ZonedDateTime.parse("2015-05-17T08:05:30Z");
        LogPaths logPaths = new LogPaths(URLS, null);
        LogsStatisticsGatherer logsStatisticsGatherer =
            new LogsStatisticsGatherer(logPaths, from, to, null, null, new NginxLogsHandler());
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertTrue(!zdt.isBefore(from) && !zdt.isAfter(to));
        }
    }

    @Test
    public void testFromDate() {
        ZonedDateTime from = ZonedDateTime.parse("2015-05-17T08:05:00Z");
        LogPaths logPaths = new LogPaths(URLS, null);
        LogsStatisticsGatherer logsStatisticsGatherer =
            new LogsStatisticsGatherer(logPaths, from, null, null, null, new NginxLogsHandler());
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertFalse(zdt.isBefore(from));
        }
    }

    @Test
    public void testToDate() {
        ZonedDateTime to = ZonedDateTime.parse("2015-05-17T08:05:30Z");
        LogPaths logPaths = new LogPaths(URLS, null);
        LogsStatisticsGatherer logsStatisticsGatherer =
            new LogsStatisticsGatherer(logPaths, null, to, null, null, new NginxLogsHandler());
        LogsStatistics logsStatistics = logsStatisticsGatherer.gatherStatistics();

        for (ZonedDateTime zdt : logsStatistics.requestsOnDate().keySet()) {
            assertFalse(zdt.isAfter(to));
        }
    }

    @Test
    @SneakyThrows
    public void testNoDate() {
        LogPaths logPaths = new LogPaths(URLS, null);
        InputStream is = logPaths.urls().getFirst().openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        long lines = bufferedReader.lines().count();

        LogsStatisticsGatherer logsStatisticsGatherer =
            new LogsStatisticsGatherer(logPaths, null, null, null, null, new NginxLogsHandler());
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
        LogsStatisticsGatherer logsStatisticsGatherer = new LogsStatisticsGatherer(
            argsData.paths(),
            argsData.from(),
            argsData.to(),
            argsData.filterField(),
            argsData.filterValuePattern(),
            new NginxLogsHandler()
        );
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
        LogsStatisticsGatherer logsStatisticsGatherer = new LogsStatisticsGatherer(
            argsData.paths(),
            argsData.from(),
            argsData.to(),
            argsData.filterField(),
            argsData.filterValuePattern(),
            new NginxLogsHandler()
        );
        LogsStatistics ls = logsStatisticsGatherer.gatherStatistics();

        assertEquals(16, ls.requestsAmount()); // there are 16 30* statuses
    }
}
