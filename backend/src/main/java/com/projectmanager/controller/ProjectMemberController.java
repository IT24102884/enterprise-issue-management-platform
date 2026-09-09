package com.projectmanager.controller;

import com.projectmanager.dto.ProjectMemberRequest;
import com.projectmanager.dto.ProjectMemberResponse;
import com.projectmanager.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project Members", description = "Endpoints for project team member assignments and permissions")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Add member to project", description = "Assigns a user to a project with a specific role. Requires ADMIN or PROJECT_MANAGER role.")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberRequest request) {
        ProjectMemberResponse response = projectMemberService.addMember(projectId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get project members", description = "Returns list of all members assigned to a project")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Remove member from project", description = "Removes a user from the project membership. Requires ADMIN or PROJECT_MANAGER role.")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
