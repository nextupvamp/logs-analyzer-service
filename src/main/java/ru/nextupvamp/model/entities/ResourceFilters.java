package ru.nextupvamp.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
public class ResourceFilters {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private ZonedDateTime fromDate;

    private ZonedDateTime toDate;

    @ElementCollection
    @CollectionTable(name = "filter_map", joinColumns = {@JoinColumn(name = "filters_id")})
    @MapKeyColumn(name = "field")
    @Column(name = "value")
    private Map<String, String> filterMap;
}
