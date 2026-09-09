package com.projectmanager.repository;

import com.projectmanager.entity.Milestone;
import com.projectmanager.entity.enums.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProjectId(Long projectId);
    List<Milestone> findByProjectIdOrderByDueDateAsc(Long projectId);
    List<Milestone> findByProjectIdAndStatusOrderByDueDateAsc(Long projectId, MilestoneStatus status);
    long countByProjectId(Long projectId);
}

