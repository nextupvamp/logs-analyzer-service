package ru.nextupvamp.dto;

import ru.nextupvamp.model.entities.ResourceType;

public record ResourceDto(
        int id,
        String user,
        ResourceType type,
        String path
) {
}
