package com.projectmanager.controller;

import com.projectmanager.dto.MilestoneRequest;
import com.projectmanager.dto.MilestoneResponse;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
@Tag(name = "Milestones", description = "Endpoints for individual milestone updates, progress tracking, and closing sprints")
@SecurityRequirement(name = "Bearer Authentication")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @GetMapping("/{id}")
    @Operation(summary = "Get milestone by ID", description = "Returns milestone details with total/open/closed issue counts and progress %")
    public ResponseEntity<MilestoneResponse> getMilestoneById(@PathVariable Long id) {
        return ResponseEntity.ok(milestoneService.getMilestoneById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update milestone", description = "Updates title, description, or due dates of a milestone")
    public ResponseEntity<MilestoneResponse> updateMilestone(
            @PathVariable Long id,
            @Valid @RequestBody MilestoneRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(milestoneService.updateMilestone(id, currentUser.getId(), request));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Close milestone", description = "Marks milestone/sprint as CLOSED")
    public ResponseEntity<MilestoneResponse> closeMilestone(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(milestoneService.closeMilestone(id, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete milestone", description = "Deletes a milestone")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        milestoneService.deleteMilestone(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
