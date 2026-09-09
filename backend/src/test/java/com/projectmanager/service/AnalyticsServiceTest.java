package com.projectmanager.service;

import com.projectmanager.dto.ProjectAnalyticsResponse;
import com.projectmanager.dto.UserDashboardResponse;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.mapper.ProjectMapper;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.*;
import com.projectmanager.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @Spy
    private IssueMapper issueMapper = new IssueMapper(new UserMapper());

    @Spy
    private ProjectMapper projectMapper = new ProjectMapper(new UserMapper());

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private User testUser;
    private Project testProject;

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
    }

    @Test
    @DisplayName("getProjectAnalytics - Returns full aggregated metrics and progress %")
    void getProjectAnalytics_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(issueRepository.countByProjectId(1L)).thenReturn(10L);
        when(issueRepository.countByProjectIdAndStatusIn(eq(1L), any())).thenReturn(6L);
        when(projectMemberRepository.countByProjectId(1L)).thenReturn(4L);
        when(milestoneRepository.countByProjectId(1L)).thenReturn(2L);

        when(issueRepository.countByProjectIdAndStatus(eq(1L), any(IssueStatus.class))).thenReturn(2L);
        when(issueRepository.countByProjectIdAndPriority(eq(1L), any(IssuePriority.class))).thenReturn(2L);
        when(issueRepository.countByProjectIdAndType(eq(1L), any(IssueType.class))).thenReturn(2L);

        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(eq("PROJECT"), eq(1L), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        ProjectAnalyticsResponse response = analyticsService.getProjectAnalytics(1L);

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.getProjectKey()).isEqualTo("CORE");
        assertThat(response.getTotalIssues()).isEqualTo(10);
        assertThat(response.getCompletedIssues()).isEqualTo(6);
        assertThat(response.getOpenIssues()).isEqualTo(4);
        assertThat(response.getProgressPercentage()).isEqualTo(60.0);
        assertThat(response.getMemberCount()).isEqualTo(4);
        assertThat(response.getMilestoneCount()).isEqualTo(2);
        assertThat(response.getStatusBreakdown()).isNotEmpty();
        assertThat(response.getPriorityBreakdown()).isNotEmpty();
        assertThat(response.getTypeBreakdown()).isNotEmpty();
    }

    @Test
    @DisplayName("getUserDashboard - Returns user assigned tasks and personal activity")
    void getUserDashboard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(issueRepository.countByAssigneeId(1L)).thenReturn(5L);
        when(issueRepository.countByAssigneeIdAndStatusIn(eq(1L), any())).thenReturn(3L);
        when(issueRepository.findTop10ByAssigneeIdOrderByUpdatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(projectMemberRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(auditLogRepository.findTop10ByUserIdOrderByTimestampDesc(1L)).thenReturn(Collections.emptyList());

        UserDashboardResponse response = analyticsService.getUserDashboard(1L);

        assertThat(response).isNotNull();
        assertThat(response.getAssignedIssuesCount()).isEqualTo(5);
        assertThat(response.getCompletedIssuesCount()).isEqualTo(3);
        assertThat(response.getPendingIssuesCount()).isEqualTo(2);
    }
}
