# MediLens AI - Multi-Tier Testing Strategy

## 1. Testing Philosophy & Quality Gates

MediLens AI operates in the clinical and healthcare domain where system accuracy, data integrity, and strict confidentiality are paramount. Software delivery is gated by a rigorous multi-tier testing strategy.

```text
               +----------------------------------+
               |        End-to-End (E2E)          |  <- Cypress / Playwright
               |   Full user flow & report upload |
               +-----------------+----------------+
                                 |
               +-----------------+----------------+
               |      Integration & Security      |  <- Testcontainers & Spring MockMvc
               |  Auth, Tenant Isolation, RAG/DB  |
               +-----------------+----------------+
                                 |
               +-----------------+----------------+
               |     Deterministic Golden Tests   |  <- Python Pytest Lab Fixtures
               |  OCR, Layout, Extraction, Status |
               +-----------------+----------------+
                                 |
               +-----------------+----------------+
               |           Unit Tests             |  <- JUnit 5, Pytest, Vitest
               |  Regex, Unit Converter, Services |
               +----------------------------------+
```

---

## 2. Testing Categories & Specifications

### 2.1 Unit Tests
- **Backend (JUnit 5 + Mockito)**:
  - Token generation, expiration, and signature validation in `JwtServiceTest`.
  - Password encoding and complexity enforcement in `UserServiceTest`.
  - State machine transition rules in `ReportStateMachineTest`.
- **AI Service (Pytest)**:
  - Unit conversion accuracy in `test_unit_converter.py` (e.g., $\text{mg/dL} \leftrightarrow \text{mmol/L}$ for Glucose and Cholesterol).
  - Regular expression biomarker pattern matchers in `test_regex_extractor.py`.
  - Deskew orientation calculations in `test_deskew.py`.
- **Frontend (Vitest + React Testing Library)**:
  - Redux reducer tests for auth state and report lists.
  - Biomarker status badge color/label mapping (`LOW` = Blue, `NORMAL` = Green, `HIGH` = Orange, `CRITICAL` = Red, `UNKNOWN` = Gray).

### 2.2 Deterministic Validation Golden Tests
A dedicated test suite in `ai-service/tests/validation/` validates numerical status evaluation against canonical clinical guidelines without mock data:
- **Boundary Tests**:
  - Hemoglobin Male: $13.7 \to \text{LOW}$, $13.8 \to \text{NORMAL}$, $17.2 \to \text{NORMAL}$, $17.3 \to \text{HIGH}$, $6.9 \to \text{CRITICAL}$.
  - Fasting Glucose: $69 \to \text{LOW}$, $70 \to \text{NORMAL}$, $99 \to \text{NORMAL}$, $100 \to \text{HIGH}$, $45 \to \text{CRITICAL}$.
- **Unknown Status Handling**:
  - Missing reference interval $\to \text{UNKNOWN}$.
  - Unrecognized unit $\to \text{UNKNOWN}$.
  - Low OCR confidence (< 0.50) $\to \text{UNKNOWN}$.

### 2.3 OCR & Extraction Golden Dataset Tests
- A synthetic fixture library of 20 standardized lab report images (CBC, Lipid Panel, Metabolic Panel, Thyroid Panel) with ground-truth JSON annotations.
- **Metrics Evaluated**:
  - Character Error Rate (CER) $\le 2\%$.
  - Word Error Rate (WER) $\le 5\%$.
  - Biomarker Field Precision $\ge 98\%$.
  - Biomarker Field Recall $\ge 95\%$.
- Tests verify automatic fallback to Tesseract when simulated blur or skew reduces PaddleOCR confidence below 0.70.

### 2.4 Integration & Database Tests
- Uses **Testcontainers** for PostgreSQL + pgvector and Redis to execute integration tests against real database engines.
- Verifies:
  - Database migrations and table constraints.
  - HNSW vector index creation and cosine similarity querying.
  - Foreign key cascades (e.g., deleting a report cascades to its measurements).

### 2.5 Security & Authorization Tests (Anti-IDOR)
- **Tenant Isolation**:
  - User A attempts to view User B's report via `GET /api/v1/reports/{userB_report_id}` $\to$ Asserts `403 Forbidden` and verifies audit log emission.
  - User A attempts to query measurements belonging to User B $\to$ Asserts empty result set or `403 Forbidden`.
- **MIME & File Upload Security**:
  - Uploading `.exe` renamed to `.pdf` $\to$ Asserts rejection via magic byte validator (`400 Bad Request`).
  - Uploading a 30MB file $\to$ Asserts rejection via upload size limit (`413 Payload Too Large`).

### 2.6 RAG Groundedness & Clinical Guardrail Tests
- **Citation Assertion**: Every generated response must contain at least one valid `[Source X]` citation matching an ID in `knowledge_chunks`.
- **Refusal Test**: Querying off-topic or unindexed questions (e.g., "What is the capital of France?" or unsubstantiated herbal therapies) must trigger the deterministic refusal response.
- **Diagnostic Prevention**: Asserts that post-generation guardrail rejects text containing phrases like *"You are diagnosed with"* or *"Take 20mg of"*.

### 2.7 End-to-End (E2E) Tests
- Complete automated flow:
  1. User registers and logs in.
  2. Uploads synthetic sample lab report.
  3. Frontend polls `/api/v1/reports/{id}/status` through stages until `COMPLETED`.
  4. Structured measurements table displays extracted values matching ground truth.
  5. Longitudinal trend chart reflects new data point.
  6. Conversational query retrieves grounded explanation citing guideline source.

---

## 3. Continuous Integration Gates

| Stage | Trigger | Gate Criteria |
| :--- | :--- | :--- |
| **Lint & Format** | Pre-commit / PR | Zero ESLint, Ruff, or Checkstyle errors. |
| **Unit & Golden Tests** | Every PR commit | 100% pass rate; $\ge 85\%$ code coverage on business logic. |
| **Integration & Security**| PR to main | All Testcontainers tests pass; zero IDOR leaks. |
| **Deployment Gate** | Release tag | End-to-end synthetic lab report pipeline completes successfully. |
