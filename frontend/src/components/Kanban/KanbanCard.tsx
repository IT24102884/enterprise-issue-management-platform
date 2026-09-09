import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Issue } from '../../types';
import { PriorityBadge, TypeBadge } from '../Badges';
import { Calendar, MessageSquare } from 'lucide-react';
import { format } from 'date-fns';

interface KanbanCardProps {
  issue: Issue;
  onClick: (issue: Issue) => void;
}

export const KanbanCard: React.FC<KanbanCardProps> = ({ issue, onClick }) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: issue.id.toString(),
    data: { issue },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onClick={() => onClick(issue)}
      className="group relative cursor-grab active:cursor-grabbing rounded-xl bg-white p-3.5 shadow-xs hover:shadow-md border border-slate-200/80 transition-all hover:border-blue-300 text-left space-y-2.5"
    >
      {/* Top Header: Key + Badges */}
      <div className="flex items-center justify-between gap-1.5">
        <span className="text-xs font-mono font-bold text-slate-500 group-hover:text-blue-600 transition-colors">
          {issue.issueKey}
        </span>
        <div className="flex items-center gap-1">
          <TypeBadge type={issue.type} />
          <PriorityBadge priority={issue.priority} />
        </div>
      </div>

      {/* Issue Title */}
      <h4 className="text-sm font-semibold text-slate-800 line-clamp-2 leading-snug">
        {issue.title}
      </h4>

      {/* Labels */}
      {issue.labels && issue.labels.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {issue.labels.slice(0, 3).map((l) => (
            <span
              key={l.id}
              className="text-[10px] font-medium px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 border border-slate-200"
            >
              {l.name}
            </span>
          ))}
          {issue.labels.length > 3 && (
            <span className="text-[10px] text-slate-400">+{issue.labels.length - 3}</span>
          )}
        </div>
      )}

      {/* Footer Info: Due date, comments, Assignee */}
      <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-[11px] text-slate-400">
        <div className="flex items-center gap-2.5">
          {issue.dueDate && (
            <span className="flex items-center gap-1 text-slate-500">
              <Calendar className="w-3 h-3 text-slate-400" />
              {format(new Date(issue.dueDate), 'MMM d')}
            </span>
          )}
          {issue.commentCount > 0 && (
            <span className="flex items-center gap-1">
              <MessageSquare className="w-3 h-3" />
              {issue.commentCount}
            </span>
          )}
        </div>

        {/* Assignee Avatar */}
        {issue.assignee ? (
          <div
            title={`Assigned to ${issue.assignee.name}`}
            className="h-5 w-5 rounded-full bg-blue-100 text-blue-700 font-bold text-[9px] flex items-center justify-center border border-blue-200"
          >
            {issue.assignee.name.charAt(0).toUpperCase()}
          </div>
        ) : (
          <div className="h-5 w-5 rounded-full border border-dashed border-slate-300 text-[10px] text-slate-300 flex items-center justify-center">
            -
          </div>
        )}
      </div>
    </div>
  );
};

