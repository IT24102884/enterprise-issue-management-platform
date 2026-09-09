-- V3: Create Projects Table
CREATE TABLE projects (
    id              BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(150) NOT NULL,
    key             VARCHAR(10)  NOT NULL UNIQUE,
    description     TEXT,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE,
    end_date        DATE,
    created_by      BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_projects_org ON projects(organization_id);
CREATE INDEX idx_projects_key ON projects(key);
CREATE INDEX idx_projects_status ON projects(status);

