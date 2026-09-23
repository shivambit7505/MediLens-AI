-- =============================================================================
-- MediLens AI - Seed Data for Canonical Biomarkers & Reference Ranges
-- Source Evidence: WHO, Clinical Laboratory Standards, Mayo Clinic References
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Canonical Biomarkers
-- -----------------------------------------------------------------------------
INSERT INTO biomarkers (id, canonical_name, code_loinc, code_snomed, category, standard_unit, description, aliases_json)
VALUES
(
    'a0000000-0000-0000-0000-000000000001',
    'Hemoglobin',
    '718-7',
    '38082009',
    'Complete Blood Count',
    'g/dL',
    'Iron-containing oxygen-transport metalloprotein in red blood cells.',
    '["Hb", "HGB", "Haemoglobin", "Total Hemoglobin"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000002',
    'Fasting Blood Glucose',
    '1558-6',
    '36048009',
    'Metabolic Panel',
    'mg/dL',
    'Measurement of blood sugar level after an overnight fast.',
    '["FBG", "FBS", "Fasting Glucose", "Blood Sugar Fasting", "Glucose Fasting"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000003',
    'Hemoglobin A1c',
    '4548-4',
    '43396009',
    'Metabolic Panel',
    '%',
    'Average blood glucose levels over the past 2 to 3 months.',
    '["HbA1c", "A1c", "Glycated Hemoglobin", "Glycohemoglobin"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000004',
    'Serum Creatinine',
    '2160-0',
    '70901006',
    'Renal Panel',
    'mg/dL',
    'Breakdown product of creatine phosphate from muscle and protein metabolism.',
    '["Creatinine", "S. Creatinine", "Cr", "Creat"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000005',
    'Serum Potassium',
    '2823-3',
    '88262002',
    'Electrolyte Panel',
    'mmol/L',
    'Essential electrolyte maintaining fluid balance and cardiac muscle rhythm.',
    '["Potassium", "K", "K+", "Serum K"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000006',
    'Serum Sodium',
    '2951-2',
    '52627005',
    'Electrolyte Panel',
    'mmol/L',
    'Primary extracellular cation maintaining osmotic pressure and hydration.',
    '["Sodium", "Na", "Na+", "Serum Na"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000007',
    'Total Cholesterol',
    '2093-3',
    '121868005',
    'Lipid Panel',
    'mg/dL',
    'Overall measure of cholesterol components including LDL, HDL, and VLDL.',
    '["Cholesterol", "Total Chol", "Chol", "TC"]'::jsonb
),
(
    'a0000000-0000-0000-0000-000000000008',
    'Platelet Count',
    '777-3',
    '61594008',
    'Complete Blood Count',
    '10^3/uL',
    'Blood cells instrumental in physiological blood clotting and wound repair.',
    '["Platelets", "PLT", "Thrombocyte Count"]'::jsonb
)
ON CONFLICT (canonical_name) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Canonical Reference Ranges (Demographic Stratified)
-- -----------------------------------------------------------------------------
INSERT INTO reference_ranges (biomarker_id, gender_applicable, age_min_years, age_max_years, unit, low_value, high_value, critical_low, critical_high, source_citation)
VALUES
-- Hemoglobin (Adult Male)
(
    'a0000000-0000-0000-0000-000000000001',
    'MALE',
    18.0,
    120.0,
    'g/dL',
    13.8,
    17.2,
    7.0,
    20.0,
    'Mayo Clinic Laboratories Reference Manual 2024'
),
-- Hemoglobin (Adult Female)
(
    'a0000000-0000-0000-0000-000000000001',
    'FEMALE',
    18.0,
    120.0,
    'g/dL',
    12.1,
    15.1,
    7.0,
    20.0,
    'Mayo Clinic Laboratories Reference Manual 2024'
),
-- Fasting Blood Glucose (All Adults)
(
    'a0000000-0000-0000-0000-000000000002',
    'ALL',
    18.0,
    120.0,
    'mg/dL',
    70.0,
    99.0,
    45.0,
    400.0,
    'American Diabetes Association (ADA) Standards of Care 2024'
),
-- Hemoglobin A1c (All Adults)
(
    'a0000000-0000-0000-0000-000000000003',
    'ALL',
    18.0,
    120.0,
    '%',
    4.0,
    5.6,
    NULL,
    14.0,
    'American Diabetes Association (ADA) 2024'
),
-- Serum Creatinine (Adult Male)
(
    'a0000000-0000-0000-0000-000000000004',
    'MALE',
    18.0,
    120.0,
    'mg/dL',
    0.74,
    1.35,
    NULL,
    4.00,
    'National Kidney Foundation (KDOQI) Guidelines'
),
-- Serum Creatinine (Adult Female)
(
    'a0000000-0000-0000-0000-000000000004',
    'FEMALE',
    18.0,
    120.0,
    'mg/dL',
    0.59,
    1.04,
    NULL,
    4.00,
    'National Kidney Foundation (KDOQI) Guidelines'
),
-- Serum Potassium (All Adults)
(
    'a0000000-0000-0000-0000-000000000005',
    'ALL',
    18.0,
    120.0,
    'mmol/L',
    3.5,
    5.0,
    2.8,
    6.5,
    'WHO Essential Diagnostics & Electrolyte Guidelines'
)
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- Authoritative Medical Knowledge Sources for RAG
-- -----------------------------------------------------------------------------
INSERT INTO medical_sources (id, title, organization, url_or_reference, publication_date, version, topic, source_type)
VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'Interpretation of Complete Blood Count (CBC) Parameters',
    'World Health Organization',
    'https://www.who.int/publications/cbc-guidelines-2023',
    '2023-05-15',
    '2.1',
    'Hematology',
    'CLINICAL_GUIDELINE'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'Standards of Care in Diabetes—2024',
    'American Diabetes Association',
    'https://diabetesjournals.org/care/issue/47/Supplement_1',
    '2024-01-01',
    '2024.1',
    'Endocrinology & Metabolism',
    'CLINICAL_GUIDELINE'
)
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- Deterministic Triage Rules (Emergency & Red-Flag Safeguards)
-- -----------------------------------------------------------------------------
INSERT INTO triage_rules (id, rule_name, condition_json, urgency_level, deterministic_action_instruction, disclaimer_text)
VALUES
(
    'c0000000-0000-0000-0000-000000000001',
    'Severe Hyperkalemia Alert',
    '{"biomarker_loinc": "2823-3", "operator": ">=", "threshold_numeric": 6.5, "unit": "mmol/L"}'::jsonb,
    'EMERGENCY',
    'Immediate emergency medical attention is strongly advised. Severe hyperkalemia carries substantial risk of cardiac arrhythmias.',
    'MediLens AI automated safety flag. Not a replacement for emergency clinical evaluation.'
),
(
    'c0000000-0000-0000-0000-000000000002',
    'Critical Hypoglycemia Alert',
    '{"biomarker_loinc": "1558-6", "operator": "<=", "threshold_numeric": 50.0, "unit": "mg/dL"}'::jsonb,
    'EMERGENCY',
    'Immediate intervention recommended for acute hypoglycemia. Follow hypoglycemia recovery protocols or contact emergency healthcare.',
    'MediLens AI automated safety flag. Not a replacement for emergency clinical evaluation.'
)
ON CONFLICT DO NOTHING;
