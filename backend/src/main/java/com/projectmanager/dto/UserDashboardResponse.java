package com.projectmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardResponse {
    private long assignedIssuesCount;
    private long completedIssuesCount;
    private long pendingIssuesCount;
    private List<IssueResponse> recentAssignedIssues;
    private List<ProjectResponse> userProjects;
    private List<AuditLogResponse> recentActivity;
}
