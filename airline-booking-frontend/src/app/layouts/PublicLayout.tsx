import { Link, NavLink, Outlet } from 'react-router'
import { ROUTES } from '../router/paths'
import { Button, App } from 'antd'
import { useAuth } from '../../features/auth/hooks/useAuth'
import { useLogout } from '../../features/auth/hooks/useLogout'

export function PublicLayout() {
  const auth = useAuth()
  const logout = useLogout()
  const { message } = App.useApp()
  return (
    <div className="public-layout">
      <a className="skip-link" href="#main-content">Đến nội dung chính</a>
      <header className="site-header">
        <div className="site-header-inner">
          <Link className="brand" to={ROUTES.HOME} aria-label="Airline Booking — Trang chủ">
            <span className="brand-mark" aria-hidden="true">✈</span>
            <span>Airline<span className="brand-light">Booking</span></span>
          </Link>
          <nav className="site-nav" aria-label="Điều hướng chính">
            <NavLink className="nav-link" to={ROUTES.AIRPORTS}>Sân bay</NavLink>
            <NavLink className="nav-link" to={ROUTES.AIRCRAFTS}>Máy bay</NavLink>
            {auth.isAuthenticated ? <Button size="small" loading={logout.isPending} onClick={() => logout.mutate(undefined, {
              onError: () => { void message.error('Đã thoát trên trình duyệt nhưng chưa thu hồi được phiên trên máy chủ. Vui lòng thử lại khi có kết nối.') },
            })}>Đăng xuất</Button> : <Link to={ROUTES.LOGIN} className="login-link">Đăng nhập</Link>}
          </nav>
        </div>
      </header>
      <main className="site-main" id="main-content"><Outlet /></main>
      <footer className="site-footer"><span>Airline Booking System</span><span>Thông tin sân bay</span></footer>
    </div>
  )
}
