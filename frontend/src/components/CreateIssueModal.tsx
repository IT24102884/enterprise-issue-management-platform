import React, { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { projectApi, issueApi, milestoneApi } from '../api';
import { Project, ProjectMember, Milestone, IssuePriority, IssueType } from '../types';
import { AlertCircle } from 'lucide-react';
import { useQueryClient } from '@tanstack/react-query';

interface CreateIssueModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultProjectId?: number;
}

export const CreateIssueModal: React.FC<CreateIssueModalProps> = ({
  isOpen,
  onClose,
  defaultProjectId,
}) => {
  const queryClient = useQueryClient();
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(defaultProjectId);
  const [members, setMembers] = useState<ProjectMember[]>([]);
  const [milestones, setMilestones] = useState<Milestone[]>([]);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<IssueType>('TASK');
  const [priority, setPriority] = useState<IssuePriority>('MEDIUM');
  const [assigneeId, setAssigneeId] = useState<number | undefined>();
  const [milestoneId, setMilestoneId] = useState<number | undefined>();
  const [dueDate, setDueDate] = useState('');
  const [labelInput, setLabelInput] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      projectApi.getProjects(0, 50).then((data) => {
        setProjects(data.content);
        if (!selectedProjectId && data.content.length > 0) {
          setSelectedProjectId(data.content[0].id);
        }
      });
    }
  }, [isOpen]);

  useEffect(() => {
    if (selectedProjectId) {
      projectApi.getMembers(selectedProjectId).then(setMembers).catch(console.error);
      milestoneApi.getProjectMilestones(selectedProjectId, 'OPEN').then(setMilestones).catch(console.error);
    }
  }, [selectedProjectId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProjectId) {
      setError('Please select a project');
      return;
    }
    if (!title.trim()) {
      setError('Issue title is required');
      return;
    }

    setLoading(true);
    setError(null);

    const labels = labelInput
      .split(',')
      .map((l) => l.trim())
      .filter(Boolean);

    try {
      await issueApi.createIssue(selectedProjectId, {
        title: title.trim(),
        description: description.trim() || undefined,
        type,
        priority,
        assigneeId: assigneeId || undefined,
        milestoneId: milestoneId || undefined,
        dueDate: dueDate || undefined,
        labelNames: labels.length > 0 ? labels : undefined,
      });

      // Invalidate queries
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      queryClient.invalidateQueries({ queryKey: ['analytics'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });

      // Reset form & close
      setTitle('');
      setDescription('');
      setType('TASK');
      setPriority('MEDIUM');
      setAssigneeId(undefined);
      setMilestoneId(undefined);
      setDueDate('');
      setLabelInput('');
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create issue');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Issue" maxWidth="max-w-2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="flex items-center gap-2 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Project Selection */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Project *</label>
          <select
            value={selectedProjectId || ''}
            onChange={(e) => setSelectedProjectId(Number(e.target.value))}
            className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            required
          >
            {projects.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} ({p.key})
              </option>
            ))}
          </select>
        </div>

        {/* Title */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Issue Title *</label>
          <input
            type="text"
            placeholder="e.g. Fix button disabled state on auth modal"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            required
          />
        </div>

        {/* Type & Priority */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Type *</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value as IssueType)}
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            >
              <option value="BUG">🐛 Bug</option>
              <option value="TASK">📋 Task</option>
              <option value="FEATURE">✨ Feature / Story</option>
              <option value="IMPROVEMENT">⚡ Improvement</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Priority *</label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value as IssuePriority)}
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            >
              <option value="LOW">🔽 Low</option>
              <option value="MEDIUM">🔹 Medium</option>
              <option value="HIGH">🔼 High</option>
              <option value="CRITICAL">⚡ Critical</option>
            </select>
          </div>
        </div>

        {/* Description */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">Description</label>
          <textarea
            rows={3}
            placeholder="Provide context, reproduction steps, or acceptance criteria..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
          />
        </div>

        {/* Assignee & Milestone */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Assignee</label>
            <select
              value={assigneeId || ''}
              onChange={(e) => setAssigneeId(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            >
              <option value="">Unassigned</option>
              {members.map((m) => (
                <option key={m.user.id} value={m.user.id}>
                  {m.user.name} ({m.user.email})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Sprint / Milestone</label>
            <select
              value={milestoneId || ''}
              onChange={(e) => setMilestoneId(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            >
              <option value="">None</option>
              {milestones.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.title}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Due Date & Labels */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Due Date</label>
            <input
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Labels (comma-separated)</label>
            <input
              type="text"
              placeholder="frontend, auth, bugfix"
              value={labelInput}
              onChange={(e) => setLabelInput(e.target.value)}
              className="w-full text-sm rounded-lg border border-slate-300 p-2.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
            />
          </div>
        </div>

        {/* Buttons */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-5 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-all hover:shadow focus:ring-2 focus:ring-blue-500/20 disabled:opacity-50"
          >
            {loading ? 'Creating...' : 'Create Issue'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
