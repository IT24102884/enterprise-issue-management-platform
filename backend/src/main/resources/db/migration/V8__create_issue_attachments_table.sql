-- V8: Create Issue Attachments Table
CREATE TABLE issue_attachments (
    id           BIGSERIAL PRIMARY KEY,
    issue_id     BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    filename     VARCHAR(255) NOT NULL,
    file_url     VARCHAR(500) NOT NULL,
    file_size    BIGINT,
    content_type VARCHAR(100),
    uploaded_by  BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_attachments_issue ON issue_attachments(issue_id);

