import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { PublicLayout } from '../layouts/PublicLayout'
import { AirportListPage } from '../../features/airports/pages/AirportListPage'
import { AirportDetailPage } from '../../features/airports/pages/AirportDetailPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { ROUTES } from './paths'
import { ManagementLayout } from '../layouts/ManagementLayout'
import { RoleGuard } from './guards/RoleGuard'
import { ForbiddenPage } from '../../pages/ForbiddenPage'
import { LoginPage } from '../../features/auth/pages/LoginPage'
import { AircraftListPage } from '../../features/aircrafts/pages/AircraftListPage'
import { AircraftDetailPage } from '../../features/aircrafts/pages/AircraftDetailPage'
import { AircraftSeatsPage } from '../../features/aircrafts/pages/AircraftSeatsPage'
import { AircraftCreatePage } from '../../features/aircrafts/pages/AircraftCreatePage'
import { AircraftEditPage } from '../../features/aircrafts/pages/AircraftEditPage'
import { RegisterPage } from '../../features/auth/pages/RegisterPage'
import { OAuthCallbackPage } from '../../features/auth/pages/OAuthCallbackPage'
import { FlightSearchPage } from '../../features/flights/pages/FlightSearchPage'
import { FlightDetailPage } from '../../features/flights/pages/FlightDetailPage'
import { FlightSeatSelectionPage } from '../../features/flights/pages/FlightSeatSelectionPage'
import { CustomerLayout } from '../layouts/CustomerLayout'
import { SessionBoundary } from './guards/SessionBoundary'
import { ProtectedRoute } from './guards/ProtectedRoute'
import { ProfilePage } from '../../features/profile/pages/ProfilePage'
import { ProfileEditPage } from '../../features/profile/pages/ProfileEditPage'
import { ChangePasswordPage } from '../../features/profile/pages/ChangePasswordPage'
import { LinkedAccountsPage } from '../../features/profile/pages/LinkedAccountsPage'
import { AdminUsersPage } from '../../features/users/pages/AdminUsersPage'
import { AdminUserCreatePage } from '../../features/users/pages/AdminUserCreatePage'
import { AdminUserDetailPage } from '../../features/users/pages/AdminUserDetailPage'
import { AdminUserEditPage } from '../../features/users/pages/AdminUserEditPage'
import { RolesPage } from '../../features/users/pages/RolesPage'
import { AuthLayout } from '../layouts/AuthLayout'

function ProtectedCustomerLayout() {
  return <SessionBoundary><ProtectedRoute><CustomerLayout /></ProtectedRoute></SessionBoundary>
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path={ROUTES.HOME} element={<FlightSearchPage />} />
          <Route path={ROUTES.FLIGHTS} element={<FlightSearchPage />} />
          <Route path={ROUTES.FLIGHT_DETAIL} element={<FlightDetailPage />} />
          <Route path={ROUTES.FLIGHT_SEATS} element={<FlightSeatSelectionPage />} />
          <Route path={ROUTES.AIRPORTS} element={<AirportListPage />} />
          <Route path={ROUTES.AIRPORT_DETAIL} element={<AirportDetailPage />} />
          <Route element={<AuthLayout />}>
            <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            <Route path={ROUTES.REGISTER} element={<RegisterPage />} />
            <Route path={ROUTES.OAUTH_CALLBACK} element={<OAuthCallbackPage />} />
          </Route>
          <Route path={ROUTES.FORBIDDEN} element={<ForbiddenPage />} />
          <Route path="/403" element={<Navigate to={ROUTES.FORBIDDEN} replace />} />
          <Route element={<ProtectedCustomerLayout />}>
            <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
            <Route path={ROUTES.PROFILE_EDIT} element={<ProfileEditPage />} />
            <Route path={ROUTES.PROFILE_PASSWORD} element={<ChangePasswordPage />} />
            <Route path={ROUTES.PROFILE_LINKED_ACCOUNTS} element={<LinkedAccountsPage />} />
          </Route>
          <Route element={<ManagementLayout />}>
            <Route path={ROUTES.AIRCRAFTS} element={<AircraftListPage />} />
            <Route path={ROUTES.AIRCRAFT_DETAIL} element={<AircraftDetailPage />} />
            <Route path={ROUTES.AIRCRAFT_SEATS} element={<AircraftSeatsPage />} />
            <Route path={ROUTES.AIRCRAFT_NEW} element={<RoleGuard roles={['ADMIN']}><AircraftCreatePage /></RoleGuard>} />
            <Route path={ROUTES.AIRCRAFT_EDIT} element={<RoleGuard roles={['ADMIN']}><AircraftEditPage /></RoleGuard>} />
            <Route path={ROUTES.ADMIN_USERS} element={<RoleGuard roles={['ADMIN']}><AdminUsersPage /></RoleGuard>} />
            <Route path={ROUTES.ADMIN_USER_NEW} element={<RoleGuard roles={['ADMIN']}><AdminUserCreatePage /></RoleGuard>} />
            <Route path={ROUTES.ADMIN_USER_EDIT} element={<RoleGuard roles={['ADMIN']}><AdminUserEditPage /></RoleGuard>} />
            <Route path={ROUTES.ADMIN_USER_DETAIL} element={<RoleGuard roles={['ADMIN']}><AdminUserDetailPage /></RoleGuard>} />
            <Route path={ROUTES.ADMIN_ROLES} element={<RoleGuard roles={['ADMIN']}><RolesPage /></RoleGuard>} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
