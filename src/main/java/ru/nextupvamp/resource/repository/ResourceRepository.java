package ru.nextupvamp.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nextupvamp.resource.domain.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {
}
