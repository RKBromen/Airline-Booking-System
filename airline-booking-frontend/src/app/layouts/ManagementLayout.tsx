import { Outlet } from 'react-router'
import { SessionBoundary } from '../router/guards/SessionBoundary'
import { ProtectedRoute } from '../router/guards/ProtectedRoute'
import { RoleGuard } from '../router/guards/RoleGuard'
import { Sidebar } from '../../components/layout/Sidebar'

export function ManagementLayout() {
  return <SessionBoundary><ProtectedRoute><RoleGuard roles={['ADMIN', 'STAFF']}>
    <div className="management-shell"><Sidebar /><div className="management-content"><Outlet /></div></div>
  </RoleGuard></ProtectedRoute></SessionBoundary>
}
