import { apiClient } from './client';
import { ProjectAnalytics, UserDashboard } from '../types';

export const analyticsApi = {
  getProjectAnalytics: async (projectId: number): Promise<ProjectAnalytics> => {
    const response = await apiClient.get<ProjectAnalytics>(`/projects/${projectId}/analytics`);
    return response.data;
  },

  getUserDashboard: async (): Promise<UserDashboard> => {
    const response = await apiClient.get<UserDashboard>('/dashboard/me');
    return response.data;
  },
};

