package com.projectmanager.mapper;

import com.projectmanager.dto.ProjectResponse;
import com.projectmanager.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectResponse toResponse(Project project, int memberCount, long totalIssues, long openIssues) {
        if (project == null) {
            return null;
        }
        long completed = Math.max(0, totalIssues - openIssues);
        double progress = totalIssues > 0
                ? Math.round(((double) completed / totalIssues) * 100.0 * 10.0) / 10.0
                : 0.0;
        return toDto(project, memberCount, totalIssues, openIssues, progress);
    }

    public ProjectResponse toDto(Project project, int memberCount, long totalIssues, long openIssues, double progress) {
        if (project == null) {
            return null;
        }
        return ProjectResponse.builder()
                .id(project.getId())
                .organizationId(project.getOrganization() != null ? project.getOrganization().getId() : null)
                .organizationName(project.getOrganization() != null ? project.getOrganization().getName() : null)
                .name(project.getName())
                .key(project.getKey())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdBy(userMapper.toDto(project.getCreatedBy()))
                .memberCount(memberCount)
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .progressPercentage(progress)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}

