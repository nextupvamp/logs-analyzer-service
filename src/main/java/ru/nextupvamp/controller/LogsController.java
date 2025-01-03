package ru.nextupvamp.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private LogsAnalyzerService service;

    @PostMapping("/upload/file")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        return ResponseEntity.ok().body(getIdResponse(service.uploadFile(file)));
    }

    @PostMapping("/upload/url")
    public ResponseEntity<?> uploadUrl(@RequestBody String url) {
        return ResponseEntity.ok().body(getIdResponse(service.uploadUrl(url)));
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
}
