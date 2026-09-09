import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectApi, milestoneApi } from '../api';
import { CreateMilestoneModal } from '../components/CreateMilestoneModal';
import { Target, Plus, Calendar, CheckCircle2 } from 'lucide-react';
import { format } from 'date-fns';

export const MilestonesPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const queryClient = useQueryClient();

  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(
    searchParams.get('projectId') ? Number(searchParams.get('projectId')) : undefined
  );

  const [createMilestoneOpen, setCreateMilestoneOpen] = useState(false);

  // Fetch projects
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

  const handleProjectSelect = (id: number) => {
    setSelectedProjectId(id);
    setSearchParams({ projectId: id.toString() });
  };

  // Fetch milestones
  const { data: milestones, isLoading, refetch } = useQuery({
    queryKey: ['milestones', selectedProjectId],
    queryFn: () => (selectedProjectId ? milestoneApi.getProjectMilestones(selectedProjectId) : []),
    enabled: !!selectedProjectId,
  });

  // Close milestone mutation
  const closeMutation = useMutation({
    mutationFn: (id: number) => milestoneApi.closeMilestone(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['milestones', selectedProjectId] });
      queryClient.invalidateQueries({ queryKey: ['analytics', selectedProjectId] });
    },
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
            <Target className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block leading-tight">
              Sprints & Milestones
            </span>
            <select
              value={selectedProjectId || ''}
              onChange={(e) => handleProjectSelect(Number(e.target.value))}
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

        {selectedProjectId && (
          <button
            onClick={() => setCreateMilestoneOpen(true)}
            className="flex items-center gap-1.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold py-2 px-3.5 rounded-xl shadow-xs transition-colors self-start sm:self-center"
          >
            <Plus className="w-4 h-4" />
            New Sprint
          </button>
        )}
      </div>

      {/* Milestones List */}
      {isLoading ? (
        <div className="py-20 text-center text-xs text-slate-400">Loading sprints & milestones...</div>
      ) : !milestones || milestones.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center">
          <Target className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-sm font-bold text-slate-800">No milestones yet</h3>
          <p className="text-xs text-slate-400 mt-1 mb-4">
            Group your issues into time-boxed sprints or release targets
          </p>
          {selectedProjectId && (
            <button
              onClick={() => setCreateMilestoneOpen(true)}
              className="px-4 py-2 bg-blue-600 text-white text-xs font-semibold rounded-xl hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <Plus className="w-4 h-4" />
              Create Sprint
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {milestones.map((m) => (
            <div
              key={m.id}
              className={`bg-white rounded-2xl border p-5 shadow-xs space-y-4 text-left transition-all ${
                m.status === 'CLOSED' ? 'border-slate-200 opacity-75' : 'border-slate-200/90 hover:shadow-md'
              }`}
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        m.status === 'OPEN'
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                          : 'bg-slate-100 text-slate-600 border border-slate-200'
                      }`}
                    >
                      {m.status}
                    </span>
                    {m.dueDate && (
                      <span className="text-[11px] text-slate-400 flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        Due {format(new Date(m.dueDate), 'MMM d, yyyy')}
                      </span>
                    )}
                  </div>
                  <h3 className="text-base font-bold text-slate-800">{m.title}</h3>
                  <p className="text-xs text-slate-500 mt-0.5 line-clamp-2">
                    {m.description || 'No description provided.'}
                  </p>
                </div>

                {m.status === 'OPEN' && (
                  <button
                    onClick={() => closeMutation.mutate(m.id)}
                    className="px-2.5 py-1 text-xs font-semibold text-slate-600 hover:text-emerald-700 bg-slate-50 hover:bg-emerald-50 border border-slate-200 rounded-lg transition-colors flex items-center gap-1 flex-shrink-0"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Complete
                  </button>
                )}
              </div>

              {/* Progress Bar */}
              <div className="space-y-1.5 pt-2 border-t border-slate-100">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-500 font-medium">Sprint Completion</span>
                  <span className="font-bold text-blue-600">{m.progressPercentage}%</span>
                </div>
                <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      m.progressPercentage === 100 ? 'bg-emerald-500' : 'bg-blue-600'
                    }`}
                    style={{ width: `${m.progressPercentage}%` }}
                  />
                </div>
                <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1">
                  <span>{m.closedIssues} closed issues</span>
                  <span>{m.openIssues} open issues</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {selectedProjectId && (
        <CreateMilestoneModal
          isOpen={createMilestoneOpen}
          onClose={() => setCreateMilestoneOpen(false)}
          projectId={selectedProjectId}
          onSuccess={refetch}
        />
      )}
    </div>
  );
};
