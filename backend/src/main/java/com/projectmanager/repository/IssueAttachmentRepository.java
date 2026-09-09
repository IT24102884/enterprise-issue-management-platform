package com.projectmanager.repository;

import com.projectmanager.entity.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, Long> {
    List<IssueAttachment> findByIssueId(Long issueId);
}

