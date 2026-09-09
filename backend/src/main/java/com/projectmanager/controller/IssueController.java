package com.projectmanager.controller;

import com.projectmanager.dto.*;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.AuditLogService;
import com.projectmanager.service.IssueCommentService;
import com.projectmanager.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Endpoints for individual issue operations, comments, and audit trails")
@SecurityRequirement(name = "Bearer Authentication")
public class IssueController {

    private final IssueService issueService;
    private final IssueCommentService issueCommentService;
    private final AuditLogService auditLogService;

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID", description = "Returns detailed issue information by numeric ID")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @GetMapping("/key/{issueKey}")
    @Operation(summary = "Get issue by Key", description = "Returns detailed issue information by unique issue key (e.g., EIM-1)")
    public ResponseEntity<IssueResponse> getIssueByKey(@PathVariable String issueKey) {
        return ResponseEntity.ok(issueService.getIssueByKey(issueKey));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update issue", description = "Updates issue details including title, description, priority, assignee, and labels")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(issueService.updateIssue(id, currentUser.getId(), request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update issue status", description = "Transitions an issue to a new status (e.g. TODO -> IN_PROGRESS -> DONE)")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueStatusRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(issueService.updateIssueStatus(id, currentUser.getId(), request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete issue", description = "Permanently deletes an issue")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        issueService.deleteIssue(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to issue", description = "Appends a new comment to the issue")
    public ResponseEntity<IssueCommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody IssueCommentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        IssueCommentResponse response = issueCommentService.addComment(id, currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments for issue", description = "Returns list of comments on an issue ordered chronologically")
    public ResponseEntity<List<IssueCommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(issueCommentService.getCommentsByIssue(id));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete comment", description = "Deletes a comment. Only the author or an admin can delete.")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        issueCommentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "Get issue audit logs", description = "Returns history of all events/changes performed on the issue")
    public ResponseEntity<PagedResponse<AuditLogResponse>> getIssueAuditLogs(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getLogsForEntity("ISSUE", id, pageable));
    }
}
