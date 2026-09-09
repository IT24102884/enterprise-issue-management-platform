export type MilestoneStatus = 'OPEN' | 'CLOSED';

export interface Milestone {
  id: number;
  projectId: number;
  projectName?: string;
  title: string;
  description?: string;
  startDate?: string;
  dueDate?: string;
  status: MilestoneStatus;
  totalIssues: number;
  closedIssues: number;
  openIssues: number;
  progressPercentage: number;
  createdAt?: string;
}

export interface MilestoneRequest {
  title: string;
  description?: string;
  startDate?: string;
  dueDate?: string;
}

