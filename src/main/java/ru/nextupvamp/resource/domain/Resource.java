package ru.nextupvamp.resource.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nextupvamp.resource.enums.ResourceType;
import ru.nextupvamp.statistics.domain.Statistics;
import ru.nextupvamp.user.domain.User;

@Entity
@NoArgsConstructor
@Setter
@Getter
@Table(name = "resource")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_login")
    private User user;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private String path;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "filters_id")
    private ResourceFilters filters;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "statistics_id")
    private Statistics statistics;
}
