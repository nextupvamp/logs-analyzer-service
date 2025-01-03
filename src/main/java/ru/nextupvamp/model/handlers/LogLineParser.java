package ru.nextupvamp.model.handlers;

import ru.nextupvamp.model.data.LogData;

public interface LogLineParser {
    LogData parseLine(String line);
}
