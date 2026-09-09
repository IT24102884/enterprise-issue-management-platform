import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from '../App'

const renderWithProviders = (ui: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        {ui}
      </QueryClientProvider>
    </MemoryRouter>
  )
}

describe('App', () => {
  it('renders login page when unauthenticated', () => {
    renderWithProviders(<App />)
    expect(screen.getByText(/Welcome Back/i)).toBeTruthy()
    expect(screen.getByPlaceholderText(/name@example.com/i)).toBeTruthy()
  })
})
