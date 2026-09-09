package com.projectmanager.service;

import com.projectmanager.dto.ProjectMemberRequest;
import com.projectmanager.dto.ProjectMemberResponse;
import com.projectmanager.entity.Organization;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.ProjectMember;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.mapper.ProjectMemberMapper;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.ProjectMemberRepository;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.impl.ProjectMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @Spy
    private ProjectMemberMapper projectMemberMapper = new ProjectMemberMapper(new UserMapper());

    @InjectMocks
    private ProjectMemberServiceImpl projectMemberService;

    private Project project;
    private User user;
    private ProjectMember member;

    @BeforeEach
    void setUp() {
        Organization org = Organization.builder().id(1L).name("Acme").build();
        user = User.builder().id(2L).name("Bob Developer").email("bob@acme.com").role(Role.DEVELOPER).build();
        project = Project.builder().id(10L).name("Project A").key("PA").organization(org).build();
        member = ProjectMember.builder().id(100L).project(project).user(user).role(Role.DEVELOPER).joinedAt(OffsetDateTime.now()).build();
    }

    @Test
    @DisplayName("Should add new member to project successfully")
    void shouldAddMemberSuccessfully() {
        ProjectMemberRequest request = ProjectMemberRequest.builder()
                .userId(2L)
                .role(Role.DEVELOPER)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(member);

        ProjectMemberResponse response = projectMemberService.addMember(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(Role.DEVELOPER);
        assertThat(response.getUser().getEmail()).isEqualTo("bob@acme.com");
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Should reject adding member if user is already in project")
    void shouldRejectDuplicateMember() {
        ProjectMemberRequest request = ProjectMemberRequest.builder()
                .userId(2L)
                .role(Role.DEVELOPER)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> projectMemberService.addMember(10L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already a member");

        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should remove member from project")
    void shouldRemoveMemberSuccessfully() {
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(Optional.of(member));

        projectMemberService.removeMember(10L, 2L);

        verify(projectMemberRepository).delete(member);
    }
}
