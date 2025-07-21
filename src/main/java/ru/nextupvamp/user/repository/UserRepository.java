package ru.nextupvamp.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nextupvamp.user.domain.User;

public interface UserRepository extends JpaRepository<User, String> {
}
