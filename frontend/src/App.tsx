import React from 'react'

export const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between">
      {/* Top Clinical Disclaimer Banner */}
      <div className="bg-amber-500/10 border-b border-amber-500/20 px-4 py-2 text-center text-xs text-amber-800 font-medium">
        MediLens AI is an academic clinical decision support & research platform. It does not replace professional medical advice, diagnosis, or treatment.
      </div>

      {/* Main Container */}
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-lg bg-emerald-600 flex items-center justify-center text-white font-bold text-lg shadow-sm">
            M
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">MediLens AI</h1>
            <p className="text-xs text-slate-500">Laboratory Intelligence & Healthcare Navigation</p>
          </div>
        </div>
        <div className="flex items-center space-x-3 text-sm text-slate-600">
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
            Phase 0: Foundation Active
          </span>
        </div>
      </header>

      <main className="flex-1 max-w-7xl mx-auto w-full px-6 py-10">
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-8">
          <h2 className="text-2xl font-bold text-slate-900 mb-2">Welcome to MediLens AI Architecture</h2>
          <p className="text-slate-600 mb-6 text-sm leading-relaxed max-w-3xl">
            A production-style clinical laboratory report analysis and healthcare navigation system. Built with deterministic OCR, canonical biomarker extraction, unit normalization, reference range validation, and evidence-grounded RAG.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4 border-t border-slate-100">
            <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
              <h3 className="font-semibold text-slate-800 text-sm mb-1">Deterministic AI Pipeline</h3>
              <p className="text-xs text-slate-500">Dual OCR (PaddleOCR + Tesseract), layout parsing, regex extraction, canonical aliasing, and demographic range checking without LLM hallucination.</p>
            </div>
            <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
              <h3 className="font-semibold text-slate-800 text-sm mb-1">Multi-Tier Security & Isolation</h3>
              <p className="text-xs text-slate-500">Stateless JWT, RBAC authorization, anti-IDOR user ownership barriers, private file storage, and immutable security audit logs.</p>
            </div>
            <div className="p-4 rounded-lg bg-slate-50 border border-slate-200">
              <h3 className="font-semibold text-slate-800 text-sm mb-1">Grounded RAG Guidance</h3>
              <p className="text-xs text-slate-500">pgvector semantic search against authoritative medical literature with required source citations and refusal barriers.</p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-4 text-center text-xs text-slate-500">
        &copy; {new Date().getFullYear()} MediLens AI. Designed for clinical safety and patient empowerment.
      </footer>
    </div>
  )
}

export default App
