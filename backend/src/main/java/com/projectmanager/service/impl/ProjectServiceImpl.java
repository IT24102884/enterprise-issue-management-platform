package com.projectmanager.service.impl;

import com.projectmanager.dto.*;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.Organization;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.ProjectMember;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.ProjectMapper;
import com.projectmanager.repository.*;
import com.projectmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IssueRepository issueRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, Long creatorId) {
        String normalizedKey = request.getKey().trim().toUpperCase();

        if (projectRepository.existsByKey(normalizedKey)) {
            throw new BadRequestException("Project key '" + normalizedKey + "' is already in use");
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + request.getOrganizationId()));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));

        Project project = Project.builder()
                .organization(organization)
                .name(request.getName().trim())
                .key(normalizedKey)
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(creator)
                .build();

        Project savedProject = projectRepository.save(project);

        // Automatically assign the creator as a Project Manager in the project membership
        ProjectMember initialMember = ProjectMember.builder()
                .project(savedProject)
                .user(creator)
                .role(Role.PROJECT_MANAGER)
                .build();
        projectMemberRepository.save(initialMember);

        log.info("Created project '{}' ({}) by user {}", savedProject.getName(), savedProject.getKey(), creator.getEmail());

        return projectMapper.toDto(savedProject, 1, 0, 0, 0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return buildProjectResponseWithStats(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectByKey(String key) {
        Project project = projectRepository.findByKey(key.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + key));
        return buildProjectResponseWithStats(project);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> getAllProjects(Pageable pageable) {
        Page<Project> projectPage = projectRepository.findAll(pageable);
        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(this::buildProjectResponseWithStats)
                .toList();

        return PagedResponse.<ProjectResponse>builder()
                .content(content)
                .page(projectPage.getNumber())
                .size(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .last(projectPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project updated = projectRepository.save(project);
        log.info("Updated project id {}: {}", id, updated.getName());
        return buildProjectResponseWithStats(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
        log.info("Deleted project id {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        List<Issue> issues = issueRepository.findByProjectId(id);
        long totalIssues = issues.size();

        long todoCount = issues.stream().filter(i -> i.getStatus() == IssueStatus.TODO).count();
        long inProgressCount = issues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
        long inReviewCount = issues.stream().filter(i -> i.getStatus() == IssueStatus.IN_REVIEW).count();
        long doneCount = issues.stream().filter(i -> i.getStatus() == IssueStatus.DONE).count();
        long closedCount = issues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count();

        double progress = totalIssues == 0 ? 0.0 :
                Math.round(((doneCount + closedCount) * 100.0 / totalIssues) * 10.0) / 10.0;

        int totalMembers = projectMemberRepository.findByProjectId(id).size();

        return ProjectStatsResponse.builder()
                .projectId(project.getId())
                .projectKey(project.getKey())
                .projectName(project.getName())
                .totalIssues(totalIssues)
                .todoIssues(todoCount)
                .inProgressIssues(inProgressCount)
                .inReviewIssues(inReviewCount)
                .doneIssues(doneCount)
                .closedIssues(closedCount)
                .progressPercentage(progress)
                .totalMembers(totalMembers)
                .build();
    }

    private ProjectResponse buildProjectResponseWithStats(Project project) {
        int memberCount = projectMemberRepository.findByProjectId(project.getId()).size();
        List<Issue> issues = issueRepository.findByProjectId(project.getId());
        long totalIssues = issues.size();
        long openIssues = issues.stream().filter(i -> i.getStatus() != IssueStatus.DONE && i.getStatus() != IssueStatus.CLOSED).count();
        long completed = issues.stream().filter(i -> i.getStatus() == IssueStatus.DONE || i.getStatus() == IssueStatus.CLOSED).count();

        double progress = totalIssues == 0 ? 0.0 :
                Math.round((completed * 100.0 / totalIssues) * 10.0) / 10.0;

        return projectMapper.toDto(project, memberCount, totalIssues, openIssues, progress);
    }
}

