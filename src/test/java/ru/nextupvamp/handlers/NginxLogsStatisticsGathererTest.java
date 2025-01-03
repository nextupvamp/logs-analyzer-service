//package ru.nextupvamp.handlers;
//
//import ru.nextupvamp.data.HandledArgsData;
//import ru.nextupvamp.model.data.LogsStatistics;
//import ru.nextupvamp.io.LogsReaderTest;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//import java.util.stream.LongStream;
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//import ru.nextupvamp.model.handlers.NginxLogsHandler;
//import ru.nextupvamp.model.handlers.NginxLogsStatisticsGatherer;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class NginxLogsStatisticsGathererTest {
//    public static final Path DATA_SOURCE;
//
//    static {
//        try {
//            DATA_SOURCE =
//                Path.of(Objects.requireNonNull(
//                    NginxLogsStatisticsGathererTest.class.getClassLoader().getResource("test_logs.txt")).toURI());
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static final List<Path> PATHS = List.of(DATA_SOURCE);
//
//    @Test
//    public void testFromToDate() {
//        ZonedDateTime fromTime = ZonedDateTime.parse("2015-05-17T08:05:00Z");
//        ZonedDateTime toTime = ZonedDateTime.parse("2015-05-17T08:05:30Z");
//        PathsData logPaths = new PathsData(null, PATHS);
//        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
//            .paths(logPaths)
//            .fromTime(fromTime)
//            .toTime(toTime)
//            .logsHandler(new NginxLogsHandler())
//            .build();
//        LogsStatistics logsStatistics = nginxLogsStatisticsGatherer.gatherStatistics();
//
//        for (ZonedDateTime zdt : logsStatistics.nativeLogsStatistics().requestsOnDate().keySet()) {
//            assertTrue(!zdt.isBefore(fromTime) && !zdt.isAfter(toTime));
//        }
//    }
//
//    @Test
//    public void testFromDate() {
//        ZonedDateTime fromTime = ZonedDateTime.parse("2015-05-17T08:05:00Z");
//        PathsData logPaths = new PathsData(null, PATHS);
//        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
//            .paths(logPaths)
//            .fromTime(fromTime)
//            .logsHandler(new NginxLogsHandler())
//            .build();
//        LogsStatistics logsStatistics = nginxLogsStatisticsGatherer.gatherStatistics();
//
//        for (ZonedDateTime zdt : logsStatistics.nativeLogsStatistics().requestsOnDate().keySet()) {
//            assertFalse(zdt.isBefore(fromTime));
//        }
//    }
//
//    @Test
//    public void testToDate() {
//        ZonedDateTime toTime = ZonedDateTime.parse("2015-05-17T08:05:30Z");
//        PathsData logPaths = new PathsData(null, PATHS);
//        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
//            .paths(logPaths)
//            .toTime(toTime)
//            .logsHandler(new NginxLogsHandler())
//            .build();
//        LogsStatistics logsStatistics = nginxLogsStatisticsGatherer.gatherStatistics();
//
//        for (ZonedDateTime zdt : logsStatistics.nativeLogsStatistics().requestsOnDate().keySet()) {
//            assertFalse(zdt.isAfter(toTime));
//        }
//    }
//
//    @Test
//    @SneakyThrows
//    public void testNoDate() {
//        PathsData logPaths = new PathsData(null, PATHS);
//        InputStream is = Files.newInputStream(logPaths.paths().getFirst());
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
//        long lines = bufferedReader.lines().count();
//        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
//            .paths(logPaths)
//            .logsHandler(new NginxLogsHandler())
//            .build();
//        LogsStatistics logsStatistics = nginxLogsStatisticsGatherer.gatherStatistics();
//
//        assertEquals(lines, logsStatistics.computedLogsStatistic().requestsAmount());
//    }
//
//    @Test
//    public void testCompute95p() {
//        List<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().collect(Collectors.toList());
//        long p95 = NginxLogsStatisticsGatherer.count95pBytesSent(list);
//
//        assertEquals(96, p95);
//    }
//
//    @Test
//    public void testComputeAverage() {
//        List<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().toList();
//
//        assertEquals(50, NginxLogsStatisticsGatherer.countAverageBytesSent(list));
//    }
//
//    @Test
//    public void testCounters() {
//        String[] args = {
//            "--path",
//            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt"
//        };
//        ArgsHandler argsHandler = new ArgsHandler(args);
//        HandledArgsData handledArgsData = argsHandler.handle();
//        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
//            .paths(handledArgsData.paths())
//            .fromTime(handledArgsData.fromTime())
//            .toTime(handledArgsData.toTime())
//            .filterField(handledArgsData.filterField())
//            .filterValuePattern(handledArgsData.filterValuePattern())
//            .logsHandler(new NginxLogsHandler())
//            .build();
//        LogsStatistics ls = nginxLogsStatisticsGatherer.gatherStatistics();
//
//        assertEquals(30, ls.computedLogsStatistic().requestsAmount()); // there are 30 lines in logs1.txt
//        assertEquals(30, ls.nativeLogsStatistics().requestMethods().get("GET")); // all requests are GET
//        assertEquals(16, ls.nativeLogsStatistics().statuses().get((short) 304)); // 16 304 statuses
//        // i've counted it by my hands
//    }
//
//    @Test
//    public void testFieldValueFilter() {
//        String[] args = {
//            "--path",
//            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt",
//            "--filter-field",
//            "status",
//            "--filter-value",
//            "30.*"
//        };
//
//        ArgsHandler argsHandler = new ArgsHandler(args);
//        HandledArgsData handledArgsData = argsHandler.handle();
//        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
//            .paths(handledArgsData.paths())
//            .fromTime(handledArgsData.fromTime())
//            .toTime(handledArgsData.toTime())
//            .filterField(handledArgsData.filterField())
//            .filterValuePattern(handledArgsData.filterValuePattern())
//            .logsHandler(new NginxLogsHandler())
//            .build();
//        LogsStatistics ls = nginxLogsStatisticsGatherer.gatherStatistics();
//
//        assertEquals(16, ls.computedLogsStatistic().requestsAmount()); // there are 16 30* statuses
//    }
//}
