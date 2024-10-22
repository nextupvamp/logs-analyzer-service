package backend.academy.io;

import backend.academy.handlers.ArgsData;
import backend.academy.handlers.ArgsHandler;
import backend.academy.handlers.LogPaths;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArgsHandlerTest {
    @Test
    @SneakyThrows
    public void urlPathTest() {
        String[] args = {
            "--path",
            "http://example.com",
            "https://example.com",
            "ftp://example.com",
            "file:///home/user/file.txt",
            "http://example.com:8080/path/to/resource",
            "https://example.com/resource?query=param",
            "http://example.com/path#section",
            "https://example.com/path?name=value&other=param#fragment",
            "--format",
            "adoc"
        };
        List<URL> correctUrls = Stream.of(
            "http://example.com",
            "https://example.com",
            "ftp://example.com",
            "file:///home/user/file.txt",
            "http://example.com:8080/path/to/resource",
            "https://example.com/resource?query=param",
            "http://example.com/path#section",
            "https://example.com/path?name=value&other=param#fragment"
        ).map(URI::create).map(uri -> {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        LogPaths paths = new ArgsHandler(args).handle().paths();
        assertEquals(correctUrls, paths.urls());
        assertTrue(paths.paths().isEmpty());
    }

    @Test
    @SneakyThrows
    public void singleFilePathTest() {
        String[] args = {
            "--path",
            "C:\\Users\\user\\Documents\\file.txt",
            "C:\\Program Files\\app\\config.cfg",
            "C:\\",
            "C:\\folder\\subfolder\\",
            "/home/user/file.txt",
            "/etc/nginx/nginx.conf",
            "/var/log/",
            "/",
            "file.txt",
            "./file.txt",
            "../file.txt",
            "subfolder/file.txt"
        };
        List<Path> correctPaths = Stream.of(
            "C:\\Users\\user\\Documents\\file.txt",
            "C:\\Program Files\\app\\config.cfg",
            "C:\\",
            "C:\\folder\\subfolder\\",
            "/home/user/file.txt",
            "/etc/nginx/nginx.conf",
            "/var/log/",
            "/",
            "file.txt",
            "./file.txt",
            "../file.txt",
            "subfolder/file.txt"
        ).map(Path::of).toList();
        LogPaths paths = new ArgsHandler(args).handle().paths();
        assertEquals(correctPaths, paths.paths());
        assertTrue(paths.urls().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2024-10-15T12:34:56Z",
        "2024-10-15T12:34:56+02:00",
        "2024-10-15T12:34:56-05:00",
    }
    )
    public void fromDateTest(String inputDate) {
        String[] args = {
            "--path",
            "logs.txt",
            "--from",
            inputDate
        };

        ZonedDateTime zdt = new ArgsHandler(args).handle().from();

        assertEquals(ZonedDateTime.parse(inputDate), zdt);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2024-10-15T12:34:56Z",
        "2024-10-15T12:34:56+02:00",
        "2024-10-15T12:34:56-05:00",
    }
    )
    public void toDateTest(String inputDate) {
        String[] args = {
            "--path",
            "logs.txt",
            "--to",
            inputDate
        };

        ZonedDateTime zdt = new ArgsHandler(args).handle().to();

        assertEquals(ZonedDateTime.parse(inputDate), zdt);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-10-15T12:34:56Z,2024-10-15T12:34:56Z",
        "2024-10-15T12:34:55Z,2024-10-15T12:34:57Z"
    })
    public void fromToDateValidTest(String fromDate, String toDate) {
        String[] args = {
            "--path",
            "logs.txt",
            "--from",
            fromDate,
            "--to",
            toDate
        };

        ArgsHandler argsHandler = new ArgsHandler(args);
        ZonedDateTime from = argsHandler.handle().from();
        ZonedDateTime to = argsHandler.handle().to();
        ZonedDateTime zdt = ZonedDateTime.parse("2024-10-15T12:34:56Z");

        assertTrue(!from.isAfter(zdt) && !to.isBefore(zdt));
    }

    @Test
    public void fromToDAteInvalidTest() {
        String[] args = {
            "--path",
            "logs.txt",
            "--from",
            "2024-10-15T12:34:56Z",
            "--to",
            "2024-10-14T12:34:56Z"
        };

        ArgsHandler argsHandler = new ArgsHandler(args);

        assertThrows(IllegalArgumentException.class, argsHandler::handle);
    }

    @ParameterizedTest
    @CsvSource({
        "3.*,300",
        "[A-Z]+,ENVELOPE",
        "[a-z_]+,void_sx"
    })
    public void filterValueTest(String regex, String value) {
        String[] args = {
            "--path",
            "logs.txt",
            "--filter-field",
            "smth",
            "--filter-value",
            regex
        };
        ArgsHandler argsHandler = new ArgsHandler(args);
        ArgsData argsData = argsHandler.handle();
        Pattern pattern = argsData.filterValuePattern();
        Matcher matcher = pattern.matcher(value);

        assertTrue(matcher.matches());
    }

    @ParameterizedTest
    @CsvSource({
        "--path,--fromat,adoc",
        "-path,logs.txt",
        "--path,logs.txt,--from,2024-10-15T12:34:59Z,--to,2024-10-15T12:34:56Z", // from after to
        "--path,logs.txt,--filter-field,field", // filter field w/o filter value
        "--path,logs.txt,--filter-value,value" // filter value w/o filter field
    })
    public void testWrongParameters(String input) {
        String[] args = input.split(",");
        ArgsHandler argsHandler = new ArgsHandler(args);
        assertThrows(IllegalArgumentException.class, argsHandler::handle);
    }
}
