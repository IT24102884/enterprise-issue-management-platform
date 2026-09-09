package com.projectmanager.mapper;

import com.projectmanager.dto.MilestoneResponse;
import com.projectmanager.entity.Milestone;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    public MilestoneResponse toResponse(Milestone milestone, long totalIssues, long closedIssues) {
        if (milestone == null) {
            return null;
        }

        long openIssues = Math.max(0, totalIssues - closedIssues);
        double progressPercentage = totalIssues > 0
                ? Math.round(((double) closedIssues / totalIssues) * 100.0 * 10.0) / 10.0
                : 0.0;

        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProject() != null ? milestone.getProject().getId() : null)
                .projectName(milestone.getProject() != null ? milestone.getProject().getName() : null)
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .startDate(milestone.getStartDate())
                .dueDate(milestone.getDueDate())
                .status(milestone.getStatus())
                .totalIssues(totalIssues)
                .closedIssues(closedIssues)
                .openIssues(openIssues)
                .progressPercentage(progressPercentage)
                .createdAt(milestone.getCreatedAt())
                .build();
    }
}
