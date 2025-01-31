package ru.nextupvamp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.data.ResourceDto;
import ru.nextupvamp.model.data.UserDto;
import ru.nextupvamp.model.entities.ResourceFilters;
import ru.nextupvamp.model.entities.Statistics;
import ru.nextupvamp.service.ResourceService;

@RestController
@RequestMapping("resources")
@AllArgsConstructor
@Tag(
        name = "Resource controller",
        description = "Controller is used to manage resources and gather statistics on them"
)
public class ResourceController {
    private ResourceService resourceService;

    @Operation(
            summary = "Uploading new resource as file",
            description = "File can be uploaded for anonymous or authorized user " +
                    "depends on if the user request part is passed"
    )
    @PostMapping("upload_file")
    public IdResponse uploadFile(@RequestPart("file") MultipartFile file,
                                 @Valid @RequestPart(name = "user", required = false) UserDto user) {
        return new IdResponse(resourceService.uploadFile(file, user));
    }

    @Operation(
            summary = "Uploading new resource as url",
            description = "Url can be uploaded for anonymous or authorized user " +
                    "depends on if the user request part is passed"
    )
    @PostMapping("upload_url")
    public IdResponse uploadUrl(@RequestPart("url") UrlRequest url,
                                @Valid @RequestPart(name = "user", required = false) UserDto user) {
        return new IdResponse(resourceService.uploadUrl(url.url(), user));
    }

    @Operation(
            summary = "Uploading filters for resource",
            description = "Requires a map of filter fields and filter values"
    )
    @PostMapping("{id}/upload_filters")
    public IdResponse uploadFilters(@PathVariable int id, @RequestBody ResourceFilters filters) {
        return new IdResponse(resourceService.uploadFilters(id, filters));
    }

    @Operation(summary = "Getting resource information")
    @GetMapping("{id}")
    public ResourceDto getResource(@PathVariable int id) {
        return resourceService.getResource(id);
    }

    @Operation(
            summary = "Gathering resource statistics once",
            description = "Meant to be used for anonymous user. Statistics won't be saved and " +
                    "resource will be deleted after gathering"
    )
    @GetMapping("{id}/statistics")
    public Statistics getStatistics(@PathVariable int id) {
        return resourceService.getStatisticsOnce(id);
    }

    @Operation(
            summary = "Getting already gathered statistics on resource",
            description = "Meant to be used for authorized user. Resource data won't be processed, " +
                    "method will just return already gathered data"
    )
    @GetMapping("{id}/statistics/saved")
    public Statistics getSavedStatistics(@PathVariable int id) {
        return resourceService.getSavedStatistics(id);
    }

    @Operation(
            summary = "Gathering statistics on resource",
            description = "Meant to be used for authorized user. Resource data will be processed and " +
                    "the method will return updated statistics even if it has been already saved"
    )
    @GetMapping("{id}/statistics/updated")
    public Statistics getUpdatedStatistics(@PathVariable int id) {
        return resourceService.getUpdatedStatistics(id);
    }

    @Operation(summary = "Deleting resource")
    @DeleteMapping("{id}")
    public void deleteResource(@PathVariable int id) {
        resourceService.deleteResource(id);
    }
}