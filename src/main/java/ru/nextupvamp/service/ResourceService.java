package ru.nextupvamp.service;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.entities.*;
import ru.nextupvamp.model.handlers.LogsStatisticsGatherer;
import ru.nextupvamp.repository.ResourceRepository;
import ru.nextupvamp.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogsAnalyzerService {
    @Value("${file.user-file-directory}")
    private String userFilesDirectory;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final LogsStatisticsGatherer logsStatisticsGatherer;

    public LogsStatistics getStatistics(int id) {
        var resource = resourceRepository.findById(id).orElseThrow();
    public Statistics getSavedStatistics(int resourceId) {
        return resourceRepository.findById(resourceId).orElseThrow(NO_RESOURCE_WITH_SUCH_ID).statistics();
    }

    public Statistics getUpdatedStatistics(int resourceId) {
        var resource = resourceRepository.findById(resourceId).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        var statistics = gatherStatistics(resource);
        resource.statistics(statistics);
        resourceRepository.save(resource);
        return statistics;
    }

    public Statistics getStatisticsOnce(int resourceId) {
        var resource = resourceRepository.findById(resourceId).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        var statistics = gatherStatistics(resource);
        resourceRepository.delete(resource);
        return statistics;
    }

    private Statistics gatherStatistics(Resource resource) {
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

    private Statistics getStatisticsFromUri(Resource resource) {
        var uri = URI.create(resource.path());
        var resourceFilters = resource.filters();
        return logsStatisticsGatherer.gatherStatisticsFromUri(uri, resourceFilters);
    }

    @SneakyThrows
    public int uploadFile(MultipartFile file, User user) {
        User persistentUser = null;
        if (user != null && user.login() != null && !user.login().isEmpty()) {
            persistentUser = userRepository.findById(user.login()).orElseThrow(USER_NOT_FOUND);
        }

        String path = getFreeFileName();
        File actualFile = new File(path);
        @Cleanup FileOutputStream fos = new FileOutputStream(actualFile);
        fos.write(file.getBytes());

        var resource = new Resource();
        resource.type(ResourceType.FILE).path(path).user(persistentUser);
        resourceRepository.save(resource);
        return resource.id();
    }

    private String getFreeFileName() {
        String fileName = userFilesDirectory + "logs" + System.currentTimeMillis() + ".txt";
        int i = 0;
        while (Files.exists(Path.of(fileName))) {
            fileName = userFilesDirectory + "logs" + System.currentTimeMillis() + (++i) + ".txt";
        }
        return fileName;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public int uploadUrl(String url, User user) {
        User persistentUser = null;
        if (user != null && user.login() != null && !user.login().isEmpty()) {
            persistentUser = userRepository.findById(user.login()).orElseThrow(USER_NOT_FOUND);
        }

        try {
            URI.create(url);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("URL is null");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL Format");
        }

        var resource = new Resource();
        resource.type(ResourceType.URL).path(url).user(persistentUser);
        resourceRepository.save(resource);
        return resource.id();
    }

    public int uploadFilters(int id, ResourceFilters filters) {
        validateFilters(filters);

        var resource = resourceRepository.findById(id).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);

        var resourceFilters = new ResourceFilters();
        resourceFilters.fromDate(filters.fromDate());
        resourceFilters.toDate(filters.toDate());
        resourceFilters.filterMap(filters.filterMap());
        resourceFilters.fromDate(filters.fromDate())
                .toDate(filters.toDate())
                .filterMap(filters.filterMap());

        resource.filters(resourceFilters);
        resourceRepository.save(resource);

        return id;
    }

    private void validateFilters(ResourceFilters filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters is null");
        }

        ZonedDateTime from = filters.fromDate();
        ZonedDateTime to = filters.toDate();
        Map<String, String> filterMap = filters.filterMap();

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("From time must not be after to time");
        }
        if (filterMap != null) {
            filterMap.forEach((field, value) -> {
                if (field == null && value != null) {
                    throw new IllegalArgumentException("Missing filter field for value " + value);
                }
                if (field != null && value == null) {
                    throw new IllegalArgumentException("Missing filter value regex for field " + field);
                }
            });
        }
    }

    @SneakyThrows
    public void deleteResource(int id) {
        var resource = resourceRepository.findById(id).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        if (resource.type() == ResourceType.FILE) {
            Files.delete(Path.of(resource.path()));
        }
        resourceRepository.delete(resource);
    }
}
