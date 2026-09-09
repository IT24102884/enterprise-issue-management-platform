package com.projectmanager.dto;

import com.projectmanager.entity.enums.ProjectStatus;
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
public class ProjectResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String name;
    private String key;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UserDto createdBy;
    private int memberCount;
    private long totalIssues;
    private long openIssues;
    private double progressPercentage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

