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
import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReportCreator {
    public static final int DEFAULT_HEADER_LEVEL = 4;
    public static final Properties STATUS_CODES_NAME;

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

    public static void createReport(LogsStatistics logsStatistics, TextFormatter tf, PrintStream out) {
        if (logsStatistics == null
            || (logsStatistics.paths().uris().isEmpty()
            && logsStatistics.paths().paths().isEmpty())) {
            throw new IllegalArgumentException("Invalid log statistics");
        }

        // default statistics
        printGeneralStatistics(logsStatistics, tf, out);
        printResourcesStatistics(logsStatistics, tf, out);
        printResourceCodesStatistics(logsStatistics, tf, out);
        // additional statistics
        printRequestMethodsStatistics(logsStatistics, tf, out);
        printRequestOnDateStatistics(logsStatistics, tf, out);
    }

    private static void printRequestOnDateStatistics(LogsStatistics logsStatistics, TextFormatter tf, PrintStream out) {
        out.println(tf.toHeaderLine("Requests on date", DEFAULT_HEADER_LEVEL));
        out.println(tf.toTableHeader("Date", "Amount"));
        logsStatistics.requestsOnDate().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .forEach(entry -> out.println(tf.toTableRow(entry.getKey().toString(), entry.getValue().toString())));
        out.println(tf.getTableFooter());
    }

    private static void printRequestMethodsStatistics(
        LogsStatistics logsStatistics,
        TextFormatter tf,
        PrintStream out
    ) {
        out.println(tf.toHeaderLine("Request Methods", DEFAULT_HEADER_LEVEL));
        out.println(tf.toTableHeader("Method", "Amount"));
        logsStatistics.requestMethods().entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> out.println(tf.toTableRow(entry.getKey(), entry.getValue().toString())));
        out.println(tf.getTableFooter());
    }

    private static void printResourceCodesStatistics(LogsStatistics logsStatistics, TextFormatter tf, PrintStream out) {
        out.println(tf.toHeaderLine("Response codes", DEFAULT_HEADER_LEVEL));
        out.println(tf.toTableHeader("Code", "Name", "Amount"));
        logsStatistics.statuses().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .forEach(
                e -> out.println(tf.toTableRow(String.valueOf(e.getKey()),
                    STATUS_CODES_NAME.getProperty(String.valueOf(e.getKey())),
                    e.getValue().toString()
                ))
            );
        out.println(tf.getTableFooter());
    }

    private static void printResourcesStatistics(LogsStatistics logsStatistics, TextFormatter tf, PrintStream out) {
        out.println(tf.toHeaderLine("Queried resources", DEFAULT_HEADER_LEVEL));
        out.println(tf.toTableHeader("Resource", "Amount"));
        logsStatistics.requestResources().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .forEach(e -> out.println(tf.toTableRow(tf.toMonospaced(e.getKey()), e.getValue().toString())));
        out.println(tf.getTableFooter());
    }

    private static void printGeneralStatistics(LogsStatistics logsStatistics, TextFormatter tf, PrintStream out) {
        out.println(tf.toHeaderLine("General information", DEFAULT_HEADER_LEVEL));
        out.println(tf.toTableHeader("Metric", "Value"));

        Stream<String> urisStream = logsStatistics.paths().uris().stream().map(URI::toString);
        Stream<String> pathsStream = logsStatistics.paths().paths().stream().map(Path::toString);
        List<String> pathsList = Stream.concat(urisStream, pathsStream).toList();

        out.println(tf.toTableRow("Files", tf.toMonospaced(pathsList.getFirst())));
        for (int i = 1; i < pathsList.size(); i++) {
            out.println(tf.toTableRow("     ", tf.toMonospaced(pathsList.get(i))));
        }

        out.println(tf.toTableRow("Start date",
            logsStatistics.from() == null ? "-" : logsStatistics.from().toString()));
        out.println(tf.toTableRow("End date",
            logsStatistics.to() == null ? "-" : logsStatistics.to().toString()));

        out.println(tf.toTableRow("Number of requests", String.valueOf(logsStatistics.requestsAmount())));
        out.println(tf.toTableRow("Average response size", String.valueOf(logsStatistics.averageBytesSent())));
        out.println(tf.toTableRow("95p response size", String.valueOf(logsStatistics.p95BytesSent())));
        out.println(tf.getTableFooter());
    }
}
