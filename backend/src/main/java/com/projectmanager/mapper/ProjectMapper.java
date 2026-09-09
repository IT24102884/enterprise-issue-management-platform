package com.projectmanager.mapper;

import com.projectmanager.dto.ProjectResponse;
import com.projectmanager.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectResponse toDto(Project project, int memberCount, long totalIssues, long openIssues, double progress) {
        if (project == null) {
            return null;
        }
        return ProjectResponse.builder()
                .id(project.getId())
                .organizationId(project.getOrganization().getId())
                .organizationName(project.getOrganization().getName())
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
