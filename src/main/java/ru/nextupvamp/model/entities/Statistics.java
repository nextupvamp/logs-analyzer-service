package ru.nextupvamp.model.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@Getter(onMethod = @__(@JsonProperty))
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
            name = "statistics_id_gen",
            sequenceName = "statistics_id_gen",
            allocationSize = 1
    )
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

    private long averageBytesSent;

    @Column(name = "p_95_bytes_sent")
    private long p95BytesSent;
}
