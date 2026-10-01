import { apiClient } from './client';
import { Report, ReportDetail, Measurement, ReportExplanation } from '../types';

export const reportApi = {
  uploadReport: async (file: File): Promise<Report> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<Report>('/reports/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getReports: async (page = 0, size = 20): Promise<{ content: Report[]; totalElements: number; totalPages: number }> => {
    const response = await apiClient.get('/reports', {
      params: { page, size },
    });
    return response.data;
  },

  getReportDetail: async (reportId: string): Promise<ReportDetail> => {
    const response = await apiClient.get<ReportDetail>(`/reports/${reportId}`);
    return response.data;
  },

  getReportMeasurements: async (reportId: string): Promise<Measurement[]> => {
    const response = await apiClient.get<Measurement[]>(`/reports/${reportId}/measurements`);
    return response.data;
  },

  getReportExplanation: async (reportId: string): Promise<ReportExplanation> => {
    const response = await apiClient.get<ReportExplanation>(`/reports/${reportId}/explanation`);
    return response.data;
  },

  getPageImageUrl: (reportId: string, pageNumber: number): string => {
    const baseURL = apiClient.defaults.baseURL || 'http://127.0.0.1:8080/api/v1';
    return `${baseURL}/reports/${reportId}/pages/${pageNumber}/image`;
  },

  exportReportPdf: async (reportId: string): Promise<Blob> => {
    const response = await apiClient.get(`/reports/${reportId}/export-pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

