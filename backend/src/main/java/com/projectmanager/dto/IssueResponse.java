package com.projectmanager.dto;

import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private Long id;
    private String issueKey;
    private String title;
    private String description;
    private IssueType type;
    private IssueStatus status;
    private IssuePriority priority;
    private Long projectId;
    private String projectKey;
    private String projectName;
    private UserDto reporter;
    private UserDto assignee;
    private Long milestoneId;
    private String milestoneTitle;
    private LocalDate dueDate;
    private List<IssueLabelDto> labels;
    private int commentCount;
    private int attachmentCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
