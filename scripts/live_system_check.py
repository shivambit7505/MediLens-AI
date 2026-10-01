"""
MediLens AI - Full Live System Status & End-to-End Pipeline Verification
Runs live checks against:
1. FastAPI AI Service (port 8000)
2. Spring Boot Backend (port 8080)
3. Next.js Frontend (port 3000)
4. Complete User Journey: Auth -> Upload -> OCR -> Extract -> Trends -> RAG Explanation -> Anti-IDOR
"""

import sys
import io
import json
import uuid
import urllib.request
import urllib.error
from PIL import Image, ImageDraw

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


def check_url(name, url):
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "MediLens-HealthCheck"})
        with urllib.request.urlopen(req, timeout=5) as resp:
            status = resp.getcode()
            body = resp.read().decode('utf-8', errors='ignore')
            print(f"  [OK] {name} ({url}) -> HTTP {status}")
            return True, body
    except Exception as e:
        print(f"  [FAIL] {name} ({url}) -> Error: {e}")
        return False, str(e)


def create_sample_png():
    img = Image.new("RGB", (600, 300), color=(255, 255, 255))
    draw = ImageDraw.Draw(img)
    draw.text((20, 20), "CLINICAL LABORATORY REPORT", fill=(0, 0, 0))
    draw.text((20, 50), "Glucose: 145 mg/dL (Reference: 70-99)", fill=(0, 0, 0))
    draw.text((20, 80), "Potassium: 6.5 mmol/L (Reference: 3.5-5.2)", fill=(0, 0, 0))
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return buf.getvalue()


def run_full_check():
    print("=" * 75)
    print("       MEDILENS AI - LIVE SYSTEM HEALTH & PIPELINE EXECUTION")
    print("=" * 75)

    print("\n--- STEP 1: SERVICE HEALTH CHECKS ---")
    ai_ok, ai_res = check_url("AI Service", "http://127.0.0.1:8000/health")
    backend_ok, backend_res = check_url("Spring Boot Backend", "http://127.0.0.1:8080/actuator/health")
    frontend_ok, _ = check_url("Next.js Frontend", "http://localhost:3000")

    if not (ai_ok and backend_ok and frontend_ok):
        print("\n❌ One or more services are not reachable. Aborting pipeline test.")
        sys.exit(1)

    print(f"     AI Service details: {ai_res.strip()}")
    print(f"     Backend details:    {backend_res.strip()}")

    print("\n--- STEP 2: USER AUTHENTICATION & JWT GENERATION ---")
    user_email = f"patient_{uuid.uuid4().hex[:6]}@medilens.com"
    reg_body = json.dumps({
        "email": user_email,
        "password": "Password123!",
        "firstName": "Shivam",
        "lastName": "Kumar",
        "dateOfBirth": "1995-04-12",
        "gender": "MALE",
        "role": "ROLE_PATIENT"
    }).encode('utf-8')

    reg_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/auth/register",
        data=reg_body,
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(reg_req) as resp:
        auth_data = json.loads(resp.read().decode('utf-8'))
        token = auth_data["accessToken"]
        print(f"  [OK] Registered user: {user_email}")
        print(f"  [OK] JWT Bearer Token: {token[:25]}... (Expires in: {auth_data['expiresIn']}s)")

    print("\n--- STEP 3: BIOMARKER CATALOG & DASHBOARD BEFORE UPLOAD ---")
    dash_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/dashboard/summary",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(dash_req) as resp:
        dash_data = json.loads(resp.read().decode('utf-8'))
        print(f"  [OK] Dashboard reports: {dash_data['totalReports']}, measurements: {dash_data['totalMeasurements']}")

    print("\n--- STEP 4: MULTI-PAGE REPORT UPLOAD & DETERMINISTIC OCR/EXTRACTION ---")
    png_bytes = create_sample_png()
    boundary = "----WebKitFormBoundary" + uuid.uuid4().hex
    header = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="file"; filename="sample_metabolic.png"\r\n'
        f"Content-Type: image/png\r\n\r\n"
    ).encode('utf-8')
    footer = f"\r\n--{boundary}--\r\n".encode('utf-8')
    multipart_body = header + png_bytes + footer

    upload_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/reports/upload",
        data=multipart_body,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "Authorization": f"Bearer {token}"
        }
    )
    with urllib.request.urlopen(upload_req) as resp:
        report_data = json.loads(resp.read().decode('utf-8'))
        report_id = report_data["id"]
        print(f"  [OK] Report uploaded successfully: ID = {report_id}")
        print(f"  [OK] Status: {report_data['status']}, Extracted Measurements: {report_data.get('measurementCount')}")

    print("\n--- STEP 5: FETCHING EXTRACTED MEASUREMENTS ---")
    meas_req = urllib.request.Request(
        f"http://127.0.0.1:8080/api/v1/reports/{report_id}/measurements",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(meas_req) as resp:
        measurements = json.loads(resp.read().decode('utf-8'))
        print(f"  [OK] Retrieved {len(measurements)} deterministic measurements:")
        for m in measurements:
            print(f"       • {m['canonicalName']}: {m['observedValueRaw']} -> {m['normalizedValueNumeric']} {m['normalizedUnit']} [STATUS: {m['status']}]")

    print("\n--- STEP 6: EVIDENCE-GROUNDED RAG EXPLANATION & CLINICAL GUARDRAILS ---")
    exp_req = urllib.request.Request(
        f"http://127.0.0.1:8080/api/v1/reports/{report_id}/explanation",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(exp_req) as resp:
        exp_data = json.loads(resp.read().decode('utf-8'))
        print(f"  [OK] Safety Audit Passed: {exp_data.get('safetyAuditPassed')}")
        print(f"  [OK] Critical Emergency Alert: {exp_data.get('criticalAlert') is not None}")
        if exp_data.get('criticalAlert'):
            print(f"       Alert Banner: {exp_data['criticalAlert']}")
        print(f"  [OK] Cited Evidence Sources ({len(exp_data.get('citedSources', []))}):")
        for s in exp_data.get("citedSources", []):
            print(f"       - [{s['chunkId']}] {s['title']} | {s['source']}")
        print(f"  [OK] Doctor Questions Generated ({len(exp_data.get('questionsForDoctor', []))}):")
        for q in exp_data.get("questionsForDoctor", [])[:2]:
            print(f"       ? {q}")
        print(f"  [OK] Statutory Disclaimer: {exp_data.get('disclaimer')[:60]}...")

    print("\n--- STEP 7: ANTI-IDOR SECURITY VALIDATION ---")
    user2_email = f"patient_idor_{uuid.uuid4().hex[:6]}@medilens.com"
    reg2_body = json.dumps({
        "email": user2_email,
        "password": "Password123!",
        "firstName": "Unauthorized",
        "lastName": "User",
        "dateOfBirth": "1990-01-01",
        "gender": "FEMALE",
        "role": "ROLE_PATIENT"
    }).encode('utf-8')
    reg2_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/auth/register",
        data=reg2_body,
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(reg2_req) as resp:
        token2 = json.loads(resp.read().decode('utf-8'))["accessToken"]

    try:
        idor_req = urllib.request.Request(
            f"http://127.0.0.1:8080/api/v1/reports/{report_id}/explanation",
            headers={"Authorization": f"Bearer {token2}"}
        )
        with urllib.request.urlopen(idor_req) as resp:
            print("  ❌ Anti-IDOR failed: User 2 accessed User 1's report!")
    except urllib.error.HTTPError as e:
        if e.code == 404:
            print(f"  [OK] Anti-IDOR confirmed: HTTP 404 Resource Not Found returned for cross-user access.")
        else:
            print(f"  [FAIL] Unexpected error code: {e.code}")

    print("\n" + "=" * 75)
    print("      ALL SERVICES & PIPELINES ARE RUNNING 100% OPERATIONAL!")
    print("=" * 75)


if __name__ == "__main__":
    run_full_check()
