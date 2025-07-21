package ru.nextupvamp.statistics.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nextupvamp.resource.domain.Resource;
import ru.nextupvamp.resource.repository.ResourceRepository;
import ru.nextupvamp.statistics.dto.StatisticsDto;
import ru.nextupvamp.statistics.mapper.StatisticsMapper;

import java.net.URI;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final Supplier<NoSuchElementException> NO_RESOURCE_WITH_SUCH_ID =
            () -> new NoSuchElementException("No Resource With Such ID");

    private final ResourceRepository resourceRepository;
    private final LogsStatisticsGatherer logsStatisticsGatherer;
    private final StatisticsMapper statisticsMapper;

    @Override
    public StatisticsDto getSavedStatistics(int resourceId) {
        return statisticsMapper.mapDomainToDto(resourceRepository.findById(resourceId)
                .orElseThrow(NO_RESOURCE_WITH_SUCH_ID).statistics());
    }

    @Override
    public StatisticsDto getUpdatedStatistics(int resourceId) {
        var resource = resourceRepository.findById(resourceId).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        var statistics = gatherStatistics(resource);
        resource.statistics(statisticsMapper.mapDtoToDomain(statistics));
        resourceRepository.save(resource);
        return statistics;
    }

    @Override
    public StatisticsDto getStatisticsOnce(int resourceId) {
        var resource = resourceRepository.findById(resourceId).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        var statistics = gatherStatistics(resource);
        resourceRepository.delete(resource);
        return statistics;
    }

    private StatisticsDto gatherStatistics(Resource resource) {
        return switch (resource.type()) {
            case FILE -> getStatisticsFromFile(resource);
            case URL -> getStatisticsFromUri(resource);
        };
    }

    private StatisticsDto getStatisticsFromFile(Resource resource) {
        var file = Path.of(resource.path());
        var resourceFilters = resource.filters();
        return logsStatisticsGatherer.gatherStatisticsFromFile(file, resourceFilters);
    }

    private StatisticsDto getStatisticsFromUri(Resource resource) {
        var uri = URI.create(resource.path());
        var resourceFilters = resource.filters();
        return logsStatisticsGatherer.gatherStatisticsFromUri(uri, resourceFilters);
    }
}
