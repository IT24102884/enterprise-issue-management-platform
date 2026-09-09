import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { analyticsApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { StatusBadge, PriorityBadge } from '../components/Badges';
import { IssueDetailModal } from '../components/IssueDetailModal';
import {
  CheckCircle2,
  Clock,
  FolderGit2,
  ListTodo,
  TrendingUp,
  ArrowUpRight,
} from 'lucide-react';
import { Link, useOutletContext } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const { openCreateIssue } = useOutletContext<{ openCreateIssue: () => void }>() || {};

  const [selectedIssueId, setSelectedIssueId] = useState<number | null>(null);

  const { data: dashboard, isLoading, refetch } = useQuery({
    queryKey: ['dashboard'],
    queryFn: analyticsApi.getUserDashboard,
  });

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-blue-600 via-blue-700 to-indigo-800 rounded-2xl p-6 text-white shadow-lg shadow-blue-500/10">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-white/20 backdrop-blur-xs">
              Welcome back, {user?.name.split(' ')[0]} 👋
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight">Personal Workspace Dashboard</h1>
          <p className="text-xs text-blue-100 mt-1 max-w-xl">
            Track your assigned sprint tasks, project progress, and recent team activities in real-time.
          </p>
        </div>

        {openCreateIssue && (
          <button
            onClick={openCreateIssue}
            className="self-start sm:self-center px-4 py-2.5 bg-white text-blue-700 font-semibold rounded-xl text-xs shadow-sm hover:bg-blue-50 transition-all hover:shadow"
          >
            + Create New Issue
          </button>
        )}
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Assigned */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Assigned Tasks
            </span>
            <h3 className="text-2xl font-bold text-slate-800 mt-1">
              {isLoading ? '-' : dashboard?.assignedIssuesCount || 0}
            </h3>
          </div>
          <div className="h-11 w-11 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <ListTodo className="w-5 h-5" />
          </div>
        </div>

        {/* Pending Tasks */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              In Progress / Pending
            </span>
            <h3 className="text-2xl font-bold text-amber-600 mt-1">
              {isLoading ? '-' : dashboard?.pendingIssuesCount || 0}
            </h3>
          </div>
          <div className="h-11 w-11 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        {/* Completed */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Completed
            </span>
            <h3 className="text-2xl font-bold text-emerald-600 mt-1">
              {isLoading ? '-' : dashboard?.completedIssuesCount || 0}
            </h3>
          </div>
          <div className="h-11 w-11 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>

        {/* Projects */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Active Projects
            </span>
            <h3 className="text-2xl font-bold text-indigo-600 mt-1">
              {isLoading ? '-' : dashboard?.userProjects?.length || 0}
            </h3>
          </div>
          <div className="h-11 w-11 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
            <FolderGit2 className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main 2-Column Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left (Assigned Issues) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Assigned Issues Table */}
          <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <ListTodo className="w-4 h-4 text-blue-600" />
                <h3 className="text-sm font-bold text-slate-800">My Assigned Issues</h3>
              </div>
              <Link
                to="/kanban"
                className="text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1"
              >
                Go to Kanban Board
                <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="divide-y divide-slate-100 max-h-[380px] overflow-y-auto">
              {isLoading ? (
                <div className="p-8 text-center text-xs text-slate-400">Loading your issues...</div>
              ) : !dashboard?.recentAssignedIssues || dashboard.recentAssignedIssues.length === 0 ? (
                <div className="p-8 text-center">
                  <CheckCircle2 className="w-8 h-8 text-slate-300 mx-auto mb-2" />
                  <p className="text-xs text-slate-500 font-medium">All caught up!</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">No tasks currently assigned to you</p>
                </div>
              ) : (
                dashboard.recentAssignedIssues.map((issue) => (
                  <div
                    key={issue.id}
                    onClick={() => setSelectedIssueId(issue.id)}
                    className="p-4 hover:bg-slate-50/80 cursor-pointer transition-colors flex items-center justify-between gap-3 text-left"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <span className="text-xs font-mono font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded border border-blue-200 flex-shrink-0">
                        {issue.issueKey}
                      </span>
                      <div className="min-w-0">
                        <h4 className="text-xs font-semibold text-slate-800 truncate">
                          {issue.title}
                        </h4>
                        <span className="text-[11px] text-slate-400">
                          {issue.projectName}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 flex-shrink-0">
                      <PriorityBadge priority={issue.priority} />
                      <StatusBadge status={issue.status} />
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Projects Overview */}
          <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <FolderGit2 className="w-4 h-4 text-indigo-600" />
                <h3 className="text-sm font-bold text-slate-800">My Projects</h3>
              </div>
              <Link
                to="/projects"
                className="text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1"
              >
                View All Projects
                <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {dashboard?.userProjects?.map((project) => (
                <Link
                  key={project.id}
                  to={`/kanban?projectId=${project.id}`}
                  className="p-4 rounded-xl border border-slate-200/80 hover:border-blue-300 hover:shadow-xs transition-all text-left bg-slate-50/40"
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-mono font-bold text-slate-600 bg-white px-2 py-0.5 rounded border border-slate-200">
                      {project.key}
                    </span>
                    <span className="text-xs font-semibold text-blue-600">
                      {project.progressPercentage}%
                    </span>
                  </div>

                  <h4 className="text-xs font-bold text-slate-800 truncate mb-1">
                    {project.name}
                  </h4>

                  {/* Progress Bar */}
                  <div className="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden mt-2">
                    <div
                      className="bg-blue-600 h-full rounded-full transition-all duration-500"
                      style={{ width: `${project.progressPercentage}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-slate-400 mt-2">
                    <span>{project.totalIssues} issues</span>
                    <span>{project.memberCount} members</span>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </div>

        {/* Right (Personal Activity Feed) */}
        <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-5">
          <div className="flex items-center gap-2 mb-4 pb-3 border-b border-slate-100">
            <TrendingUp className="w-4 h-4 text-slate-600" />
            <h3 className="text-sm font-bold text-slate-800">Recent Activity</h3>
          </div>

          <div className="space-y-4 max-h-[460px] overflow-y-auto pr-1">
            {!dashboard?.recentActivity || dashboard.recentActivity.length === 0 ? (
              <p className="text-xs text-slate-400 text-center py-8">No recent activity recorded.</p>
            ) : (
              dashboard.recentActivity.map((log) => (
                <div key={log.id} className="text-xs text-slate-600 flex items-start gap-2.5 text-left">
                  <div className="h-2 w-2 rounded-full bg-blue-500 mt-1.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-slate-800 text-xs leading-relaxed font-medium">
                      {log.metadata || log.action}
                    </p>
                    <span className="text-[10px] text-slate-400 mt-0.5 block">
                      {formatDistanceToNow(new Date(log.timestamp), { addSuffix: true })}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Issue Detail Modal */}
      <IssueDetailModal
        issueId={selectedIssueId}
        isOpen={!!selectedIssueId}
        onClose={() => setSelectedIssueId(null)}
        onIssueUpdated={refetch}
        onIssueDeleted={refetch}
      />
    </div>
  );
};
