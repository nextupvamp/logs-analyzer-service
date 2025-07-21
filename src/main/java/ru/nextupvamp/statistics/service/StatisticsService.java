package ru.nextupvamp.statistics.service;

import ru.nextupvamp.statistics.dto.StatisticsDto;

public interface StatisticsService {

    StatisticsDto getStatisticsOnce(int resourceId);

    StatisticsDto getSavedStatistics(int resourceId);

    StatisticsDto getUpdatedStatistics(int resourceId);
}
