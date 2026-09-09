import React from 'react';
import { IssuePriority, IssueStatus, IssueType, Role } from '../types';
import {
  ArrowDown,
  ArrowUp,
  Bug,
  CheckCircle2,
  Clock,
  Sparkles,
  Zap,
} from 'lucide-react';

export const StatusBadge: React.FC<{ status: IssueStatus }> = ({ status }) => {
  const styles: Record<IssueStatus, { bg: string; text: string; label: string }> = {
    TODO: { bg: 'bg-slate-100 border-slate-200', text: 'text-slate-700', label: 'To Do' },
    IN_PROGRESS: { bg: 'bg-blue-50 border-blue-200', text: 'text-blue-700', label: 'In Progress' },
    IN_REVIEW: { bg: 'bg-purple-50 border-purple-200', text: 'text-purple-700', label: 'In Review' },
    DONE: { bg: 'bg-emerald-50 border-emerald-200', text: 'text-emerald-700', label: 'Done' },
    CLOSED: { bg: 'bg-gray-100 border-gray-200', text: 'text-gray-600', label: 'Closed' },
  };

  const style = styles[status] || styles.TODO;

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${style.bg} ${style.text}`}>
      {style.label}
    </span>
  );
};

export const PriorityBadge: React.FC<{ priority: IssuePriority }> = ({ priority }) => {
  switch (priority) {
    case 'CRITICAL':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200">
          <Zap className="w-3 h-3 text-rose-600 fill-rose-600" />
          Critical
        </span>
      );
    case 'HIGH':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
          <ArrowUp className="w-3 h-3 text-amber-600 font-bold" />
          High
        </span>
      );
    case 'MEDIUM':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
          <Clock className="w-3 h-3 text-blue-500" />
          Medium
        </span>
      );
    case 'LOW':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-slate-50 text-slate-600 border border-slate-200">
          <ArrowDown className="w-3 h-3 text-slate-400" />
          Low
        </span>
      );
    default:
      return null;
  }
};

export const TypeBadge: React.FC<{ type: IssueType }> = ({ type }) => {
  switch (type) {
    case 'BUG':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-red-50 text-red-700 border border-red-200">
          <Bug className="w-3 h-3 text-red-500" />
          Bug
        </span>
      );
    case 'FEATURE':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
          <Sparkles className="w-3 h-3 text-emerald-500" />
          Feature
        </span>
      );
    case 'TASK':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
          <CheckCircle2 className="w-3 h-3 text-blue-500" />
          Task
        </span>
      );
    case 'IMPROVEMENT':
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-teal-50 text-teal-700 border border-teal-200">
          <Zap className="w-3 h-3 text-teal-500" />
          Improvement
        </span>
      );
    default:
      return null;
  }
};

export const RoleBadge: React.FC<{ role: Role }> = ({ role }) => {
  const styles: Record<Role, { bg: string; text: string; label: string }> = {
    ADMIN: { bg: 'bg-indigo-50 border-indigo-200', text: 'text-indigo-700', label: 'Admin' },
    PROJECT_MANAGER: { bg: 'bg-emerald-50 border-emerald-200', text: 'text-emerald-700', label: 'PM' },
    DEVELOPER: { bg: 'bg-blue-50 border-blue-200', text: 'text-blue-700', label: 'Developer' },
    VIEWER: { bg: 'bg-gray-100 border-gray-200', text: 'text-gray-600', label: 'Viewer' },
  };

  const style = styles[role] || styles.DEVELOPER;

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${style.bg} ${style.text}`}>
      {style.label}
    </span>
  );
};
