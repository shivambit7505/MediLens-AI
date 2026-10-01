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
