package com.projectmanager.service;

import com.projectmanager.dto.CreateIssueRequest;
import com.projectmanager.dto.IssueResponse;
import com.projectmanager.dto.UpdateIssueRequest;
import com.projectmanager.dto.UpdateIssueStatusRequest;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.Organization;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.*;
import com.projectmanager.service.impl.IssueServiceImpl;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private IssueLabelRepository issueLabelRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @Spy
    private IssueMapper issueMapper = new IssueMapper(new UserMapper());

    @InjectMocks
    private IssueServiceImpl issueService;

    private User testUser;
    private Project testProject;
    private Issue testIssue;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Jane Developer")
                .email("jane@example.com")
                .passwordHash("hash")
                .role(Role.DEVELOPER)
                .createdAt(OffsetDateTime.now())
                .build();

        Organization org = Organization.builder().id(1L).name("Acme Corp").build();

        testProject = Project.builder()
                .id(1L)
                .organization(org)
                .name("Core Platform")
                .key("CORE")
                .createdBy(testUser)
                .createdAt(OffsetDateTime.now())
                .build();

        testIssue = Issue.builder()
                .id(1L)
                .project(testProject)
                .title("Fix NPE on auth login")
                .description("Occurs when payload is empty")
                .issueKey("CORE-1")
                .type(IssueType.BUG)
                .status(IssueStatus.TODO)
                .priority(IssuePriority.HIGH)
                .reporter(testUser)
                .labels(new HashSet<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createIssue - Generates sequential key CORE-1 when no prior issues exist")
    void createIssue_FirstKey_Success() {
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Fix NPE on auth login")
                .description("Occurs when payload is empty")
                .type(IssueType.BUG)
                .priority(IssuePriority.HIGH)
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(issueRepository.findMaxSequenceByPrefix("CORE")).thenReturn(null);
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        IssueResponse response = issueService.createIssue(1L, 1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getIssueKey()).isEqualTo("CORE-1");
        assertThat(response.getTitle()).isEqualTo("Fix NPE on auth login");
        assertThat(response.getType()).isEqualTo(IssueType.BUG);
        assertThat(response.getStatus()).isEqualTo(IssueStatus.TODO);

        verify(auditLogService).logAction(eq(testUser), eq("ISSUE_CREATED"), eq("ISSUE"), eq(1L), any());
    }

    @Test
    @DisplayName("createIssue - Generates sequential key CORE-6 when max sequence is 5")
    void createIssue_NextSequence_Success() {
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Add export CSV button")
                .type(IssueType.FEATURE)
                .build();

        Issue nextIssue = Issue.builder()
                .id(2L)
                .project(testProject)
                .title("Add export CSV button")
                .issueKey("CORE-6")
                .type(IssueType.FEATURE)
                .status(IssueStatus.TODO)
                .priority(IssuePriority.MEDIUM)
                .reporter(testUser)
                .labels(new HashSet<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(issueRepository.findMaxSequenceByPrefix("CORE")).thenReturn(5);
        when(issueRepository.save(any(Issue.class))).thenReturn(nextIssue);

        IssueResponse response = issueService.createIssue(1L, 1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getIssueKey()).isEqualTo("CORE-6");
    }

    @Test
    @DisplayName("createIssue - Throws ResourceNotFoundException if project does not exist")
    void createIssue_ProjectNotFound_ThrowsException() {
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Test issue")
                .type(IssueType.TASK)
                .build();

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.createIssue(999L, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found with id: 999");
    }

    @Test
    @DisplayName("getIssueById - Returns issue details")
    void getIssueById_Success() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));

        IssueResponse response = issueService.getIssueById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getIssueKey()).isEqualTo("CORE-1");
    }

    @Test
    @DisplayName("getIssueByKey - Returns issue details by key")
    void getIssueByKey_Success() {
        when(issueRepository.findByIssueKey("CORE-1")).thenReturn(Optional.of(testIssue));

        IssueResponse response = issueService.getIssueByKey("core-1");

        assertThat(response).isNotNull();
        assertThat(response.getIssueKey()).isEqualTo("CORE-1");
    }

    @Test
    @DisplayName("updateIssueStatus - Updates status and records audit log")
    void updateIssueStatus_Success() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(issueRepository.save(any(Issue.class))).thenReturn(testIssue);

        IssueResponse response = issueService.updateIssueStatus(1L, 1L, IssueStatus.IN_PROGRESS);

        assertThat(response).isNotNull();
        verify(auditLogService).logAction(eq(testUser), eq("ISSUE_STATUS_CHANGED"), eq("ISSUE"), eq(1L), any());
    }

    @Test
    @DisplayName("deleteIssue - Deletes issue and records audit log")
    void deleteIssue_Success() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        issueService.deleteIssue(1L, 1L);

        verify(issueRepository).delete(testIssue);
        verify(auditLogService).logAction(eq(testUser), eq("ISSUE_DELETED"), eq("ISSUE"), eq(1L), any());
    }
}
