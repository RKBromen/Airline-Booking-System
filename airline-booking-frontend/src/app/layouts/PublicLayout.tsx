import { Link, NavLink, Outlet } from 'react-router'
import { ROUTES } from '../router/paths'

export function PublicLayout() {
  return (
    <div className="public-layout">
      <a className="skip-link" href="#main-content">Đến nội dung chính</a>
      <header className="site-header">
        <div className="site-header-inner">
          <Link className="brand" to={ROUTES.HOME} aria-label="Airline Booking — Trang chủ">
            <span className="brand-mark" aria-hidden="true">✈</span>
            <span>Airline<span className="brand-light">Booking</span></span>
          </Link>
          <nav aria-label="Điều hướng chính"><NavLink className="nav-link" to={ROUTES.AIRPORTS}>Sân bay</NavLink></nav>
        </div>
      </header>
      <main className="site-main" id="main-content"><Outlet /></main>
      <footer className="site-footer"><span>Airline Booking System</span><span>Thông tin sân bay</span></footer>
    </div>
  )
}
