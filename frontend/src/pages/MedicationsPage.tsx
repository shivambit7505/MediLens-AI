import React, { useState, useEffect } from 'react';
import { medicationApi } from '../api/medicationApi';
import { Medication, UserMedication, InteractionCheckResponse } from '../types';
import {
  Pill,
  AlertOctagon,
  Plus,
  Trash2,
  RefreshCw,
  Sparkles,
} from 'lucide-react';

export const MedicationsPage: React.FC = () => {
  const [catalog, setCatalog] = useState<Medication[]>([]);
  const [userMeds, setUserMeds] = useState<UserMedication[]>([]);
  const [selectedCatalogMedId, setSelectedCatalogMedId] = useState<string>('');
  const [dosage, setDosage] = useState('');
  const [frequency, setFrequency] = useState('');
  const [interactionResult, setInteractionResult] = useState<InteractionCheckResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [checkingInteractions, setCheckingInteractions] = useState(false);

  // Sandbox testing arbitrary drugs
  const [sandboxMedIds, setSandboxMedIds] = useState<string[]>([]);
  const [sandboxResult, setSandboxResult] = useState<InteractionCheckResponse | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [allMeds, myMeds] = await Promise.all([
        medicationApi.searchCatalog(),
        medicationApi.getUserMedications().catch(() => []),
      ]);
      setCatalog(allMeds);
      setUserMeds(myMeds);
      if (allMeds.length > 0 && !selectedCatalogMedId) {
        setSelectedCatalogMedId(allMeds[0].id);
      }
    } catch (err) {
      console.error('Failed to load medications', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddMedication = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCatalogMedId || !dosage || !frequency) return;

    try {
      setLoading(true);
      await medicationApi.addUserMedication({
        medicationId: selectedCatalogMedId,
        dosage,
        frequency,
      });
      setDosage('');
      setFrequency('');
      await loadData();
    } catch (err) {
      console.error('Failed to add medication', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveMedication = async (id: string) => {
    try {
      await medicationApi.removeUserMedication(id);
      setUserMeds(userMeds.filter((m) => m.id !== id));
      if (interactionResult) {
        handleCheckUserInteractions();
      }
    } catch (err) {
      console.error('Failed to remove medication', err);
    }
  };

  const handleCheckUserInteractions = async () => {
    try {
      setCheckingInteractions(true);
      const res = await medicationApi.checkUserActiveInteractions();
      setInteractionResult(res);
    } catch (err) {
      console.error('Failed to check interactions', err);
    } finally {
      setCheckingInteractions(false);
    }
  };

  const toggleSandboxMed = (id: string) => {
    const updated = sandboxMedIds.includes(id)
      ? sandboxMedIds.filter((mId) => mId !== id)
      : [...sandboxMedIds, id];
    setSandboxMedIds(updated);
  };

  const handleRunSandboxCheck = async () => {
    if (sandboxMedIds.length < 2) return;
    try {
      setCheckingInteractions(true);
      const res = await medicationApi.checkInteractions(sandboxMedIds);
      setSandboxResult(res);
    } catch (err) {
      console.error('Failed to check sandbox interactions', err);
    } finally {
      setCheckingInteractions(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      {/* Header */}
      <div>
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-blue-100 dark:bg-blue-950 text-blue-600 dark:text-blue-400 rounded-lg">
            <Pill className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Medication Management & Interaction Matrix
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Deterministic pairwise drug-drug contraindication safety analysis backed by FDA & Clinical Pharmacology evidence.
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: User Medications & Add Form */}
        <div className="lg:col-span-7 space-y-6">
          {/* Active Medications List */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="text-base font-semibold text-gray-900 dark:text-white flex items-center">
                  <Pill className="h-4 w-4 mr-2 text-blue-600" />
                  My Prescribed & Active Medications ({userMeds.length})
                </h2>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Medications in your active clinical profile
                </p>
              </div>

              {userMeds.length >= 2 && (
                <button
                  type="button"
                  onClick={handleCheckUserInteractions}
                  disabled={checkingInteractions}
                  className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold flex items-center space-x-1.5 shadow-sm transition-all"
                >
                  <RefreshCw className={`h-3.5 w-3.5 ${checkingInteractions ? 'animate-spin' : ''}`} />
                  <span>Check Interactions</span>
                </button>
              )}
            </div>

            {userMeds.length === 0 ? (
              <div className="p-6 text-center bg-gray-50 dark:bg-gray-900/40 rounded-lg border border-dashed border-gray-200 dark:border-gray-700">
                <Pill className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  No medications registered yet. Add medications below or run a custom check.
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {userMeds.map((um) => (
                  <div
                    key={um.id}
                    className="p-3.5 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-100 dark:border-gray-700 flex items-center justify-between text-xs"
                  >
                    <div>
                      <div className="font-bold text-gray-900 dark:text-white">
                        {um.medication.brandName} ({um.medication.genericName})
                      </div>
                      <div className="text-gray-600 dark:text-gray-400 mt-0.5">
                        <span className="font-medium text-gray-800 dark:text-gray-200">{um.dosage}</span> • {um.frequency}
                      </div>
                      <div className="text-[11px] text-teal-600 dark:text-teal-400 mt-0.5">
                        Class: {um.medication.therapeuticClass}
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveMedication(um.id)}
                      className="p-1.5 text-gray-400 hover:text-red-500 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-800"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Add Medication Form */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
            <h3 className="text-sm font-bold text-gray-900 dark:text-white mb-4 flex items-center">
              <Plus className="h-4 w-4 mr-1 text-teal-600" />
              Add Medication to Profile
            </h3>

            <form onSubmit={handleAddMedication} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Select Medication from Master Catalog
                </label>
                <select
                  value={selectedCatalogMedId}
                  onChange={(e) => setSelectedCatalogMedId(e.target.value)}
                  className="w-full px-3 py-2 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
                >
                  {catalog.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.brandName} ({m.genericName}) — {m.therapeuticClass}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Dosage
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. 500mg, 10mg"
                    value={dosage}
                    onChange={(e) => setDosage(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                    Frequency
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Once daily, Twice daily"
                    value={frequency}
                    onChange={(e) => setFrequency(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-900 text-gray-900 dark:text-white"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-xs font-semibold transition-all shadow-sm"
              >
                Save Medication
              </button>
            </form>
          </div>

          {/* Interactive Multi-Drug Sandbox */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
            <h3 className="text-sm font-bold text-gray-900 dark:text-white mb-2 flex items-center">
              <Sparkles className="h-4 w-4 mr-1 text-purple-600" />
              Pairwise Drug-Drug Interaction Sandbox
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-4">
              Select any 2 or more medications below to preview theoretical pairwise contraindications before prescribing or consuming:
            </p>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 mb-4">
              {catalog.map((m) => {
                const isSelected = sandboxMedIds.includes(m.id);
                return (
                  <button
                    key={m.id}
                    type="button"
                    onClick={() => toggleSandboxMed(m.id)}
                    className={`p-2 rounded-lg text-left text-xs transition-all border ${
                      isSelected
                        ? 'bg-purple-50 dark:bg-purple-950/60 border-purple-400 text-purple-900 dark:text-purple-200 font-semibold'
                        : 'bg-gray-50 dark:bg-gray-900 border-gray-200 dark:border-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-100'
                    }`}
                  >
                    <div className="truncate">{m.genericName}</div>
                    <div className="text-[10px] text-gray-400 truncate">{m.brandName}</div>
                  </button>
                );
              })}
            </div>

            <button
              type="button"
              onClick={handleRunSandboxCheck}
              disabled={sandboxMedIds.length < 2 || checkingInteractions}
              className="w-full py-2 bg-purple-600 hover:bg-purple-700 disabled:opacity-50 text-white rounded-lg text-xs font-semibold transition-all"
            >
              Analyze Pairwise Interaction Matrix ({sandboxMedIds.length} Selected)
            </button>
          </div>
        </div>

        {/* Right Column: Interaction Matrix Analysis Results */}
        <div className="lg:col-span-5 space-y-6">
          {/* Active Profile Results */}
          {interactionResult && (
            <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700 space-y-4">
              <div className="flex items-center justify-between border-b pb-3 dark:border-gray-700">
                <h3 className="text-sm font-bold text-gray-900 dark:text-white flex items-center">
                  <AlertOctagon className="h-4 w-4 mr-2 text-red-600" />
                  Prescription Interaction Evaluation
                </h3>
                <span className={`px-2 py-0.5 rounded text-[10px] uppercase font-bold ${
                  interactionResult.highestSeverityBadgeColor === 'red'
                    ? 'bg-red-600 text-white'
                    : interactionResult.highestSeverityBadgeColor === 'rose'
                    ? 'bg-rose-500 text-white'
                    : interactionResult.highestSeverityBadgeColor === 'amber'
                    ? 'bg-amber-500 text-white'
                    : 'bg-emerald-600 text-white'
                }`}>
                  {interactionResult.highestSeverity || 'CLEAN'}
                </span>
              </div>

              <div className={`p-3 rounded-lg text-xs ${
                interactionResult.hasInteractions
                  ? 'bg-red-50 dark:bg-red-950/40 text-red-800 dark:text-red-200 border border-red-200 dark:border-red-900'
                  : 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-200 border border-emerald-200'
              }`}>
                {interactionResult.clinicalWarning}
              </div>

              {interactionResult.interactions.map((inter) => (
                <div key={inter.id} className="p-3 bg-gray-50 dark:bg-gray-900 rounded-lg text-xs space-y-2 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-gray-900 dark:text-white">
                      {inter.medicationA.genericName} ⇄ {inter.medicationB.genericName}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      inter.severity === 'CONTRAINDICATED' ? 'bg-red-600 text-white' : 'bg-amber-500 text-white'
                    }`}>
                      {inter.severity}
                    </span>
                  </div>
                  <p className="text-gray-700 dark:text-gray-300">
                    {inter.interactionMechanism}
                  </p>
                  <p className="text-[10px] text-gray-400 italic">
                    Source: {inter.clinicalEvidenceSource}
                  </p>
                </div>
              ))}

              <div className="text-[10px] text-gray-400 italic">
                {interactionResult.statutoryDisclaimer}
              </div>
            </div>
          )}

          {/* Sandbox Results */}
          {sandboxResult && (
            <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-purple-200 dark:border-purple-900 space-y-4">
              <div className="flex items-center justify-between border-b pb-3 dark:border-gray-700">
                <h3 className="text-sm font-bold text-purple-900 dark:text-purple-300 flex items-center">
                  <Sparkles className="h-4 w-4 mr-2 text-purple-600" />
                  Sandbox Interaction Findings
                </h3>
                <span className={`px-2 py-0.5 rounded text-[10px] uppercase font-bold ${
                  sandboxResult.highestSeverityBadgeColor === 'red'
                    ? 'bg-red-600 text-white'
                    : sandboxResult.highestSeverityBadgeColor === 'rose'
                    ? 'bg-rose-500 text-white'
                    : sandboxResult.highestSeverityBadgeColor === 'amber'
                    ? 'bg-amber-500 text-white'
                    : 'bg-emerald-600 text-white'
                }`}>
                  {sandboxResult.highestSeverity || 'CLEAN'}
                </span>
              </div>

              <div className={`p-3 rounded-lg text-xs ${
                sandboxResult.hasInteractions
                  ? 'bg-red-50 dark:bg-red-950/40 text-red-800 dark:text-red-200 border border-red-200 dark:border-red-900'
                  : 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-200 border border-emerald-200'
              }`}>
                {sandboxResult.clinicalWarning}
              </div>

              {sandboxResult.interactions.map((inter) => (
                <div key={inter.id} className="p-3 bg-gray-50 dark:bg-gray-900 rounded-lg text-xs space-y-2 border border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-gray-900 dark:text-white">
                      {inter.medicationA.genericName} ⇄ {inter.medicationB.genericName}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      inter.severity === 'CONTRAINDICATED' ? 'bg-red-600 text-white' : 'bg-amber-500 text-white'
                    }`}>
                      {inter.severity}
                    </span>
                  </div>
                  <p className="text-gray-700 dark:text-gray-300">
                    {inter.interactionMechanism}
                  </p>
                  <p className="text-[10px] text-gray-400 italic">
                    Evidence: {inter.clinicalEvidenceSource}
                  </p>
                </div>
              ))}
            </div>
          )}

          {!interactionResult && !sandboxResult && (
            <div className="bg-gray-50 dark:bg-gray-800/50 border border-dashed border-gray-300 dark:border-gray-700 rounded-xl p-8 text-center space-y-3">
              <Pill className="h-10 w-10 text-gray-400 mx-auto" />
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
                Awaiting Interaction Evaluation
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
                Click "Check Interactions" on your active prescriptions, or test any combination of drugs in the Sandbox to inspect pharmacokinetic interactions.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
