import { apiClient } from './client';
import { AddUserMedicationRequest, InteractionCheckResponse, Medication, UserMedication } from '../types';

export const medicationApi = {
  searchCatalog: async (query?: string): Promise<Medication[]> => {
    const res = await apiClient.get<Medication[]>('/medications', {
      params: query ? { query } : undefined,
    });
    return res.data;
  },

  getUserMedications: async (): Promise<UserMedication[]> => {
    const res = await apiClient.get<UserMedication[]>('/medications/user');
    return res.data;
  },

  addUserMedication: async (req: AddUserMedicationRequest): Promise<UserMedication> => {
    const res = await apiClient.post<UserMedication>('/medications/user', req);
    return res.data;
  },

  removeUserMedication: async (id: string): Promise<void> => {
    await apiClient.delete(`/medications/user/${id}`);
  },

  checkInteractions: async (medicationIds: string[]): Promise<InteractionCheckResponse> => {
    const res = await apiClient.post<InteractionCheckResponse>('/medications/check-interactions', {
      medicationIds,
    });
    return res.data;
  },

  checkUserActiveInteractions: async (): Promise<InteractionCheckResponse> => {
    const res = await apiClient.get<InteractionCheckResponse>('/medications/user/check-interactions');
    return res.data;
  },
};
