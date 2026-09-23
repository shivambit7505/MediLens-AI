-- =============================================================================
-- MediLens AI - PostgreSQL & pgvector Database Initialization Script
-- Executed on container bootstrap via /docker-entrypoint-initdb.d/
-- =============================================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "vector";

-- Set client timezone to UTC
SET timezone = 'UTC';

-- Schema comment
COMMENT ON DATABASE medilens_db IS 'MediLens AI clinical data repository with pgvector support';
