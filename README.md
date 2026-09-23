# MediLens AI

> **Clinical-Grade Medical Laboratory Report Analysis & Healthcare Navigation Platform**

[![Architecture: Polyglot Monorepo](https://img.shields.io/badge/Architecture-Polyglot%20Monorepo-blue.svg)](#architecture)
[![Backend: Spring Boot 3](https://img.shields.io/badge/Backend-Spring%20Boot%203%20%7C%20Java%2017+-brightgreen.svg)](#backend)
[![AI Service: FastAPI](https://img.shields.io/badge/AI%20Service-FastAPI%20%7C%20Python%203.10+-orange.svg)](#ai-service)
[![Frontend: React + TS](https://img.shields.io/badge/Frontend-React%2018%20%7C%20TypeScript%20%7C%20Tailwind-cyan.svg)](#frontend)
[![Database: PostgreSQL + pgvector](https://img.shields.io/badge/Database-PostgreSQL%2016%20%2B%20pgvector-blueviolet.svg)](#database)

---

## 1. Project Objective

**MediLens AI** is an academic and clinical research-grade platform designed to ingest diagnostic laboratory reports in PDF, PNG, and JPEG formats, extract structured clinical biomarkers using a deterministic OCR and NLP pipeline, validate observed values against demographic reference ranges, track longitudinal health trends, and provide evidence-grounded medical explanations via Retrieval-Augmented Generation (RAG).

### Non-Negotiable Medical Safety Invariants
1. **Never use an LLM as the source of truth for extracted laboratory values.** Biomarker extraction, unit normalization, and reference-range evaluations are computed deterministically.
2. **Deterministic Status Classification**: All biomarker readings are categorized strictly into `LOW`, `NORMAL`, `HIGH`, `CRITICAL`, or `UNKNOWN`.
3. **Emergency Detection via Rules**: Red-flag emergency detection operates deterministically based on clinical threshold rules before conversational AI.
4. **No Definitive Diagnoses or Prescriptions**: MediLens AI does not diagnose diseases or advise starting, stopping, or altering prescription medications.
5. **Grounded RAG with Provenance**: Conversational explanations must cite verified medical knowledge sources with metadata (title, authoring body, publication date). If evidence is absent, the system refuses to guess.

---

## 2. System Architecture

```text
                               +----------------------------------+
                               |     Client Browser (React/TS)    |
                               |  Dashboard & Health Navigation   |
                               +-----------------+----------------+
                                                 |
                                         HTTPS / REST API
                                                 |
                                                 v
                       +--------------------------------------------------+
                       |           Spring Boot 3 Backend (:8080)          |
                       |  - JWT Authentication & RBAC (Patient/Clinician) |
                       |  - User Ownership & Resource Isolation           |
                       |  - Report Processing State Machine               |
                       |  - Audit Logging & Rate Limiting                 |
                       +---------+------------------+---------------------+
                                 |                  |
                       Internal REST RPC      JPA / Hibernate
                                 |                  |
                                 v                  v
+------------------------------------+  +-------------------------------------+
|      FastAPI AI Engine (:8000)     |  |    PostgreSQL 16 + pgvector (:5432) |
| - OpenCV Preprocessing (Deskew)    |  |  - 16 Normalized Tables             |
| - PaddleOCR + Tesseract Fallback   |  |  - Encrypted User Health Data       |
| - Deterministic Biomarker Extractor|  |  - High-dim Vector Embeddings       |
| - Canonical Aliasing & Unit Conv   |  |  - Audit Log Trail                  |
| - Clinical Range Rule Validator    |  +-------------------------------------+
| - RAG Pipeline & Safety Guardrails |                  ^
+-----------------+------------------+                  |
                  |                                     |
                  +-------- pgvector Semantic Search ---+
```

---

## 3. Repository Structure

```text
.
├── .editorconfig                    # Unified code formatting rules
├── .env.example                     # Environment configuration template
├── .gitignore                       # Multi-stack gitignore
├── docker-compose.yml               # Multi-service local orchestrator
├── README.md                        # Project documentation root
├── docs/                            # Comprehensive engineering blueprints
│   ├── architecture/                # System architecture & sequence flows
│   ├── database/                    # Schema DDL (16 tables) and ERD
│   ├── api/                         # OpenAPI 3.0 specifications (Backend & AI)
│   ├── roadmap/                     # 8-Phase development roadmap & milestones
│   ├── security/                    # Threat modeling, RBAC & HIPAA-aligned privacy
│   ├── ai/                          # Deterministic OCR, extraction & RAG pipeline
│   └── testing/                     # Multi-tier testing matrix & golden datasets
├── scripts/                         # Database bootstrap & seed scripts
│   ├── init-db.sql                  # PostgreSQL & pgvector init script
│   └── seed-data.sql                # Canonical biomarkers & clinical ranges
├── backend/                         # Java 17+ Spring Boot service
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── ai-service/                      # Python 3.10+ FastAPI AI engine
│   ├── requirements.txt
│   ├── Dockerfile
│   └── app/
└── frontend/                        # React 18+ TypeScript application
    ├── package.json
    ├── Dockerfile
    └── src/
```

---

## 4. Quickstart Guide (Phase 0 Foundation)

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/) & Docker Compose (v2.20+)
- Java 17+ & Maven 3.9+ (for native backend dev)
- Python 3.10+ (for native AI service dev)
- Node.js 18+ & npm/pnpm (for native frontend dev)

### Setup & Launch

1. **Clone & Configure Environment**:
   ```bash
   cp .env.example .env
   ```

2. **Start Infrastructure Services**:
   ```bash
   docker compose up -d postgres redis
   ```

3. **Verify Database Initialization**:
   ```bash
   docker compose exec postgres psql -U medilens_admin -d medilens_db -c "\dx"
   ```
   *Expected output: `vector` extension listed.*

4. **Spin Up Full Stack**:
   ```bash
   docker compose up --build
   ```

---

## 5. Development Roadmap Progress

- [x] **Phase 0: Foundation & Architecture** (Current)
  - Polyglot monorepo scaffold
  - Architecture documentation & sequence diagrams
  - 16-table normalized PostgreSQL + pgvector schema
  - Spring Boot & FastAPI OpenAPI specifications
  - Docker Compose foundation with health checks
  - Security, AI pipeline, and Testing blueprints
- [ ] **Phase 1**: Database Entities, Spring Boot Auth (JWT/RBAC) & Audit Logging
- [ ] **Phase 2**: FastAPI OCR Preprocessing, PaddleOCR/Tesseract Fallback & Deterministic Extraction
- [ ] **Phase 3**: Asynchronous Report Processing State Machine & Backend Integration
- [ ] **Phase 4**: Controlled Medical RAG Ingestion, pgvector Search & Grounded Explanation
- [ ] **Phase 5**: Deterministic Symptom Triage, Medication Interaction & Doctor Finder
- [ ] **Phase 6**: React Healthcare Dashboard, Trend Charts, Report Viewer & i18n
- [ ] **Phase 7**: Comprehensive E2E Verification, Security Audit & Clinical Guardrail Hardening
