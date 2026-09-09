package com.projectmanager.service;

import com.projectmanager.dto.ProjectCreateRequest;
import com.projectmanager.dto.ProjectResponse;
import com.projectmanager.dto.ProjectStatsResponse;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.Organization;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import com.projectmanager.entity.enums.ProjectStatus;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.ProjectMapper;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.*;
import com.projectmanager.service.impl.ProjectServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private IssueRepository issueRepository;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @Spy
    private ProjectMapper projectMapper = new ProjectMapper(new UserMapper());

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Organization organization;
    private User creator;
    private Project project;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(1L)
                .name("Acme Corp")
                .createdAt(OffsetDateTime.now())
                .build();

        creator = User.builder()
                .id(1L)
                .name("Jane Manager")
                .email("jane@acme.com")
                .passwordHash("hashed")
                .role(Role.PROJECT_MANAGER)
                .createdAt(OffsetDateTime.now())
                .build();

        project = Project.builder()
                .id(10L)
                .organization(organization)
                .name("Enterprise Issue Manager")
                .key("EIM")
                .description("Issue manager project")
                .status(ProjectStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .createdBy(creator)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create project and automatically assign creator as member")
    void shouldCreateProjectSuccessfully() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("Enterprise Issue Manager")
                .key("EIM")
                .organizationId(1L)
                .description("Issue manager project")
                .build();

        when(projectRepository.existsByKey("EIM")).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.createProject(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo("EIM");
        assertThat(response.getName()).isEqualTo("Enterprise Issue Manager");
        verify(projectMemberRepository).save(any());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should reject project creation if project key already exists")
    void shouldRejectDuplicateProjectKey() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("Duplicate Project")
                .key("EIM")
                .organizationId(1L)
                .build();

        when(projectRepository.existsByKey("EIM")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void shouldThrowWhenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should calculate project statistics accurately")
    void shouldCalculateProjectStatsAccurately() {
        Issue todo = Issue.builder().id(1L).project(project).status(IssueStatus.TODO).priority(IssuePriority.MEDIUM).type(IssueType.TASK).title("T1").issueKey("EIM-1").reporter(creator).build();
        Issue inProg = Issue.builder().id(2L).project(project).status(IssueStatus.IN_PROGRESS).priority(IssuePriority.HIGH).type(IssueType.BUG).title("T2").issueKey("EIM-2").reporter(creator).build();
        Issue done = Issue.builder().id(3L).project(project).status(IssueStatus.DONE).priority(IssuePriority.LOW).type(IssueType.FEATURE).title("T3").issueKey("EIM-3").reporter(creator).build();
        Issue closed = Issue.builder().id(4L).project(project).status(IssueStatus.CLOSED).priority(IssuePriority.MEDIUM).type(IssueType.TASK).title("T4").issueKey("EIM-4").reporter(creator).build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(issueRepository.findByProjectId(10L)).thenReturn(List.of(todo, inProg, done, closed));
        when(projectMemberRepository.findByProjectId(10L)).thenReturn(Collections.emptyList());

        ProjectStatsResponse stats = projectService.getProjectStats(10L);

        assertThat(stats.getTotalIssues()).isEqualTo(4);
        assertThat(stats.getTodoIssues()).isEqualTo(1);
        assertThat(stats.getInProgressIssues()).isEqualTo(1);
        assertThat(stats.getDoneIssues()).isEqualTo(1);
        assertThat(stats.getClosedIssues()).isEqualTo(1);
        assertThat(stats.getProgressPercentage()).isEqualTo(50.0); // 2 of 4 completed = 50%
    }
}

