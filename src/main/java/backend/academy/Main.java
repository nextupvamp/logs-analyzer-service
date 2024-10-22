package backend.academy;

import backend.academy.handlers.ArgsData;
import backend.academy.handlers.ArgsHandler;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import backend.academy.io.ReportCreator;
import backend.academy.log_data.LogsStatisticsGatherer;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        ArgsHandler argsHandler = new ArgsHandler(args);
        ArgsData argsData = argsHandler.handle();

        LogsStatisticsGatherer statisticsGatherer = new LogsStatisticsGatherer(
            argsData.paths(),
            argsData.from(),
            argsData.to(),
            argsData.filterField(),
            argsData.filterValuePattern(),
            new NginxLogsHandler()
        );

        Path file = Path.of("C:\\Users\\Владислав\\log_report.md");
        PrintStream printStream = new PrintStream(file.toFile(), StandardCharsets.UTF_8);
        ReportCreator.createReport(statisticsGatherer.gatherStatistics(), argsData.format(), printStream);
    }
}
