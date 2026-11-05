import type { PropsWithChildren } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../../features/auth/hooks/useAuth'
import type { Role } from '../../features/auth/types/auth.enums'

/** Mount after the app's initial authApi.refresh() attempt has settled. */
export function ProtectedRoute({ children, roles }: PropsWithChildren<{ roles?: readonly Role[] }>) {
  const auth = useAuth()
  const location = useLocation()
  if (!auth.isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />
  if (roles?.length && !roles.some(role => auth.roles.includes(role))) return <Navigate to="/403" replace />
  return children
}
