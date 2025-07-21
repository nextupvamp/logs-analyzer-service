package ru.nextupvamp.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.nextupvamp.resource.domain.Resource;

import java.util.List;

@Entity
@Setter
@NoArgsConstructor
@Getter
@Table(name = "users")
public class User {
    @Id
    private String login;

    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Resource> resources;
}
