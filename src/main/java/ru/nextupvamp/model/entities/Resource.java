package ru.nextupvamp.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Getter(onMethod = @__(@JsonProperty))
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
            name = "resource_id_gen",
            sequenceName = "resource_id_gen",
            allocationSize = 1
    )
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_login")
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private String path;

    @OneToOne(cascade = CascadeType.ALL)
    private ResourceFilters filters;

    @OneToOne(cascade = CascadeType.ALL)
    private Statistics statistics;
}
