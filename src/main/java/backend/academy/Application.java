package backend.academy;

import backend.academy.data.HandledArgsData;
import backend.academy.handlers.ArgsHandler;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import backend.academy.handlers.log_handlers.NginxLogsStatisticsGatherer;
import backend.academy.io.ReportCreator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Application {
    public static final String REPORT_FILE_NAME = "report";

    @SneakyThrows
    @SuppressFBWarnings // suppress warnings concerned with file user input
    public static void main(String[] args) {
        ArgsHandler argsHandler = new ArgsHandler(args);
        HandledArgsData handledArgsData = argsHandler.handle();

        NginxLogsStatisticsGatherer statisticsGatherer = NginxLogsStatisticsGatherer.builder()
            .paths(handledArgsData.paths())
            .from(handledArgsData.from())
            .to(handledArgsData.to())
            .filterField(handledArgsData.filterField())
            .filterValuePattern(handledArgsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();

        String fileFormat = handledArgsData.format().getFileFormat();
        Properties properties = new Properties();
        properties.load(Application.class.getClassLoader().getResourceAsStream("application.property"));
        Path reportFile = Paths.get(
            properties.getProperty("report.directory.file.path") + REPORT_FILE_NAME + Instant.now().toEpochMilli()
                + fileFormat);

        PrintStream printStream = new PrintStream(Files.newOutputStream(reportFile), true, StandardCharsets.UTF_8);
        ReportCreator.createReport(statisticsGatherer.gatherStatistics(), handledArgsData.format(), printStream);
    }
}
