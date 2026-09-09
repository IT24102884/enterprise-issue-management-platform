package com.projectmanager.dto;

import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIssueRequest {

    @Size(max = 255, message = "Issue title must not exceed 255 characters")
    private String title;

    private String description;

    private IssueType type;

    private IssueStatus status;

    private IssuePriority priority;

    private Long assigneeId;

    private Long milestoneId;

    private LocalDate dueDate;

    private Set<String> labelNames;
}
