import React, { useState, useEffect } from 'react';
import { triageApi } from '../api/triageApi';
import { TriageEvaluationResponse, TriageRule, BiomarkerReading } from '../types';
import {
  AlertTriangle,
  ShieldAlert,
  CheckCircle2,
  Clock,
  PhoneCall,
  Activity,
  Plus,
  Trash2,
  BookOpen,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';
import { Link } from 'react-router-dom';

const COMMON_SYMPTOMS = [
  'Chest pain',
  'Shortness of breath',
  'Sudden weakness or numbness',
  'Severe sudden headache',
  'Loss of consciousness',
  'Uncontrolled bleeding',
  'High fever (>103°F)',
  'Persistent vomiting',
  'Severe abdominal pain',
  'Sudden confusion or disorientation',
  'Prolonged dizziness',
];

export const TriagePage: React.FC = () => {
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>([]);
  const [customSymptom, setCustomSymptom] = useState('');
  const [includeLatestReport, setIncludeLatestReport] = useState(true);
  const [manualReadings, setManualReadings] = useState<BiomarkerReading[]>([
    { canonicalName: 'Serum Potassium', valueNumeric: 4.5, unit: 'mmol/L' },
  ]);
  const [rules, setRules] = useState<TriageRule[]>([]);
  const [showRules, setShowRules] = useState(false);
  const [evaluation, setEvaluation] = useState<TriageEvaluationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    triageApi.getActiveRules()
      .then(setRules)
      .catch((err) => console.error('Failed to load triage rules', err));
  }, []);

  const toggleSymptom = (sym: string) => {
    setSelectedSymptoms((prev) =>
      prev.includes(sym) ? prev.filter((s) => s !== sym) : [...prev, sym]
    );
  };

  const addCustomSymptom = () => {
    if (customSymptom.trim() && !selectedSymptoms.includes(customSymptom.trim())) {
      setSelectedSymptoms((prev) => [...prev, customSymptom.trim()]);
      setCustomSymptom('');
    }
  };

  const handleReadingChange = (index: number, field: keyof BiomarkerReading, value: any) => {
    const updated = [...manualReadings];
    updated[index] = { ...updated[index], [field]: value };
    setManualReadings(updated);
  };

  const addReadingRow = () => {
    setManualReadings([...manualReadings, { canonicalName: '', valueNumeric: 0, unit: '' }]);
  };

  const removeReadingRow = (index: number) => {
    setManualReadings(manualReadings.filter((_, i) => i !== index));
  };

  const handleEvaluate = async () => {
    try {
      setLoading(true);
      setError(null);

      const validReadings = manualReadings.filter(
        (r) => r.canonicalName.trim() && !isNaN(Number(r.valueNumeric))
      );

      const response = await triageApi.evaluateTriage({
        readings: validReadings,
        symptoms: selectedSymptoms,
        includeLatestReportBiomarkers: includeLatestReport,
      });

      setEvaluation(response);
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to complete triage evaluation. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      {/* Header */}
      <div>
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-red-100 dark:bg-red-950 text-red-600 dark:text-red-400 rounded-lg">
            <ShieldAlert className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Deterministic Symptom & Biomarker Triage
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Zero-LLM clinical emergency rule evaluation for critical laboratory anomalies and acute red-flag symptoms.
            </p>
          </div>
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Triage Inputs */}
        <div className="lg:col-span-7 space-y-6">
          {/* Symptoms Selection Card */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
            <h2 className="text-base font-semibold text-gray-900 dark:text-white mb-2 flex items-center">
              <Activity className="h-4 w-4 mr-2 text-teal-600" />
              1. Acute Reported Symptoms
            </h2>
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-4">
              Select any symptoms you or the patient are currently experiencing:
            </p>

            <div className="flex flex-wrap gap-2 mb-4">
              {COMMON_SYMPTOMS.map((sym) => {
                const selected = selectedSymptoms.includes(sym);
                return (
                  <button
                    key={sym}
                    type="button"
                    onClick={() => toggleSymptom(sym)}
                    className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
                      selected
                        ? 'bg-red-600 text-white shadow-sm'
                        : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                    }`}
                  >
                    {selected ? '✓ ' : '+ '} {sym}
                  </button>
                );
              })}
            </div>

            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Add other symptom (e.g. Sudden severe dizziness)..."
                value={customSymptom}
                onChange={(e) => setCustomSymptom(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addCustomSymptom())}
                className="flex-1 px-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
              />
              <button
                type="button"
                onClick={addCustomSymptom}
                className="px-3 py-1.5 bg-gray-100 dark:bg-gray-700 text-xs font-medium rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-200"
              >
                Add
              </button>
            </div>
          </div>

          {/* Biomarkers Input Card */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between mb-2">
              <h2 className="text-base font-semibold text-gray-900 dark:text-white flex items-center">
                <Activity className="h-4 w-4 mr-2 text-teal-600" />
                2. Key Biomarker Values
              </h2>
              <button
                type="button"
                onClick={addReadingRow}
                className="inline-flex items-center text-xs text-teal-600 dark:text-teal-400 font-medium hover:underline"
              >
                <Plus className="h-3 w-3 mr-1" /> Add Biomarker
              </button>
            </div>

            <div className="flex items-center mb-4">
              <input
                type="checkbox"
                id="includeLatest"
                checked={includeLatestReport}
                onChange={(e) => setIncludeLatestReport(e.target.checked)}
                className="h-4 w-4 text-teal-600 rounded border-gray-300 focus:ring-teal-500"
              />
              <label htmlFor="includeLatest" className="ml-2 text-xs text-gray-700 dark:text-gray-300">
                Automatically merge latest measurements from my uploaded lab report
              </label>
            </div>

            <div className="space-y-3">
              {manualReadings.map((reading, index) => (
                <div key={index} className="flex items-center gap-2">
                  <input
                    type="text"
                    placeholder="Biomarker Name (e.g. Serum Potassium)"
                    value={reading.canonicalName}
                    onChange={(e) => handleReadingChange(index, 'canonicalName', e.target.value)}
                    className="flex-2 px-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
                  />
                  <input
                    type="number"
                    step="any"
                    placeholder="Value"
                    value={reading.valueNumeric || ''}
                    onChange={(e) => handleReadingChange(index, 'valueNumeric', parseFloat(e.target.value) || 0)}
                    className="w-24 px-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
                  />
                  <input
                    type="text"
                    placeholder="Unit (e.g. mmol/L)"
                    value={reading.unit}
                    onChange={(e) => handleReadingChange(index, 'unit', e.target.value)}
                    className="w-24 px-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
                  />
                  <button
                    type="button"
                    onClick={() => removeReadingRow(index)}
                    className="p-1.5 text-gray-400 hover:text-red-500 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>

            <div className="mt-6 pt-4 border-t border-gray-100 dark:border-gray-700">
              <button
                type="button"
                onClick={handleEvaluate}
                disabled={loading}
                className="w-full py-2.5 px-4 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg text-sm transition-colors shadow-sm disabled:opacity-50 flex items-center justify-center space-x-2"
              >
                {loading ? (
                  <span>Evaluating Clinical Rules...</span>
                ) : (
                  <>
                    <ShieldAlert className="h-4 w-4" />
                    <span>Run Deterministic Triage Evaluation</span>
                  </>
                )}
              </button>
            </div>
          </div>

          {/* Clinical Rules Inspector */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-5 shadow-sm border border-gray-200 dark:border-gray-700">
            <button
              type="button"
              onClick={() => setShowRules(!showRules)}
              className="w-full flex items-center justify-between text-left text-xs font-semibold text-gray-700 dark:text-gray-300"
            >
              <span className="flex items-center">
                <BookOpen className="h-4 w-4 mr-2 text-teal-600" />
                View Active Deterministic Safety Rules ({rules.length})
              </span>
              {showRules ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
            </button>

            {showRules && (
              <div className="mt-4 space-y-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                {rules.map((r) => (
                  <div key={r.id} className="p-3 bg-gray-50 dark:bg-gray-900 rounded-lg text-xs space-y-1">
                    <div className="flex items-center justify-between font-semibold text-gray-900 dark:text-white">
                      <span>{r.ruleName}</span>
                      <span className={`px-2 py-0.5 rounded text-[10px] ${
                        r.urgencyLevel === 'EMERGENCY' ? 'bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-400' : 'bg-amber-100 text-amber-700'
                      }`}>
                        {r.urgencyLevel}
                      </span>
                    </div>
                    <p className="text-gray-600 dark:text-gray-400">
                      Condition: <code className="bg-gray-200 dark:bg-gray-800 px-1 py-0.5 rounded font-mono">{r.biomarkerCanonicalName} {r.comparisonOperator} {r.thresholdNumeric} {r.unit}</code>
                    </p>
                    <p className="text-gray-500 dark:text-gray-400 text-[11px] italic">
                      {r.deterministicActionInstruction}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Triage Result Card */}
        <div className="lg:col-span-5 space-y-6">
          {error && (
            <div className="p-4 bg-red-50 dark:bg-red-950 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-300 text-sm">
              {error}
            </div>
          )}

          {evaluation ? (
            <div className="space-y-6">
              {/* Urgency Badge Banner */}
              <div className={`rounded-xl p-6 shadow-md border ${
                evaluation.overallUrgency === 'EMERGENCY'
                  ? 'bg-red-50 dark:bg-red-950/70 border-red-300 dark:border-red-800 ring-2 ring-red-500 animate-pulse'
                  : evaluation.overallUrgency === 'URGENT'
                  ? 'bg-amber-50 dark:bg-amber-950/70 border-amber-300 dark:border-amber-800'
                  : 'bg-emerald-50 dark:bg-emerald-950/70 border-emerald-300 dark:border-emerald-800'
              }`}>
                <div className="flex items-start justify-between">
                  <div className="flex items-center space-x-3">
                    {evaluation.overallUrgency === 'EMERGENCY' ? (
                      <AlertTriangle className="h-8 w-8 text-red-600 dark:text-red-400" />
                    ) : evaluation.overallUrgency === 'URGENT' ? (
                      <Clock className="h-8 w-8 text-amber-600 dark:text-amber-400" />
                    ) : (
                      <CheckCircle2 className="h-8 w-8 text-emerald-600 dark:text-emerald-400" />
                    )}
                    <div>
                      <span className="text-xs uppercase tracking-wider font-bold text-gray-500 dark:text-gray-400">
                        Assessed Triage Acuity
                      </span>
                      <h3 className={`text-2xl font-black ${
                        evaluation.overallUrgency === 'EMERGENCY'
                          ? 'text-red-700 dark:text-red-300'
                          : evaluation.overallUrgency === 'URGENT'
                          ? 'text-amber-700 dark:text-amber-300'
                          : 'text-emerald-700 dark:text-emerald-300'
                      }`}>
                        {evaluation.overallUrgency}
                      </h3>
                    </div>
                  </div>
                </div>

                <div className="mt-4 p-3 bg-white dark:bg-gray-900 rounded-lg text-sm font-semibold text-gray-800 dark:text-gray-200">
                  {evaluation.primaryActionDirective}
                </div>

                {evaluation.overallUrgency === 'EMERGENCY' && (
                  <div className="mt-4 flex gap-3">
                    <a
                      href="tel:911"
                      className="flex-1 py-2 px-3 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-bold text-center flex items-center justify-center space-x-1"
                    >
                      <PhoneCall className="h-4 w-4 mr-1" />
                      Call Emergency Services (911)
                    </a>
                    <Link
                      to="/providers"
                      className="py-2 px-3 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-lg text-xs font-semibold text-center"
                    >
                      Nearest ER / Urgent Care
                    </Link>
                  </div>
                )}
              </div>

              {/* Triggers Breakdown */}
              <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                <h4 className="text-sm font-bold text-gray-900 dark:text-white mb-3 flex items-center justify-between">
                  <span>Detected Triggers ({evaluation.triggers.length})</span>
                  <span className="text-xs font-normal text-gray-500">Deterministic Evidence</span>
                </h4>

                {evaluation.triggers.length === 0 ? (
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    No critical physiological red-flags or urgent emergency symptoms detected.
                  </p>
                ) : (
                  <div className="space-y-3">
                    {evaluation.triggers.map((t, idx) => (
                      <div
                        key={idx}
                        className={`p-3 rounded-lg border text-xs space-y-1 ${
                          t.urgencyLevel === 'EMERGENCY'
                            ? 'bg-red-50 dark:bg-red-950/40 border-red-200 dark:border-red-900'
                            : 'bg-amber-50 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900'
                        }`}
                      >
                        <div className="flex items-center justify-between font-semibold">
                          <span className="text-gray-900 dark:text-white">{t.name}</span>
                          <span className={`px-2 py-0.5 rounded text-[10px] uppercase font-bold ${
                            t.urgencyLevel === 'EMERGENCY' ? 'bg-red-600 text-white' : 'bg-amber-600 text-white'
                          }`}>
                            {t.urgencyLevel}
                          </span>
                        </div>
                        <p className="text-gray-700 dark:text-gray-300 font-medium">
                          {t.clinicalInstruction}
                        </p>
                        <p className="text-gray-500 dark:text-gray-400 text-[11px]">
                          {t.rationale}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Specialist Routing */}
              {evaluation.recommendedSpecialties.length > 0 && (
                <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
                  <h4 className="text-sm font-bold text-gray-900 dark:text-white mb-3">
                    Recommended Clinical Referrals
                  </h4>
                  <div className="flex flex-wrap gap-2 mb-4">
                    {evaluation.recommendedSpecialties.map((spec) => (
                      <span
                        key={spec}
                        className="px-3 py-1 bg-teal-50 dark:bg-teal-950 text-teal-700 dark:text-teal-300 border border-teal-200 dark:border-teal-800 rounded-lg text-xs font-semibold"
                      >
                        {spec}
                      </span>
                    ))}
                  </div>
                  <Link
                    to="/providers"
                    className="block text-center text-xs font-semibold text-teal-600 dark:text-teal-400 hover:underline"
                  >
                    Find Accredited Specialists in Care Navigator →
                  </Link>
                </div>
              )}

              {/* Disclaimer */}
              <div className="text-[11px] text-gray-400 dark:text-gray-500 italic p-3 bg-gray-50 dark:bg-gray-900/50 rounded-lg">
                {evaluation.disclaimerText}
              </div>
            </div>
          ) : (
            <div className="bg-gray-50 dark:bg-gray-800/50 border border-dashed border-gray-300 dark:border-gray-700 rounded-xl p-8 text-center space-y-3">
              <ShieldAlert className="h-10 w-10 text-gray-400 mx-auto" />
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
                Awaiting Triage Input
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
                Select your symptoms and key biomarker readings on the left, then click "Run Deterministic Triage Evaluation" to receive immediate clinical acuity guidance.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
