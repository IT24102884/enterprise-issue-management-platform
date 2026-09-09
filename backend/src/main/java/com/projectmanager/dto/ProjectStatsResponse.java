package com.projectmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsResponse {
    private Long projectId;
    private String projectKey;
    private String projectName;
    private long totalIssues;
    private long todoIssues;
    private long inProgressIssues;
    private long inReviewIssues;
    private long doneIssues;
    private long closedIssues;
    private double progressPercentage;
    private int totalMembers;
}
