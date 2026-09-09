import { apiClient } from './client';
import { Milestone, MilestoneRequest, MilestoneStatus } from '../types';

export const milestoneApi = {
  getProjectMilestones: async (projectId: number, status?: MilestoneStatus): Promise<Milestone[]> => {
    const response = await apiClient.get<Milestone[]>(`/projects/${projectId}/milestones`, {
      params: status ? { status } : undefined,
    });
    return response.data;
  },

  getMilestoneById: async (id: number): Promise<Milestone> => {
    const response = await apiClient.get<Milestone>(`/milestones/${id}`);
    return response.data;
  },

  createMilestone: async (projectId: number, data: MilestoneRequest): Promise<Milestone> => {
    const response = await apiClient.post<Milestone>(`/projects/${projectId}/milestones`, data);
    return response.data;
  },

  updateMilestone: async (id: number, data: MilestoneRequest): Promise<Milestone> => {
    const response = await apiClient.put<Milestone>(`/milestones/${id}`, data);
    return response.data;
  },

  closeMilestone: async (id: number): Promise<Milestone> => {
    const response = await apiClient.patch<Milestone>(`/milestones/${id}/close`);
    return response.data;
  },

  deleteMilestone: async (id: number): Promise<void> => {
    await apiClient.delete(`/milestones/${id}`);
  },
};

