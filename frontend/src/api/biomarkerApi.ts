import { apiClient } from './client';
import { Biomarker, BiomarkerHistory } from '../types';

export const biomarkerApi = {
  getBiomarkers: async (): Promise<Biomarker[]> => {
    const response = await apiClient.get<Biomarker[]>('/biomarkers');
    return response.data;
  },

  getBiomarkerHistory: async (biomarkerId: string): Promise<BiomarkerHistory> => {
    const response = await apiClient.get<BiomarkerHistory>(`/biomarkers/${biomarkerId}/history`);
    return response.data;
  },
};
