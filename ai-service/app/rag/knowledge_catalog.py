"""
MediLens AI - Pre-Curated Clinical Knowledge Catalog
Verified medical guideline chunks from ADA 2024, Mayo Clinic, WHO, KDIGO, AHA, and ACG.
Used exclusively for evidence grounding in educational explanations.
"""

from typing import List, Dict, Any

CLINICAL_KNOWLEDGE_BASE: List[Dict[str, Any]] = [
    {
        "chunk_id": "ADA-2024-GLUCOSE",
        "title": "Fasting Plasma Glucose & Glycemic Targets",
        "source": "American Diabetes Association (ADA) Standards of Care in Diabetes 2024",
        "category": "METABOLIC",
        "biomarkers": ["glucose", "fasting glucose", "blood sugar", "fpg"],
        "summary": "Fasting plasma glucose normal range is typically 70-99 mg/dL. Values between 100-125 mg/dL reflect impaired fasting glucose (prediabetes), while 126 mg/dL or higher on repeat testing indicates diabetes criteria.",
        "clinical_significance": "Elevated fasting blood sugar suggests insulin resistance or impaired pancreatic beta-cell insulin secretion. Low levels (< 70 mg/dL) indicate hypoglycemia, which can cause dizziness, sweating, or cognitive impairment. Severe hypoglycemia (< 54 mg/dL) requires immediate carbohydrate intake or glucagon.",
        "lifestyle_and_followup": "Dietary changes emphasizing low-glycemic Mediterranean or DASH dietary patterns, regular aerobic and resistance exercise, and consultation with a primary care clinician or endocrinologist.",
        "critical_alert_guidance": "Severe hypoglycemia (< 50 mg/dL) or profound hyperglycemia (> 350 mg/dL with symptoms) warrants prompt emergency clinical assessment for risk of ketoacidosis or hyperosmolar state.",
        "tags": ["glucose", "diabetes", "metabolic", "prediabetes", "hypoglycemia", "hyperglycemia"]
    },
    {
        "chunk_id": "ADA-2024-HBA1C",
        "title": "Glycated Hemoglobin (HbA1c) Diagnostic Criteria",
        "source": "American Diabetes Association (ADA) Standards of Care in Diabetes 2024",
        "category": "METABOLIC",
        "biomarkers": ["hba1c", "glycated hemoglobin", "a1c"],
        "summary": "HbA1c reflects average blood glucose levels over the prior 2 to 3 months. Normal HbA1c is below 5.7%. Prediabetes is defined as 5.7% to 6.4%, and diabetes is confirmed at 6.5% or greater on standard laboratory assay.",
        "clinical_significance": "HbA1c provides a reliable retrospective view of glycemic exposure without daily fasting fluctuations. Chronically elevated levels increase long-term microvascular and macrovascular risk.",
        "lifestyle_and_followup": "Lifestyle modification targeting 7% weight loss and 150 minutes per week of physical activity reduces progression risk. Discuss personalized target A1c (typically < 7% for non-pregnant adults) with a physician.",
        "critical_alert_guidance": "HbA1c exceeding 10.0% signals severely unmanaged hyperglycemia requiring prompt pharmacotherapy adjustment.",
        "tags": ["hba1c", "a1c", "glycemic control", "diabetes", "endocrine"]
    },
    {
        "chunk_id": "MAYO-ELECTROLYTES-POTASSIUM",
        "title": "Serum Potassium Homeostasis and Critical Limits",
        "source": "Mayo Clinic Laboratories Clinical Reference Guide - Electrolytes",
        "category": "ELECTROLYTE",
        "biomarkers": ["potassium", "serum potassium", "k+"],
        "summary": "Normal adult serum potassium is 3.5 to 5.2 mEq/L (or mmol/L). Potassium is the primary intracellular cation critical for cardiac conduction, neuromuscular excitability, and acid-base balance.",
        "clinical_significance": "Hypokalemia (< 3.5 mEq/L) can precipitate muscle weakness, fatigue, cramps, and ventricular arrhythmias. Hyperkalemia (> 5.2 mEq/L) may lead to cardiac conduction abnormalities, peaked T waves, bradycardia, and life-threatening ventricular fibrillation.",
        "lifestyle_and_followup": "Potassium deviations necessitate clinical review of medication regimens (such as ACE inhibitors, ARBs, diuretics) and renal function tests. Mild cases are adjusted via dietary modification or oral supplementation.",
        "critical_alert_guidance": "CRITICAL EMERGENCY: Potassium < 2.8 mEq/L or > 6.0 mEq/L poses imminent risk of lethal cardiac dysrhythmias and warrants immediate emergency department evaluation and continuous ECG monitoring.",
        "tags": ["potassium", "electrolyte", "cardiac", "arrhythmia", "hyperkalemia", "hypokalemia"]
    },
    {
        "chunk_id": "MAYO-ELECTROLYTES-SODIUM",
        "title": "Serum Sodium & Osmotic Equilibrium",
        "source": "Mayo Clinic Laboratories Clinical Reference Guide - Electrolytes",
        "category": "ELECTROLYTE",
        "biomarkers": ["sodium", "serum sodium", "na+"],
        "summary": "Normal serum sodium ranges from 135 to 145 mEq/L (or mmol/L). Sodium is the chief extracellular cation governing plasma osmolality and fluid volume distribution.",
        "clinical_significance": "Hyponatremia (< 135 mEq/L) can cause headache, nausea, confusion, and in severe cases cerebral edema and seizures. Hypernatremia (> 145 mEq/L) reflects relative water deficit or excessive salt intake.",
        "lifestyle_and_followup": "Evaluation of fluid balance, diuretic therapy, and adrenal or pituitary status. Rapid correction of chronic sodium disturbances must be avoided to prevent neurological osmotic demyelination.",
        "critical_alert_guidance": "Severe sodium anomalies (< 120 mEq/L or > 160 mEq/L) constitute medical emergencies requiring controlled hospitalization.",
        "tags": ["sodium", "electrolyte", "hydration", "fluid balance", "hyponatremia"]
    },
    {
        "chunk_id": "KDIGO-2024-RENAL",
        "title": "Serum Creatinine and Kidney Function Assessment",
        "source": "KDIGO 2024 Clinical Practice Guideline for the Evaluation and Management of Chronic Kidney Disease",
        "category": "RENAL",
        "biomarkers": ["creatinine", "serum creatinine", "egfr", "blood urea nitrogen", "bun"],
        "summary": "Serum creatinine reference ranges are approximately 0.7-1.3 mg/dL for adult males and 0.5-1.1 mg/dL for adult females. Estimated glomerular filtration rate (eGFR) calculated from creatinine reflects kidney filtration capacity.",
        "clinical_significance": "Elevated creatinine or decreased eGFR (< 60 mL/min/1.73m²) suggests acute kidney injury (AKI) or chronic kidney disease (CKD). Mild fluctuations can also occur with intense exercise, high meat intake, dehydration, or certain medications.",
        "lifestyle_and_followup": "Adequate hydration, avoidance of nephrotoxic agents (such as frequent high-dose NSAIDs), blood pressure control, and follow-up urinalysis for albuminuria.",
        "critical_alert_guidance": "Acute sudden doubling of creatinine or levels > 4.0 mg/dL accompanied by oliguria (low urine output) requires urgent nephrology evaluation.",
        "tags": ["creatinine", "kidney", "renal", "egfr", "bun", "ckd"]
    },
    {
        "chunk_id": "AHA-ACC-LIPIDS",
        "title": "Cholesterol, Triglycerides and Atherosclerotic Cardiovascular Risk",
        "source": "AHA/ACC/Multisociety Guideline on the Management of Blood Cholesterol",
        "category": "LIPID",
        "biomarkers": ["cholesterol", "total cholesterol", "ldl", "hdl", "triglycerides", "vldl"],
        "summary": "Desirable total cholesterol is under 200 mg/dL. Optimal LDL cholesterol is below 100 mg/dL (or < 70 mg/dL for high cardiovascular risk individuals). HDL cholesterol is protective above 40 mg/dL (men) and 50 mg/dL (women). Triglycerides should ideally remain below 150 mg/dL.",
        "clinical_significance": "Elevated LDL ('bad' cholesterol) and triglycerides contribute directly to atheroma formation, coronary artery disease, and ischemic stroke. High HDL exerts reverse cholesterol transport and is cardioprotective.",
        "lifestyle_and_followup": "Reduction in dietary saturated and trans fats, increased soluble fiber, regular cardiovascular exercise, smoking cessation, and physician calculation of 10-year ASCVD risk score.",
        "critical_alert_guidance": "Triglycerides > 500-1000 mg/dL represent acute pancreatitis risk and necessitate urgent lipid-lowering medical therapy.",
        "tags": ["cholesterol", "lipid", "cardiovascular", "heart", "ldl", "hdl", "triglycerides"]
    },
    {
        "chunk_id": "WHO-CBC-HEMOGLOBIN",
        "title": "Hemoglobin, Hematocrit and Anemia Diagnosis",
        "source": "World Health Organization (WHO) Nutritional Anemias Technical Report",
        "category": "HEMATOLOGY",
        "biomarkers": ["hemoglobin", "hgb", "hematocrit", "hct", "red blood cell count", "rbc"],
        "summary": "Normal hemoglobin thresholds are 13.8 to 17.2 g/dL for adult males and 12.1 to 15.1 g/dL for adult females. Hemoglobin carries oxygen from the lungs to peripheral tissues via erythrocytes.",
        "clinical_significance": "Low hemoglobin and hematocrit define anemia, frequently stemming from iron deficiency, vitamin B12/folate deficiency, chronic kidney disease, or blood loss. High levels (erythrocytosis/polycythemia) may result from chronic hypoxia, smoking, high altitude, or myeloproliferative disorders.",
        "lifestyle_and_followup": "Investigate potential iron store depletion (ferritin), dietary intake, occult gastrointestinal bleeding, or heavy menses. Discuss iron or vitamin repletion with a clinician.",
        "critical_alert_guidance": "Hemoglobin < 7.0 g/dL is critically low and frequently requires immediate blood transfusion support in symptomatic patients.",
        "tags": ["hemoglobin", "anemia", "hematocrit", "rbc", "blood", "iron"]
    },
    {
        "chunk_id": "MAYO-CBC-WBC",
        "title": "White Blood Cell Count and Immune Response",
        "source": "Mayo Clinic Laboratories Hematology Clinical Reference Guide",
        "category": "HEMATOLOGY",
        "biomarkers": ["white blood cell count", "wbc", "leukocytes", "neutrophils", "lymphocytes"],
        "summary": "Normal total leukocyte count ranges between 4,500 and 11,000 cells/mcL (or 4.5 - 11.0 x10^9/L). White blood cells mediate immune defense against infections and inflammation.",
        "clinical_significance": "Leukocytosis (> 11,000 /mcL) is commonly seen with bacterial infections, physiological stress, systemic inflammation, tissue necrosis, or hematologic malignancies. Leukopenia (< 4,000 /mcL) impairs infection defense and can be caused by viral suppression, bone marrow suppression, or autoimmune conditions.",
        "lifestyle_and_followup": "Clinical review for symptoms of active infection (fever, chills, cough, dysuria), medication review, and repeat differential count if persistent.",
        "critical_alert_guidance": "Severe neutropenia (Absolute Neutrophil Count < 500 /mcL) or WBC < 1,500 /mcL leaves the patient at imminent risk for life-threatening sepsis and demands emergency medical evaluation.",
        "tags": ["wbc", "white blood cells", "infection", "immunity", "leukocytosis", "neutrophils"]
    },
    {
        "chunk_id": "MAYO-CBC-PLATELETS",
        "title": "Platelet Count and Hemostatic Function",
        "source": "Mayo Clinic Laboratories Hematology Reference Guide",
        "category": "HEMATOLOGY",
        "biomarkers": ["platelets", "platelet count", "plt"],
        "summary": "Normal platelet count spans 150,000 to 450,000 /mcL (150 - 450 x10^9/L). Platelets are essential cellular fragments that form primary hemostatic plugs during vascular injury.",
        "clinical_significance": "Thrombocytopenia (< 150,000 /mcL) increases risk of bruising, petechiae, and spontaneous hemorrhage. Thrombocytosis (> 450,000 /mcL) may arise reactively from acute inflammation, iron deficiency, or clonal essential thrombocythemia.",
        "lifestyle_and_followup": "Evaluate for antiplatelet medication use, signs of bleeding, liver disease, or viral syndromes. Confirm with peripheral blood smear examination to rule out pseudothrombocytopenia from EDTA clumping.",
        "critical_alert_guidance": "Platelets < 20,000 /mcL represent an extreme risk for spontaneous intracranial or internal hemorrhage, requiring immediate hematology consultation or platelet transfusion.",
        "tags": ["platelets", "clotting", "thrombocytopenia", "bleeding", "hemostasis"]
    },
    {
        "chunk_id": "ACG-LIVER-ENZYMES",
        "title": "Hepatic Transaminases (ALT, AST) and Liver Function",
        "source": "American College of Gastroenterology (ACG) Clinical Guideline: Evaluation of Abnormal Liver Chemistries",
        "category": "HEPATIC",
        "biomarkers": ["alt", "alanine aminotransferase", "ast", "aspartate aminotransferase", "sgpt", "sgot", "bilirubin", "alp", "alkaline phosphatase"],
        "summary": "ALT and AST are intracellular hepatic enzymes. Upper normal limits are generally 29-33 IU/L for males and 19-25 IU/L for females for ALT. AST normal range is approximately 8 to 48 U/L.",
        "clinical_significance": "Elevated transaminases signify hepatocellular injury. Common etiologies include non-alcoholic fatty liver disease (MASLD), alcohol-related liver injury, viral hepatitis, medications (e.g. statins, acetaminophen), and herbal supplements.",
        "lifestyle_and_followup": "Avoidance of hepatotoxins and alcohol, review of prescription/OTC medications, metabolic optimization, and hepatic ultrasound if elevations persist for > 3 months.",
        "critical_alert_guidance": "Acute transaminase elevations > 1000 IU/L indicate acute liver injury or ischemia requiring emergent emergency room evaluation.",
        "tags": ["liver", "alt", "ast", "transaminases", "hepatic", "bilirubin"]
    },
    {
        "chunk_id": "ATA-2023-THYROID",
        "title": "Thyroid Stimulating Hormone (TSH) and Free T4",
        "source": "American Thyroid Association (ATA) Guidelines on Thyroid Function Evaluation",
        "category": "ENDOCRINE",
        "biomarkers": ["tsh", "thyroid stimulating hormone", "free t4", "thyroxine", "free t3"],
        "summary": "Normal adult serum TSH is generally 0.45 to 4.50 mIU/L, and Free T4 is 0.8 to 1.8 ng/dL. TSH is the primary screening indicator of hypothalamic-pituitary-thyroid axis equilibrium.",
        "clinical_significance": "Elevated TSH with low Free T4 indicates primary hypothyroidism (sluggish metabolism, fatigue, cold intolerance, weight gain). Suppressed TSH (< 0.1 mIU/L) with high Free T4 indicates hyperthyroidism (tremor, palpitations, heat intolerance, weight loss).",
        "lifestyle_and_followup": "Endocrine evaluation, screening for anti-TPO thyroid antibodies, and titration of levothyroxine or antithyroid medications under clinical guidance.",
        "critical_alert_guidance": "Extremely low TSH with marked tachycardia or fever may herald thyroid storm, requiring emergency medical intervention.",
        "tags": ["thyroid", "tsh", "free t4", "hypothyroidism", "hyperthyroidism", "endocrine"]
    },
    {
        "chunk_id": "MAYO-MINERALS-CALCIUM",
        "title": "Serum Calcium and Parathyroid Mineral Balance",
        "source": "Mayo Clinic Laboratories Mineral Metabolism Reference Guide",
        "category": "MINERAL",
        "biomarkers": ["calcium", "serum calcium", "ca++"],
        "summary": "Total serum calcium normal range is 8.5 to 10.5 mg/dL. Calcium is vital for neuromuscular transmission, myocardial excitation-contraction coupling, and skeletal mineralization.",
        "clinical_significance": "Hypercalcemia (> 10.5 mg/dL) frequently stems from primary hyperparathyroidism or malignancy ('stones, bones, abdominal groans, psychiatric moans'). Hypocalcemia (< 8.5 mg/dL) causes paresthesias, tetany, and QT prolongation.",
        "lifestyle_and_followup": "Assess ionized calcium, serum albumin (correct total calcium for low albumin), PTH, and vitamin D levels. Ensure adequate hydration.",
        "critical_alert_guidance": "Severe hypercalcemia (> 13.0 mg/dL) or symptomatic severe hypocalcemia (< 6.5 mg/dL) are critical medical emergencies requiring intravenous therapy and hospital telemetry.",
        "tags": ["calcium", "bone", "parathyroid", "hypercalcemia", "hypocalcemia", "minerals"]
    }
]
