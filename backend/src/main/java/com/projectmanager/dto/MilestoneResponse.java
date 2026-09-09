package com.projectmanager.dto;

import com.projectmanager.entity.enums.MilestoneStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private MilestoneStatus status;
    private long totalIssues;
    private long closedIssues;
    private long openIssues;
    private double progressPercentage;
    private OffsetDateTime createdAt;
}
