import urllib.request
import json
import uuid
import io
from PIL import Image

def run_verification():
    print("--- 1. Registering test user ---")
    user_email = f"live_{uuid.uuid4().hex[:8]}@medilens.ai"
    reg_payload = json.dumps({
        "email": user_email,
        "password": "SecurePassword123!",
        "firstName": "Clinical",
        "lastName": "Tester",
        "dateOfBirth": "1988-06-15",
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

    print("\n--- 2. Generating test PNG lab report ---")
    img_byte_arr = io.BytesIO()
    Image.new("RGB", (200, 200), color="white").save(img_byte_arr, format="PNG")
    png_bytes = img_byte_arr.getvalue()

    print("\n--- 3. Uploading report to /api/v1/reports/upload ---")
    boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
    part_header = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="file"; filename="cbc_sample.png"\r\n'
        f"Content-Type: image/png\r\n\r\n"
    ).encode("utf-8")
    part_footer = f"\r\n--{boundary}--\r\n".encode("utf-8")
    body = part_header + png_bytes + part_footer

    upload_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/reports/upload",
        data=body,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "Authorization": f"Bearer {token}"
        }
    )
    with urllib.request.urlopen(upload_req) as resp:
        report_data = json.loads(resp.read().decode("utf-8"))
        report_id = report_data["id"]
        print(f"Report uploaded successfully! ID: {report_id}")
        print(f"Status: {report_data['status']}, PageCount: {report_data['pageCount']}")

    print("\n--- 4. Querying /api/v1/reports/{id} details ---")
    detail_req = urllib.request.Request(
        f"http://127.0.0.1:8080/api/v1/reports/{report_id}",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(detail_req) as resp:
        detail_data = json.loads(resp.read().decode("utf-8"))
        print(f"Report Status: {detail_data['status']}")
        print(f"Pages recorded: {len(detail_data['pages'])}")
        print(f"Measurements recorded: {len(detail_data['measurements'])}")

    print("\n--- 5. Querying /api/v1/reports list ---")
    list_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/reports",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(list_req) as resp:
        list_data = json.loads(resp.read().decode("utf-8"))
        print(f"Reports list count: {list_data['totalElements']}")

    print("\n--- 6. Streaming page image from /api/v1/reports/{id}/pages/1/image ---")
    img_req = urllib.request.Request(
        f"http://127.0.0.1:8080/api/v1/reports/{report_id}/pages/1/image",
        headers={"Authorization": f"Bearer {token}"}
    )
    with urllib.request.urlopen(img_req) as resp:
        content_type = resp.headers.get("Content-Type")
        length = len(resp.read())
        print(f"Image streamed. HTTP: {resp.getcode()}, Content-Type: {content_type}, Size: {length} bytes")

    print("\n--- 7. Testing Deduplication (Uploading identical file again) ---")
    dup_req = urllib.request.Request(
        "http://127.0.0.1:8080/api/v1/reports/upload",
        data=body,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "Authorization": f"Bearer {token}"
        }
    )
    with urllib.request.urlopen(dup_req) as resp:
        dup_data = json.loads(resp.read().decode("utf-8"))
        assert dup_data["id"] == report_id, "Deduplication failed: returned different report ID"
        print(f"Deduplication SUCCESS: identical report returned immediately with ID: {dup_data['id']}")

    print("\n=== ALL PHASE 3 LIVE VERIFICATIONS PASSED WITH ZERO ERRORS ===")

if __name__ == "__main__":
    run_verification()
