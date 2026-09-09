import { IssuePriority, IssueStatus, IssueType, Issue, AuditLog } from './issue';
import { Project } from './project';

export interface ProjectAnalytics {
  projectId: number;
  projectName: string;
  projectKey: string;
  totalIssues: number;
  openIssues: number;
  completedIssues: number;
  progressPercentage: number;
  memberCount: number;
  milestoneCount: number;
  statusBreakdown: Record<IssueStatus, number>;
  priorityBreakdown: Record<IssuePriority, number>;
  typeBreakdown: Record<IssueType, number>;
  recentActivity: AuditLog[];
}

export interface UserDashboard {
  assignedIssuesCount: number;
  completedIssuesCount: number;
  pendingIssuesCount: number;
  recentAssignedIssues: Issue[];
  userProjects: Project[];
  recentActivity: AuditLog[];
}

