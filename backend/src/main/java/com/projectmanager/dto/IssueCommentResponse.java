package com.projectmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCommentResponse {
    private Long id;
    private Long issueId;
    private UserDto user;
    private String comment;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
