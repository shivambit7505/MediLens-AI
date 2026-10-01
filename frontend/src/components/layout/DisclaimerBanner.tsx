import React from 'react';
import { AlertTriangle } from 'lucide-react';

export const DisclaimerBanner: React.FC = () => {
  return (
    <div className="bg-amber-500/10 border-b border-amber-500/20 px-4 py-2 text-center text-xs text-amber-900 font-medium flex items-center justify-center space-x-2">
      <AlertTriangle className="w-3.5 h-3.5 text-amber-600 flex-shrink-0" />
      <span>
        <strong>Notice:</strong> MediLens AI is an academic clinical decision support & research platform. It does not provide medical diagnoses or prescriptions. Always consult a qualified healthcare provider.
      </span>
    </div>
  );
};
