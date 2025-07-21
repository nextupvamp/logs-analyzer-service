package ru.nextupvamp.statistics.mapper;

import org.springframework.stereotype.Component;
import ru.nextupvamp.statistics.domain.Statistics;
import ru.nextupvamp.statistics.dto.StatisticsDto;

@Component
public class StatisticsMapper {

    public StatisticsDto mapDomainToDto(Statistics statistics) {
        return new StatisticsDto(
                statistics.ignoredRows(),
                statistics.remoteAddresses(),
                statistics.remoteUsers(),
                statistics.fromDate(),
                statistics.toDate(),
                statistics.requestsOnDate(),
                statistics.requestMethods(),
                statistics.requestResources(),
                statistics.statuses(),
                statistics.requestsAmount(),
                statistics.averageBytesSent(),
                statistics.p95BytesSent()
        );
    }

    public Statistics mapDtoToDomain(StatisticsDto statisticsDto) {
        Statistics statistics = new Statistics();
        statistics.ignoredRows(statisticsDto.ignoredRows());
        statistics.remoteAddresses(statisticsDto.remoteAddresses());
        statistics.remoteUsers(statisticsDto.remoteUsers());
        statistics.fromDate(statisticsDto.fromDate());
        statistics.toDate(statisticsDto.toDate());
        statistics.requestsOnDate(statisticsDto.requestsOnDate());
        statistics.requestMethods(statisticsDto.requestMethods());
        statistics.requestResources(statisticsDto.requestResources());
        statistics.statuses(statisticsDto.statuses());
        statistics.requestsAmount(statisticsDto.requestsAmount());
        statistics.averageBytesSent(statisticsDto.averageBytesSent());
        statistics.p95BytesSent(statisticsDto.p95BytesSent());
        return statistics;
    }
}
