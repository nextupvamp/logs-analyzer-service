package ru.nextupvamp.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.resource.domain.ResourceFilters;
import ru.nextupvamp.resource.dto.IdResponse;
import ru.nextupvamp.resource.dto.ResourceDto;
import ru.nextupvamp.resource.dto.UrlRequest;
import ru.nextupvamp.resource.service.ResourceServiceImpl;
import ru.nextupvamp.user.dto.UserDto;

@RestController
@RequestMapping("resources")
@AllArgsConstructor
@Tag(
        name = "Resource controller",
        description = "Controller is used to manage resources and gather statistics on them"
)
public class ResourceController {

    private ResourceServiceImpl resourceService;

    @Operation(
            summary = "Uploading new resource as file",
            description = "File can be uploaded for anonymous or authorized user " +
                    "depends on if the user request part is passed"
    )
    @PostMapping("upload_file")
    public IdResponse uploadFile(@RequestPart("file") MultipartFile file,
                                 @Valid @RequestPart(name = "user", required = false) UserDto user) {
        return resourceService.uploadFile(file, user);
    }

    @Operation(
            summary = "Uploading new resource as url",
            description = "Url can be uploaded for anonymous or authorized user " +
                    "depends on if the user request part is passed"
    )
    @PostMapping("upload_url")
    public IdResponse uploadUrl(@RequestPart("url") UrlRequest url,
                                @Valid @RequestPart(name = "user", required = false) UserDto user) {
        return resourceService.uploadUrl(url.url(), user);
    }

    @Operation(
            summary = "Uploading filters for resource",
            description = "Requires a map of filter fields and filter values"
    )
    @PostMapping("{id}/upload_filters")
    public IdResponse uploadFilters(@PathVariable int id, @RequestBody ResourceFilters filters) {
        return resourceService.uploadFilters(id, filters);
    }

    @Operation(summary = "Getting resource information")
    @GetMapping("{id}")
    public ResourceDto getResource(@PathVariable int id) {
        return resourceService.getResource(id);
    }


    @Operation(summary = "Deleting resource")
    @DeleteMapping("{id}")
    public void deleteResource(@PathVariable int id) {
        resourceService.deleteResource(id);
    }
}