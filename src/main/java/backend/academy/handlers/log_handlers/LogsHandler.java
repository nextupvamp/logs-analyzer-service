package backend.academy.handlers.log_handlers;

import backend.academy.log_data.LogData;

public interface LogsHandler {
    LogData parseLogLineData(String line);
}
