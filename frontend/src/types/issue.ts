import { User } from './auth';

export type IssueStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE' | 'CLOSED';
export type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type IssueType = 'BUG' | 'TASK' | 'FEATURE' | 'IMPROVEMENT';

export interface IssueLabel {
  id: number;
  name: string;
  color: string;
}

export interface IssueComment {
  id: number;
  issueId: number;
  user: User;
  comment: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuditLog {
  id: number;
  user?: User;
  action: string;
  entityType: string;
  entityId?: number;
  timestamp: string;
  metadata?: string;
}

export interface Issue {
  id: number;
  issueKey: string;
  title: string;
  description?: string;
  type: IssueType;
  status: IssueStatus;
  priority: IssuePriority;
  projectId: number;
  projectKey: string;
  projectName: string;
  reporter?: User;
  assignee?: User;
  milestoneId?: number;
  milestoneTitle?: string;
  dueDate?: string;
  labels: IssueLabel[];
  commentCount: number;
  attachmentCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateIssueRequest {
  title: string;
  description?: string;
  type: IssueType;
  priority?: IssuePriority;
  assigneeId?: number;
  milestoneId?: number;
  dueDate?: string;
  labelNames?: string[];
}

export interface UpdateIssueRequest {
  title?: string;
  description?: string;
  type?: IssueType;
  status?: IssueStatus;
  priority?: IssuePriority;
  assigneeId?: number;
  milestoneId?: number;
  dueDate?: string;
  labelNames?: string[];
}

export interface IssueFilterParams {
  status?: IssueStatus;
  priority?: IssuePriority;
  type?: IssueType;
  assigneeId?: number;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

