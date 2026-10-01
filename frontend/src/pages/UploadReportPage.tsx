import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { reportApi } from '../api/reportApi';
import { 
  UploadCloud, 
  CheckCircle2, 
  AlertCircle, 
  ArrowRight, 
  ShieldCheck
} from 'lucide-react';

const PROCESSING_STEPS = [
  { key: 'UPLOADED', label: 'File Validated & Uploaded', desc: 'Magic bytes verified, stored in isolated user volume' },
  { key: 'PREPROCESSING', label: 'Document Preprocessing', desc: 'Apache PDFBox rendering (200 DPI), OpenCV deskew & CLAHE' },
  { key: 'OCR_PROCESSING', label: 'Dual-Engine OCR Extraction', desc: 'PaddleOCR primary with Tesseract fallback' },
  { key: 'EXTRACTING', label: 'Biomarker Extraction', desc: 'Spatial regex parsing, LOINC dictionary mapping' },
  { key: 'NORMALIZING', label: 'Unit & Reference Validation', desc: 'Deterministic conversions, ADA/Mayo reference bounds' },
  { key: 'COMPLETED', label: 'Analysis Complete', desc: 'Data persisted with immutable audit logging' },
];

export const UploadReportPage: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [currentStep, setCurrentStep] = useState<number>(0);
  const [reportId, setReportId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelected(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelected(e.target.files[0]);
    }
  };

  const handleFileSelected = (file: File) => {
    setError(null);
    const validTypes = ['application/pdf', 'image/png', 'image/jpeg'];
    if (!validTypes.includes(file.type) && !file.name.match(/\.(pdf|png|jpe?g)$/i)) {
      setError('Invalid file format. Please upload a valid medical PDF, PNG, or JPEG file.');
      return;
    }

    if (file.size > 25 * 1024 * 1024) {
      setError('File exceeds 25 MB size limit.');
      return;
    }

    setSelectedFile(file);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setError(null);
    setIsUploading(true);
    setCurrentStep(0);

    // Simulated stepper animation while synchronous backend runs
    const interval = setInterval(() => {
      setCurrentStep((prev) => (prev < 4 ? prev + 1 : prev));
    }, 900);

    try {
      const result = await reportApi.uploadReport(selectedFile);
      clearInterval(interval);
      setCurrentStep(5);
      setReportId(result.id);
    } catch (err: any) {
      clearInterval(interval);
      setError(err.response?.data?.message || 'Report processing failed. Please check the document format.');
      setIsUploading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Upload Diagnostic Lab Report</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Supported formats: PDF (multi-page), PNG, JPEG up to 25 MB. Deterministic processing guarantee.
        </p>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center space-x-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-600" />
          <span>{error}</span>
        </div>
      )}

      {/* Upload Box / Processing State */}
      {!isUploading && !reportId ? (
        <div className="space-y-6">
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-3xl p-10 text-center cursor-pointer transition-all ${
              isDragging
                ? 'border-emerald-500 bg-emerald-50/50'
                : 'border-slate-300 hover:border-emerald-500 bg-white hover:bg-slate-50/50'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.png,.jpg,.jpeg"
              onChange={handleFileChange}
              className="hidden"
            />
            <div className="w-14 h-14 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto mb-4">
              <UploadCloud className="w-8 h-8" />
            </div>
            <h3 className="text-base font-bold text-slate-900">
              {selectedFile ? selectedFile.name : 'Click to upload or drag & drop'}
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              {selectedFile
                ? `${(selectedFile.size / (1024 * 1024)).toFixed(2)} MB • Ready to analyze`
                : 'PDF, PNG, or JPEG (Max 25 MB)'}
            </p>
          </div>

          {selectedFile && (
            <div className="flex justify-end">
              <button
                type="button"
                onClick={handleUpload}
                className="flex items-center space-x-2 px-6 py-3 rounded-xl text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 shadow-sm transition-all"
              >
                <span>Start Deterministic Ingestion</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      ) : (
        /* Live Processing Stepper */
        <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-xs space-y-8">
          <div className="text-center">
            <h3 className="text-lg font-bold text-slate-900">
              {currentStep === 5 ? 'Processing Complete!' : 'Processing Lab Document...'}
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Executing multi-stage clinical normalization pipeline without LLM hallucinations
            </p>
          </div>

          <div className="space-y-4 max-w-lg mx-auto">
            {PROCESSING_STEPS.map((step, idx) => {
              const isPast = currentStep > idx;
              const isCurrent = currentStep === idx;
              return (
                <div key={step.key} className="flex items-start space-x-4">
                  <div className="mt-0.5 flex-shrink-0">
                    {isPast ? (
                      <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                    ) : isCurrent ? (
                      <div className="w-5 h-5 rounded-full border-2 border-emerald-600 border-t-transparent animate-spin" />
                    ) : (
                      <div className="w-5 h-5 rounded-full border-2 border-slate-200" />
                    )}
                  </div>
                  <div>
                    <p className={`text-xs font-bold ${isCurrent || isPast ? 'text-slate-900' : 'text-slate-400'}`}>
                      {step.label}
                    </p>
                    <p className="text-[11px] text-slate-500">{step.desc}</p>
                  </div>
                </div>
              );
            })}
          </div>

          {reportId && (
            <div className="text-center pt-4 border-t border-slate-100">
              <button
                type="button"
                onClick={() => navigate(`/reports/${reportId}`)}
                className="inline-flex items-center space-x-2 px-6 py-3 rounded-xl text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 shadow-sm transition-all"
              >
                <span>View Extracted Findings & Explanations</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      )}

      {/* Safety Invariant Note */}
      <div className="p-4 bg-slate-100 rounded-2xl border border-slate-200 text-xs text-slate-600 flex items-center space-x-3">
        <ShieldCheck className="w-5 h-5 text-emerald-600 flex-shrink-0" />
        <p>
          <strong>Clinical Safety Invariant:</strong> Raw image coordinates, extracted text tokens, and reference intervals are retained for 100% auditability. All numerical determinations are strictly deterministic.
        </p>
      </div>
    </div>
  );
};
