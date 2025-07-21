package ru.nextupvamp.statistics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nextupvamp.resource.domain.Resource;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Setter
@NoArgsConstructor
@Getter
@Table(name = "statistics")
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    private int ignoredRows;

    @ElementCollection
    @Column(name = "amount")
    @MapKeyColumn(name = "remote_address")
    @CollectionTable(name = "remote_address", joinColumns = {@JoinColumn(name = "resource_id")})
    private Map<String, Integer> remoteAddresses;

    @ElementCollection
    @Column(name = "amount")
    @MapKeyColumn(name = "remote_user")
    @CollectionTable(name = "remote_user", joinColumns = {@JoinColumn(name = "resource_id")})
    private Map<String, Integer> remoteUsers;

    private ZonedDateTime fromDate;

    private ZonedDateTime toDate;

    @ElementCollection
    @Column(name = "amount")
    @MapKeyColumn(name = "date")
    @CollectionTable(name = "requests_on_date", joinColumns = {@JoinColumn(name = "resource_id")})
    private Map<ZonedDateTime, Integer> requestsOnDate;

    @ElementCollection
    @Column(name = "amount")
    @MapKeyColumn(name = "method")
    @CollectionTable(name = "request_method", joinColumns = {@JoinColumn(name = "resource_id")})
    private Map<String, Integer> requestMethods;

    @ElementCollection
    @Column(name = "amount")
    @MapKeyColumn(name = "request_resource")
    @CollectionTable(name = "request_resources", joinColumns = {@JoinColumn(name = "resource_id")})
    private Map<String, Integer> requestResources;

    @ElementCollection
    @Column(name = "amount")
    @MapKeyColumn(name = "status")
    @CollectionTable(name = "statuses", joinColumns = {@JoinColumn(name = "resource_id")})
    private Map<Short, Integer> statuses;

    private int requestsAmount;

    private double averageBytesSent;

    @Column(name = "p_95_bytes_sent")
    private long p95BytesSent;
}
