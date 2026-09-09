package com.projectmanager.service.impl;

import com.projectmanager.dto.IssueCommentRequest;
import com.projectmanager.dto.IssueCommentResponse;
import com.projectmanager.entity.Issue;
import com.projectmanager.entity.IssueComment;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.ForbiddenException;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.repository.IssueCommentRepository;
import com.projectmanager.repository.IssueRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.AuditLogService;
import com.projectmanager.service.IssueCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

    private final IssueRepository issueRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final UserRepository userRepository;
    private final IssueMapper issueMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public IssueCommentResponse addComment(Long issueId, Long userId, IssueCommentRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        IssueComment comment = IssueComment.builder()
                .issue(issue)
                .user(user)
                .comment(request.getComment().trim())
                .build();

        IssueComment savedComment = issueCommentRepository.save(comment);

        auditLogService.logAction(
                user,
                "COMMENT_ADDED",
                "ISSUE",
                issue.getId(),
                "Added a comment to issue " + issue.getIssueKey()
        );

        return issueMapper.toCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueCommentResponse> getCommentsByIssue(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new ResourceNotFoundException("Issue not found with id: " + issueId);
        }

        return issueCommentRepository.findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(issueMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        IssueComment comment = issueCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException("You are not authorized to delete this comment");
        }

        issueCommentRepository.delete(comment);

        auditLogService.logAction(
                user,
                "COMMENT_DELETED",
                "ISSUE",
                comment.getIssue().getId(),
                "Deleted a comment on issue " + comment.getIssue().getIssueKey()
        );
    }
}
