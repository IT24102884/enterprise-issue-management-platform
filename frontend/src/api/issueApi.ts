import { apiClient } from './client';
import {
  Issue,
  CreateIssueRequest,
  UpdateIssueRequest,
  IssueStatus,
  IssueComment,
  AuditLog,
  PagedResponse,
  IssueFilterParams,
} from '../types';

export const issueApi = {
  getProjectIssues: async (projectId: number, params?: IssueFilterParams): Promise<PagedResponse<Issue>> => {
    const response = await apiClient.get<PagedResponse<Issue>>(`/projects/${projectId}/issues`, {
      params,
    });
    return response.data;
  },

  getIssueById: async (id: number): Promise<Issue> => {
    const response = await apiClient.get<Issue>(`/issues/${id}`);
    return response.data;
  },

  getIssueByKey: async (key: string): Promise<Issue> => {
    const response = await apiClient.get<Issue>(`/issues/key/${key}`);
    return response.data;
  },

  createIssue: async (projectId: number, data: CreateIssueRequest): Promise<Issue> => {
    const response = await apiClient.post<Issue>(`/projects/${projectId}/issues`, data);
    return response.data;
  },

  updateIssue: async (id: number, data: UpdateIssueRequest): Promise<Issue> => {
    const response = await apiClient.put<Issue>(`/issues/${id}`, data);
    return response.data;
  },

  updateIssueStatus: async (id: number, status: IssueStatus): Promise<Issue> => {
    const response = await apiClient.patch<Issue>(`/issues/${id}/status`, { status });
    return response.data;
  },

  deleteIssue: async (id: number): Promise<void> => {
    await apiClient.delete(`/issues/${id}`);
  },

  getComments: async (issueId: number): Promise<IssueComment[]> => {
    const response = await apiClient.get<IssueComment[]>(`/issues/${issueId}/comments`);
    return response.data;
  },

  addComment: async (issueId: number, comment: string): Promise<IssueComment> => {
    const response = await apiClient.post<IssueComment>(`/issues/${issueId}/comments`, { comment });
    return response.data;
  },

  deleteComment: async (commentId: number): Promise<void> => {
    await apiClient.delete(`/issues/comments/${commentId}`);
  },

  getAuditLogs: async (issueId: number): Promise<PagedResponse<AuditLog>> => {
    const response = await apiClient.get<PagedResponse<AuditLog>>(`/issues/${issueId}/audit-logs`);
    return response.data;
  },
};

