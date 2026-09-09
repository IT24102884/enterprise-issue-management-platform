package com.projectmanager.repository;

import com.projectmanager.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    Optional<Issue> findByIssueKey(String issueKey);
    Page<Issue> findByProjectId(Long projectId, Pageable pageable);
    List<Issue> findByProjectId(Long projectId);

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT MAX(CAST(SUBSTRING(i.issueKey, LENGTH(:prefix) + 2) AS int)) FROM Issue i WHERE i.issueKey LIKE CONCAT(:prefix, '-%')")
    Integer findMaxSequenceByPrefix(@Param("prefix") String prefix);
}

