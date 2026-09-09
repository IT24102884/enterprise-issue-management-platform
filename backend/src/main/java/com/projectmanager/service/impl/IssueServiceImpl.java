package com.projectmanager.service.impl;

import com.projectmanager.dto.CreateIssueRequest;
import com.projectmanager.dto.IssueResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.dto.UpdateIssueRequest;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.IssueLabel;
import com.projectmanager.entity.Milestone;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.repository.IssueLabelRepository;
import com.projectmanager.repository.IssueRepository;
import com.projectmanager.repository.MilestoneRepository;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.AuditLogService;
import com.projectmanager.service.IssueService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;
    private final IssueLabelRepository issueLabelRepository;
    private final IssueMapper issueMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public IssueResponse createIssue(Long projectId, Long reporterUserId, CreateIssueRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found with id: " + reporterUserId));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
        }

        Milestone milestone = null;
        if (request.getMilestoneId() != null) {
            milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + request.getMilestoneId()));
        }

        // Generate Jira-style issue key: e.g. PROJ-1
        String projectKey = project.getKey().trim().toUpperCase();
        Integer maxSeq = issueRepository.findMaxSequenceByPrefix(projectKey);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        String issueKey = projectKey + "-" + nextSeq;

        // Process labels
        Set<IssueLabel> labels = resolveLabels(project, request.getLabelNames());

        Issue issue = Issue.builder()
                .project(project)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .issueKey(issueKey)
                .type(request.getType())
                .status(IssueStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM)
                .reporter(reporter)
                .assignee(assignee)
                .milestone(milestone)
                .dueDate(request.getDueDate())
                .labels(labels)
                .build();

        Issue savedIssue = issueRepository.save(issue);

        auditLogService.logAction(
                reporter,
                "ISSUE_CREATED",
                "ISSUE",
                savedIssue.getId(),
                "Created issue " + issueKey + ": " + savedIssue.getTitle()
        );

        return issueMapper.toResponse(savedIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
        return issueMapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueByKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with key: " + issueKey));
        return issueMapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<IssueResponse> getProjectIssues(
            Long projectId,
            IssueStatus status,
            IssuePriority priority,
            IssueType type,
            Long assigneeId,
            String search,
            Pageable pageable
    ) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        Specification<Issue> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate keyMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("issueKey")), searchPattern);
                predicates.add(criteriaBuilder.or(titleMatch, descMatch, keyMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Issue> issuePage = issueRepository.findAll(spec, pageable);

        List<IssueResponse> content = issuePage.getContent().stream()
                .map(issueMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<IssueResponse>builder()
                .content(content)
                .page(issuePage.getNumber())
                .size(issuePage.getSize())
                .totalElements(issuePage.getTotalElements())
                .totalPages(issuePage.getTotalPages())
                .last(issuePage.isLast())
                .build();
    }

    @Override
    @Transactional
    public IssueResponse updateIssue(Long issueId, Long currentUserId, UpdateIssueRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            issue.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            issue.setType(request.getType());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            issue.setDueDate(request.getDueDate());
        }

        if (request.getStatus() != null && request.getStatus() != issue.getStatus()) {
            IssueStatus oldStatus = issue.getStatus();
            issue.setStatus(request.getStatus());
            auditLogService.logAction(
                    currentUser,
                    "ISSUE_STATUS_CHANGED",
                    "ISSUE",
                    issue.getId(),
                    "Status changed from " + oldStatus + " to " + request.getStatus()
            );
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            issue.setAssignee(assignee);
        }

        if (request.getMilestoneId() != null) {
            Milestone milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + request.getMilestoneId()));
            issue.setMilestone(milestone);
        }

        if (request.getLabelNames() != null) {
            Set<IssueLabel> labels = resolveLabels(issue.getProject(), request.getLabelNames());
            issue.setLabels(labels);
        }

        Issue updatedIssue = issueRepository.save(issue);

        auditLogService.logAction(
                currentUser,
                "ISSUE_UPDATED",
                "ISSUE",
                updatedIssue.getId(),
                "Updated details for issue " + updatedIssue.getIssueKey()
        );

        return issueMapper.toResponse(updatedIssue);
    }

    @Override
    @Transactional
    public IssueResponse updateIssueStatus(Long issueId, Long currentUserId, IssueStatus newStatus) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        IssueStatus oldStatus = issue.getStatus();
        if (oldStatus != newStatus) {
            issue.setStatus(newStatus);
            Issue updatedIssue = issueRepository.save(issue);

            auditLogService.logAction(
                    currentUser,
                    "ISSUE_STATUS_CHANGED",
                    "ISSUE",
                    updatedIssue.getId(),
                    "Status changed from " + oldStatus + " to " + newStatus
            );

            return issueMapper.toResponse(updatedIssue);
        }

        return issueMapper.toResponse(issue);
    }

    @Override
    @Transactional
    public void deleteIssue(Long issueId, Long currentUserId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        auditLogService.logAction(
                currentUser,
                "ISSUE_DELETED",
                "ISSUE",
                issue.getId(),
                "Deleted issue " + issue.getIssueKey() + " (" + issue.getTitle() + ")"
        );

        issueRepository.delete(issue);
    }

    private Set<IssueLabel> resolveLabels(Project project, Set<String> labelNames) {
        Set<IssueLabel> labels = new HashSet<>();
        if (labelNames == null || labelNames.isEmpty()) {
            return labels;
        }

        for (String rawName : labelNames) {
            if (rawName == null || rawName.trim().isEmpty()) {
                continue;
            }
            String name = rawName.trim();
            IssueLabel label = issueLabelRepository.findByProjectIdAndName(project.getId(), name)
                    .orElseGet(() -> issueLabelRepository.save(
                            IssueLabel.builder()
                                    .project(project)
                                    .name(name)
                                    .color("#3b82f6")
                                    .build()
                    ));
            labels.add(label);
        }
        return labels;
    }
}
