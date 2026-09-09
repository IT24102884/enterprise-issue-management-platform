package com.projectmanager.service;

import com.projectmanager.dto.ProjectAnalyticsResponse;
import com.projectmanager.dto.UserDashboardResponse;

public interface AnalyticsService {
    ProjectAnalyticsResponse getProjectAnalytics(Long projectId);
    UserDashboardResponse getUserDashboard(Long userId);
}
