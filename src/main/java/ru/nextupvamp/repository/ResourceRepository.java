package ru.nextupvamp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nextupvamp.model.entities.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {
}
