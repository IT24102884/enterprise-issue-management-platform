package com.projectmanager.controller;

import com.projectmanager.dto.*;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.ProjectService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Endpoints for project management, workspaces, and analytics")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create a project", description = "Creates a new project and assigns creator as project manager. Requires ADMIN or PROJECT_MANAGER role.")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse response = projectService.createProject(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all projects", description = "Returns paginated list of all projects with summary statistics")
    public ResponseEntity<PagedResponse<ProjectResponse>> getAllProjects(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Returns detailed project information including member and issue counts")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get project by Key", description = "Returns detailed project information by unique key (e.g. EIM)")
    public ResponseEntity<ProjectResponse> getProjectByKey(@PathVariable String key) {
        return ResponseEntity.ok(projectService.getProjectByKey(key));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update a project", description = "Updates project details and status. Requires ADMIN or PROJECT_MANAGER role.")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project", description = "Deletes a project and all associated issues. Requires ADMIN role.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get project statistics", description = "Returns issue breakdown by status (TODO, IN_PROGRESS, IN_REVIEW, DONE) and progress percentage")
    public ResponseEntity<ProjectStatsResponse> getProjectStats(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectStats(id));
    }
}
