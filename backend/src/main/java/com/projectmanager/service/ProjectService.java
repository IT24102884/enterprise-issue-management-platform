package com.projectmanager.service;

import com.projectmanager.dto.*;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request, Long creatorId);
    ProjectResponse getProjectById(Long id);
    ProjectResponse getProjectByKey(String key);
    PagedResponse<ProjectResponse> getAllProjects(Pageable pageable);
    ProjectResponse updateProject(Long id, ProjectUpdateRequest request);
    void deleteProject(Long id);
    ProjectStatsResponse getProjectStats(Long id);
}
