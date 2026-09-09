import React, { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { issueApi, projectApi, milestoneApi } from '../api';
import {
  Issue,
  IssueStatus,
  IssuePriority,
  IssueComment,
  AuditLog,
  ProjectMember,
  Milestone,
} from '../types';
import { PriorityBadge, TypeBadge } from './Badges';
import {
  MessageSquare,
  History,
  Send,
  Trash2,
  Clock,
} from 'lucide-react';
import { formatDistanceToNow, format } from 'date-fns';
import { useAuth } from '../context/AuthContext';
import { useQueryClient } from '@tanstack/react-query';

interface IssueDetailModalProps {
  issueId: number | null;
  isOpen: boolean;
  onClose: () => void;
  onIssueUpdated?: () => void;
  onIssueDeleted?: () => void;
}

export const IssueDetailModal: React.FC<IssueDetailModalProps> = ({
  issueId,
  isOpen,
  onClose,
  onIssueUpdated,
  onIssueDeleted,
}) => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const isViewer = user?.role === 'VIEWER';

  const [issue, setIssue] = useState<Issue | null>(null);
  const [comments, setComments] = useState<IssueComment[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [members, setMembers] = useState<ProjectMember[]>([]);
  const [milestones, setMilestones] = useState<Milestone[]>([]);

  const [activeTab, setActiveTab] = useState<'comments' | 'history'>('comments');
  const [newComment, setNewComment] = useState('');
  const [isPostingComment, setIsPostingComment] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchIssueData = async () => {
    if (!issueId) return;
    setLoading(true);
    setError(null);
    try {
      const issueData = await issueApi.getIssueById(issueId);
      setIssue(issueData);

      const [commentsData, auditData, membersData, milestonesData] = await Promise.all([
        issueApi.getComments(issueId),
        issueApi.getAuditLogs(issueId),
        projectApi.getMembers(issueData.projectId),
        milestoneApi.getProjectMilestones(issueData.projectId, 'OPEN'),
      ]);

      setComments(commentsData);
      setAuditLogs(auditData.content);
      setMembers(membersData);
      setMilestones(milestonesData);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load issue details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && issueId) {
      fetchIssueData();
    }
  }, [isOpen, issueId]);

  const handleStatusChange = async (newStatus: IssueStatus) => {
    if (!issue) return;
    try {
      const updated = await issueApi.updateIssueStatus(issue.id, newStatus);
      setIssue(updated);
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      queryClient.invalidateQueries({ queryKey: ['analytics'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      if (onIssueUpdated) onIssueUpdated();
      // Refresh audit logs
      const auditData = await issueApi.getAuditLogs(issue.id);
      setAuditLogs(auditData.content);
    } catch (e) {
      console.error('Failed to update status', e);
    }
  };

  const handlePriorityChange = async (newPriority: IssuePriority) => {
    if (!issue) return;
    try {
      const updated = await issueApi.updateIssue(issue.id, { priority: newPriority });
      setIssue(updated);
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      if (onIssueUpdated) onIssueUpdated();
    } catch (e) {
      console.error('Failed to update priority', e);
    }
  };

  const handleAssigneeChange = async (newAssigneeId: number | undefined) => {
    if (!issue) return;
    try {
      const updated = await issueApi.updateIssue(issue.id, { assigneeId: newAssigneeId });
      setIssue(updated);
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      if (onIssueUpdated) onIssueUpdated();
    } catch (e) {
      console.error('Failed to update assignee', e);
    }
  };

  const handleMilestoneChange = async (newMilestoneId: number | undefined) => {
    if (!issue) return;
    try {
      const updated = await issueApi.updateIssue(issue.id, { milestoneId: newMilestoneId });
      setIssue(updated);
      queryClient.invalidateQueries({ queryKey: ['issues'] });
      if (onIssueUpdated) onIssueUpdated();
    } catch (e) {
      console.error('Failed to update milestone', e);
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!issue || !newComment.trim()) return;

    setIsPostingComment(true);
    try {
      const comment = await issueApi.addComment(issue.id, newComment.trim());
      setComments((prev) => [...prev, comment]);
      setNewComment('');
      // Refresh audit logs
      const auditData = await issueApi.getAuditLogs(issue.id);
      setAuditLogs(auditData.content);
    } catch (e) {
      console.error('Failed to add comment', e);
    } finally {
      setIsPostingComment(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    try {
      await issueApi.deleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (e) {
      console.error('Failed to delete comment', e);
    }
  };

  const handleDeleteIssue = async () => {
    if (!issue) return;
    if (window.confirm(`Are you sure you want to delete ${issue.issueKey}?`)) {
      try {
        await issueApi.deleteIssue(issue.id);
        queryClient.invalidateQueries({ queryKey: ['issues'] });
        queryClient.invalidateQueries({ queryKey: ['analytics'] });
        if (onIssueDeleted) onIssueDeleted();
        onClose();
      } catch (e) {
        console.error('Failed to delete issue', e);
      }
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} maxWidth="max-w-4xl">
      {loading ? (
        <div className="py-16 text-center text-sm text-slate-500">Loading issue details...</div>
      ) : error ? (
        <div className="p-4 bg-rose-50 text-rose-700 rounded-lg text-sm">{error}</div>
      ) : issue ? (
        <div className="space-y-6">
          {/* Header */}
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-4">
            <div className="flex items-center gap-2">
              <span className="text-sm font-mono font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-md border border-blue-200">
                {issue.issueKey}
              </span>
              <TypeBadge type={issue.type} />
              <PriorityBadge priority={issue.priority} />
            </div>

            <div className="flex items-center gap-2">
              <select
                value={issue.status}
                disabled={isViewer}
                onChange={(e) => handleStatusChange(e.target.value as IssueStatus)}
                className="text-xs font-semibold rounded-lg border border-slate-300 px-3 py-1.5 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 outline-none disabled:opacity-60 disabled:cursor-not-allowed"
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="IN_REVIEW">In Review</option>
                <option value="DONE">Done</option>
                <option value="CLOSED">Closed</option>
              </select>

              {user?.role === 'ADMIN' && (
                <button
                  onClick={handleDeleteIssue}
                  title="Delete issue"
                  className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              )}
            </div>
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left Column (Details + Comments) */}
            <div className="lg:col-span-2 space-y-6">
              <div>
                <h2 className="text-xl font-bold text-slate-900 mb-2 leading-tight">
                  {issue.title}
                </h2>
                <div className="bg-slate-50/70 rounded-xl p-4 border border-slate-100 min-h-[100px]">
                  <p className="text-sm text-slate-700 whitespace-pre-wrap leading-relaxed">
                    {issue.description || <span className="text-slate-400 italic">No description provided.</span>}
                  </p>
                </div>
              </div>

              {/* Labels */}
              {issue.labels && issue.labels.length > 0 && (
                <div className="flex flex-wrap items-center gap-1.5">
                  <span className="text-xs text-slate-400 mr-1">Labels:</span>
                  {issue.labels.map((l) => (
                    <span
                      key={l.id}
                      className="px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-700 border border-slate-200"
                    >
                      {l.name}
                    </span>
                  ))}
                </div>
              )}

              {/* Tabs (Comments vs History) */}
              <div className="border-t border-slate-200 pt-4">
                <div className="flex items-center gap-4 border-b border-slate-100 pb-2 mb-4">
                  <button
                    onClick={() => setActiveTab('comments')}
                    className={`flex items-center gap-1.5 text-xs font-semibold pb-1.5 border-b-2 transition-colors ${
                      activeTab === 'comments'
                        ? 'border-blue-600 text-blue-600'
                        : 'border-transparent text-slate-500 hover:text-slate-800'
                    }`}
                  >
                    <MessageSquare className="w-4 h-4" />
                    Comments ({comments.length})
                  </button>
                  <button
                    onClick={() => setActiveTab('history')}
                    className={`flex items-center gap-1.5 text-xs font-semibold pb-1.5 border-b-2 transition-colors ${
                      activeTab === 'history'
                        ? 'border-blue-600 text-blue-600'
                        : 'border-transparent text-slate-500 hover:text-slate-800'
                    }`}
                  >
                    <History className="w-4 h-4" />
                    Activity History ({auditLogs.length})
                  </button>
                </div>

                {/* Tab: Comments */}
                {activeTab === 'comments' && (
                  <div className="space-y-4">
                    {/* Add Comment Input */}
                    {!isViewer && (
                      <form onSubmit={handleAddComment} className="flex gap-2">
                        <input
                          type="text"
                          placeholder="Write a comment..."
                          value={newComment}
                          onChange={(e) => setNewComment(e.target.value)}
                          className="flex-1 text-xs rounded-lg border border-slate-300 px-3 py-2 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none"
                        />
                        <button
                          type="submit"
                          disabled={isPostingComment || !newComment.trim()}
                          className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold transition-colors disabled:opacity-50 flex items-center gap-1"
                        >
                          <Send className="w-3.5 h-3.5" />
                          Post
                        </button>
                      </form>
                    )}

                    {/* Comments List */}
                    <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
                      {comments.length === 0 ? (
                        <p className="text-xs text-slate-400 text-center py-4">No comments yet. Start the conversation!</p>
                      ) : (
                        comments.map((comment) => (
                          <div
                            key={comment.id}
                            className="p-3 bg-slate-50 rounded-xl border border-slate-100 flex items-start justify-between gap-3 text-left"
                          >
                            <div className="flex items-start gap-2.5 flex-1 min-w-0">
                              <div className="h-6 w-6 rounded-full bg-blue-100 text-blue-700 font-bold text-[10px] flex items-center justify-center flex-shrink-0">
                                {comment.user?.name?.charAt(0).toUpperCase() || 'U'}
                              </div>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2">
                                  <span className="text-xs font-semibold text-slate-800">
                                    {comment.user?.name || 'User'}
                                  </span>
                                  <span className="text-[10px] text-slate-400">
                                    {formatDistanceToNow(new Date(comment.createdAt), { addSuffix: true })}
                                  </span>
                                </div>
                                <p className="text-xs text-slate-700 mt-1 whitespace-pre-wrap leading-relaxed">
                                  {comment.comment}
                                </p>
                              </div>
                            </div>

                            {(user?.id === comment.user?.id || user?.role === 'ADMIN') && (
                              <button
                                onClick={() => handleDeleteComment(comment.id)}
                                className="text-slate-400 hover:text-rose-600 p-1 transition-colors"
                              >
                                <Trash2 className="w-3 h-3" />
                              </button>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}

                {/* Tab: Activity History */}
                {activeTab === 'history' && (
                  <div className="space-y-2.5 max-h-60 overflow-y-auto pr-1">
                    {auditLogs.length === 0 ? (
                      <p className="text-xs text-slate-400 text-center py-4">No history recorded yet.</p>
                    ) : (
                      auditLogs.map((log) => (
                        <div key={log.id} className="text-xs text-slate-600 flex items-start gap-2 py-1.5 border-b border-slate-50">
                          <Clock className="w-3.5 h-3.5 text-slate-400 flex-shrink-0 mt-0.5" />
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
                )}
              </div>
            </div>

            {/* Right Column (Attributes Sidebar) */}
            <div className="space-y-4 bg-slate-50/70 p-4 rounded-xl border border-slate-100 text-left text-xs">
              <span className="font-semibold text-slate-800 text-xs block mb-3 uppercase tracking-wider text-[11px] text-slate-400">
                Attributes
              </span>

              {/* Priority */}
              <div>
                <label className="text-slate-500 font-medium block mb-1">Priority</label>
                <select
                  value={issue.priority}
                  disabled={isViewer}
                  onChange={(e) => handlePriorityChange(e.target.value as IssuePriority)}
                  className="w-full text-xs rounded-lg border border-slate-300 p-2 bg-white text-slate-800 outline-none disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>

              {/* Assignee */}
              <div>
                <label className="text-slate-500 font-medium block mb-1">Assignee</label>
                <select
                  value={issue.assignee?.id || ''}
                  disabled={isViewer}
                  onChange={(e) => handleAssigneeChange(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full text-xs rounded-lg border border-slate-300 p-2 bg-white text-slate-800 outline-none disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  <option value="">Unassigned</option>
                  {members.map((m) => (
                    <option key={m.user.id} value={m.user.id}>
                      {m.user.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Milestone */}
              <div>
                <label className="text-slate-500 font-medium block mb-1">Milestone / Sprint</label>
                <select
                  value={issue.milestoneId || ''}
                  disabled={isViewer}
                  onChange={(e) => handleMilestoneChange(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full text-xs rounded-lg border border-slate-300 p-2 bg-white text-slate-800 outline-none disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  <option value="">None</option>
                  {milestones.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.title}
                    </option>
                  ))}
                </select>
              </div>

              {/* Reporter */}
              <div className="pt-2 border-t border-slate-200">
                <span className="text-slate-400 block mb-0.5">Reporter</span>
                <span className="font-semibold text-slate-700">{issue.reporter?.name || 'Unknown'}</span>
              </div>

              {/* Created / Updated */}
              <div className="pt-2 border-t border-slate-200 space-y-1 text-[11px] text-slate-400">
                <div>
                  Created: {issue.createdAt ? format(new Date(issue.createdAt), 'MMM d, yyyy') : 'N/A'}
                </div>
                {issue.dueDate && (
                  <div className="text-amber-600 font-medium">
                    Due: {format(new Date(issue.dueDate), 'MMM d, yyyy')}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </Modal>
  );
};
