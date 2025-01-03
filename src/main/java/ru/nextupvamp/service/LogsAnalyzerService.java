package ru.nextupvamp.service;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;
import ru.nextupvamp.model.entities.Resource;
import ru.nextupvamp.model.entities.ResourceType;
import ru.nextupvamp.model.handlers.LogsStatisticsGatherer;
import ru.nextupvamp.repository.ResourceRepository;

@Service
@RequiredArgsConstructor
public class LogsAnalyzerService {
    @Value("${file.user-file-directory}")
    private String userFilesDirectory;
    private final ResourceRepository resourceRepository;
    private final LogsStatisticsGatherer logsStatisticsGatherer;

    public LogsStatistics getStatistics(int id) {
        var resource = resourceRepository.findById(id).orElseThrow();
        return switch (resource.type()) {
            case FILE -> getStatisticsFromFile(resource);
            case URL -> getStatisticsFromUri(resource);
        };
    }

    private LogsStatistics getStatisticsFromFile(Resource resource) {
        var file = Path.of(resource.path());

        Filters filters = resource.filters();
        if (filters == null) {
            filters = Filters.EMPTY;
        }

        var statistics =  logsStatisticsGatherer.gatherStatisticsFromFile(file, filters);
        resourceRepository.delete(resource);
        return statistics;
    }

    private LogsStatistics getStatisticsFromUri(Resource resource) {
        var uri = URI.create(resource.path());

        Filters filters = resource.filters();
        if (filters == null) {
            filters = Filters.EMPTY;
        }

        var statistics = logsStatisticsGatherer.gatherStatisticsFromUri(uri, filters);
        resourceRepository.delete(resource);
        return statistics;
    }

    @SneakyThrows
    public int uploadFile(MultipartFile file) {
        String path = getFreeName();
        file.transferTo(new File(path));
        var resource = new Resource();
        resource.type(ResourceType.FILE);
        resource.path(path);
        resourceRepository.save(resource);
        return resource.id();
    }

    public int uploadUrl(String url) {
        var resource = new Resource();
        resource.type(ResourceType.URL);
        resource.path(url);
        resourceRepository.save(resource);
        return resource.id();
    }

    public int uploadFilters(int id, Filters filters) {
        validateFilters(filters);
        var resource = resourceRepository.findById(id).orElseThrow();
        resource.filters(filters);
        resourceRepository.save(resource);
        return id;
    }

    private void validateFilters(Filters filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters is null");
        }

        ZonedDateTime from = filters.fromTime();
        ZonedDateTime to = filters.toTime();
        String filterField = filters.filterField();
        String filterValueRegex = filters.filterValueRegex();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From time must not be after to time");
        }
        if (filterField == null && filterValueRegex != null) {
            throw new IllegalArgumentException("Missing filter field");
        }
        if (filterField != null && filterValueRegex == null) {
            throw new IllegalArgumentException("Missing filter value regex");
        }
    }

    private String getFreeName() {
        String fileName = userFilesDirectory + "logs" + System.currentTimeMillis() + ".txt";
        int i = 0;
        while (Files.exists(Path.of(fileName))) {
            fileName = userFilesDirectory + "logs" + System.currentTimeMillis() + (++i) + ".txt";
        }
        return fileName;
    }
}
