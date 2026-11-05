import type { PropsWithChildren } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../../../features/auth/hooks/useAuth'

/** Mount after the app's initial authApi.refresh() attempt has settled. */
export function ProtectedRoute({ children }: PropsWithChildren) {
  const auth = useAuth()
  const location = useLocation()
  if (!auth.isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />
  return children
}
