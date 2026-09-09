package com.projectmanager.controller;

import com.projectmanager.dto.MilestoneRequest;
import com.projectmanager.dto.MilestoneResponse;
import com.projectmanager.entity.enums.MilestoneStatus;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/milestones")
@RequiredArgsConstructor
@Tag(name = "Project Milestones", description = "Endpoints for creating and listing milestones/sprints within a project")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectMilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create milestone in project", description = "Creates a new sprint or release milestone for the project")
    public ResponseEntity<MilestoneResponse> createMilestone(
            @PathVariable Long projectId,
            @Valid @RequestBody MilestoneRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MilestoneResponse response = milestoneService.createMilestone(projectId, currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get project milestones", description = "Returns all milestones for a project with completion progress metrics")
    public ResponseEntity<List<MilestoneResponse>> getProjectMilestones(
            @PathVariable Long projectId,
            @RequestParam(required = false) MilestoneStatus status) {
        return ResponseEntity.ok(milestoneService.getMilestonesByProject(projectId, status));
    }
}
