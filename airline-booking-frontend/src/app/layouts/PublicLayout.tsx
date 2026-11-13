import { Link, NavLink, Outlet } from 'react-router'
import { ROUTES } from '../router/paths'
import { Button, App, Dropdown } from 'antd'
import { useAuth } from '../../features/auth/hooks/useAuth'
import { useLogout } from '../../features/auth/hooks/useLogout'

export function PublicLayout() {
  const auth = useAuth()
  const logout = useLogout()
  const { message } = App.useApp()
  const accountMenuItems = auth.isAuthenticated
    ? [
        { key: 'profile', label: <Link to={ROUTES.PROFILE}>Tài khoản</Link> },
        { key: 'logout', label: 'Đăng xuất', disabled: logout.isPending, danger: true },
      ]
    : [
        { key: 'login', label: <Link to={ROUTES.LOGIN}>Đăng nhập</Link> },
        { key: 'register', label: <Link to={ROUTES.REGISTER}>Đăng ký</Link> },
      ]
  const handleAccountMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') logout.mutate(undefined, {
      onError: () => { void message.error('Đã thoát trên trình duyệt nhưng chưa thu hồi được phiên trên máy chủ. Vui lòng thử lại khi có kết nối.') },
    })
  }
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
            <NavLink className="nav-link" to={ROUTES.HOME}>Tìm chuyến bay</NavLink>
            <NavLink className="nav-link" to={ROUTES.AIRPORTS}>Sân bay</NavLink>
            {auth.roles.some(role => role === 'ADMIN' || role === 'STAFF') &&
              <NavLink className="nav-link" to={ROUTES.AIRCRAFTS}>Máy bay</NavLink>}
            {auth.roles.includes('ADMIN') && <NavLink className="nav-link" to={ROUTES.ADMIN_USERS}>Quản trị</NavLink>}
          </nav>
          <Dropdown className="header-account-menu" trigger={['click']} placement="bottomRight" menu={{ items: accountMenuItems, onClick: handleAccountMenuClick }}>
            <Button className="header-menu-button" aria-label="Mở menu tài khoản" title="Menu tài khoản">
              <span className="hamburger-icon" aria-hidden="true"><span /><span /><span /></span>
            </Button>
          </Dropdown>
        </div>
      </header>
      <main className="site-main" id="main-content"><Outlet /></main>
      <footer className="site-footer"><span>Airline Booking System</span><span>Thông tin sân bay</span></footer>
    </div>
  )
}
