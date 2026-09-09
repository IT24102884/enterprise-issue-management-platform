package com.projectmanager.repository;

import com.projectmanager.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByKey(String key);
    boolean existsByKey(String key);
    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);
}

