import { NavLink, Outlet } from 'react-router'
import { ROUTES } from '../router/paths'

export function CustomerLayout() {
  return <div className="customer-area">
    <nav className="customer-nav" aria-label="Điều hướng tài khoản">
      <NavLink to={ROUTES.PROFILE} end>Thông tin tài khoản</NavLink>
      <NavLink to={ROUTES.PROFILE_EDIT}>Cập nhật hồ sơ</NavLink>
      <NavLink to={ROUTES.PROFILE_PASSWORD}>Đổi mật khẩu</NavLink>
      <NavLink to={ROUTES.PROFILE_LINKED_ACCOUNTS}>Tài khoản liên kết</NavLink>
    </nav>
    <div className="customer-content"><Outlet /></div>
  </div>
}
