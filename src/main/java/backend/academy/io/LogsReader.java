package backend.academy.io;

import backend.academy.handlers.log_handlers.LogsHandler;
import backend.academy.log_data.LogData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.SneakyThrows;

@SuppressFBWarnings // suppress warnings concerned with user url paths input
public class LogsReader implements AutoCloseable {
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;

    @SneakyThrows
    public Stream<LogData> readFromFileAsStream(Path file, LogsHandler handler) {
        inputStream = Files.newInputStream(file);
        initReaders(inputStream);

        return bufferedReader.lines().map(handler::parseLogLineData);
    }

    @SneakyThrows
    public Stream<LogData> readFromUrlAsStream(URL url, LogsHandler handler) {
        inputStream = url.openStream();
        initReaders(inputStream);

        return bufferedReader.lines().map(handler::parseLogLineData);
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
    }

    private void initReaders(InputStream inputStream) {
        inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(inputStreamReader);
    }
}
