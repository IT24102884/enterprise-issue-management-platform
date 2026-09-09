package com.projectmanager.service;

import com.projectmanager.dto.MilestoneRequest;
import com.projectmanager.dto.MilestoneResponse;
import com.projectmanager.entity.enums.MilestoneStatus;

import java.util.List;

public interface MilestoneService {
    MilestoneResponse createMilestone(Long projectId, Long currentUserId, MilestoneRequest request);
    List<MilestoneResponse> getMilestonesByProject(Long projectId, MilestoneStatus status);
    MilestoneResponse getMilestoneById(Long milestoneId);
    MilestoneResponse updateMilestone(Long milestoneId, Long currentUserId, MilestoneRequest request);
    MilestoneResponse closeMilestone(Long milestoneId, Long currentUserId);
    void deleteMilestone(Long milestoneId, Long currentUserId);
}
