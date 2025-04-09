package ru.nextupvamp.model.handlers;

import ru.nextupvamp.dto.LogData;

public interface LogLineParser {
    LogData parseLine(String line);
}
