package ru.nextupvamp.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.entities.Resource;
import ru.nextupvamp.model.entities.ResourceFilters;
import ru.nextupvamp.model.entities.Statistics;
import ru.nextupvamp.model.entities.User;
import ru.nextupvamp.service.ResourceService;

@RestController
@RequestMapping("resources")
@AllArgsConstructor
public class ResourceController {
    private ResourceService resourceService;

    @PostMapping("upload_file")
    public IdResponse uploadFile(@RequestPart("file") MultipartFile file,
                                 @RequestPart(name = "user", required = false) User user) {
        return new IdResponse(resourceService.uploadFile(file, user));
    }

    @PostMapping("upload_url")
    public IdResponse uploadUrl(@RequestPart("url") UrlRequest url,
                                @RequestPart(name = "user", required = false) User user) {
        return new IdResponse(resourceService.uploadUrl(url.url(), user));
    }

    @PostMapping("{id}/upload_filters")
    public IdResponse uploadFilters(@PathVariable int id, @RequestBody ResourceFilters filters) {
        return new IdResponse(resourceService.uploadFilters(id, filters));
    }

    @GetMapping("{id}")
    public Resource getResource(@PathVariable int id) {
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