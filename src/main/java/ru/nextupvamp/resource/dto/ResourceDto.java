package ru.nextupvamp.resource.dto;

import ru.nextupvamp.resource.enums.ResourceType;

public record ResourceDto(
        int id,
        String user,
        ResourceType type,
        String path
) {
}
