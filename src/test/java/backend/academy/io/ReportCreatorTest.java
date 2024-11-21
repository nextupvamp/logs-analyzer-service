package backend.academy.io;

import backend.academy.data.HandledArgsData;
import backend.academy.data.LogsStatistics;
import backend.academy.handlers.ArgsHandler;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import backend.academy.handlers.log_handlers.NginxLogsStatisticsGatherer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportCreatorTest {
    public final OutputStream BAOS = new ByteArrayOutputStream();
    public final PrintStream BAOSPrintStream = new PrintStream(BAOS);

    @Test
    public void testADocReportCreate() {
        String[] args = {
            "--path",
            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt",
            "--format",
            "adoc"
        };

        ArgsHandler argsHandler = new ArgsHandler(args);
        HandledArgsData handledArgsData = argsHandler.handle();
        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
            .paths(handledArgsData.paths())
            .from(handledArgsData.from())
            .to(handledArgsData.to())
            .filterField(handledArgsData.filterField())
            .filterValuePattern(handledArgsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics ls = nginxLogsStatisticsGatherer.gatherStatistics();

        ReportCreator.createReport(ls, handledArgsData.format(), BAOSPrintStream);

        String report = BAOS.toString();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    ReportCreatorTest.class.getClassLoader().getResourceAsStream("report_example.adoc"))
            )
        );

        String expected = br.lines().collect(Collectors.joining(System.lineSeparator()));

        assertEquals(expected, report);
    }

    @Test
    public void testMarkdownReportCreate() {
        String[] args = {
            "--path",
            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt"
        };

        ArgsHandler argsHandler = new ArgsHandler(args);
        HandledArgsData handledArgsData = argsHandler.handle();
        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
            .paths(handledArgsData.paths())
            .from(handledArgsData.from())
            .to(handledArgsData.to())
            .filterField(handledArgsData.filterField())
            .filterValuePattern(handledArgsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics ls = nginxLogsStatisticsGatherer.gatherStatistics();

        ReportCreator.createReport(ls, handledArgsData.format(), BAOSPrintStream);

        String report = BAOS.toString();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    ReportCreatorTest.class.getClassLoader().getResourceAsStream("report_example.md"))
            )
        );

        String expected = br.lines().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expected, report);
    }
}
