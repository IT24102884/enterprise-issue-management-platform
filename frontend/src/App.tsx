import { Routes, Route, Navigate } from 'react-router-dom'

// Placeholder pages — will be replaced in later phases
const ComingSoon = ({ page }: { page: string }) => (
  <div className="flex items-center justify-center min-h-screen bg-gray-50">
    <div className="text-center">
      <h1 className="text-3xl font-bold text-gray-900 mb-2">
        Enterprise Issue Manager
      </h1>
      <p className="text-gray-500 text-lg">
        <span className="font-medium text-blue-600">{page}</span> — Phase 1 Setup ✅
      </p>
      <p className="text-sm text-gray-400 mt-4">
        Backend: <code className="bg-gray-100 px-2 py-1 rounded">http://localhost:8080</code>
      </p>
    </div>
  </div>
)

function App() {
  return (
    <Routes>
      {/* Auth routes */}
      <Route path="/login" element={<ComingSoon page="Login" />} />
      <Route path="/register" element={<ComingSoon page="Register" />} />

      {/* App routes */}
      <Route path="/dashboard" element={<ComingSoon page="Dashboard" />} />
      <Route path="/projects" element={<ComingSoon page="Projects" />} />
      <Route path="/projects/:projectId" element={<ComingSoon page="Project Detail" />} />
      <Route path="/projects/:projectId/board" element={<ComingSoon page="Kanban Board" />} />
      <Route path="/projects/:projectId/issues" element={<ComingSoon page="Issues" />} />
      <Route path="/projects/:projectId/members" element={<ComingSoon page="Members" />} />
      <Route path="/projects/:projectId/settings" element={<ComingSoon page="Settings" />} />
      <Route path="/issues/:issueId" element={<ComingSoon page="Issue Detail" />} />
      <Route path="/profile" element={<ComingSoon page="Profile" />} />
      <Route path="/notifications" element={<ComingSoon page="Notifications" />} />

      {/* Default redirect */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
