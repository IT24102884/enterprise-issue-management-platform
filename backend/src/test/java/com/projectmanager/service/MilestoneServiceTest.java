package com.projectmanager.service;

import com.projectmanager.dto.MilestoneRequest;
import com.projectmanager.dto.MilestoneResponse;
import com.projectmanager.entity.Milestone;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.MilestoneStatus;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.mapper.MilestoneMapper;
import com.projectmanager.repository.IssueRepository;
import com.projectmanager.repository.MilestoneRepository;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.impl.MilestoneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private MilestoneMapper milestoneMapper = new MilestoneMapper();

    @InjectMocks
    private MilestoneServiceImpl milestoneService;

    private User testUser;
    private Project testProject;
    private Milestone testMilestone;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Alice Lead")
                .email("alice@acme.com")
                .role(Role.PROJECT_MANAGER)
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Core Platform")
                .key("CORE")
                .build();

        testMilestone = Milestone.builder()
                .id(1L)
                .project(testProject)
                .title("Sprint 1 - MVP")
                .description("First sprint deliverables")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .status(MilestoneStatus.OPEN)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createMilestone - Successfully creates milestone with 0% progress")
    void createMilestone_Success() {
        MilestoneRequest request = MilestoneRequest.builder()
                .title("Sprint 1 - MVP")
                .description("First sprint deliverables")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(testMilestone);

        MilestoneResponse response = milestoneService.createMilestone(1L, 1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Sprint 1 - MVP");
        assertThat(response.getStatus()).isEqualTo(MilestoneStatus.OPEN);
        assertThat(response.getTotalIssues()).isEqualTo(0);
        assertThat(response.getProgressPercentage()).isEqualTo(0.0);

        verify(auditLogService).logAction(eq(testUser), eq("MILESTONE_CREATED"), eq("MILESTONE"), eq(1L), any());
    }

    @Test
    @DisplayName("getMilestoneById - Correctly calculates 50% progress for 2 total and 1 done issue")
    void getMilestoneById_ProgressCalculation() {
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(issueRepository.countByMilestoneId(1L)).thenReturn(2L);
        when(issueRepository.countByMilestoneIdAndStatusIn(eq(1L), any())).thenReturn(1L);

        MilestoneResponse response = milestoneService.getMilestoneById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getTotalIssues()).isEqualTo(2);
        assertThat(response.getClosedIssues()).isEqualTo(1);
        assertThat(response.getOpenIssues()).isEqualTo(1);
        assertThat(response.getProgressPercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("closeMilestone - Marks milestone as CLOSED and logs audit event")
    void closeMilestone_Success() {
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(testMilestone);

        MilestoneResponse response = milestoneService.closeMilestone(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(testMilestone.getStatus()).isEqualTo(MilestoneStatus.CLOSED);
        verify(auditLogService).logAction(eq(testUser), eq("MILESTONE_CLOSED"), eq("MILESTONE"), eq(1L), any());
    }

    @Test
    @DisplayName("deleteMilestone - Deletes milestone entity")
    void deleteMilestone_Success() {
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        milestoneService.deleteMilestone(1L, 1L);

        verify(milestoneRepository).delete(testMilestone);
        verify(auditLogService).logAction(eq(testUser), eq("MILESTONE_DELETED"), eq("MILESTONE"), eq(1L), any());
    }
}
