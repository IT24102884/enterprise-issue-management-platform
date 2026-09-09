package com.projectmanager.repository;

import com.projectmanager.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;

import java.util.Collection;
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

    List<Issue> findByMilestoneId(Long milestoneId);
    long countByMilestoneId(Long milestoneId);
    long countByMilestoneIdAndStatusIn(Long milestoneId, Collection<IssueStatus> statuses);

    long countByProjectIdAndStatus(Long projectId, IssueStatus status);
    long countByProjectIdAndStatusIn(Long projectId, Collection<IssueStatus> statuses);
    long countByProjectIdAndPriority(Long projectId, IssuePriority priority);
    long countByProjectIdAndType(Long projectId, IssueType type);

    long countByAssigneeId(Long assigneeId);
    long countByAssigneeIdAndStatusIn(Long assigneeId, Collection<IssueStatus> statuses);
    List<Issue> findTop10ByAssigneeIdOrderByUpdatedAtDesc(Long assigneeId);
}

