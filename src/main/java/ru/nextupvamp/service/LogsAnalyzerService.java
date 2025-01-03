package ru.nextupvamp.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;
import ru.nextupvamp.model.entities.FieldValueFilter;
import ru.nextupvamp.model.entities.Resource;
import ru.nextupvamp.model.entities.ResourceFilters;
import ru.nextupvamp.model.entities.ResourceType;
import ru.nextupvamp.model.handlers.LogsStatisticsGatherer;
import ru.nextupvamp.repository.ResourceRepository;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        var resourceFilters = resource.filters();
        Filters filters = mapResourceFiltersToFilters(resourceFilters);

        return logsStatisticsGatherer.gatherStatisticsFromFile(file, filters);
    }

    private LogsStatistics getStatisticsFromUri(Resource resource) {
        var uri = URI.create(resource.path());

        var resourceFilters = resource.filters();
        Filters filters = mapResourceFiltersToFilters(resourceFilters);

        return logsStatisticsGatherer.gatherStatisticsFromUri(uri, filters);
    }

    private Filters mapResourceFiltersToFilters(ResourceFilters resourceFilters) {
        if (resourceFilters == null) {
            return Filters.EMPTY;
        }
        ZonedDateTime fromDate = resourceFilters.fromDate();
        ZonedDateTime toDate = resourceFilters.toDate();
        Map<String, String> filterMap = null;

        if (resourceFilters.fieldValueFilters() != null && !resourceFilters.fieldValueFilters().isEmpty()) {
            filterMap = new HashMap<>();
            for (var filter : resourceFilters.fieldValueFilters()) {
                filterMap.put(filter.field(), filter.value());
            }
        }

        return Filters.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .filterMap(filterMap)
                .build();
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

        ZonedDateTime fromDate = filters.fromDate();
        ZonedDateTime toDate = filters.toDate();
        Map<String, String> filterMap = filters.filterMap();

        List<FieldValueFilter> fieldValueFilters = new ArrayList<>();
        filterMap.forEach((field, value) -> {
            var fieldValueFilter = new FieldValueFilter();
            fieldValueFilter.field(field);
            fieldValueFilter.value(value);
            fieldValueFilters.add(fieldValueFilter);
        });

        var resourceFilters = new ResourceFilters();
        resourceFilters.fromDate(fromDate);
        resourceFilters.toDate(toDate);
        resourceFilters.fieldValueFilters(fieldValueFilters);

        resource.filters(resourceFilters);
        resourceRepository.save(resource);

        return id;
    }

    private void validateFilters(Filters filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters is null");
        }

        ZonedDateTime from = filters.fromDate();
        ZonedDateTime to = filters.toDate();
        Map<String, String> filterMap = filters.filterMap();

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("From time must not be after to time");
        }
        filterMap.forEach((field, value) -> {
            if (field == null && value != null) {
                throw new IllegalArgumentException("Missing filter field for value " + value);
            }
            if (field != null && value == null) {
                throw new IllegalArgumentException("Missing filter value regex for field " + field);
            }
        });
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
