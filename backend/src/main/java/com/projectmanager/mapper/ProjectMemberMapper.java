package com.projectmanager.mapper;

import com.projectmanager.dto.ProjectMemberResponse;
import com.projectmanager.entity.ProjectMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMemberMapper {

    private final UserMapper userMapper;

    public ProjectMemberResponse toDto(ProjectMember member) {
        if (member == null) {
            return null;
        }
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .user(userMapper.toDto(member.getUser()))
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

