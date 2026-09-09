import React from 'react';
import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Issue, IssueStatus } from '../../types';
import { KanbanCard } from './KanbanCard';
import { Plus } from 'lucide-react';

interface KanbanColumnProps {
  status: IssueStatus;
  title: string;
  issues: Issue[];
  onCardClick: (issue: Issue) => void;
  onAddIssue?: (status: IssueStatus) => void;
}

export const KanbanColumn: React.FC<KanbanColumnProps> = ({
  status,
  title,
  issues,
  onCardClick,
  onAddIssue,
}) => {
  const { setNodeRef, isOver } = useDroppable({
    id: status,
  });

  const columnStyles: Record<IssueStatus, { border: string; badge: string }> = {
    TODO: { border: 'border-slate-300', badge: 'bg-slate-200 text-slate-700' },
    IN_PROGRESS: { border: 'border-blue-300', badge: 'bg-blue-100 text-blue-700' },
    IN_REVIEW: { border: 'border-purple-300', badge: 'bg-purple-100 text-purple-700' },
    DONE: { border: 'border-emerald-300', badge: 'bg-emerald-100 text-emerald-700' },
    CLOSED: { border: 'border-gray-300', badge: 'bg-gray-200 text-gray-700' },
  };

  const style = columnStyles[status] || columnStyles.TODO;

  return (
    <div
      ref={setNodeRef}
      className={`flex flex-col rounded-2xl bg-slate-100/70 p-3 min-w-[280px] w-full max-w-sm border transition-colors ${
        isOver ? 'bg-blue-50/50 border-blue-400' : 'border-slate-200/70'
      }`}
    >
      {/* Column Header */}
      <div className="flex items-center justify-between px-1.5 py-1 mb-2.5">
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-slate-800 uppercase tracking-wide">
            {title}
          </span>
          <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full ${style.badge}`}>
            {issues.length}
          </span>
        </div>

        {onAddIssue && (
          <button
            onClick={() => onAddIssue(status)}
            className="p-1 text-slate-400 hover:text-slate-700 hover:bg-slate-200/60 rounded-md transition-colors"
            title={`Add issue to ${title}`}
          >
            <Plus className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Card List Container */}
      <SortableContext
        items={issues.map((i) => i.id.toString())}
        strategy={verticalListSortingStrategy}
      >
        <div className="flex-1 space-y-2.5 overflow-y-auto min-h-[150px] max-h-[calc(100vh-280px)] pr-0.5">
          {issues.map((issue) => (
            <KanbanCard key={issue.id} issue={issue} onClick={onCardClick} />
          ))}

          {issues.length === 0 && (
            <div className="h-24 rounded-xl border border-dashed border-slate-300 flex items-center justify-center text-xs text-slate-400">
              Drop issues here
            </div>
          )}
        </div>
      </SortableContext>
    </div>
  );
};

