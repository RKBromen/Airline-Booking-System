import type { PropsWithChildren } from 'react'
import { Navigate } from 'react-router'
import { useAuth } from '../../../features/auth/hooks/useAuth'
import type { Role } from '../../../features/auth/types/auth.enums'
import { ROUTES } from '../paths'

export function RoleGuard({ roles, children }: PropsWithChildren<{ roles: readonly Role[] }>) {
  const auth = useAuth()
  if (!roles.some(role => auth.roles.includes(role))) return <Navigate to={ROUTES.FORBIDDEN} replace />
  return children
}
