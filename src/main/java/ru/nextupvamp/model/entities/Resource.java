package ru.nextupvamp.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private String path;

    @OneToOne(cascade = CascadeType.ALL)
    private ResourceFilters filters;
}
