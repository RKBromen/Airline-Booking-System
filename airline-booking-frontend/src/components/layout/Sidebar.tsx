import { NavLink } from 'react-router'
import { useAuth } from '../../features/auth/hooks/useAuth'
import { ROUTES } from '../../app/router/paths'

export function Sidebar() {
  const { roles } = useAuth()
  const isAdmin = roles.includes('ADMIN')
  return <div className="management-sidebars">
    <aside className="management-sidebar" aria-label="Khu vực quản lý">
      <p className="eyebrow">Khu vực quản lý</p>
      <NavLink to={ROUTES.AIRCRAFTS}>Máy bay</NavLink>
    </aside>
    {isAdmin && <aside className="management-sidebar" aria-label="Quản lý tài khoản">
      <p className="eyebrow">Tài khoản</p>
      <NavLink to={ROUTES.ADMIN_USERS}>Người dùng</NavLink>
      <NavLink to={ROUTES.ADMIN_ROLES}>Vai trò</NavLink>
    </aside>}
  </div>
}
