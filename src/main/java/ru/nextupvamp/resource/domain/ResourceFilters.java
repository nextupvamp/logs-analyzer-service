package ru.nextupvamp.resource.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "resource_filters")
public class ResourceFilters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private ZonedDateTime fromDate;

    private ZonedDateTime toDate;

    @ElementCollection
    @CollectionTable(name = "filter_map", joinColumns = {@JoinColumn(name = "filters_id")})
    @MapKeyColumn(name = "field")
    @Column(name = "value")
    private Map<String, String> filterMap; // filter field : filter value regex
}
