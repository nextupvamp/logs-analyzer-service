package ru.nextupvamp.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;
import ru.nextupvamp.service.LogsAnalyzerService;

@RestController
@AllArgsConstructor
public class LogsController {
    LogsAnalyzerService service;

    // todo: add exception handling, generalize code to make it extensible, add more log types to be read

    @PostMapping("/upload/file")
    public IdResponse uploadFile(@RequestBody MultipartFile file) {
        return new IdResponse(service.uploadFile(file));
    }

    @PostMapping("/upload/url")
    public IdResponse uploadUrl(@RequestBody UrlRequest url) {
        return new IdResponse(service.uploadUrl(url.url()));
    }

    @PostMapping("/upload/filters/{id}")
    public void uploadFilters(@PathVariable int id, @RequestBody Filters filters) {
        service.uploadFilters(id, filters);
    }

    @GetMapping("/statistics/{id}")
    public LogsStatistics getStatistics(@PathVariable int id) {
        return service.getStatistics(id);
    }
}
