import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { reportApi } from '../api/reportApi';
import { ReportDetail, ReportExplanation, MeasurementStatus } from '../types';
import { 
  FileText, 
  Activity, 
  AlertCircle, 
  BookOpen, 
  HelpCircle, 
  ShieldCheck, 
  ChevronDown,
  ChevronUp,
  Image as ImageIcon,
  Sparkles
} from 'lucide-react';

const getStatusBadge = (status: MeasurementStatus) => {
  switch (status) {
    case 'NORMAL':
      return <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800">NORMAL</span>;
    case 'HIGH':
      return <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">HIGH</span>;
    case 'LOW':
      return <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-800">LOW</span>;
    case 'CRITICAL':
      return (
        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-800 animate-pulse flex items-center space-x-1">
          <AlertCircle className="w-3 h-3" />
          <span>CRITICAL</span>
        </span>
      );
    default:
      return <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-700">UNKNOWN</span>;
  }
};

export const ReportDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [report, setReport] = useState<ReportDetail | null>(null);
  const [explanation, setExplanation] = useState<ReportExplanation | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activePage, setActivePage] = useState(1);
  const [expandedFindings, setExpandedFindings] = useState<Record<string, boolean>>({});

  useEffect(() => {
    if (!id) return;

    const loadData = async () => {
      setLoading(true);
      try {
        const detail = await reportApi.getReportDetail(id);
        setReport(detail);

        try {
          const exp = await reportApi.getReportExplanation(id);
          setExplanation(exp);
        } catch (expErr) {
          console.warn('RAG explanation not available yet:', expErr);
        }
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load report analysis.');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [id]);

  const toggleFinding = (name: string) => {
    setExpandedFindings((prev) => ({ ...prev, [name]: !prev[name] }));
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center space-y-3">
        <div className="w-10 h-10 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
        <p className="text-xs text-slate-500 font-medium">Loading clinical analysis & verified findings...</p>
      </div>
    );
  }

  if (error || !report) {
    return (
      <div className="py-16 text-center">
        <AlertCircle className="w-12 h-12 text-rose-500 mx-auto mb-3" />
        <h3 className="text-base font-bold text-slate-900">Unable to load report</h3>
        <p className="text-xs text-slate-500 mt-1 mb-4">{error || 'Report not found'}</p>
        <Link to="/reports" className="text-xs font-semibold text-emerald-700 hover:underline">
          &larr; Return to Reports list
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Top Breadcrumb & Metadata Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="flex items-center space-x-2 text-xs text-slate-400 mb-1">
            <Link to="/reports" className="hover:text-emerald-700">Reports</Link>
            <span>/</span>
            <span className="text-slate-600 font-mono text-[11px] truncate max-w-[200px]">{report.id}</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center space-x-3">
            <FileText className="w-6 h-6 text-emerald-600" />
            <span>{report.originalFilename}</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Processed on {new Date(report.createdAt).toLocaleDateString()} • {report.pageCount} page(s) rendered • {report.measurements.length} biomarkers validated
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
            Status: {report.status}
          </span>
        </div>
      </div>

      {/* Emergency Red-Flag Banner (Phase 5 Clinical Invariant) */}
      {explanation?.criticalAlert && (
        <div className="p-4 bg-rose-50 border-2 border-rose-300 rounded-2xl flex items-start space-x-3 text-rose-900">
          <AlertCircle className="w-6 h-6 text-rose-600 flex-shrink-0 mt-0.5 animate-bounce" />
          <div>
            <h4 className="text-sm font-bold">URGENT CLINICAL ALERT</h4>
            <p className="text-xs mt-0.5 leading-relaxed font-medium">
              {explanation.criticalAlert}
            </p>
          </div>
        </div>
      )}

      {/* Dual Pane Inspection: High-Res Rendered Image + Extracted Table */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Rendered Document Page Viewer (5 Cols) */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-100 text-xs font-semibold text-slate-700">
              <span className="flex items-center space-x-1.5">
                <ImageIcon className="w-4 h-4 text-emerald-600" />
                <span>Original Document Page</span>
              </span>
              <div className="flex items-center space-x-2">
                <span className="text-slate-400">Page {activePage} of {report.pageCount}</span>
                {report.pageCount > 1 && (
                  <div className="flex items-center space-x-1">
                    {Array.from({ length: report.pageCount }, (_, i) => i + 1).map((p) => (
                      <button
                        key={p}
                        onClick={() => setActivePage(p)}
                        className={`w-5 h-5 rounded text-[10px] font-bold ${
                          activePage === p ? 'bg-emerald-600 text-white' : 'bg-slate-100 text-slate-600'
                        }`}
                      >
                        {p}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="border border-slate-100 rounded-xl overflow-hidden bg-slate-50 flex items-center justify-center min-h-[350px]">
              <img
                src={reportApi.getPageImageUrl(report.id, activePage)}
                alt={`Report Page ${activePage}`}
                className="w-full h-auto object-contain max-h-[550px] shadow-xs"
                onError={(e) => {
                  (e.target as any).style.display = 'none';
                }}
              />
            </div>

            <p className="text-[11px] text-slate-400 mt-2 text-center">
              Rendered with Apache PDFBox (200 DPI) & OpenCV Contrast Auto-Tuning
            </p>
          </div>
        </div>

        {/* Right Column: Deterministic Measurements Table (7 Cols) */}
        <div className="lg:col-span-7 space-y-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-900">Deterministic Biomarker Observations</h3>
                <p className="text-[11px] text-slate-400">Validated against age/gender demographic guidelines (ADA 2024, Mayo, WHO)</p>
              </div>
              <span className="px-2.5 py-1 rounded-md bg-emerald-50 text-emerald-800 text-[11px] font-bold">
                {report.measurements.length} Extracted
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                    <th className="py-3 px-4">Biomarker</th>
                    <th className="py-3 px-4">Observed Value</th>
                    <th className="py-3 px-4">Standardized</th>
                    <th className="py-3 px-4">Ref Range</th>
                    <th className="py-3 px-4">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-slate-700">
                  {report.measurements.map((m) => (
                    <tr key={m.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="py-3.5 px-4 font-semibold text-slate-900">
                        <div>{m.canonicalName}</div>
                        {m.codeLoinc && (
                          <div className="text-[10px] text-slate-400 font-mono">LOINC: {m.codeLoinc}</div>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-slate-600 font-mono">
                        {m.observedValueRaw}
                      </td>
                      <td className="py-3.5 px-4 font-semibold font-mono text-slate-900">
                        {m.normalizedValueNumeric} {m.normalizedUnit}
                      </td>
                      <td className="py-3.5 px-4 text-slate-500 font-mono text-[11px]">
                        {m.extractedReferenceText || 'N/A'}
                      </td>
                      <td className="py-3.5 px-4">
                        {getStatusBadge(m.status)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Evidence-Grounded Medical RAG Explanations (Phase 5 Integration) */}
      {explanation && (
        <div className="space-y-6 pt-4">
          <div className="bg-gradient-to-r from-emerald-900 to-teal-900 text-white p-6 sm:p-8 rounded-3xl shadow-sm">
            <div className="flex items-center space-x-2 text-emerald-300 text-xs font-semibold uppercase tracking-wider mb-2">
              <Sparkles className="w-4 h-4 text-emerald-400" />
              <span>Evidence-Grounded Clinical Intelligence</span>
            </div>
            <h2 className="text-xl sm:text-2xl font-bold tracking-tight">
              Educational Analysis & Clinical Guidance
            </h2>
            <p className="text-xs sm:text-sm text-emerald-100 mt-2 leading-relaxed max-w-3xl">
              {explanation.summary}
            </p>
          </div>

          {/* Finding-by-Finding Educational Cards */}
          <div className="space-y-4">
            <h3 className="text-base font-bold text-slate-900 flex items-center space-x-2">
              <Activity className="w-5 h-5 text-emerald-600" />
              <span>Detailed Biomarker Findings & Evidence Grounding</span>
            </h3>

            <div className="grid grid-cols-1 gap-4">
              {explanation.findings.map((f) => {
                const isExpanded = expandedFindings[f.canonicalName] ?? true;
                return (
                  <div
                    key={f.canonicalName}
                    className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden transition-all"
                  >
                    <div
                      onClick={() => toggleFinding(f.canonicalName)}
                      className="p-5 flex items-center justify-between cursor-pointer hover:bg-slate-50/50 transition-colors"
                    >
                      <div className="flex items-center space-x-3">
                        <div className="w-3 h-3 rounded-full bg-emerald-500 flex-shrink-0" />
                        <div>
                          <div className="flex items-center space-x-2">
                            <h4 className="text-sm font-bold text-slate-900">{f.canonicalName}</h4>
                            <span className="font-mono text-xs text-slate-600 font-semibold">({f.observedValue})</span>
                            {getStatusBadge(f.status)}
                          </div>
                          {f.referenceInterval && (
                            <p className="text-[11px] text-slate-400 mt-0.5">Reference: {f.referenceInterval}</p>
                          )}
                        </div>
                      </div>

                      <div className="flex items-center space-x-3">
                        <div className="flex items-center space-x-1">
                          {f.sources.map((src) => (
                            <span key={src} className="px-2 py-0.5 rounded bg-blue-50 text-blue-700 text-[10px] font-bold font-mono">
                              {src}
                            </span>
                          ))}
                        </div>
                        {isExpanded ? (
                          <ChevronUp className="w-4 h-4 text-slate-400" />
                        ) : (
                          <ChevronDown className="w-4 h-4 text-slate-400" />
                        )}
                      </div>
                    </div>

                    {isExpanded && (
                      <div className="px-5 pb-5 pt-1 border-t border-slate-100 text-xs text-slate-600 space-y-3 bg-slate-50/40">
                        <div>
                          <span className="font-bold text-slate-800">Educational Explanation: </span>
                          <span>{f.explanation}</span>
                        </div>
                        {f.clinicalSignificance && (
                          <div>
                            <span className="font-bold text-slate-800">Clinical Significance: </span>
                            <span>{f.clinicalSignificance}</span>
                          </div>
                        )}
                        {f.lifestyleGuidance && (
                          <div>
                            <span className="font-bold text-slate-800">Follow-up Guidance: </span>
                            <span>{f.lifestyleGuidance}</span>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Actionable Doctor Discussion Questions */}
          {explanation.questionsForDoctor && explanation.questionsForDoctor.length > 0 && (
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-3">
              <div className="flex items-center space-x-2 text-slate-900 font-bold text-sm">
                <HelpCircle className="w-5 h-5 text-indigo-600" />
                <span>Recommended Questions For Your Physician</span>
              </div>
              <ul className="space-y-2">
                {explanation.questionsForDoctor.map((q, idx) => (
                  <li key={idx} className="flex items-start space-x-2.5 text-xs text-slate-700">
                    <span className="w-5 h-5 rounded-full bg-indigo-50 text-indigo-700 flex items-center justify-center font-bold text-[10px] flex-shrink-0 mt-0.5">
                      {idx + 1}
                    </span>
                    <span className="leading-relaxed">{q}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Cited Clinical Evidence Guidelines (Grounding Provenance) */}
          {explanation.citedSources && explanation.citedSources.length > 0 && (
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
              <div className="flex items-center space-x-2 text-slate-900 font-bold text-sm">
                <BookOpen className="w-5 h-5 text-emerald-600" />
                <span>Cited Clinical Evidence & Guidelines</span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {explanation.citedSources.map((source) => (
                  <div key={source.chunkId} className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs">
                    <span className="inline-block px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 font-mono text-[10px] font-bold mb-1">
                      {source.chunkId}
                    </span>
                    <h5 className="font-bold text-slate-900">{source.title}</h5>
                    <p className="text-[11px] text-slate-500 mt-1">{source.source}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Mandatory Statutory Disclaimer Card */}
          <div className="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-xs text-amber-900 leading-relaxed flex items-start space-x-3">
            <ShieldCheck className="w-5 h-5 text-amber-700 flex-shrink-0 mt-0.5" />
            <p>
              <strong>Statutory Medical Disclaimer: </strong>
              {explanation.disclaimer}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};
