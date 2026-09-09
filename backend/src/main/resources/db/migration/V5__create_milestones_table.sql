-- V5: Create Milestones Table
CREATE TABLE milestones (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    start_date  DATE,
    due_date    DATE,
    status      VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_milestones_project ON milestones(project_id);

