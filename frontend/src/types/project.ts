import { User, Role } from './auth';

export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';

export interface Organization {
  id: number;
  name: string;
  slug?: string;
  description?: string;
  createdAt?: string;
}

export interface Project {
  id: number;
  organizationId: number;
  organizationName?: string;
  name: string;
  key: string;
  description?: string;
  status: ProjectStatus;
  startDate?: string;
  endDate?: string;
  createdBy?: User;
  memberCount: number;
  totalIssues: number;
  openIssues: number;
  progressPercentage: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectCreateRequest {
  name: string;
  key: string;
  organizationId: number;
  description?: string;
  startDate?: string;
  endDate?: string;
}

export interface ProjectUpdateRequest {
  name: string;
  description?: string;
  status?: ProjectStatus;
  startDate?: string;
  endDate?: string;
}

export interface ProjectMember {
  id: number;
  projectId: number;
  user: User;
  role: Role;
  joinedAt?: string;
}

export interface ProjectMemberRequest {
  userId: number;
  role?: Role;
}

