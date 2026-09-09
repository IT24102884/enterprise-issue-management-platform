-- ============================================================
-- V1: Create Users Table
-- ============================================================
-- This is a placeholder migration to allow the backend to start.
-- Full schema migrations will be added in Phase 2.
-- ============================================================

CREATE TABLE IF NOT EXISTS schema_info (
    id      SERIAL PRIMARY KEY,
    version VARCHAR(10) NOT NULL,
    note    TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO schema_info (version, note)
VALUES ('1.0', 'Phase 1 scaffold — full schema coming in Phase 2');

