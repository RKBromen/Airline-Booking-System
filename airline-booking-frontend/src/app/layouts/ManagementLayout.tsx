import { Outlet } from 'react-router'
import { SessionBoundary } from '../router/guards/SessionBoundary'
import { ProtectedRoute } from '../router/guards/ProtectedRoute'
import { RoleGuard } from '../router/guards/RoleGuard'

export function ManagementLayout() {
  return <SessionBoundary><ProtectedRoute><RoleGuard roles={['ADMIN', 'STAFF']}>
    <div className="management-content"><Outlet /></div>
  </RoleGuard></ProtectedRoute></SessionBoundary>
}
