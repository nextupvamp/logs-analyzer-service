package ru.nextupvamp.model.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Getter(onMethod = @__(@JsonProperty))
@Table(name = "user_table")
public class User {
    @Id
    private String login;

    private String password;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_login")
    @JsonManagedReference
    private List<Resource> resources;
}
