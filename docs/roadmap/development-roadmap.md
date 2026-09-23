# MediLens AI - Phased Engineering Roadmap

## 1. Development Principles & Milestone Gates

MediLens AI is developed phase-by-phase. No phase is considered complete without passing its corresponding automated test suite, verifying database migrations, confirming API contracts, and ensuring adherence to clinical safety invariants.

---

## 2. Phase Breakdown & Deliverables

### Phase 0: Architectural Blueprint & Foundation Setup (CURRENT)
- [x] Polyglot monorepo scaffold (`backend/`, `ai-service/`, `frontend/`, `docs/`, `scripts/`).
- [x] Multi-container Docker Compose infrastructure with health checks.
- [x] PostgreSQL 16 schema with `pgvector` extension and 16 normalized tables.
- [x] Seed data for canonical biomarkers, demographic reference ranges, and emergency triage rules.
- [x] OpenAPI 3.0 specifications for Spring Boot Backend and internal FastAPI AI Service.
- [x] Comprehensive documentation: Architecture, Security, AI Pipeline, and Testing Strategy.
- **Verification Gate**: Docker Compose file validates (`docker compose config`); all schema DDL and documentation artifacts created and verified.

---

### Phase 1: Database Entities, Authentication & Security Gateway
- **Scope**:
  - Spring Boot 3 JPA entity mappings for `users`, `reports`, `audit_logs`.
  - Spring Security configuration with stateless JWT authentication.
  - Refresh token rotation and bcrypt password hashing.
  - Role-based authorization (`ROLE_PATIENT`, `ROLE_CLINICIAN`, `ROLE_ADMIN`).
  - Tenant ownership interceptor (preventing IDOR attacks on user records).
  - Immutable audit logging service writing to `audit_logs`.
- **Deliverables**:
  - `AuthController`, `UserService`, `JwtService`, `SecurityConfig`, `AuditService`.
  - Unit tests for token issuance, expiration, and password hashing.
  - Integration tests verifying tenant isolation and unauthorized access rejection (401/403).
- **Verification Gate**: All auth endpoints pass integration tests with 100% authorization barrier.

---

### Phase 2: AI Service & Deterministic Extraction Engine
- **Scope**:
  - Python 3.10+ FastAPI service environment with PaddleOCR and Tesseract.
  - Image preprocessing module: Grayscale, deskew (Hough transform), bilateral denoise, CLAHE contrast.
  - Dual-engine OCR worker: PaddleOCR as primary, Tesseract as fallback if confidence < 0.70.
  - Tabular layout parser and regex biomarker extractor.
  - Canonical dictionary mapper (resolves aliases like "HGB" to "Hemoglobin").
  - Deterministic unit converter (e.g., converts mmol/L to mg/dL when standardizing).
  - Deterministic reference-range validator: Evaluates values into `LOW`, `NORMAL`, `HIGH`, `CRITICAL`, or `UNKNOWN`.
- **Deliverables**:
  - `app/ocr/preprocessor.py`, `app/ocr/engine.py`, `app/extraction/parser.py`, `app/normalization/units.py`, `app/validation/range_validator.py`.
  - Synthetic golden lab report test suite verifying extraction accuracy $\ge 95\%$.
- **Verification Gate**: Deterministic test matrix confirms that no value classification is handled by an LLM.

---

### Phase 3: Backend Report Processing Orchestration
- **Scope**:
  - Asynchronous report processing state machine:
    `UPLOADED` $\to$ `VALIDATING` $\to$ `PREPROCESSING` $\to$ `OCR_PROCESSING` $\to$ `EXTRACTING` $\to$ `NORMALIZING` $\to$ `VALIDATING_VALUES` $\to$ `COMPLETED` / `FAILED`.
  - File upload controller with MIME type verification, magic number checking, and SHA-256 deduplication.
  - Redis task dispatch and Spring asynchronous executor.
  - REST client communicating with FastAPI AI service via `X-Internal-API-Key`.
  - Structured measurement persistence into `measurements` and `report_pages` tables.
- **Deliverables**:
  - `ReportUploadController`, `ReportProcessingService`, `AiServiceClient`, `ReportStateMachine`.
  - Integration test: Upload synthetic PDF/image, poll status until `COMPLETED`, assert structured measurements in database.
- **Verification Gate**: End-to-end report upload flow completes successfully with state transitions accurately logged.

---

### Phase 4: Controlled Medical RAG Ingestion & Grounded Assistance
- **Scope**:
  - Ingestion pipeline for authoritative medical sources (`medical_sources`, `knowledge_chunks`).
  - Semantic chunking (400 tokens, 50-token overlap) and embedding generation (`vector(1536)`).
  - High-speed HNSW cosine similarity search in PostgreSQL.
  - Grounded prompt construction: Restricts LLM to retrieved context only with explicit source citation tags `[Source ID]`.
  - Clinical Guardrail Filter: Strips any probabilistic diagnosis or drug prescription modifications; enforces standard refusal if similarity < 0.75.
- **Deliverables**:
  - `app/rag/ingestion.py`, `app/rag/retriever.py`, `app/rag/generator.py`, `app/rag/guardrails.py`.
  - RAG grounding test suite: Verifies citations on all responses and asserts refusal when ungrounded queries are submitted.
- **Verification Gate**: Groundedness evaluation benchmark confirms zero unsupported clinical claims.

---

### Phase 5: Deterministic Triage, Drug Interactions & Safety Modules
- **Scope**:
  - Deterministic symptom triage engine evaluating against `triage_rules` table.
  - Immediate emergency alert generation for red-flag biomarker values (e.g., Potassium $\ge 6.5\text{ mmol/L}$, Glucose $\le 50\text{ mg/dL}$).
  - Pairwise drug interaction engine evaluating against `drug_interactions` matrix.
  - Healthcare provider discovery mock/service for post-triage care navigation.
- **Deliverables**:
  - `TriageService`, `DrugInteractionService`, `DoctorFinderService`.
  - Test suite covering critical emergency triggers and multi-drug interaction combinations.
- **Verification Gate**: 100% of defined emergency rules trigger deterministic alerts without LLM mediation.

---

### Phase 6: Healthcare Presentation Dashboard (Frontend)
- **Scope**:
  - React 18 + TypeScript SPA with Tailwind CSS design tokens.
  - Redux Toolkit store for user authentication, report uploads, and active state.
  - Report upload interface with drag-and-drop and live stage-by-stage status progress bar.
  - Detailed report viewer displaying canonical name, observed value, unit, reference interval, status badge, confidence, and provenance snippet.
  - Longitudinal trend visualizer (Recharts) charting historical biomarker values over time.
  - Conversational medical assistant UI displaying expandable source citations and clinical disclaimers.
  - Multilingual support via `i18next`.
- **Deliverables**:
  - Clean component architecture, custom UI hooks, responsive layout for desktop and tablet.
- **Verification Gate**: Frontend builds cleanly with zero TypeScript errors and renders responsive views.

---

### Phase 7: End-to-End Verification, Security Audit & Hardening
- **Scope**:
  - Comprehensive end-to-end testing of complete user journey.
  - Security audit: OWASP Top 10 penetration tests (IDOR, XSS, CSRF, SQL Injection, MIME Spoofing).
  - Stress testing: Concurrent report processing under load.
  - Final clinical safety audit: Verify persistent disclaimer banners and non-diagnostic phrasing across all views.
- **Deliverables**:
  - Complete test reports, benchmark figures, and deployment readiness signoff.
- **Verification Gate**: Production-ready research platform with all safety checks certified.
