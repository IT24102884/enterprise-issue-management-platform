package com.projectmanager.dto;

import com.projectmanager.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {
    private Long id;
    private Long projectId;
    private UserDto user;
    private Role role;
    private OffsetDateTime joinedAt;
}

