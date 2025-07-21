package ru.nextupvamp.statistics.service;

import ru.nextupvamp.resource.domain.ResourceFilters;
import ru.nextupvamp.statistics.dto.StatisticsDto;

import java.net.URI;
import java.nio.file.Path;

public interface LogsStatisticsGatherer {
    StatisticsDto gatherStatisticsFromFile(Path file, ResourceFilters filters);

    StatisticsDto gatherStatisticsFromUri(URI uri, ResourceFilters filters);
}
