import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectApi, issueApi } from '../api';
import { Issue, IssueStatus, IssuePriority, IssueType } from '../types';
import { KanbanBoard } from '../components/Kanban/KanbanBoard';
import { CreateIssueModal } from '../components/CreateIssueModal';
import { IssueDetailModal } from '../components/IssueDetailModal';
import { Plus, Search, Kanban, FolderGit2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const KanbanPage: React.FC = () => {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const queryClient = useQueryClient();

  const canCreateIssue = user?.role !== 'VIEWER';
  const canChangeStatus = user?.role !== 'VIEWER';

  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(
    searchParams.get('projectId') ? Number(searchParams.get('projectId')) : undefined
  );

  const [search, setSearch] = useState('');
  const [priorityFilter, setPriorityFilter] = useState<IssuePriority | ''>('');
  const [typeFilter, setTypeFilter] = useState<IssueType | ''>('');

  const [createIssueOpen, setCreateIssueOpen] = useState(false);
  const [selectedIssueId, setSelectedIssueId] = useState<number | null>(null);

  // Fetch all projects for dropdown
  const { data: projectsData } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectApi.getProjects(0, 50),
  });

  const projects = projectsData?.content || [];

  // Default to first project if none selected
  useEffect(() => {
    if (!selectedProjectId && projects.length > 0) {
      setSelectedProjectId(projects[0].id);
      setSearchParams({ projectId: projects[0].id.toString() });
    }
  }, [projects, selectedProjectId]);

  const handleProjectSelect = (id: number) => {
    setSelectedProjectId(id);
    setSearchParams({ projectId: id.toString() });
  };

  // Fetch issues for selected project
  const { data: issuesData, isLoading } = useQuery({
    queryKey: ['issues', selectedProjectId],
    queryFn: () =>
      selectedProjectId
        ? issueApi.getProjectIssues(selectedProjectId, { size: 100 })
        : Promise.resolve({ content: [], page: 0, size: 100, totalElements: 0, totalPages: 0, last: true }),
    enabled: !!selectedProjectId,
  });

  const issues = issuesData?.content || [];

  // Filter issues locally by search, priority, type
  const filteredIssues = issues.filter((issue) => {
    const matchesSearch =
      !search ||
      issue.title.toLowerCase().includes(search.toLowerCase()) ||
      issue.issueKey.toLowerCase().includes(search.toLowerCase()) ||
      (issue.description && issue.description.toLowerCase().includes(search.toLowerCase()));

    const matchesPriority = !priorityFilter || issue.priority === priorityFilter;
    const matchesType = !typeFilter || issue.type === typeFilter;

    return matchesSearch && matchesPriority && matchesType;
  });

  // Mutation for updating status on drag-and-drop
  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: IssueStatus }) =>
      issueApi.updateIssueStatus(id, status),
    onMutate: async ({ id, status }) => {
      // Optimistic update
      await queryClient.cancelQueries({ queryKey: ['issues', selectedProjectId] });
      const previousIssues = queryClient.getQueryData(['issues', selectedProjectId]);

      queryClient.setQueryData(['issues', selectedProjectId], (old: any) => {
        if (!old) return old;
        return {
          ...old,
          content: old.content.map((issue: Issue) =>
            issue.id === id ? { ...issue, status } : issue
          ),
        };
      });

      return { previousIssues };
    },
    onError: (_err, _variables, context) => {
      if (context?.previousIssues) {
        queryClient.setQueryData(['issues', selectedProjectId], context.previousIssues);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['issues', selectedProjectId] });
      queryClient.invalidateQueries({ queryKey: ['analytics', selectedProjectId] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });

  const handleStatusChange = (issueId: number, newStatus: IssueStatus) => {
    statusMutation.mutate({ id: issueId, status: newStatus });
  };

  return (
    <div className="space-y-5">
      {/* Header Controls */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-4 sm:p-5 rounded-2xl border border-slate-200/80 shadow-xs">
        {/* Project Selector & Title */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="h-10 w-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
            <Kanban className="w-5 h-5" />
          </div>

          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block leading-tight">
              Kanban Workspace
            </span>
            <div className="flex items-center gap-2 mt-0.5">
              <select
                value={selectedProjectId || ''}
                onChange={(e) => handleProjectSelect(Number(e.target.value))}
                className="text-base font-bold text-slate-800 bg-transparent border-none outline-none cursor-pointer hover:text-blue-600 transition-colors p-0"
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

        {/* Action Controls: Search, Filters, Add Issue */}
        <div className="flex flex-wrap items-center gap-2.5">
          {/* Search */}
          <div className="relative">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Filter tasks..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="text-xs rounded-xl border border-slate-300 pl-8 pr-3 py-2 bg-slate-50 text-slate-800 focus:bg-white focus:ring-2 focus:ring-blue-500/20 outline-none w-36 sm:w-44"
            />
          </div>

          {/* Priority filter */}
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value as IssuePriority | '')}
            className="text-xs rounded-xl border border-slate-300 px-3 py-2 bg-slate-50 text-slate-700 outline-none"
          >
            <option value="">All Priorities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          {/* Type filter */}
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value as IssueType | '')}
            className="text-xs rounded-xl border border-slate-300 px-3 py-2 bg-slate-50 text-slate-700 outline-none"
          >
            <option value="">All Types</option>
            <option value="BUG">Bug</option>
            <option value="FEATURE">Feature</option>
            <option value="TASK">Task</option>
            <option value="IMPROVEMENT">Improvement</option>
          </select>

          {/* Add Issue Button */}
          {canCreateIssue && (
            <button
              onClick={() => setCreateIssueOpen(true)}
              className="flex items-center gap-1.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold py-2 px-3.5 rounded-xl shadow-xs transition-colors"
            >
              <Plus className="w-4 h-4" />
              New Issue
            </button>
          )}
        </div>
      </div>

      {/* Kanban Board Container */}
      {isLoading ? (
        <div className="py-24 text-center text-xs text-slate-400">Loading board...</div>
      ) : !selectedProjectId ? (
        <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center">
          <FolderGit2 className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-sm font-bold text-slate-800">No project selected</h3>
          <p className="text-xs text-slate-400 mt-1">Please select or create a project to view the Kanban board</p>
        </div>
      ) : (
        <KanbanBoard
          issues={filteredIssues}
          onStatusChange={canChangeStatus ? handleStatusChange : undefined}
          onCardClick={(issue) => setSelectedIssueId(issue.id)}
          onAddIssue={canCreateIssue ? () => setCreateIssueOpen(true) : undefined}
        />
      )}

      {/* Modals */}
      <CreateIssueModal
        isOpen={createIssueOpen}
        onClose={() => setCreateIssueOpen(false)}
        defaultProjectId={selectedProjectId}
      />

      <IssueDetailModal
        issueId={selectedIssueId}
        isOpen={!!selectedIssueId}
        onClose={() => setSelectedIssueId(null)}
      />
    </div>
  );
};
