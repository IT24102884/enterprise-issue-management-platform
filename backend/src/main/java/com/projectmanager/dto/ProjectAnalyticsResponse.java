package com.projectmanager.dto;

import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAnalyticsResponse {
    private Long projectId;
    private String projectName;
    private String projectKey;
    private long totalIssues;
    private long openIssues;
    private long completedIssues;
    private double progressPercentage;
    private int memberCount;
    private int milestoneCount;
    private Map<IssueStatus, Long> statusBreakdown;
    private Map<IssuePriority, Long> priorityBreakdown;
    private Map<IssueType, Long> typeBreakdown;
    private List<AuditLogResponse> recentActivity;
}
