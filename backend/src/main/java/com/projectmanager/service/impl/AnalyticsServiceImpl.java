package com.projectmanager.service.impl;

import com.projectmanager.dto.*;
import com.projectmanager.entity.AuditLog;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.ProjectMember;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.mapper.ProjectMapper;
import com.projectmanager.repository.*;
import com.projectmanager.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Set<IssueStatus> COMPLETED_STATUSES = Set.of(IssueStatus.DONE, IssueStatus.CLOSED);

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final IssueMapper issueMapper;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse getProjectAnalytics(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        long totalIssues = issueRepository.countByProjectId(projectId);
        long completedIssues = issueRepository.countByProjectIdAndStatusIn(projectId, COMPLETED_STATUSES);
        long openIssues = Math.max(0, totalIssues - completedIssues);
        double progressPercentage = totalIssues > 0
                ? Math.round(((double) completedIssues / totalIssues) * 100.0 * 10.0) / 10.0
                : 0.0;

        int memberCount = (int) projectMemberRepository.countByProjectId(projectId);
        int milestoneCount = (int) milestoneRepository.countByProjectId(projectId);

        // Status breakdown
        Map<IssueStatus, Long> statusBreakdown = new EnumMap<>(IssueStatus.class);
        for (IssueStatus status : IssueStatus.values()) {
            statusBreakdown.put(status, issueRepository.countByProjectIdAndStatus(projectId, status));
        }

        // Priority breakdown
        Map<IssuePriority, Long> priorityBreakdown = new EnumMap<>(IssuePriority.class);
        for (IssuePriority priority : IssuePriority.values()) {
            priorityBreakdown.put(priority, issueRepository.countByProjectIdAndPriority(projectId, priority));
        }

        // Type breakdown
        Map<IssueType, Long> typeBreakdown = new EnumMap<>(IssueType.class);
        for (IssueType type : IssueType.values()) {
            typeBreakdown.put(type, issueRepository.countByProjectIdAndType(projectId, type));
        }

        // Recent activity
        List<AuditLogResponse> recentActivity = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByTimestampDesc("PROJECT", projectId, PageRequest.of(0, 10))
                .getContent()
                .stream()
                .map(issueMapper::toAuditLogResponse)
                .collect(Collectors.toList());

        return ProjectAnalyticsResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .projectKey(project.getKey())
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .completedIssues(completedIssues)
                .progressPercentage(progressPercentage)
                .memberCount(memberCount)
                .milestoneCount(milestoneCount)
                .statusBreakdown(statusBreakdown)
                .priorityBreakdown(priorityBreakdown)
                .typeBreakdown(typeBreakdown)
                .recentActivity(recentActivity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long assignedIssuesCount = issueRepository.countByAssigneeId(userId);
        long completedIssuesCount = issueRepository.countByAssigneeIdAndStatusIn(userId, COMPLETED_STATUSES);
        long pendingIssuesCount = Math.max(0, assignedIssuesCount - completedIssuesCount);

        List<IssueResponse> recentAssignedIssues = issueRepository.findTop10ByAssigneeIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        List<ProjectResponse> userProjects = memberships.stream()
                .map(pm -> {
                    Project p = pm.getProject();
                    int members = (int) projectMemberRepository.countByProjectId(p.getId());
                    long total = issueRepository.countByProjectId(p.getId());
                    long open = total - issueRepository.countByProjectIdAndStatusIn(p.getId(), COMPLETED_STATUSES);
                    return projectMapper.toResponse(p, members, total, Math.max(0, open));
                })
                .collect(Collectors.toList());

        List<AuditLogResponse> recentActivity = auditLogRepository.findTop10ByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(issueMapper::toAuditLogResponse)
                .collect(Collectors.toList());

        return UserDashboardResponse.builder()
                .assignedIssuesCount(assignedIssuesCount)
                .completedIssuesCount(completedIssuesCount)
                .pendingIssuesCount(pendingIssuesCount)
                .recentAssignedIssues(recentAssignedIssues)
                .userProjects(userProjects)
                .recentActivity(recentActivity)
                .build();
    }
}
