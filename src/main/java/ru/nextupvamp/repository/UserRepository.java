package ru.nextupvamp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nextupvamp.model.entities.User;

public interface UserRepository extends JpaRepository<User, String> {
}
