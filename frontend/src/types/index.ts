// MediLens AI - Core TypeScript Definitions

export type Role = 'ROLE_PATIENT' | 'ROLE_CLINICIAN' | 'ROLE_ADMIN';

export type MeasurementStatus = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL' | 'UNKNOWN';

export type Trajectory = 'RISING' | 'FALLING' | 'STABLE' | 'INSUFFICIENT_DATA';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: string;
  role: Role;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface Report {
  id: string;
  originalFilename: string;
  status: 'UPLOADED' | 'PREPROCESSING' | 'OCR_PROCESSING' | 'EXTRACTING' | 'NORMALIZING' | 'COMPLETED' | 'FAILED';
  pageCount: number;
  measurementCount: number;
  createdAt: string;
  processingCompletedAt?: string;
  failureReason?: string;
}

export interface ReportPage {
  id: string;
  pageNumber: number;
  ocrRawText?: string;
  ocrConfidenceScore?: number;
  ocrEngineUsed?: string;
}

export interface Measurement {
  id: string;
  canonicalName: string;
  extractedName: string;
  codeLoinc?: string;
  observedValueRaw: string;
  observedValueNumeric?: number;
  extractedUnit?: string;
  normalizedValueNumeric: number;
  normalizedUnit: string;
  extractedReferenceText?: string;
  status: MeasurementStatus;
  confidence: number;
  pageNumber: number;
  sourceTextSnippet?: string;
  matchedRangeCitation?: string;
}

export interface ReportDetail extends Report {
  mimeType: string;
  fileSizeBytes: number;
  pages: ReportPage[];
  measurements: Measurement[];
}

export interface EvidenceSource {
  chunkId: string;
  title: string;
  source: string;
  category: string;
}

export interface FindingExplanation {
  canonicalName: string;
  observedValue: string;
  status: MeasurementStatus;
  referenceInterval?: string;
  explanation: string;
  clinicalSignificance: string;
  lifestyleGuidance?: string;
  sources: string[];
}

export interface ReportExplanation {
  reportId: string;
  summary: string;
  findings: FindingExplanation[];
  questionsForDoctor: string[];
  criticalAlert?: string;
  disclaimer: string;
  citedSources: EvidenceSource[];
  safetyAuditPassed: boolean;
}

export interface Biomarker {
  id: string;
  canonicalName: string;
  codeLoinc?: string;
  category: string;
  standardUnit: string;
  description?: string;
}

export interface BiomarkerDataPoint {
  measurementId: string;
  reportId: string;
  timestamp: string;
  observedValue: number;
  unit: string;
  status: MeasurementStatus;
  referenceIntervalText?: string;
  isAbnormal: boolean;
}

export interface TrendStatistics {
  latestValue?: number;
  previousValue?: number;
  minValue?: number;
  maxValue?: number;
  delta?: number;
  deltaPercentage?: number;
  trajectory: Trajectory;
}

export interface BiomarkerHistory {
  biomarkerId: string;
  canonicalName: string;
  standardUnit: string;
  codeLoinc?: string;
  category: string;
  dataPoints: BiomarkerDataPoint[];
  statistics: TrendStatistics;
}

export interface DashboardSummary {
  totalReports: number;
  totalMeasurements: number;
  abnormalCount: number;
  criticalCount: number;
  recentReports: Report[];
}

// ----------------------------------------------------------------------------
// Phase 7: Clinical Safety, Triage, Drug Interactions & Care Navigation Types
// ----------------------------------------------------------------------------

export type TriageUrgency = 'ROUTINE' | 'URGENT' | 'EMERGENCY';

export interface BiomarkerReading {
  canonicalName: string;
  valueNumeric: number;
  unit: string;
}

export interface TriageTrigger {
  triggerType: 'BIOMARKER' | 'SYMPTOM';
  name: string;
  observedValue?: number;
  operator?: string;
  threshold?: number;
  unit?: string;
  urgencyLevel: TriageUrgency;
  clinicalInstruction: string;
  rationale: string;
}

export interface TriageEvaluationRequest {
  readings: BiomarkerReading[];
  symptoms: string[];
  includeLatestReportBiomarkers?: boolean;
}

export interface TriageEvaluationResponse {
  overallUrgency: TriageUrgency;
  emergencyFlag: boolean;
  urgencyBadgeColor: string;
  primaryActionDirective: string;
  triggers: TriageTrigger[];
  recommendedSpecialties: string[];
  disclaimerText: string;
}

export interface TriageRule {
  id: string;
  ruleName: string;
  biomarkerCanonicalName: string;
  comparisonOperator: string;
  thresholdNumeric: number;
  unit: string;
  urgencyLevel: TriageUrgency;
  deterministicActionInstruction: string;
  disclaimerText: string;
}

export type InteractionSeverity = 'MINOR' | 'MODERATE' | 'MAJOR' | 'CONTRAINDICATED';

export interface Medication {
  id: string;
  brandName: string;
  genericName: string;
  rxnormCui?: string;
  therapeuticClass: string;
  standardDosageGuidelines?: string;
}

export interface UserMedication {
  id: string;
  medication: Medication;
  dosage: string;
  frequency: string;
  startDate: string;
  endDate?: string;
  active: boolean;
}

export interface AddUserMedicationRequest {
  medicationId: string;
  dosage: string;
  frequency: string;
  startDate?: string;
  endDate?: string;
}

export interface DrugInteraction {
  id: string;
  medicationA: Medication;
  medicationB: Medication;
  severity: InteractionSeverity;
  severityBadgeColor: string;
  interactionMechanism: string;
  clinicalEvidenceSource: string;
}

export interface InteractionCheckResponse {
  hasInteractions: boolean;
  totalInteractionsCount: number;
  highestSeverity?: InteractionSeverity;
  highestSeverityBadgeColor: string;
  interactions: DrugInteraction[];
  clinicalWarning: string;
  statutoryDisclaimer: string;
}

export interface Provider {
  id: string;
  name: string;
  title: string;
  specialty: string;
  clinicName: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
  rating: number;
  reviewCount: number;
  distanceMiles: number;
  telehealthAvailable: boolean;
  acceptingNewPatients: boolean;
  affiliatedHospitals: string[];
  clinicalInterests: string[];
}

export interface SpecialtyRecommendation {
  specialty: string;
  clinicalReason: string;
  urgency: 'URGENT' | 'ROUTINE';
  triggeringBiomarkers: string[];
  suggestedQuestionsForDoctor: string;
}

export interface CareNavigationResponse {
  recommendations: SpecialtyRecommendation[];
  nearbyProviders: Provider[];
  statutoryDisclaimer: string;
}

export interface ChatMessage {
  id: string;
  sender: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  citedSources: string[];
  suggestedQuestions: string[];
  safetyPassed: boolean;
  createdAt: string;
}

export interface ConversationSummary {
  id: string;
  reportId?: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  lastMessageSnippet?: string;
  messageCount: number;
}

export interface ConversationDetail {
  id: string;
  reportId?: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: ChatMessage[];
  suggestedPrompts: string[];
}

