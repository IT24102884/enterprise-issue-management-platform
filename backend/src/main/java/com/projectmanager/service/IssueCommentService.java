package com.projectmanager.service;

import com.projectmanager.dto.IssueCommentRequest;
import com.projectmanager.dto.IssueCommentResponse;

import java.util.List;

public interface IssueCommentService {
    IssueCommentResponse addComment(Long issueId, Long userId, IssueCommentRequest request);
    List<IssueCommentResponse> getCommentsByIssue(Long issueId);
    void deleteComment(Long commentId, Long userId);
}
