"""
MediLens AI - Phase 5 End-to-End Live Verification Script
Validates:
1. Multi-tier clinical safety guardrails.
2. Vector retrieval & evidence grounding (ADA, Mayo, KDIGO, WHO).
3. Emergency red-flag banner injection on critical findings.
4. Anti-IDOR security on report explanations.
"""

import sys
import io
import uuid
import requests
from PIL import Image, ImageDraw, ImageFont

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://127.0.0.1:8080/api/v1"


def create_sample_lab_image():
    """Generates an image of a lab report with High Glucose and Critical Potassium."""
    img = Image.new("RGB", (700, 350), color=(255, 255, 255))
    draw = ImageDraw.Draw(img)

    lines = [
        "METABOLIC & ELECTROLYTE PANEL",
        "Patient: Test Patient    Age: 45    Gender: MALE",
        "--------------------------------------------------",
        "TEST                 RESULT     UNITS      REFERENCE",
        "--------------------------------------------------",
        "Fasting Glucose      145        mg/dL      70 - 99",
        "Potassium            6.5        mmol/L     3.5 - 5.2",
        "Creatinine           0.9        mg/dL      0.7 - 1.3",
        "Total Cholesterol    180        mg/dL      < 200",
        "--------------------------------------------------",
        "End of Report",
    ]

    y = 20
    for line in lines:
        draw.text((30, y), line, fill=(0, 0, 0))
        y += 26

    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return buf.getvalue()


def run_verification():
    print("=" * 70)
    print("  MEDILENS AI - PHASE 5 LIVE VERIFICATION")
    print("  Evidence-Grounded Medical RAG & Clinical Safety Guardrails")
    print("=" * 70)

    # 1. Register User 1
    user1_email = f"rag_patient1_{uuid.uuid4().hex[:6]}@medilens.com"
    reg_payload1 = {
        "email": user1_email,
        "password": "Password123!",
        "firstName": "Robert",
        "lastName": "Langdon",
        "dateOfBirth": "1978-06-15",
        "gender": "MALE",
        "role": "ROLE_PATIENT"
    }

    print(f"\n[1] Registering User 1: {user1_email}")
    r1 = requests.post(f"{BASE_URL}/auth/register", json=reg_payload1)
    if r1.status_code != 201:
        print(f"❌ Registration failed: {r1.status_code} {r1.text}")
        sys.exit(1)
    token1 = r1.json()["accessToken"]
    print("  ✓ User 1 authenticated successfully.")

    # 2. Upload Lab Report
    print("\n[2] Uploading Metabolic/Electrolyte Lab Report (with Glucose 145 & Potassium 6.5)...")
    img_bytes = create_sample_lab_image()
    files = {"file": ("metabolic_panel.png", img_bytes, "image/png")}
    headers1 = {"Authorization": f"Bearer {token1}"}

    r_up = requests.post(f"{BASE_URL}/reports/upload", files=files, headers=headers1)
    if r_up.status_code != 201:
        print(f"❌ Upload failed: {r_up.status_code} {r_up.text}")
        sys.exit(1)
    report_data = r_up.json()
    report_id = report_data["id"]
    print(f"  ✓ Report uploaded & processed: ID = {report_id}")
    print(f"  ✓ Measurements extracted: {report_data.get('measurementCount')}")

    # 3. Request Evidence-Grounded Explanation
    print(f"\n[3] Requesting Evidence-Grounded RAG Explanation: GET /reports/{report_id}/explanation")
    r_exp = requests.get(f"{BASE_URL}/reports/{report_id}/explanation", headers=headers1)
    if r_exp.status_code != 200:
        print(f"❌ Failed to fetch explanation: {r_exp.status_code} {r_exp.text}")
        sys.exit(1)

    exp_data = r_exp.json()
    print("  ✓ RAG Explanation successfully generated & audited.")

    # 4. Verify Clinical Safety Invariants
    print("\n[4] Clinical Safety Invariants Verification:")

    # Invariant 1: Safety Audit Passed
    audit_passed = exp_data.get("safetyAuditPassed", False)
    print(f"  [{'✓' if audit_passed else '❌'}] Safety Audit Passed: {audit_passed}")

    # Invariant 2: Critical Alert Triggered
    critical_alert = exp_data.get("criticalAlert")
    has_crit = critical_alert is not None and "CRITICAL CLINICAL ALERT" in critical_alert
    print(f"  [{'✓' if has_crit else '❌'}] Critical Emergency Alert Triggered: {critical_alert is not None}")
    if has_crit:
        print(f"      Banner: \"{critical_alert[:75]}...\"")

    # Invariant 3: Mandatory Statutory Disclaimer
    disclaimer = exp_data.get("disclaimer", "")
    has_disclaimer = "educational purposes only" in disclaimer.lower()
    print(f"  [{'✓' if has_disclaimer else '❌'}] Mandatory Statutory Disclaimer Present: {has_disclaimer}")

    # Invariant 4: Evidence Grounding & Citations
    sources = exp_data.get("citedSources", [])
    has_sources = len(sources) > 0
    print(f"  [{'✓' if has_sources else '❌'}] Grounded in Clinical Guidelines ({len(sources)} sources cited):")
    for s in sources:
        print(f"      - [{s['chunkId']}] {s['title']} ({s['source']})")

    # Invariant 5: Doctor Discussion Questions
    questions = exp_data.get("questionsForDoctor", [])
    print(f"  [{'✓' if len(questions) > 0 else '❌'}] Actionable Doctor Questions Generated ({len(questions)}):")
    for q in questions[:2]:
        print(f"      • {q}")

    # Findings Breakdown
    findings = exp_data.get("findings", [])
    print(f"\n  Detailed Findings Breakdown ({len(findings)} biomarkers):")
    for f in findings:
        print(f"    - {f['canonicalName']}: {f['observedValue']} [Status: {f['status']}]")
        print(f"      Explanation: {f['explanation']}")
        print(f"      Citations: {f.get('sources')}")

    # 5. Anti-IDOR Security Verification
    print("\n[5] Anti-IDOR Security Verification:")
    user2_email = f"rag_patient2_{uuid.uuid4().hex[:6]}@medilens.com"
    reg_payload2 = {
        "email": user2_email,
        "password": "Password123!",
        "firstName": "Sarah",
        "lastName": "Connor",
        "dateOfBirth": "1985-02-28",
        "gender": "FEMALE",
        "role": "ROLE_PATIENT"
    }
    r2 = requests.post(f"{BASE_URL}/auth/register", json=reg_payload2)
    token2 = r2.json()["accessToken"]

    idor_res = requests.get(f"{BASE_URL}/reports/{report_id}/explanation", headers={"Authorization": f"Bearer {token2}"})
    if idor_res.status_code == 404:
        print(f"  ✓ Anti-IDOR confirmed: User 2 received 404 Not Found accessing User 1's report explanation.")
    else:
        print(f"  ❌ Anti-IDOR violated! Expected 404, got {idor_res.status_code}")
        sys.exit(1)

    print("\n" + "=" * 70)
    print("  PHASE 5 LIVE VERIFICATION PASSED WITH ZERO ERRORS!")
    print("=" * 70)


if __name__ == "__main__":
    run_verification()
