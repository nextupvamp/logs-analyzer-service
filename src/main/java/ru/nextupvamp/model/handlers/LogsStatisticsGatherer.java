package ru.nextupvamp.model.handlers;

import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;

import java.net.URI;
import java.nio.file.Path;

public interface LogsStatisticsGatherer {
    LogsStatistics gatherStatisticsFromFile(Path file, Filters filters);

    LogsStatistics gatherStatisticsFromUri(URI uri, Filters filters);
}
