package com.projectmanager.controller;

import com.projectmanager.dto.CreateIssueRequest;
import com.projectmanager.dto.IssueResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.entity.enums.IssuePriority;
import com.projectmanager.entity.enums.IssueStatus;
import com.projectmanager.entity.enums.IssueType;
import com.projectmanager.security.UserPrincipal;
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

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
@RequiredArgsConstructor
@Tag(name = "Project Issues", description = "Endpoints for creating and listing issues within a project")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectIssueController {

    private final IssueService issueService;

    @PostMapping
    @Operation(summary = "Create an issue in project", description = "Creates a new issue/bug/task/story under the specified project with auto-generated issue key (e.g., PROJ-1)")
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        IssueResponse response = issueService.createIssue(projectId, currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get issues by project", description = "Returns a paginated list of issues for a project with optional filters for status, priority, type, assignee, and search")
    public ResponseEntity<PagedResponse<IssueResponse>> getProjectIssues(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) IssueType type,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<IssueResponse> response = issueService.getProjectIssues(
                projectId, status, priority, type, assigneeId, search, pageable
        );
        return ResponseEntity.ok(response);
    }
}
