import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { projectApi, analyticsApi } from '../api';
import { BarChart3 } from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import { formatDistanceToNow } from 'date-fns';

const STATUS_COLORS: Record<string, string> = {
  TODO: '#94a3b8',
  IN_PROGRESS: '#3b82f6',
  IN_REVIEW: '#a855f7',
  DONE: '#10b981',
  CLOSED: '#64748b',
};

const PRIORITY_COLORS: Record<string, string> = {
  LOW: '#64748b',
  MEDIUM: '#3b82f6',
  HIGH: '#f59e0b',
  CRITICAL: '#ef4444',
};

export const AnalyticsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(
    searchParams.get('projectId') ? Number(searchParams.get('projectId')) : undefined
  );

  const { data: projectsData } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectApi.getProjects(0, 50),
  });

  const projects = projectsData?.content || [];

  useEffect(() => {
    if (!selectedProjectId && projects.length > 0) {
      setSelectedProjectId(projects[0].id);
      setSearchParams({ projectId: projects[0].id.toString() });
    }
  }, [projects, selectedProjectId]);

  const { data: analytics, isLoading } = useQuery({
    queryKey: ['analytics', selectedProjectId],
    queryFn: () => (selectedProjectId ? analyticsApi.getProjectAnalytics(selectedProjectId) : null),
    enabled: !!selectedProjectId,
  });

  // Prepare chart data
  const statusData = analytics?.statusBreakdown
    ? Object.entries(analytics.statusBreakdown).map(([status, count]) => ({
        name: status.replace('_', ' '),
        count,
        fill: STATUS_COLORS[status] || '#3b82f6',
      }))
    : [];

  const priorityData = analytics?.priorityBreakdown
    ? Object.entries(analytics.priorityBreakdown).map(([priority, count]) => ({
        name: priority,
        value: count,
        fill: PRIORITY_COLORS[priority] || '#3b82f6',
      }))
    : [];


  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center flex-shrink-0">
            <BarChart3 className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block leading-tight">
              Project Analytics
            </span>
            <select
              value={selectedProjectId || ''}
              onChange={(e) => {
                const val = Number(e.target.value);
                setSelectedProjectId(val);
                setSearchParams({ projectId: val.toString() });
              }}
              className="text-base font-bold text-slate-800 bg-transparent border-none outline-none cursor-pointer hover:text-blue-600 transition-colors p-0 mt-0.5"
            >
              {projects.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.key})
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {isLoading ? (
        <div className="py-20 text-center text-xs text-slate-400">Loading analytics...</div>
      ) : !analytics ? (
        <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center text-xs text-slate-400">
          Select a project to view analytics.
        </div>
      ) : (
        <>
          {/* Key Summary Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Overall Progress
              </span>
              <h3 className="text-2xl font-bold text-blue-600 mt-1">
                {analytics.progressPercentage}%
              </h3>
              <p className="text-[11px] text-slate-400 mt-1">
                {analytics.completedIssues} of {analytics.totalIssues} issues completed
              </p>
            </div>

            <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Open Tasks
              </span>
              <h3 className="text-2xl font-bold text-amber-600 mt-1">
                {analytics.openIssues}
              </h3>
              <p className="text-[11px] text-slate-400 mt-1">Pending resolution</p>
            </div>

            <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Team Members
              </span>
              <h3 className="text-2xl font-bold text-indigo-600 mt-1">
                {analytics.memberCount}
              </h3>
              <p className="text-[11px] text-slate-400 mt-1">Collaborators in project</p>
            </div>

            <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Sprints / Milestones
              </span>
              <h3 className="text-2xl font-bold text-purple-600 mt-1">
                {analytics.milestoneCount}
              </h3>
              <p className="text-[11px] text-slate-400 mt-1">Active targets</p>
            </div>
          </div>

          {/* Charts Row */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Status Breakdown Bar Chart */}
            <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs text-left">
              <h3 className="text-sm font-bold text-slate-800 mb-4">Issues by Status</h3>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={statusData}>
                    <XAxis dataKey="name" fontSize={11} tickLine={false} />
                    <YAxis allowDecimals={false} fontSize={11} tickLine={false} />
                    <Tooltip />
                    <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                      {statusData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.fill} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Priority Distribution Pie Chart */}
            <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs text-left">
              <h3 className="text-sm font-bold text-slate-800 mb-4">Priority Breakdown</h3>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={priorityData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      label={({ name, value }) => `${name}: ${value}`}
                      fontSize={11}
                    >
                      {priorityData.map((entry, index) => (
                        <Cell key={`pie-cell-${index}`} fill={entry.fill} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend wrapperStyle={{ fontSize: '11px' }} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          {/* Recent Activity Stream */}
          <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs text-left">
            <h3 className="text-sm font-bold text-slate-800 mb-3">Project Activity Trail</h3>
            <div className="divide-y divide-slate-100 max-h-60 overflow-y-auto">
              {!analytics.recentActivity || analytics.recentActivity.length === 0 ? (
                <p className="text-xs text-slate-400 py-6 text-center">No recent activity logs.</p>
              ) : (
                analytics.recentActivity.map((log) => (
                  <div key={log.id} className="py-2.5 flex items-start gap-3 text-xs text-slate-600">
                    <div className="h-2 w-2 rounded-full bg-blue-500 mt-1.5 flex-shrink-0" />
                    <div className="flex-1">
                      <span className="font-semibold text-slate-800">{log.user?.name || 'System'}: </span>
                      <span>{log.metadata || log.action}</span>
                      <span className="text-[10px] text-slate-400 block mt-0.5">
                        {formatDistanceToNow(new Date(log.timestamp), { addSuffix: true })}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};
