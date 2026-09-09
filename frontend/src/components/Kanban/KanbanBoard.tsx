import React, { useState } from 'react';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragStartEvent,
  DragEndEvent,
} from '@dnd-kit/core';
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import { Issue, IssueStatus } from '../../types';
import { KanbanColumn } from './KanbanColumn';
import { KanbanCard } from './KanbanCard';

interface KanbanBoardProps {
  issues: Issue[];
  onStatusChange: (issueId: number, newStatus: IssueStatus) => void;
  onCardClick: (issue: Issue) => void;
  onAddIssue?: (status: IssueStatus) => void;
}

const COLUMNS: { status: IssueStatus; title: string }[] = [
  { status: 'TODO', title: 'To Do' },
  { status: 'IN_PROGRESS', title: 'In Progress' },
  { status: 'IN_REVIEW', title: 'In Review' },
  { status: 'DONE', title: 'Done' },
];

export const KanbanBoard: React.FC<KanbanBoardProps> = ({
  issues,
  onStatusChange,
  onCardClick,
  onAddIssue,
}) => {
  const [activeIssue, setActiveIssue] = useState<Issue | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 5, // 5px movement required before drag starts
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const handleDragStart = (event: DragStartEvent) => {
    const { active } = event;
    const issue = issues.find((i) => i.id.toString() === active.id);
    if (issue) {
      setActiveIssue(issue);
    }
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveIssue(null);

    if (!over) return;

    const activeIssueId = Number(active.id);
    const draggedIssue = issues.find((i) => i.id === activeIssueId);
    if (!draggedIssue) return;

    // Check if dropped onto a column or onto another card in a column
    let targetStatus: IssueStatus | null = null;

    if (COLUMNS.some((col) => col.status === over.id)) {
      targetStatus = over.id as IssueStatus;
    } else {
      const overIssue = issues.find((i) => i.id.toString() === over.id);
      if (overIssue) {
        targetStatus = overIssue.status;
      }
    }

    if (targetStatus && draggedIssue.status !== targetStatus) {
      onStatusChange(draggedIssue.id, targetStatus);
    }
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className="flex gap-4 overflow-x-auto pb-4 items-start min-h-[500px]">
        {COLUMNS.map((column) => {
          const columnIssues = issues.filter((i) => i.status === column.status);
          return (
            <KanbanColumn
              key={column.status}
              status={column.status}
              title={column.title}
              issues={columnIssues}
              onCardClick={onCardClick}
              onAddIssue={onAddIssue}
            />
          );
        })}
      </div>

      {/* Floating Drag Overlay */}
      <DragOverlay>
        {activeIssue ? (
          <div className="rotate-2 scale-105 shadow-2xl">
            <KanbanCard issue={activeIssue} onClick={() => {}} />
          </div>
        ) : null}
      </DragOverlay>
    </DndContext>
  );
};

