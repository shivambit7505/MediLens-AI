import { apiClient } from './client';
import { TriageEvaluationRequest, TriageEvaluationResponse, TriageRule } from '../types';

export const triageApi = {
  getActiveRules: async (): Promise<TriageRule[]> => {
    const res = await apiClient.get<TriageRule[]>('/triage/rules');
    return res.data;
  },

  evaluateTriage: async (req: TriageEvaluationRequest): Promise<TriageEvaluationResponse> => {
    const res = await apiClient.post<TriageEvaluationResponse>('/triage/evaluate', req);
    return res.data;
  },
};
