package com.projectmanager.mapper;

import com.projectmanager.dto.AuditLogResponse;
import com.projectmanager.dto.IssueCommentResponse;
import com.projectmanager.dto.IssueLabelDto;
import com.projectmanager.dto.IssueResponse;
import com.projectmanager.entity.AuditLog;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.IssueComment;
import com.projectmanager.entity.IssueLabel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IssueMapper {

    private final UserMapper userMapper;

    public IssueLabelDto toLabelDto(IssueLabel label) {
        if (label == null) {
            return null;
        }
        return IssueLabelDto.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }

    public IssueResponse toResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        List<IssueLabelDto> labelDtos = issue.getLabels() != null
                ? issue.getLabels().stream().map(this::toLabelDto).collect(Collectors.toList())
                : Collections.emptyList();

        int commentCount = issue.getComments() != null ? issue.getComments().size() : 0;
        int attachmentCount = issue.getAttachments() != null ? issue.getAttachments().size() : 0;

        return IssueResponse.builder()
                .id(issue.getId())
                .issueKey(issue.getIssueKey())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .type(issue.getType())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .projectId(issue.getProject() != null ? issue.getProject().getId() : null)
                .projectKey(issue.getProject() != null ? issue.getProject().getKey() : null)
                .projectName(issue.getProject() != null ? issue.getProject().getName() : null)
                .reporter(userMapper.toDto(issue.getReporter()))
                .assignee(userMapper.toDto(issue.getAssignee()))
                .milestoneId(issue.getMilestone() != null ? issue.getMilestone().getId() : null)
                .milestoneTitle(issue.getMilestone() != null ? issue.getMilestone().getTitle() : null)
                .dueDate(issue.getDueDate())
                .labels(labelDtos)
                .commentCount(commentCount)
                .attachmentCount(attachmentCount)
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }

    public IssueCommentResponse toCommentResponse(IssueComment comment) {
        if (comment == null) {
            return null;
        }
        return IssueCommentResponse.builder()
                .id(comment.getId())
                .issueId(comment.getIssue() != null ? comment.getIssue().getId() : null)
                .user(userMapper.toDto(comment.getUser()))
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public AuditLogResponse toAuditLogResponse(AuditLog log) {
        if (log == null) {
            return null;
        }
        return AuditLogResponse.builder()
                .id(log.getId())
                .user(userMapper.toDto(log.getUser()))
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .timestamp(log.getTimestamp())
                .metadata(log.getMetadata())
                .build();
    }
}
