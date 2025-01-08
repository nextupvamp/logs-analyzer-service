package ru.nextupvamp.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nextupvamp.model.data.Filters;
import ru.nextupvamp.model.data.LogsStatistics;
import ru.nextupvamp.service.LogsAnalyzerService;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
public class LogsController {
    private LogsAnalyzerService service;

    @PostMapping("/upload/file")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        return ResponseEntity.ok().body(getIdResponse(service.uploadFile(file)));
    }

    @PostMapping("/upload/url")
    public ResponseEntity<?> uploadUrl(@RequestBody UrlRequest url) {
        return ResponseEntity.ok().body(getIdResponse(service.uploadUrl(url.url())));
    }

    @PostMapping("/upload/filters/{id}")
    public ResponseEntity<?> uploadFilters(@PathVariable int id, @RequestBody Filters filters) {
        return ResponseEntity.ok().body(getIdResponse(service.uploadFilters(id, filters)));
    }

    private Map<String, Integer> getIdResponse(int id) {
        Map<String, Integer> idResponse = new HashMap<>();
        idResponse.put("id", id);
        return idResponse;
    }

    @GetMapping("/statistics/{id}")
    public LogsStatistics getStatistics(@PathVariable int id) {
        return service.getStatistics(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable int id) {
        service.deleteResource(id);
        return ResponseEntity.ok().build();
    }
}
