package ru.nextupvamp.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
            name = "resource_id_gen",
            sequenceName = "resource_id_gen",
            allocationSize = 1
    )
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private String path;

    @OneToOne(cascade = CascadeType.ALL)
    private ResourceFilters filters;
}
