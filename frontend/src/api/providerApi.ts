import { apiClient } from './client';
import { CareNavigationResponse, Provider } from '../types';

export const providerApi = {
  getCareNavigation: async (): Promise<CareNavigationResponse> => {
    const res = await apiClient.get<CareNavigationResponse>('/providers/recommendations');
    return res.data;
  },

  searchProviders: async (params?: {
    specialty?: string;
    query?: string;
    telehealthOnly?: boolean;
  }): Promise<Provider[]> => {
    const res = await apiClient.get<Provider[]>('/providers/search', { params });
    return res.data;
  },
};
