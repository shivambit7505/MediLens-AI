-- =============================================================================
-- MediLens AI - Complete Production Relational & Vector Database Schema
-- Standard: PostgreSQL 16+ with pgvector
-- Normalized Entities: 16 Core Tables
-- =============================================================================

-- Ensure extensions are loaded
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "vector";

-- -----------------------------------------------------------------------------
-- 1. USERS TABLE
-- Stores authenticated application users (Patients, Clinicians, Administrators)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_PATIENT' CHECK (role IN ('ROLE_PATIENT', 'ROLE_CLINICIAN', 'ROLE_ADMIN')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- -----------------------------------------------------------------------------
-- 2. REPORTS TABLE
-- Tracks uploaded medical report documents and processing state machine
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    mime_type VARCHAR(100) NOT NULL CHECK (mime_type IN ('application/pdf', 'image/png', 'image/jpeg', 'image/jpg')),
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes > 0 AND file_size_bytes <= 26214400), -- 25MB limit
    file_hash_sha256 VARCHAR(64) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED' CHECK (status IN (
        'UPLOADED',
        'VALIDATING',
        'PREPROCESSING',
        'OCR_PROCESSING',
        'EXTRACTING',
        'NORMALIZING',
        'VALIDATING_VALUES',
        'COMPLETED',
        'FAILED'
    )),
    failure_reason TEXT,
    page_count INTEGER DEFAULT 0,
    processing_started_at TIMESTAMPTZ,
    processing_completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reports_user_id ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports(created_at DESC);

-- -----------------------------------------------------------------------------
-- 3. REPORT_PAGES TABLE
-- Individual pages extracted from multi-page PDFs or image documents
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS report_pages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    page_number INTEGER NOT NULL CHECK (page_number >= 1),
    image_storage_path VARCHAR(1024) NOT NULL,
    ocr_raw_text TEXT,
    ocr_confidence_score NUMERIC(5, 4) CHECK (ocr_confidence_score >= 0.0000 AND ocr_confidence_score <= 1.0000),
    ocr_engine_used VARCHAR(50) DEFAULT 'paddleocr',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_report_page UNIQUE (report_id, page_number)
);

CREATE INDEX IF NOT EXISTS idx_report_pages_report_id ON report_pages(report_id);

-- -----------------------------------------------------------------------------
-- 4. BIOMARKERS TABLE
-- Canonical biomarker master catalog (LOINC, SNOMED-CT mapped)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biomarkers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    canonical_name VARCHAR(200) NOT NULL UNIQUE,
    code_loinc VARCHAR(50),
    code_snomed VARCHAR(50),
    category VARCHAR(100) NOT NULL, -- e.g., Complete Blood Count, Metabolic Panel, Lipid Panel
    standard_unit VARCHAR(50) NOT NULL, -- Canonical unit (e.g., g/dL, mg/dL, mmol/L)
    description TEXT,
    clinical_significance TEXT,
    aliases_json JSONB NOT NULL DEFAULT '[]'::jsonb, -- e.g., ["Hb", "HGB", "Haemoglobin"]
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_biomarkers_canonical_name ON biomarkers(canonical_name);
CREATE INDEX IF NOT EXISTS idx_biomarkers_category ON biomarkers(category);
CREATE INDEX IF NOT EXISTS idx_biomarkers_aliases ON biomarkers USING gin(aliases_json);

-- -----------------------------------------------------------------------------
-- 5. REFERENCE_RANGES TABLE
-- Deterministic, demographic-stratified clinical reference intervals
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reference_ranges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    biomarker_id UUID NOT NULL REFERENCES biomarkers(id) ON DELETE CASCADE,
    gender_applicable VARCHAR(20) NOT NULL DEFAULT 'ALL' CHECK (gender_applicable IN ('ALL', 'MALE', 'FEMALE')),
    age_min_years NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    age_max_years NUMERIC(5, 2) NOT NULL DEFAULT 150.0,
    unit VARCHAR(50) NOT NULL,
    low_value NUMERIC(12, 4) NOT NULL,
    high_value NUMERIC(12, 4) NOT NULL,
    critical_low NUMERIC(12, 4),
    critical_high NUMERIC(12, 4),
    source_citation VARCHAR(255) NOT NULL, -- e.g., "Mayo Clinic Laboratories 2024", "WHO 2023"
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_range_logic CHECK (low_value <= high_value),
    CONSTRAINT chk_critical_range CHECK (
        (critical_low IS NULL OR critical_low <= low_value) AND
        (critical_high IS NULL OR critical_high >= high_value)
    )
);

CREATE INDEX IF NOT EXISTS idx_ref_ranges_biomarker_id ON reference_ranges(biomarker_id);
CREATE INDEX IF NOT EXISTS idx_ref_ranges_lookup ON reference_ranges(biomarker_id, gender_applicable, age_min_years, age_max_years);

-- -----------------------------------------------------------------------------
-- 6. MEASUREMENTS TABLE
-- Structured longitudinal health measurements extracted from reports
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS measurements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    report_page_id UUID REFERENCES report_pages(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    biomarker_id UUID NOT NULL REFERENCES biomarkers(id) ON DELETE RESTRICT,
    extracted_name VARCHAR(255) NOT NULL,
    observed_value_raw VARCHAR(100) NOT NULL,
    observed_value_numeric NUMERIC(14, 4),
    extracted_unit VARCHAR(50),
    normalized_value_numeric NUMERIC(14, 4) NOT NULL,
    normalized_unit VARCHAR(50) NOT NULL,
    extracted_reference_text VARCHAR(255),
    matched_reference_range_id UUID REFERENCES reference_ranges(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL', 'UNKNOWN')),
    confidence NUMERIC(5, 4) NOT NULL CHECK (confidence >= 0.0000 AND confidence <= 1.0000),
    page_number INTEGER NOT NULL DEFAULT 1,
    source_text_snippet TEXT NOT NULL,
    bounding_box_json JSONB, -- Coordinates: {x, y, width, height}
    measurement_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_measurements_user_id ON measurements(user_id);
CREATE INDEX IF NOT EXISTS idx_measurements_report_id ON measurements(report_id);
CREATE INDEX IF NOT EXISTS idx_measurements_biomarker_id ON measurements(biomarker_id);
CREATE INDEX IF NOT EXISTS idx_measurements_status ON measurements(status);
CREATE INDEX IF NOT EXISTS idx_measurements_trends ON measurements(user_id, biomarker_id, measurement_date ASC);

-- -----------------------------------------------------------------------------
-- 7. MEDICAL_SOURCES TABLE
-- Curated, authoritative medical knowledge corpora metadata for RAG
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medical_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    organization VARCHAR(255) NOT NULL, -- e.g., "World Health Organization", "CDC", "NIH", "NHS"
    url_or_reference VARCHAR(1024) NOT NULL,
    publication_date DATE,
    version VARCHAR(50),
    topic VARCHAR(255) NOT NULL,
    source_type VARCHAR(50) NOT NULL CHECK (source_type IN (
        'CLINICAL_GUIDELINE',
        'REFERENCE_MANUAL',
        'PEER_REVIEWED_RESEARCH',
        'GOVERNMENT_HEALTH_BULLETIN'
    )),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_medical_sources_org ON medical_sources(organization);
CREATE INDEX IF NOT EXISTS idx_medical_sources_topic ON medical_sources(topic);

-- -----------------------------------------------------------------------------
-- 8. KNOWLEDGE_CHUNKS TABLE
-- Semantic text chunks and high-dimensional vector embeddings for RAG
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES medical_sources(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER NOT NULL CHECK (token_count > 0),
    embedding vector(1536) NOT NULL, -- Compatible with OpenAI text-embedding-3-small or similar 1536d models
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb, -- Heading hierarchy, section, tags
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_source ON knowledge_chunks(source_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_metadata ON knowledge_chunks USING gin(metadata_json);
-- HNSW Cosine Similarity Index for rapid vector retrieval
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_embedding ON knowledge_chunks 
USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- -----------------------------------------------------------------------------
-- 9. CONVERSATIONS TABLE
-- Health assistant chat session metadata
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    report_id UUID REFERENCES reports(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL DEFAULT 'Health Information Session',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_report_id ON conversations(report_id);

-- -----------------------------------------------------------------------------
-- 10. MESSAGES TABLE
-- Individual messages exchanged within a conversation with provenance tracking
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender VARCHAR(20) NOT NULL CHECK (sender IN ('USER', 'ASSISTANT', 'SYSTEM')),
    content TEXT NOT NULL,
    retrieved_evidence_ids UUID[] DEFAULT '{}',
    safety_disclaimer_appended BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at ASC);

-- -----------------------------------------------------------------------------
-- 11. MEDICATIONS TABLE
-- Canonical master database of medications
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand_name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255) NOT NULL,
    rxnorm_cui VARCHAR(50),
    therapeutic_class VARCHAR(255) NOT NULL,
    standard_dosage_guidelines TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_medications_generic ON medications(generic_name);
CREATE INDEX IF NOT EXISTS idx_medications_brand ON medications(brand_name);

-- -----------------------------------------------------------------------------
-- 12. USER_MEDICATIONS TABLE
-- Patient current and past medications
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_medications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE RESTRICT,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_medications_user_id ON user_medications(user_id);
CREATE INDEX IF NOT EXISTS idx_user_medications_active ON user_medications(user_id, is_active);

-- -----------------------------------------------------------------------------
-- 13. DRUG_INTERACTIONS TABLE
-- Deterministic pairwise drug-drug interaction matrix
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drug_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medication_a_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    medication_b_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    severity VARCHAR(50) NOT NULL CHECK (severity IN ('MINOR', 'MODERATE', 'MAJOR', 'CONTRAINDICATED')),
    interaction_mechanism TEXT NOT NULL,
    clinical_evidence_source VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_drug_pair UNIQUE (medication_a_id, medication_b_id),
    CONSTRAINT chk_distinct_drugs CHECK (medication_a_id <> medication_b_id)
);

CREATE INDEX IF NOT EXISTS idx_drug_int_a ON drug_interactions(medication_a_id);
CREATE INDEX IF NOT EXISTS idx_drug_int_b ON drug_interactions(medication_b_id);

-- -----------------------------------------------------------------------------
-- 14. SYMPTOMS TABLE
-- Canonical symptom ontology
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS symptoms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    canonical_name VARCHAR(255) NOT NULL UNIQUE,
    icd10_code VARCHAR(50),
    category VARCHAR(100) NOT NULL,
    emergency_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_symptoms_name ON symptoms(canonical_name);
CREATE INDEX IF NOT EXISTS idx_symptoms_emergency ON symptoms(emergency_flag);

-- -----------------------------------------------------------------------------
-- 15. TRIAGE_RULES TABLE
-- Deterministic emergency and urgency decision rules (No LLM ambiguity)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS triage_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(255) NOT NULL UNIQUE,
    condition_json JSONB NOT NULL, -- e.g., {"biomarker": "Potassium", "operator": ">", "threshold": 6.5}
    urgency_level VARCHAR(50) NOT NULL CHECK (urgency_level IN ('ROUTINE', 'URGENT', 'EMERGENCY')),
    deterministic_action_instruction TEXT NOT NULL,
    disclaimer_text TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_triage_urgency ON triage_rules(urgency_level);

-- -----------------------------------------------------------------------------
-- 16. AUDIT_LOGS TABLE
-- Immutable security and compliance event tracking
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL, -- e.g., "UPLOAD_REPORT", "VIEW_MEASUREMENT", "LOGIN_SUCCESS"
    resource_type VARCHAR(100) NOT NULL, -- e.g., "REPORT", "MEASUREMENT", "AUTH"
    resource_id VARCHAR(255),
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512),
    status_code INTEGER NOT NULL,
    details_json JSONB,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
