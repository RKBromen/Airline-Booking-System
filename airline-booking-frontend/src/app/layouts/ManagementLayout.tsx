import { Outlet, useLocation } from 'react-router'
import { SessionBoundary } from '../router/guards/SessionBoundary'
import { ProtectedRoute } from '../router/guards/ProtectedRoute'
import { RoleGuard } from '../router/guards/RoleGuard'
import { Sidebar } from '../../components/layout/Sidebar'
import { ROUTES } from '../router/paths'

export function ManagementLayout() {
  const location = useLocation()
  const loginPath = location.pathname.startsWith('/admin/') ? ROUTES.ADMIN_LOGIN : ROUTES.LOGIN
  return <SessionBoundary><ProtectedRoute loginPath={loginPath}><RoleGuard roles={['ADMIN', 'STAFF']}>
    <div className="management-shell"><Sidebar /><div className="management-content"><Outlet /></div></div>
  </RoleGuard></ProtectedRoute></SessionBoundary>
}
