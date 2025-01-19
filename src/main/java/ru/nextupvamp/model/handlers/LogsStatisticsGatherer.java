package ru.nextupvamp.model.handlers;

import ru.nextupvamp.model.entities.ResourceFilters;
import ru.nextupvamp.model.entities.Statistics;

import java.net.URI;
import java.nio.file.Path;

public interface LogsStatisticsGatherer {
    Statistics gatherStatisticsFromFile(Path file, ResourceFilters filters);

    Statistics gatherStatisticsFromUri(URI uri, ResourceFilters filters);
}
