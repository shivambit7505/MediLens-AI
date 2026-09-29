import urllib.request
import json
import uuid
import io
import time
from PIL import Image

def run_verification():
    print("--- 1. Checking Service Health ---")
    time.sleep(2)
    with urllib.request.urlopen("http://127.0.0.1:8080/actuator/health") as resp:
        health_data = json.loads(resp.read().decode("utf-8"))
        print(f"Backend Health: {resp.getcode()} -> {health_data['status']}")

    print("\n--- 2. Registering Patient ---")
    user_email = f"patient_phase4_{uuid.uuid4().hex[:8]}@medilens.ai"
    reg_payload = json.dumps({
        "email": user_email,
        "password": "SecurePassword123!",
        "firstName": "Robert",
        "lastName": "Chen",
        "dateOfBirth": "1975-08-22",
        "gender": "MALE",
        "role": "ROLE_PATIENT"
    }).encode("utf-8")

    req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/auth/register",
        data=reg_payload,
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req) as resp:
        auth_data = json.loads(resp.read().decode("utf-8"))
        token = auth_data["accessToken"]
        print(f"User registered. Token: {token[:20]}...")

    print("\n--- 3. Testing Biomarker Catalog Listing ---")
    bio_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/biomarkers",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(bio_req) as resp:
        bio_list = json.loads(resp.read().decode("utf-8"))
        print(f"Biomarkers in catalog: {len(bio_list)}")
        first_bio = bio_list[0] if bio_list else None
        if first_bio:
            print(f"First biomarker: {first_bio['canonicalName']} ({first_bio['standardUnit']})")

    print("\n--- 4. Testing Patient Dashboard Summary ---")
    dash_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/dashboard/summary",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(dash_req) as resp:
        dash_data = json.loads(resp.read().decode("utf-8"))
        print(f"Dashboard total reports: {dash_data['totalReports']}")
        print(f"Dashboard total measurements: {dash_data['totalMeasurements']}")
        print(f"Dashboard abnormal findings: {dash_data['abnormalCount']}")
        print(f"Dashboard critical alerts: {dash_data['criticalCount']}")

    print("\n=== ALL PHASE 4 LIVE ENDPOINTS VERIFIED WITH ZERO ERRORS ===")

if __name__ == "__main__":
    run_verification()
