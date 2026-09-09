-- V9: Create Issue Labels & Mapping Tables
CREATE TABLE issue_labels (
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(20) NOT NULL DEFAULT '#3b82f6',
    CONSTRAINT uq_project_label UNIQUE (project_id, name)
);

CREATE INDEX idx_labels_project ON issue_labels(project_id);

CREATE TABLE issue_label_mapping (
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    label_id BIGINT NOT NULL REFERENCES issue_labels(id) ON DELETE CASCADE,
    PRIMARY KEY (issue_id, label_id)
);

