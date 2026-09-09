package com.projectmanager.service.impl;

import com.projectmanager.dto.MilestoneRequest;
import com.projectmanager.dto.MilestoneResponse;
import com.projectmanager.entity.Milestone;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.MilestoneStatus;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.MilestoneMapper;
import com.projectmanager.repository.IssueRepository;
import com.projectmanager.repository.MilestoneRepository;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.AuditLogService;
import com.projectmanager.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private static final Set<IssueStatus> COMPLETED_STATUSES = Set.of(IssueStatus.DONE, IssueStatus.CLOSED);

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final MilestoneMapper milestoneMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public MilestoneResponse createMilestone(Long projectId, Long currentUserId, MilestoneRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Milestone milestone = Milestone.builder()
                .project(project)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .status(MilestoneStatus.OPEN)
                .build();

        Milestone savedMilestone = milestoneRepository.save(milestone);

        auditLogService.logAction(
                currentUser,
                "MILESTONE_CREATED",
                "MILESTONE",
                savedMilestone.getId(),
                "Created milestone '" + savedMilestone.getTitle() + "' in project " + project.getKey()
        );

        return milestoneMapper.toResponse(savedMilestone, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestonesByProject(Long projectId, MilestoneStatus status) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<Milestone> milestones;
        if (status != null) {
            milestones = milestoneRepository.findByProjectIdAndStatusOrderByDueDateAsc(projectId, status);
        } else {
            milestones = milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId);
        }

        return milestones.stream()
                .map(this::mapMilestoneWithStats)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponse getMilestoneById(Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        return mapMilestoneWithStats(milestone);
    }

    @Override
    @Transactional
    public MilestoneResponse updateMilestone(Long milestoneId, Long currentUserId, MilestoneRequest request) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(request.getDescription());
        milestone.setStartDate(request.getStartDate());
        milestone.setDueDate(request.getDueDate());

        Milestone updated = milestoneRepository.save(milestone);

        auditLogService.logAction(
                currentUser,
                "MILESTONE_UPDATED",
                "MILESTONE",
                updated.getId(),
                "Updated details for milestone '" + updated.getTitle() + "'"
        );

        return mapMilestoneWithStats(updated);
    }

    @Override
    @Transactional
    public MilestoneResponse closeMilestone(Long milestoneId, Long currentUserId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        milestone.setStatus(MilestoneStatus.CLOSED);
        Milestone closed = milestoneRepository.save(milestone);

        auditLogService.logAction(
                currentUser,
                "MILESTONE_CLOSED",
                "MILESTONE",
                closed.getId(),
                "Closed milestone '" + closed.getTitle() + "'"
        );

        return mapMilestoneWithStats(closed);
    }

    @Override
    @Transactional
    public void deleteMilestone(Long milestoneId, Long currentUserId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        auditLogService.logAction(
                currentUser,
                "MILESTONE_DELETED",
                "MILESTONE",
                milestone.getId(),
                "Deleted milestone '" + milestone.getTitle() + "'"
        );

        milestoneRepository.delete(milestone);
    }

    private MilestoneResponse mapMilestoneWithStats(Milestone milestone) {
        long totalIssues = issueRepository.countByMilestoneId(milestone.getId());
        long closedIssues = issueRepository.countByMilestoneIdAndStatusIn(milestone.getId(), COMPLETED_STATUSES);
        return milestoneMapper.toResponse(milestone, totalIssues, closedIssues);
    }
}
