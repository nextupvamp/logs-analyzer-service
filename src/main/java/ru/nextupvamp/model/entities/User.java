package ru.nextupvamp.model.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Login is mandatory")
    @Size(min = 6, message = "Login is too short")
    @Size(max = 20, message = "Login is too long")
    private String login;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password is too short")
    @Size(max = 20, message = "Password is too long")
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Resource> resources;
}
