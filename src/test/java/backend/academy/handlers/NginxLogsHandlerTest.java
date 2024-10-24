package backend.academy.handlers;

import backend.academy.data.LogData;
import backend.academy.handlers.log_handlers.LogsHandler;
import backend.academy.handlers.log_handlers.NginxLogsHandler;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NginxLogsHandlerTest {
    @ParameterizedTest
    @CsvSource({
        "91.193.156.231 - - [16/Oct/2024:04:39:21 +0000] \"GET /dedicated-4th%20generation.png HTTP/1.1\" 200 1539 \"-\" \"Mozilla/5.0 (Windows NT 6.2)\","
            +
            "91.193.156.231,-,2024-10-16T04:39:21+00:00,GET,/dedicated-4th%20generation.png,HTTP/1.1,200,1539,-,Mozilla/5.0 (Windows NT 6.2)",
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\","
            +
            "93.180.71.3,-,2015-05-17T08:05:32+00:00,GET,/downloads/product_1,HTTP/1.1,304,0,-,Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
    })
    public void testParseValidLogLineData(
        String input,
        String address,
        String user,
        String date,
        String method,
        String resource,
        String http,
        short status,
        long bytes,
        String referer,
        String userAgent
    ) {
        NginxLogsHandler nginxLogsHandler = new NginxLogsHandler();
        LogData logData = nginxLogsHandler.parseLogLineData(input);
        LogData expected = new LogData(
            address,
            user,
            ZonedDateTime.parse(date),
            method,
            resource,
            http,
            status,
            bytes,
            referer,
            userAgent
        );

        assertEquals(expected, logData);
    }

    @Test
    public void testParseInvalidLogLineData() {
        LogsHandler logsHandler = new NginxLogsHandler();
        assertThrows(IllegalArgumentException.class, () -> logsHandler.parseLogLineData(
            "[Fri Sep 09 10:42:29.902022 2011] [core:error] [pid 35708:tid 4328636416] [client 72.15.99.187] File does not exist: /usr/local/apache2/htdocs/favicon.ico"
        ));
    }
}
