package com.projectmanager.service;

import com.projectmanager.dto.ProjectMemberRequest;
import com.projectmanager.dto.ProjectMemberResponse;

import java.util.List;

public interface ProjectMemberService {
    ProjectMemberResponse addMember(Long projectId, ProjectMemberRequest request);
    List<ProjectMemberResponse> getProjectMembers(Long projectId);
    void removeMember(Long projectId, Long userId);
}

