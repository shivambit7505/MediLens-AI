package com.medilens.service;

import com.medilens.model.*;
import com.medilens.repository.DrugInteractionRepository;
import com.medilens.repository.MedicationRepository;
import com.medilens.repository.TriageRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class ClinicalDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClinicalDataInitializer.class);

    private final TriageRuleRepository triageRuleRepository;
    private final MedicationRepository medicationRepository;
    private final DrugInteractionRepository drugInteractionRepository;

    public ClinicalDataInitializer(TriageRuleRepository triageRuleRepository,
                                   MedicationRepository medicationRepository,
                                   DrugInteractionRepository drugInteractionRepository) {
        this.triageRuleRepository = triageRuleRepository;
        this.medicationRepository = medicationRepository;
        this.drugInteractionRepository = drugInteractionRepository;
    }

    @Override
    public void run(String... args) {
        seedTriageRules();
        seedMedicationsAndInteractions();
    }

    private void seedTriageRules() {
        if (triageRuleRepository.count() > 0) {
            log.info("Triage rules already present (count={}). Skipping seed.", triageRuleRepository.count());
            return;
        }

        log.info("Seeding deterministic clinical triage rules...");

        triageRuleRepository.save(new TriageRule(
                null,
                "Critical Hyperkalemia Alert",
                "Serum Potassium",
                ">=",
                6.5,
                "mmol/L",
                TriageUrgency.EMERGENCY,
                "IMMEDIATE EMERGENCY: Severe hyperkalemia carries extreme risk of lethal cardiac arrhythmias and ventricular fibrillation. Proceed to an emergency department immediately.",
                "MediLens AI deterministic safety rule. ADA & AHA Critical Values Protocol.",
                true,
                Instant.now()
        ));

        triageRuleRepository.save(new TriageRule(
                null,
                "Severe Acute Hypoglycemia Alert",
                "Fasting Blood Glucose",
                "<=",
                50.0,
                "mg/dL",
                TriageUrgency.EMERGENCY,
                "IMMEDIATE EMERGENCY: Severe neuroglycopenia can result in seizures or loss of consciousness. Administer rapid-acting oral glucose (15-20g) if conscious, and seek emergency care.",
                "MediLens AI deterministic safety rule. ADA 2024 Standards of Care.",
                true,
                Instant.now()
        ));

        triageRuleRepository.save(new TriageRule(
                null,
                "Critical Hyperglycemic Emergency",
                "Fasting Blood Glucose",
                ">=",
                400.0,
                "mg/dL",
                TriageUrgency.EMERGENCY,
                "IMMEDIATE EMERGENCY: Risk of Diabetic Ketoacidosis (DKA) or Hyperosmolar Hyperglycemic State (HHS). Immediate emergency clinical evaluation required.",
                "MediLens AI deterministic safety rule. ADA 2024 Standards of Care.",
                true,
                Instant.now()
        ));

        triageRuleRepository.save(new TriageRule(
                null,
                "Severe Critical Anemia",
                "Hemoglobin",
                "<",
                7.0,
                "g/dL",
                TriageUrgency.EMERGENCY,
                "IMMEDIATE EMERGENCY: Profound anemia with severe tissue hypoxia. Immediate emergency evaluation and possible red blood cell transfusion indicated.",
                "MediLens AI deterministic safety rule. WHO Clinical Hematology Guidelines.",
                true,
                Instant.now()
        ));

        triageRuleRepository.save(new TriageRule(
                null,
                "Spontaneous Bleed Risk Thrombocytopenia",
                "Platelet Count",
                "<",
                20.0,
                "10^3/uL",
                TriageUrgency.EMERGENCY,
                "IMMEDIATE EMERGENCY: Severe thrombocytopenia carries high risk of spontaneous mucosal or intracranial hemorrhage. Urgent hematologic intervention required.",
                "MediLens AI deterministic safety rule. Mayo Clinic Laboratories Reference Manual.",
                true,
                Instant.now()
        ));

        triageRuleRepository.save(new TriageRule(
                null,
                "Stage 3 Acute Kidney Injury Alert",
                "Serum Creatinine",
                ">=",
                4.0,
                "mg/dL",
                TriageUrgency.URGENT,
                "URGENT CLINICAL EVALUATION: Marked renal retention indicating severe acute kidney injury or chronic renal failure. Nephrology consultation required within 12-24 hours.",
                "MediLens AI deterministic safety rule. KDIGO Clinical Practice Guideline for Acute Kidney Injury.",
                true,
                Instant.now()
        ));

        log.info("Successfully seeded {} triage rules.", triageRuleRepository.count());
    }

    private void seedMedicationsAndInteractions() {
        if (medicationRepository.count() > 0) {
            log.info("Medications already present (count={}). Skipping seed.", medicationRepository.count());
            return;
        }

        log.info("Seeding canonical medications and interaction matrix...");

        Medication metformin = medicationRepository.save(new Medication(
                null, "Glucophage", "Metformin", "6809",
                "Biguanide Antihyperglycemic",
                "500mg to 2000mg daily in divided doses with meals.",
                Instant.now()
        ));

        Medication lisinopril = medicationRepository.save(new Medication(
                null, "Zestril", "Lisinopril", "29046",
                "ACE Inhibitor",
                "10mg to 40mg once daily for hypertension and heart failure.",
                Instant.now()
        ));

        Medication spironolactone = medicationRepository.save(new Medication(
                null, "Aldactone", "Spironolactone", "9997",
                "Potassium-Sparing Diuretic / Aldosterone Antagonist",
                "25mg to 100mg daily for hypertension and heart failure.",
                Instant.now()
        ));

        Medication warfarin = medicationRepository.save(new Medication(
                null, "Coumadin", "Warfarin", "11289",
                "Vitamin K Antagonist Anticoagulant",
                "2mg to 10mg daily titrated to target INR 2.0-3.0.",
                Instant.now()
        ));

        Medication aspirin = medicationRepository.save(new Medication(
                null, "Bayer Aspirin", "Aspirin", "1191",
                "Antiplatelet / NSAID",
                "81mg once daily for secondary cardiovascular prevention.",
                Instant.now()
        ));

        Medication ibuprofen = medicationRepository.save(new Medication(
                null, "Advil", "Ibuprofen", "5640",
                "Nonsteroidal Anti-inflammatory Drug (NSAID)",
                "200mg to 400mg every 4 to 6 hours as needed for pain or fever.",
                Instant.now()
        ));

        Medication atorvastatin = medicationRepository.save(new Medication(
                null, "Lipitor", "Atorvastatin", "83367",
                "HMG-CoA Reductase Inhibitor (Statin)",
                "10mg to 80mg once daily in the evening for dyslipidemia.",
                Instant.now()
        ));

        Medication clarithromycin = medicationRepository.save(new Medication(
                null, "Biaxin", "Clarithromycin", "21212",
                "Macrolide Antibiotic",
                "250mg to 500mg every 12 hours for bacterial infections.",
                Instant.now()
        ));

        // Interactions
        drugInteractionRepository.save(new DrugInteraction(
                null, lisinopril, spironolactone,
                InteractionSeverity.MAJOR,
                "Concurrent administration of ACE inhibitor and potassium-sparing diuretic produces additive retention of potassium, significantly increasing risk of life-threatening severe hyperkalemia and cardiac arrest.",
                "Lexicomp Drug Interactions & American College of Cardiology Guidelines 2023",
                Instant.now()
        ));

        drugInteractionRepository.save(new DrugInteraction(
                null, warfarin, aspirin,
                InteractionSeverity.MAJOR,
                "Additive antithrombotic and antiplatelet inhibition exponentially increases the hazard of gastrointestinal bleeding, ulceration, and hemorrhagic stroke.",
                "Chest Guidelines for Antithrombotic Therapy & FDA Black Box Warnings",
                Instant.now()
        ));

        drugInteractionRepository.save(new DrugInteraction(
                null, warfarin, ibuprofen,
                InteractionSeverity.CONTRAINDICATED,
                "Ibuprofen displaces warfarin from albumin binding sites, prolongs bleeding time via platelet inhibition, and induces gastric mucosa erosions. Concomitant use is strictly contraindicated without close coagulation supervision.",
                "FDA Drug Safety Communication & Mayo Clinic Pharmacology Reference",
                Instant.now()
        ));

        drugInteractionRepository.save(new DrugInteraction(
                null, atorvastatin, clarithromycin,
                InteractionSeverity.CONTRAINDICATED,
                "Clarithromycin is a potent CYP3A4 and OATP1B1 inhibitor that markedly reduces hepatic metabolism and clearance of atorvastatin, increasing plasma concentration up to 400% and triggering acute rhabdomyolysis and renal failure.",
                "American Heart Association Scientific Statement on Statin Safety & FDA Labeling",
                Instant.now()
        ));

        drugInteractionRepository.save(new DrugInteraction(
                null, metformin, lisinopril,
                InteractionSeverity.MODERATE,
                "ACE inhibitors may increase insulin sensitivity and slightly heighten the risk of hypoglycemia when initiated with metformin. Periodic fasting glucose monitoring is advised.",
                "ADA Clinical Pharmacology in Diabetes Care 2024",
                Instant.now()
        ));

        log.info("Successfully seeded {} canonical medications and {} critical drug interactions.",
                medicationRepository.count(), drugInteractionRepository.count());
    }
}
