package com.projectmanager.service;

import com.projectmanager.dto.IssueCommentRequest;
import com.projectmanager.dto.IssueCommentResponse;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.IssueComment;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.ForbiddenException;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.IssueCommentRepository;
import com.projectmanager.repository.IssueRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.impl.IssueCommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueCommentServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueCommentRepository issueCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @Spy
    private IssueMapper issueMapper = new IssueMapper(new UserMapper());

    @InjectMocks
    private IssueCommentServiceImpl issueCommentService;

    private User testUser;
    private User otherUser;
    private Issue testIssue;
    private IssueComment testComment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .role(Role.DEVELOPER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .name("Bob")
                .email("bob@example.com")
                .role(Role.DEVELOPER)
                .build();

        testIssue = Issue.builder()
                .id(1L)
                .issueKey("CORE-1")
                .title("Fix bug")
                .build();

        testComment = IssueComment.builder()
                .id(1L)
                .issue(testIssue)
                .user(testUser)
                .comment("Investigating this bug")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("addComment - Saves comment and creates audit log")
    void addComment_Success() {
        IssueCommentRequest request = new IssueCommentRequest("Investigating this bug");

        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(issueCommentRepository.save(any(IssueComment.class))).thenReturn(testComment);

        IssueCommentResponse response = issueCommentService.addComment(1L, 1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getComment()).isEqualTo("Investigating this bug");
        verify(auditLogService).logAction(eq(testUser), eq("COMMENT_ADDED"), eq("ISSUE"), eq(1L), any());
    }

    @Test
    @DisplayName("getCommentsByIssue - Returns list of comments")
    void getCommentsByIssue_Success() {
        when(issueRepository.existsById(1L)).thenReturn(true);
        when(issueCommentRepository.findByIssueIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(testComment));

        List<IssueCommentResponse> comments = issueCommentService.getCommentsByIssue(1L);

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getComment()).isEqualTo("Investigating this bug");
    }

    @Test
    @DisplayName("deleteComment - Author can delete own comment")
    void deleteComment_Author_Success() {
        when(issueCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        issueCommentService.deleteComment(1L, 1L);

        verify(issueCommentRepository).delete(testComment);
        verify(auditLogService).logAction(eq(testUser), eq("COMMENT_DELETED"), eq("ISSUE"), eq(1L), any());
    }

    @Test
    @DisplayName("deleteComment - Non-author non-admin gets ForbiddenException")
    void deleteComment_Unauthorized_ThrowsException() {
        when(issueCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> issueCommentService.deleteComment(1L, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized");
    }
}
