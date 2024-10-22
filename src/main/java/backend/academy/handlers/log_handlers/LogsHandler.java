package backend.academy.handlers.log_handlers;

import backend.academy.data.LogData;

public interface LogsHandler {
    LogData parseLogLineData(String line);
}
