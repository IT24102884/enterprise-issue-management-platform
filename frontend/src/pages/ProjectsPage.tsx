import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { projectApi } from '../api';
import { CreateProjectModal } from '../components/CreateProjectModal';
import {
  FolderGit2,
  Plus,
  Search,
  Kanban,
  Target,
  BarChart3,
  Users,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

export const ProjectsPage: React.FC = () => {
  const { user } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [createProjectOpen, setCreateProjectOpen] = useState(false);

  const canCreateProject = user?.role === 'ADMIN' || user?.role === 'PROJECT_MANAGER';

  const { data: projectsData, isLoading, refetch } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectApi.getProjects(0, 50),
  });

  const filteredProjects = (projectsData?.content || []).filter(
    (p) =>
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.key.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Projects</h1>
          <p className="text-xs text-slate-500 mt-1">
            Manage your organizations, active workspaces, and team assignments.
          </p>
        </div>

        {canCreateProject && (
          <button
            onClick={() => setCreateProjectOpen(true)}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold py-2.5 px-4 rounded-xl shadow-sm transition-all hover:shadow"
          >
            <Plus className="w-4 h-4" />
            Create Project
          </button>
        )}
      </div>

      {/* Search Input */}
      <div className="relative max-w-md">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
        <input
          type="text"
          placeholder="Search by project name or key..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full text-xs rounded-xl border border-slate-300 pl-10 pr-4 py-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
        />
      </div>

      {/* Projects Grid */}
      {isLoading ? (
        <div className="py-20 text-center text-xs text-slate-400">Loading projects...</div>
      ) : filteredProjects.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center">
          <FolderGit2 className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-sm font-bold text-slate-800">No projects found</h3>
          <p className="text-xs text-slate-400 mt-1 mb-4">
            {searchTerm ? 'Try adjusting your search term' : 'Create your first project to get started'}
          </p>
          {!searchTerm && canCreateProject && (
            <button
              onClick={() => setCreateProjectOpen(true)}
              className="px-4 py-2 bg-blue-600 text-white text-xs font-semibold rounded-xl hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <Plus className="w-4 h-4" />
              Create Project
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredProjects.map((project) => (
            <div
              key={project.id}
              className="bg-white rounded-2xl border border-slate-200/80 shadow-xs hover:shadow-md transition-all p-5 flex flex-col justify-between text-left space-y-4"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="text-xs font-mono font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-md border border-blue-200">
                    {project.key}
                  </span>
                  <span className="text-[11px] font-medium text-slate-400 flex items-center gap-1">
                    <Users className="w-3.5 h-3.5" />
                    {project.memberCount} members
                  </span>
                </div>

                <h3 className="text-base font-bold text-slate-800 line-clamp-1 mb-1">
                  {project.name}
                </h3>
                <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed min-h-[36px]">
                  {project.description || 'No description provided.'}
                </p>
              </div>

              <div className="space-y-3 pt-3 border-t border-slate-100">
                {/* Progress */}
                <div>
                  <div className="flex items-center justify-between text-xs mb-1">
                    <span className="text-slate-500 font-medium">Progress</span>
                    <span className="font-bold text-blue-600">{project.progressPercentage}%</span>
                  </div>
                  <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div
                      className="bg-blue-600 h-full rounded-full transition-all duration-500"
                      style={{ width: `${project.progressPercentage}%` }}
                    />
                  </div>
                  <div className="flex items-center justify-between text-[10px] text-slate-400 mt-1">
                    <span>{project.totalIssues - project.openIssues} done</span>
                    <span>{project.openIssues} open</span>
                  </div>
                </div>

                {/* Quick Action Navigation Links */}
                <div className="grid grid-cols-3 gap-1 pt-1">
                  <Link
                    to={`/kanban?projectId=${project.id}`}
                    className="flex items-center justify-center gap-1 py-2 text-[11px] font-semibold text-slate-700 bg-slate-50 hover:bg-blue-50 hover:text-blue-700 rounded-lg transition-colors border border-slate-200/60"
                  >
                    <Kanban className="w-3.5 h-3.5" />
                    Board
                  </Link>

                  <Link
                    to={`/milestones?projectId=${project.id}`}
                    className="flex items-center justify-center gap-1 py-2 text-[11px] font-semibold text-slate-700 bg-slate-50 hover:bg-blue-50 hover:text-blue-700 rounded-lg transition-colors border border-slate-200/60"
                  >
                    <Target className="w-3.5 h-3.5" />
                    Sprints
                  </Link>

                  <Link
                    to={`/analytics?projectId=${project.id}`}
                    className="flex items-center justify-center gap-1 py-2 text-[11px] font-semibold text-slate-700 bg-slate-50 hover:bg-blue-50 hover:text-blue-700 rounded-lg transition-colors border border-slate-200/60"
                  >
                    <BarChart3 className="w-3.5 h-3.5" />
                    Analytics
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Project Modal */}
      <CreateProjectModal
        isOpen={createProjectOpen}
        onClose={() => setCreateProjectOpen(false)}
        onSuccess={refetch}
      />
    </div>
  );
};
