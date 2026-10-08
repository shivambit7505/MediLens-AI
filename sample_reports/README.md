# MediLens AI - Sample Clinical Laboratory Reports

Use these pre-generated synthetic diagnostic reports to test the upload, OCR parsing, biomarker extraction, and RAG explanation pipelines.

| Filename | Panel Type | Formats | Biomarkers Included | Expected Status Trigger |
| :--- | :--- | :--- | :--- | :--- |
| **`metabolic_panel`** | Comprehensive Metabolic Panel (CMP) | `.png`, `.pdf` | Glucose, Potassium, Sodium, Calcium, Creatinine, BUN | Glucose (HIGH), Potassium (CRITICAL - Emergency Alert!) |
| **`lipid_profile`** | Lipid Profile & Cardiac Panel | `.png`, `.pdf` | Total Cholesterol, HDL, LDL, Triglycerides | Total Cholesterol (HIGH), LDL (HIGH), Triglycerides (HIGH) |
| **`complete_blood_count`** | Complete Blood Count (CBC) | `.png`, `.pdf` | Hemoglobin, WBC, Platelets, Hematocrit | All values NORMAL |

### How to Test:
1. Navigate to `http://localhost:3000/upload` in your browser.
2. Drag and drop any `.pdf` or `.png` from this folder.
3. Observe the live state machine progress:
   `UPLOADED` -> `VALIDATING` -> `PREPROCESSING` -> `OCR` -> `EXTRACTING` -> `COMPLETED`.
4. Click **View Findings** to see extracted numbers, reference ranges, status badges, and evidence citations.
5. Ask questions to the **Clinical Assistant** (`/chat`) using Google Gemini!
