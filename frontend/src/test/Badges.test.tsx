import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatusBadge, PriorityBadge, TypeBadge, RoleBadge } from '../components/Badges';

describe('Badges', () => {
  it('renders StatusBadge correctly for various statuses', () => {
    const { rerender } = render(<StatusBadge status="TODO" />);
    expect(screen.getByText('To Do')).toBeTruthy();

    rerender(<StatusBadge status="IN_PROGRESS" />);
    expect(screen.getByText('In Progress')).toBeTruthy();

    rerender(<StatusBadge status="DONE" />);
    expect(screen.getByText('Done')).toBeTruthy();
  });

  it('renders PriorityBadge correctly', () => {
    const { rerender } = render(<PriorityBadge priority="CRITICAL" />);
    expect(screen.getByText(/Critical/i)).toBeTruthy();

    rerender(<PriorityBadge priority="HIGH" />);
    expect(screen.getByText(/High/i)).toBeTruthy();

    rerender(<PriorityBadge priority="LOW" />);
    expect(screen.getByText(/Low/i)).toBeTruthy();
  });

  it('renders TypeBadge correctly', () => {
    const { rerender } = render(<TypeBadge type="BUG" />);
    expect(screen.getByText(/Bug/i)).toBeTruthy();

    rerender(<TypeBadge type="FEATURE" />);
    expect(screen.getByText(/Feature/i)).toBeTruthy();

    rerender(<TypeBadge type="TASK" />);
    expect(screen.getByText(/Task/i)).toBeTruthy();
  });

  it('renders RoleBadge correctly', () => {
    const { rerender } = render(<RoleBadge role="ADMIN" />);
    expect(screen.getByText('Admin')).toBeTruthy();

    rerender(<RoleBadge role="DEVELOPER" />);
    expect(screen.getByText('Developer')).toBeTruthy();
  });
});
