package ru.nextupvamp.resource.service;

import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.resource.domain.ResourceFilters;
import ru.nextupvamp.resource.dto.IdResponse;
import ru.nextupvamp.resource.dto.ResourceDto;
import ru.nextupvamp.user.dto.UserDto;

public interface ResourceService {

    ResourceDto getResource(int id);

    IdResponse uploadFile(MultipartFile file, UserDto user);

    IdResponse uploadUrl(String url, UserDto user);

    IdResponse uploadFilters(int id, ResourceFilters filters);

    void deleteResource(int id);
}
