package com.projectmanager.controller;

import com.projectmanager.dto.ProjectAnalyticsResponse;
import com.projectmanager.dto.UserDashboardResponse;
import com.projectmanager.security.UserPrincipal;
import com.projectmanager.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboard", description = "Endpoints for project analytics, Kanban metrics, and user overview dashboard")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/projects/{projectId}/analytics")
    @Operation(summary = "Get project analytics", description = "Returns aggregated breakdown of issues by status, priority, type, milestone count, and project progress %")
    public ResponseEntity<ProjectAnalyticsResponse> getProjectAnalytics(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.getProjectAnalytics(projectId));
    }

    @GetMapping("/dashboard/me")
    @Operation(summary = "Get personal dashboard", description = "Returns current user's assigned issues, pending tasks, project list, and personal activity log")
    public ResponseEntity<UserDashboardResponse> getUserDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(analyticsService.getUserDashboard(currentUser.getId()));
    }
}
