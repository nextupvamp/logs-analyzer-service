package ru.nextupvamp.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;
import ru.nextupvamp.service.LogsAnalyzerService;

@RestController
@AllArgsConstructor
public class LogsController {
    private LogsAnalyzerService service;

    // todo upload statistics to db, add user, then add auth

    @PostMapping("/upload/file")
    public IdResponse uploadFile(@RequestBody MultipartFile file) {
        return new IdResponse(service.uploadFile(file));
    }

    @PostMapping("/upload/url")
    public IdResponse uploadUrl(@RequestBody UrlRequest url) {
        return new IdResponse(service.uploadUrl(url.url()));
    }

    @PostMapping("/upload/filters/{id}")
    public IdResponse uploadFilters(@PathVariable int id, @RequestBody Filters filters) {
        return new IdResponse(service.uploadFilters(id, filters));
    }

    @GetMapping("/statistics/{id}")
    public LogsStatistics getStatistics(@PathVariable int id) {
        return service.getStatistics(id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteResource(@PathVariable int id) {
        service.deleteResource(id);
    }
}
