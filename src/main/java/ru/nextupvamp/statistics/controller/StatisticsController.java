package ru.nextupvamp.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nextupvamp.statistics.dto.StatisticsDto;
import ru.nextupvamp.statistics.service.StatisticsService;

@RestController
@RequestMapping("resources")
@AllArgsConstructor
@Tag(
        name = "Resource statistics controller",
        description = "Controller is used to gather statistics on resources"
)
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(
            summary = "Gathering resource statistics once",
            description = "Meant to be used for anonymous user. Statistics won't be saved and " +
                    "resource will be deleted after gathering"
    )
    @GetMapping("{id}/statistics")
    public StatisticsDto getStatistics(@PathVariable int id) {
        return statisticsService.getStatisticsOnce(id);
    }

    @Operation(
            summary = "Getting already gathered statistics on resource",
            description = "Meant to be used for authorized user. Resource data won't be processed, " +
                    "method will just return already gathered data"
    )
    @GetMapping("{id}/statistics/saved")
    public StatisticsDto getSavedStatistics(@PathVariable int id) {
        return statisticsService.getSavedStatistics(id);
    }

    @Operation(
            summary = "Gathering statistics on resource",
            description = "Meant to be used for authorized user. Resource data will be processed and " +
                    "the method will return updated statistics even if it has been already saved"
    )
    @GetMapping("{id}/statistics/updated")
    public StatisticsDto getUpdatedStatistics(@PathVariable int id) {
        return statisticsService.getUpdatedStatistics(id);
    }
}
