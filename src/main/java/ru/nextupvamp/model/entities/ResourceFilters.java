package ru.nextupvamp.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class ResourceFilters {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private ZonedDateTime fromDate;

    private ZonedDateTime toDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "filter_id")
    private List<FieldValueFilter> fieldValueFilters;
}
