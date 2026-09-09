import { apiClient } from './client';
import {
  Project,
  ProjectCreateRequest,
  ProjectUpdateRequest,
  ProjectMember,
  ProjectMemberRequest,
  Organization,
  PagedResponse,
} from '../types';

export const projectApi = {
  getProjects: async (page = 0, size = 20): Promise<PagedResponse<Project>> => {
    const response = await apiClient.get<PagedResponse<Project>>('/projects', {
      params: { page, size, sort: 'createdAt,desc' },
    });
    return response.data;
  },

  getProjectById: async (id: number): Promise<Project> => {
    const response = await apiClient.get<Project>(`/projects/${id}`);
    return response.data;
  },

  getProjectByKey: async (key: string): Promise<Project> => {
    const response = await apiClient.get<Project>(`/projects/key/${key}`);
    return response.data;
  },

  createProject: async (data: ProjectCreateRequest): Promise<Project> => {
    const response = await apiClient.post<Project>('/projects', data);
    return response.data;
  },

  updateProject: async (id: number, data: ProjectUpdateRequest): Promise<Project> => {
    const response = await apiClient.put<Project>(`/projects/${id}`, data);
    return response.data;
  },

  deleteProject: async (id: number): Promise<void> => {
    await apiClient.delete(`/projects/${id}`);
  },

  getMembers: async (projectId: number): Promise<ProjectMember[]> => {
    const response = await apiClient.get<ProjectMember[]>(`/projects/${projectId}/members`);
    return response.data;
  },

  addMember: async (projectId: number, data: ProjectMemberRequest): Promise<ProjectMember> => {
    const response = await apiClient.post<ProjectMember>(`/projects/${projectId}/members`, data);
    return response.data;
  },

  removeMember: async (projectId: number, userId: number): Promise<void> => {
    await apiClient.delete(`/projects/${projectId}/members/${userId}`);
  },

  getOrganizations: async (): Promise<Organization[]> => {
    const response = await apiClient.get<Organization[]>('/organizations');
    return response.data;
  },

  createOrganization: async (data: { name: string; slug: string; description?: string }): Promise<Organization> => {
    const response = await apiClient.post<Organization>('/organizations', data);
    return response.data;
  },
};

