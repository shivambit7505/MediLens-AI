import { apiClient } from './client';
import { AuthResponse, User } from '../types';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: string;
  role?: string;
}

export const authApi = {
  login: async (payload: LoginPayload): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', payload);
    return response.data;
  },

  register: async (payload: RegisterPayload): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/register', payload);
    return response.data;
  },

  getMe: async (): Promise<User> => {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },
};
