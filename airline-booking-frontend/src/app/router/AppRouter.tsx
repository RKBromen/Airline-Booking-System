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

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path={ROUTES.HOME} element={<Navigate to={ROUTES.AIRPORTS} replace />} />
          <Route path={ROUTES.AIRPORTS} element={<AirportListPage />} />
          <Route path={ROUTES.AIRPORT_DETAIL} element={<AirportDetailPage />} />
          <Route path={ROUTES.LOGIN} element={<LoginPage />} />
          <Route path={ROUTES.FORBIDDEN} element={<ForbiddenPage />} />
          <Route path="/403" element={<Navigate to={ROUTES.FORBIDDEN} replace />} />
          <Route element={<ManagementLayout />}>
            <Route path={ROUTES.AIRCRAFTS} element={<AircraftListPage />} />
            <Route path={ROUTES.AIRCRAFT_DETAIL} element={<AircraftDetailPage />} />
            <Route path={ROUTES.AIRCRAFT_SEATS} element={<AircraftSeatsPage />} />
            <Route path={ROUTES.AIRCRAFT_NEW} element={<RoleGuard roles={['ADMIN']}><AircraftCreatePage /></RoleGuard>} />
            <Route path={ROUTES.AIRCRAFT_EDIT} element={<RoleGuard roles={['ADMIN']}><AircraftEditPage /></RoleGuard>} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
