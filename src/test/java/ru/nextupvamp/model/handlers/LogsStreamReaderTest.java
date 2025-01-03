package ru.nextupvamp.model.handlers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import ru.nextupvamp.model.data.LogData;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogsStreamReaderTest {
    public static final URI TEST_REMOTE_URI;

    static {
        try {
            TEST_REMOTE_URI =
                    new URI(
                            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @SneakyThrows
    public void testLocalFilePathRead() {
        LogsStreamReader logsStreamReader = new LogsStreamReader();
        Path localFile = Paths.get("src/test/resources/test_logs.txt");
        Stream<LogData> stream = logsStreamReader.readFromFileAsStream(localFile, new NginxLogLineParser());

        assertTrue(stream.findAny().isPresent()); // something has been read

        logsStreamReader.close();
    }

    @Test
    @SneakyThrows
    // test will fall if no Internet connection
    public void testUrlPathRead() {
        LogsStreamReader logsStreamReader = new LogsStreamReader();
        Stream<LogData> stream = logsStreamReader.readFromUriAsStream(TEST_REMOTE_URI, new NginxLogLineParser());

        assertTrue(stream.findAny().isPresent());

        logsStreamReader.close();
    }
}