package com.projectmanager.service;

import com.projectmanager.dto.CreateIssueRequest;
import com.projectmanager.dto.IssueResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.dto.UpdateIssueRequest;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import org.springframework.data.domain.Pageable;

public interface IssueService {
    IssueResponse createIssue(Long projectId, Long reporterUserId, CreateIssueRequest request);
    IssueResponse getIssueById(Long id);
    IssueResponse getIssueByKey(String issueKey);
    PagedResponse<IssueResponse> getProjectIssues(Long projectId, IssueStatus status, IssuePriority priority, IssueType type, Long assigneeId, String search, Pageable pageable);
    IssueResponse updateIssue(Long issueId, Long currentUserId, UpdateIssueRequest request);
    IssueResponse updateIssueStatus(Long issueId, Long currentUserId, IssueStatus newStatus);
    void deleteIssue(Long issueId, Long currentUserId);
}
