# MediLens AI - Database Entity-Relationship Diagram & Schema Documentation

## 1. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    users ||--o{ reports : "owns"
    users ||--o{ measurements : "possesses"
    users ||--o{ conversations : "participates_in"
    users ||--o{ user_medications : "takes"
    users ||--o{ audit_logs : "triggers"

    reports ||--o{ report_pages : "contains"
    reports ||--o{ measurements : "yields"
    reports ||--o{ conversations : "referenced_in"
    report_pages ||--o{ measurements : "sourced_from"

    biomarkers ||--o{ reference_ranges : "defines_ranges"
    biomarkers ||--o{ measurements : "classified_as"

    reference_ranges ||--o{ measurements : "validates"

    medical_sources ||--o{ knowledge_chunks : "provides"

    conversations ||--o{ messages : "contains"

    medications ||--o{ user_medications : "prescribed_as"
    medications ||--o{ drug_interactions : "interacts_as_a"
    medications ||--o{ drug_interactions : "interacts_as_b"

    users {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar first_name
        varchar last_name
        date date_of_birth
        varchar gender
        varchar role
        timestamptz created_at
    }

    reports {
        uuid id PK
        uuid user_id FK
        varchar original_filename
        varchar storage_path
        varchar mime_type
        bigint file_size_bytes
        varchar file_hash_sha256
        varchar status
        text failure_reason
        timestamptz created_at
    }

    report_pages {
        uuid id PK
        uuid report_id FK
        int page_number
        varchar image_storage_path
        text ocr_raw_text
        numeric ocr_confidence_score
    }

    biomarkers {
        uuid id PK
        varchar canonical_name UK
        varchar code_loinc
        varchar code_snomed
        varchar category
        varchar standard_unit
        jsonb aliases_json
    }

    reference_ranges {
        uuid id PK
        uuid biomarker_id FK
        varchar gender_applicable
        numeric age_min_years
        numeric age_max_years
        varchar unit
        numeric low_value
        numeric high_value
        numeric critical_low
        numeric critical_high
        varchar source_citation
    }

    measurements {
        uuid id PK
        uuid report_id FK
        uuid report_page_id FK
        uuid user_id FK
        uuid biomarker_id FK
        varchar extracted_name
        varchar observed_value_raw
        numeric observed_value_numeric
        varchar extracted_unit
        numeric normalized_value_numeric
        varchar normalized_unit
        varchar status
        numeric confidence
        text source_text_snippet
        jsonb bounding_box_json
    }

    medical_sources {
        uuid id PK
        varchar title
        varchar organization
        varchar url_or_reference
        date publication_date
        varchar source_type
    }

    knowledge_chunks {
        uuid id PK
        uuid source_id FK
        text content
        int token_count
        vector embedding
        jsonb metadata_json
    }

    conversations {
        uuid id PK
        uuid user_id FK
        uuid report_id FK
        varchar title
        timestamptz created_at
    }

    messages {
        uuid id PK
        uuid conversation_id FK
        varchar sender
        text content
        uuid_array retrieved_evidence_ids
        boolean safety_disclaimer_appended
    }

    medications {
        uuid id PK
        varchar brand_name
        varchar generic_name
        varchar rxnorm_cui
        varchar therapeutic_class
    }

    user_medications {
        uuid id PK
        uuid user_id FK
        uuid medication_id FK
        varchar dosage
        varchar frequency
        date start_date
        date end_date
        boolean is_active
    }

    drug_interactions {
        uuid id PK
        uuid medication_a_id FK
        uuid medication_b_id FK
        varchar severity
        text interaction_mechanism
        varchar clinical_evidence_source
    }

    symptoms {
        uuid id PK
        varchar canonical_name UK
        varchar icd10_code
        varchar category
        boolean emergency_flag
    }

    triage_rules {
        uuid id PK
        varchar rule_name UK
        jsonb condition_json
        varchar urgency_level
        text deterministic_action_instruction
    }

    audit_logs {
        uuid id PK
        uuid user_id FK
        varchar action
        varchar resource_type
        varchar resource_id
        varchar ip_address
        int status_code
        timestamptz timestamp
    }
```

---

## 2. Table Specifications & Cardinalities

### 1. `users`
- **Purpose**: System identity and demographics (essential for age- and sex-stratified reference intervals).
- **Security**: Passwords hashed with BCrypt ($cost \ge 12$).
- **Cardinality**: 1 User $\to$ N Reports, N Measurements, N Conversations, N User_Medications.

### 2. `reports`
- **Purpose**: Tracks original uploaded documents and lifecycle state machine.
- **Constraints**: Enforces maximum upload size (25MB) and strict MIME check (`application/pdf`, `image/png`, `image/jpeg`).
- **Cascade**: Deleting a report automatically deletes its associated pages and extracted measurements.

### 3. `report_pages`
- **Purpose**: Stores individual rendered pages for multi-page PDF documents.
- **Provenance**: Retains OCR engine name (`paddleocr` or `tesseract`), raw extracted text, and confidence score.

### 4. `biomarkers`
- **Purpose**: Master vocabulary mapping extracted aliases (e.g., `"Hb"`, `"HGB"`, `"Haemoglobin"`) to canonical medical concepts (LOINC `718-7`, SNOMED `38082009`).
- **Index**: GIN index on `aliases_json` for $O(1)$ JSON array lookups.

### 5. `reference_ranges`
- **Purpose**: Demographic-stratified biological reference intervals.
- **Invariants**: `low_value <= high_value`. Critical boundaries (`critical_low`, `critical_high`) designate immediate clinical alerts.

### 6. `measurements`
- **Purpose**: Core clinical data table storing every extracted and validated biomarker reading.
- **Audit & Provenance**: Stores raw observed string, parsed numeric value, extracted unit, normalized value, bounding box coordinates, and exact text snippet from which the value was extracted.

### 7. `medical_sources`
- **Purpose**: Provenance tracking for clinical guidelines and literature ingested into the RAG knowledge base.
- **Attributes**: Organization, publication date, version, guideline type.

### 8. `knowledge_chunks`
- **Purpose**: Text chunks split from medical sources with 1536-dimensional vector embeddings.
- **Index**: HNSW (Hierarchical Navigable Small World) index with `vector_cosine_ops` for sub-10ms semantic similarity retrieval.

### 9. `conversations` & 10. `messages`
- **Purpose**: Threaded chat history between users and the clinical information assistant.
- **Provenance**: Each assistant message stores `retrieved_evidence_ids (UUID[])` directly referencing the `knowledge_chunks` utilized to formulate the response.

### 11. `medications` & 12. `user_medications`
- **Purpose**: Patient active and past prescription/OTC medications mapped to RxNorm identifiers.

### 13. `drug_interactions`
- **Purpose**: Pre-computed deterministic pairwise drug interaction matrix.
- **Severities**: `MINOR`, `MODERATE`, `MAJOR`, `CONTRAINDICATED`.

### 14. `symptoms` & 15. `triage_rules`
- **Purpose**: Rule-based symptom catalog and emergency triage evaluator.
- **Clinical Invariant**: Immediate emergency guidance is evaluated deterministically against `condition_json` without consulting an LLM.

### 16. `audit_logs`
- **Purpose**: Immutable security audit trail recording authentication attempts, report uploads, and data access.

---

## 3. Indexing & Performance Strategy
1. **Tenant Filtering**: Composite indexes on `(user_id, created_at DESC)` and `(user_id, biomarker_id, measurement_date ASC)` guarantee fast longitudinal query execution.
2. **JSONB Lookups**: GIN indexes on `biomarkers(aliases_json)` and `knowledge_chunks(metadata_json)`.
3. **High-Speed Vector Search**: HNSW index on `knowledge_chunks(embedding)` using `m = 16, ef_construction = 64` achieves >99% recall with millisecond query latencies.
