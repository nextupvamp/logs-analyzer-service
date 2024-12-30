package ru.nextupvamp.model.entities;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nextupvamp.model.data.Filters;

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
    @Embedded
    private Filters filters;
}
