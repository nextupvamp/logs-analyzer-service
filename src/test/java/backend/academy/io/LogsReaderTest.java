package backend.academy.io;

import backend.academy.data.LogData;
import backend.academy.data.PathsData;
import backend.academy.handlers.ArgsHandler;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogsReaderTest {
    public static final Path TEST_DIR_PATH = Paths.get("src", "test", "resources", "test_dir");
    public static final URI TEST_REMOTE_URI;
    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

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
        LogsReader logsReader = new LogsReader();
        Path localFile = Paths.get(TEST_DIR_PATH.toAbsolutePath().toString(), "logs1.txt");
        Stream<LogData> stream = logsReader.readFromFileAsStream(localFile, new NginxLogsHandler());

        assertTrue(stream.findAny().isPresent()); // something has been read

        logsReader.close();
    }

    @Test
    @SneakyThrows
    // test will fall if no Internet connection
    public void testUrlPathRead() {
        LogsReader logsReader = new LogsReader();
        Stream<LogData> stream = logsReader.readFromUriAsStream(TEST_REMOTE_URI, new NginxLogsHandler());

        assertTrue(stream.findAny().isPresent());

        logsReader.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"logs*", "*"})
    public void testLocalFilePatternRead(String input) {
        String pattern = TEST_DIR_PATH + SEPARATOR + input;
        String[] args = {
            "--path",
            pattern
        };
        ArgsHandler argsHandler = new ArgsHandler(args);
        PathsData logPaths = argsHandler.handle().paths();

        assertEquals(2, logPaths.paths().size()); // there are 2 files in test_dir
    }

    @Test
    public void testLocalFileWithInterestingPatternRead() {
        // test_dir/../**logs* - rather interesting, innit?
        String pattern = TEST_DIR_PATH + SEPARATOR + ".." + SEPARATOR + "**log*";
        String[] args = {
            "--path",
            pattern
        };
        ArgsHandler argsHandler = new ArgsHandler(args);
        PathsData logPaths = argsHandler.handle().paths();

        assertEquals(3, logPaths.paths().size()); // there are 3 files with log in resources
    }
}
