package ru.nextupvamp.resource.service;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.resource.domain.Resource;
import ru.nextupvamp.resource.domain.ResourceFilters;
import ru.nextupvamp.resource.dto.IdResponse;
import ru.nextupvamp.resource.dto.ResourceDto;
import ru.nextupvamp.resource.enums.ResourceType;
import ru.nextupvamp.resource.repository.ResourceRepository;
import ru.nextupvamp.user.domain.User;
import ru.nextupvamp.user.dto.UserDto;
import ru.nextupvamp.user.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Supplier<NoSuchElementException> NO_RESOURCE_WITH_SUCH_ID =
            () -> new NoSuchElementException("No Resource With Such ID");
    private static final Supplier<NoSuchElementException> USER_NOT_FOUND =
            () -> new NoSuchElementException("User Not Found");

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final String userFilesDirectory;

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               UserRepository userRepository,
                               @Value("${file.user-file-directory}")
                               String userFilesDirectory
    ) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.userFilesDirectory = userFilesDirectory;
    }

    @Override
    public ResourceDto getResource(int id) {
        var foundResource = resourceRepository.findById(id).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        String userLogin = null;
        if (foundResource.user() != null) {
            userLogin = foundResource.user().login();
        }
        return new ResourceDto(id, userLogin, foundResource.type(), foundResource.path());
    }

    @Override
    @SneakyThrows
    public IdResponse uploadFile(MultipartFile file, UserDto user) {
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
        return new IdResponse(resource.id());
    }

    private String getFreeFileName() {
        String fileName = userFilesDirectory + "logs" + System.currentTimeMillis() + ".txt";
        int i = 0;
        while (Files.exists(Path.of(fileName))) {
            fileName = userFilesDirectory + "logs" + System.currentTimeMillis() + (++i) + ".txt";
        }
        return fileName;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public IdResponse uploadUrl(String url, UserDto user) {
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
        return new IdResponse(resource.id());
    }

    @Override
    public IdResponse uploadFilters(int id, ResourceFilters filters) {
        validateFilters(filters);

        var resource = resourceRepository.findById(id).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);

        var resourceFilters = new ResourceFilters();
        resourceFilters.fromDate(filters.fromDate())
                .toDate(filters.toDate())
                .filterMap(filters.filterMap());

        resource.filters(resourceFilters);
        int filtersId = resourceRepository.save(resource).filters().id();

        return new IdResponse(filtersId);
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

    @Override
    @SneakyThrows
    public void deleteResource(int id) {
        var resource = resourceRepository.findById(id).orElseThrow(NO_RESOURCE_WITH_SUCH_ID);
        if (resource.type() == ResourceType.FILE) {
            Files.delete(Path.of(resource.path()));
        }
        resourceRepository.delete(resource);
    }
}
