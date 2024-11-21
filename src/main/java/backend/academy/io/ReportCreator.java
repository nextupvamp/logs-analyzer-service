package backend.academy.io;

import backend.academy.data.LogsStatistics;
import backend.academy.io.formatters.TextFormatter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReportCreator {
    private static final int DEFAULT_HEADER_LEVEL = 4;
    private static final Properties STATUS_CODES_NAME;

    private PrintStream out;
    private TextFormatter formatter;
    private LogsStatistics logsStatistics;

    static {
        STATUS_CODES_NAME = new Properties();
        try {
            STATUS_CODES_NAME.load(
                ReportCreator.class.getClassLoader().getResourceAsStream("status_codes.properties")
            );
        } catch (IOException e) {
            throw new RuntimeException("status_codes.properties not found", e);
        }
    }

    public void createReport() {
        if (logsStatistics == null
            || (logsStatistics.paths().uris().isEmpty()
            && logsStatistics.paths().paths().isEmpty())) {
            throw new IllegalArgumentException("Invalid log statistics");
        }

        // default statistics
        printGeneralStatistics(logsStatistics);
        printResourcesStatistics(logsStatistics);
        printResourceCodesStatistics(logsStatistics);
        // additional statistics
        printRequestMethodsStatistics(logsStatistics);
        printRequestOnDateStatistics(logsStatistics);
    }

    private void printRequestOnDateStatistics(LogsStatistics logsStatistics) {
        out.println(formatter.toHeaderLine("Requests on date", DEFAULT_HEADER_LEVEL));
        out.println(formatter.toTableHeader("Date", "Amount"));
        logsStatistics.requestsOnDate().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .forEach(
                entry -> out.println(formatter.toTableRow(entry.getKey().toString(), entry.getValue().toString())));
        out.println(formatter.getTableFooter());
    }

    private void printRequestMethodsStatistics(LogsStatistics logsStatistics) {
        out.println(formatter.toHeaderLine("Request Methods", DEFAULT_HEADER_LEVEL));
        out.println(formatter.toTableHeader("Method", "Amount"));
        logsStatistics.requestMethods().entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> out.println(formatter.toTableRow(entry.getKey(), entry.getValue().toString())));
        out.println(formatter.getTableFooter());
    }

    private void printResourceCodesStatistics(LogsStatistics logsStatistics) {
        out.println(formatter.toHeaderLine("Response codes", DEFAULT_HEADER_LEVEL));
        out.println(formatter.toTableHeader("Code", "Name", "Amount"));
        logsStatistics.statuses().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .forEach(
                e -> out.println(formatter.toTableRow(String.valueOf(e.getKey()),
                    STATUS_CODES_NAME.getProperty(String.valueOf(e.getKey())),
                    e.getValue().toString()
                ))
            );
        out.println(formatter.getTableFooter());
    }

    private void printResourcesStatistics(LogsStatistics logsStatistics) {
        out.println(formatter.toHeaderLine("Queried resources", DEFAULT_HEADER_LEVEL));
        out.println(formatter.toTableHeader("Resource", "Amount"));
        logsStatistics.requestResources().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .forEach(
                e -> out.println(formatter.toTableRow(formatter.toMonospaced(e.getKey()), e.getValue().toString())));
        out.println(formatter.getTableFooter());
    }

    private void printGeneralStatistics(LogsStatistics logsStatistics) {
        out.println(formatter.toHeaderLine("General information", DEFAULT_HEADER_LEVEL));
        out.println(formatter.toTableHeader("Metric", "Value"));

        Stream<String> urisStream = logsStatistics.paths().uris().stream().map(URI::toString);
        Stream<String> pathsStream = logsStatistics.paths().paths().stream().map(Path::toString);
        List<String> pathsList = Stream.concat(urisStream, pathsStream).toList();

        out.println(formatter.toTableRow("Files", formatter.toMonospaced(pathsList.getFirst())));
        for (int i = 1; i < pathsList.size(); i++) {
            out.println(formatter.toTableRow("     ", formatter.toMonospaced(pathsList.get(i))));
        }

        out.println(formatter.toTableRow("Start date",
            logsStatistics.from() == null ? "-" : logsStatistics.from().toString()));
        out.println(formatter.toTableRow("End date",
            logsStatistics.to() == null ? "-" : logsStatistics.to().toString()));

        out.println(formatter.toTableRow("Number of requests", String.valueOf(logsStatistics.requestsAmount())));
        out.println(formatter.toTableRow("Average response size", String.valueOf(logsStatistics.averageBytesSent())));
        out.println(formatter.toTableRow("95p response size", String.valueOf(logsStatistics.p95BytesSent())));
        out.println(formatter.getTableFooter());
    }
}
