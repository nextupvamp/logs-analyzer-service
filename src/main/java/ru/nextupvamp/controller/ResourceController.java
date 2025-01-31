package ru.nextupvamp.controller;

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
public class ResourceController {
    private ResourceService resourceService;

    @PostMapping("upload_file")
    public IdResponse uploadFile(@RequestPart("file") MultipartFile file,
                                 @Valid @RequestPart(name = "user", required = false) UserDto user) {
        return new IdResponse(resourceService.uploadFile(file, user));
    }

    @PostMapping("upload_url")
    public IdResponse uploadUrl(@RequestPart("url") UrlRequest url,
                                @Valid @RequestPart(name = "user", required = false) UserDto user) {
        return new IdResponse(resourceService.uploadUrl(url.url(), user));
    }

    @PostMapping("{id}/upload_filters")
    public IdResponse uploadFilters(@PathVariable int id, @RequestBody ResourceFilters filters) {
        return new IdResponse(resourceService.uploadFilters(id, filters));
    }

    @GetMapping("{id}")
    public ResourceDto getResource(@PathVariable int id) {
        return resourceService.getResource(id);
    }

    @GetMapping("{id}/statistics")
    public Statistics getStatistics(@PathVariable int id) {
        return resourceService.getStatisticsOnce(id);
    }

    @GetMapping("{id}/statistics/saved")
    public Statistics getSavedStatistics(@PathVariable int id) {
        return resourceService.getSavedStatistics(id);
    }

    @GetMapping("{id}/statistics/updated")
    public Statistics getUpdatedStatistics(@PathVariable int id) {
        return resourceService.getUpdatedStatistics(id);
    }

    @DeleteMapping("{id}")
    public void deleteResource(@PathVariable int id) {
        resourceService.deleteResource(id);
    }
}