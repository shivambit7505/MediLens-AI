"""
Generates clean, realistic synthetic clinical laboratory reports for testing MediLens AI.
Outputs both PNG and PDF versions into sample_reports/
"""

import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "sample_reports")
os.makedirs(OUTPUT_DIR, exist_ok=True)


def draw_report(title: str, patient_info: dict, tests: list) -> Image.Image:
    width, height = 800, 1050
    img = Image.new("RGB", (width, height), color=(255, 255, 255))
    draw = ImageDraw.Draw(img)

    # Header border / banner
    draw.rectangle([(30, 30), (770, 110)], fill=(240, 245, 250), outline=(70, 130, 180), width=2)
    draw.text((50, 45), "METROPOLITAN CLINICAL LABORATORIES", fill=(20, 50, 100))
    draw.text((50, 75), f"DIAGNOSTIC REPORT: {title.upper()}", fill=(30, 30, 30))

    # Patient info box
    draw.rectangle([(30, 125), (770, 205)], fill=(250, 250, 250), outline=(200, 200, 200), width=1)
    draw.text((50, 135), f"Patient: {patient_info.get('name', 'Shivam Kumar')}", fill=(40, 40, 40))
    draw.text((400, 135), f"Patient ID: {patient_info.get('id', 'MED-99214')}", fill=(40, 40, 40))
    draw.text((50, 160), f"Age / Gender: {patient_info.get('demographics', '30 Y / Male')}", fill=(40, 40, 40))
    draw.text((400, 160), f"Date of Collection: {patient_info.get('date', '2026-10-08')}", fill=(40, 40, 40))
    draw.text((50, 185), f"Ordering Clinician: Dr. Sarah Jenkins, MD", fill=(40, 40, 40))

    # Table Header
    y = 230
    draw.rectangle([(30, y), (770, y + 30)], fill=(70, 130, 180))
    draw.text((45, y + 8), "TEST NAME", fill=(255, 255, 255))
    draw.text((280, y + 8), "RESULT", fill=(255, 255, 255))
    draw.text((400, y + 8), "UNITS", fill=(255, 255, 255))
    draw.text((520, y + 8), "REFERENCE INTERVAL", fill=(255, 255, 255))
    draw.text((700, y + 8), "STATUS", fill=(255, 255, 255))

    y += 40
    # Rows
    for item in tests:
        # alternating background
        draw.rectangle([(30, y - 5), (770, y + 25)], fill=(248, 249, 250) if (y // 35) % 2 == 0 else (255, 255, 255))
        
        status = item.get("status", "NORMAL")
        status_color = (0, 128, 0) if status == "NORMAL" else ((220, 20, 60) if status in ("HIGH", "CRITICAL") else (200, 100, 0))

        draw.text((45, y), item["name"], fill=(30, 30, 30))
        draw.text((280, y), str(item["value"]), fill=(0, 0, 0))
        draw.text((400, y), item["unit"], fill=(80, 80, 80))
        draw.text((520, y), item["reference"], fill=(80, 80, 80))
        draw.text((700, y), status, fill=status_color)
        y += 35

    # Footer Disclaimer
    draw.line([(30, 950), (770, 950)], fill=(200, 200, 200), width=1)
    draw.text((40, 960), "CLINICAL INTERPRETATION NOTE:", fill=(100, 100, 100))
    draw.text((40, 980), "This diagnostic analysis is certified by Metropolitan Clinical Laboratories.", fill=(120, 120, 120))
    draw.text((40, 1000), "Confirmatory clinical evaluation by an attending physician is mandatory.", fill=(120, 120, 120))

    return img


def generate_all():
    reports = [
        {
            "filename": "metabolic_panel",
            "title": "Comprehensive Metabolic Panel (CMP)",
            "patient": {"name": "Shivam Kumar", "id": "MED-08241", "demographics": "30 Y / Male", "date": "2026-10-08"},
            "tests": [
                {"name": "Glucose", "value": "145", "unit": "mg/dL", "reference": "70 - 99", "status": "HIGH"},
                {"name": "Potassium", "value": "6.6", "unit": "mmol/L", "reference": "3.5 - 5.2", "status": "CRITICAL"},
                {"name": "Sodium", "value": "140", "unit": "mmol/L", "reference": "135 - 145", "status": "NORMAL"},
                {"name": "Calcium", "value": "9.5", "unit": "mg/dL", "reference": "8.5 - 10.2", "status": "NORMAL"},
                {"name": "Creatinine", "value": "1.1", "unit": "mg/dL", "reference": "0.7 - 1.3", "status": "NORMAL"},
                {"name": "Blood Urea Nitrogen", "value": "18", "unit": "mg/dL", "reference": "7 - 20", "status": "NORMAL"},
            ]
        },
        {
            "filename": "lipid_profile",
            "title": "Lipid Profile & Cardiovascular Risk Panel",
            "patient": {"name": "Shivam Kumar", "id": "MED-08242", "demographics": "30 Y / Male", "date": "2026-10-08"},
            "tests": [
                {"name": "Total Cholesterol", "value": "245", "unit": "mg/dL", "reference": "125 - 200", "status": "HIGH"},
                {"name": "HDL Cholesterol", "value": "42", "unit": "mg/dL", "reference": "40 - 60", "status": "NORMAL"},
                {"name": "LDL Cholesterol", "value": "165", "unit": "mg/dL", "reference": "50 - 100", "status": "HIGH"},
                {"name": "Triglycerides", "value": "190", "unit": "mg/dL", "reference": "50 - 150", "status": "HIGH"},
            ]
        },
        {
            "filename": "complete_blood_count",
            "title": "Complete Blood Count (CBC) with Differential",
            "patient": {"name": "Shivam Kumar", "id": "MED-08243", "demographics": "30 Y / Male", "date": "2026-10-08"},
            "tests": [
                {"name": "Hemoglobin", "value": "14.5", "unit": "g/dL", "reference": "13.8 - 17.2", "status": "NORMAL"},
                {"name": "White Blood Cell", "value": "7.2", "unit": "10^3/uL", "reference": "4.5 - 11.0", "status": "NORMAL"},
                {"name": "Platelets", "value": "250", "unit": "10^3/uL", "reference": "150 - 450", "status": "NORMAL"},
                {"name": "Hematocrit", "value": "44.0", "unit": "%", "reference": "40.0 - 52.0", "status": "NORMAL"},
            ]
        }
    ]

    for rep in reports:
        img = draw_report(rep["title"], rep["patient"], rep["tests"])
        png_path = os.path.join(OUTPUT_DIR, f"{rep['filename']}.png")
        pdf_path = os.path.join(OUTPUT_DIR, f"{rep['filename']}.pdf")
        img.save(png_path, "PNG")
        img.save(pdf_path, "PDF")
        print(f"Generated: {png_path} and {pdf_path}")


if __name__ == "__main__":
    generate_all()
