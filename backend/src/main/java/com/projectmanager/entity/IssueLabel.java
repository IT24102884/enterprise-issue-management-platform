package com.projectmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issue_labels", uniqueConstraints = {
    @UniqueConstraint(name = "uq_project_label", columnNames = {"project_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String color = "#3b82f6";
}
