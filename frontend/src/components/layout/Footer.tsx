import React from 'react';
import { ShieldCheck, Database, Cpu } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-white border-t border-slate-200 mt-auto py-6">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <div className="flex items-center space-x-2">
            <span className="font-semibold text-slate-700">MediLens AI Engine</span>
            <span>&bull;</span>
            <span>&copy; {new Date().getFullYear()} Clinical Laboratory Intelligence Platform</span>
          </div>

          <div className="flex items-center space-x-4">
            <span className="flex items-center space-x-1 text-slate-600">
              <Cpu className="w-3.5 h-3.5 text-emerald-600" />
              <span>Deterministic NLP Engine</span>
            </span>
            <span className="flex items-center space-x-1 text-slate-600">
              <Database className="w-3.5 h-3.5 text-blue-600" />
              <span>LOINC & Ref Range Validated</span>
            </span>
            <span className="flex items-center space-x-1 text-slate-600">
              <ShieldCheck className="w-3.5 h-3.5 text-indigo-600" />
              <span>Anti-IDOR Protected</span>
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
};
