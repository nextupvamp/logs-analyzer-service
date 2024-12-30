package ru.nextupvamp.service;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
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

    public LogsStatistics getStatisticsFromFile(Resource resource) {
        var file = Path.of(resource.path());
        var statistics =  logsStatisticsGatherer.gatherStatisticsFromFile(file, resource.filters());
        resourceRepository.delete(resource);
        return statistics;
    }

    public LogsStatistics getStatisticsFromUri(Resource resource) {
        var uri = URI.create(resource.path());
        var statistics = logsStatisticsGatherer.gatherStatisticsFromUri(uri, resource.filters());
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

    public void uploadFilters(int id, Filters filters) {
        var resource = resourceRepository.findById(id).orElseThrow();
        resource.filters(filters);
        resourceRepository.save(resource);
    }

    private String getFreeName() {
        return userFilesDirectory + "logs" + System.currentTimeMillis() + ".txt";
    }
}
