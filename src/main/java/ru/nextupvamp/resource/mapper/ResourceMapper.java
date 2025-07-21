package ru.nextupvamp.resource.mapper;

import org.springframework.stereotype.Component;
import ru.nextupvamp.resource.domain.Resource;
import ru.nextupvamp.resource.dto.ResourceDto;

@Component
public class ResourceMapper {

    public ResourceDto mapDomainToDto(Resource resource) {
        return new ResourceDto(
                resource.id(),
                resource.user().login(),
                resource.type(),
                resource.path()
        );
    }
}
