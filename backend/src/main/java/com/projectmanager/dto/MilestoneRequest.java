package com.projectmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneRequest {

    @NotBlank(message = "Milestone title is required")
    @Size(max = 150, message = "Milestone title must not exceed 150 characters")
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate dueDate;
}
