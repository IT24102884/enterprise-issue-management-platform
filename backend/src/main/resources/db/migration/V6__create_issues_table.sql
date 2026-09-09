-- V6: Create Issues Table
CREATE TABLE issues (
    id           BIGSERIAL PRIMARY KEY,
    project_id   BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    issue_key    VARCHAR(50)  NOT NULL UNIQUE,
    type         VARCHAR(30)  NOT NULL,
    status       VARCHAR(30)  NOT NULL DEFAULT 'TODO',
    priority     VARCHAR(30)  NOT NULL DEFAULT 'MEDIUM',
    reporter_id  BIGINT       NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    assignee_id  BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    milestone_id BIGINT       REFERENCES milestones(id) ON DELETE SET NULL,
    due_date     DATE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_issues_project ON issues(project_id);
CREATE INDEX idx_issues_key ON issues(issue_key);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_priority ON issues(priority);
CREATE INDEX idx_issues_type ON issues(type);
CREATE INDEX idx_issues_assignee ON issues(assignee_id);
CREATE INDEX idx_issues_reporter ON issues(reporter_id);
CREATE INDEX idx_issues_milestone ON issues(milestone_id);

