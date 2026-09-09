package com.projectmanager.repository;

import com.projectmanager.entity.IssueLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {
    List<IssueLabel> findByProjectId(Long projectId);
    Optional<IssueLabel> findByProjectIdAndName(Long projectId, String name);
}
