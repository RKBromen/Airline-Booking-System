import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { PublicLayout } from '../layouts/PublicLayout'
import { AirportListPage } from '../../features/airports/pages/AirportListPage'
import { AirportDetailPage } from '../../features/airports/pages/AirportDetailPage'
import { NotFoundPage } from '../../pages/NotFoundPage'
import { ROUTES } from './paths'

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path={ROUTES.HOME} element={<Navigate to={ROUTES.AIRPORTS} replace />} />
          <Route path={ROUTES.AIRPORTS} element={<AirportListPage />} />
          <Route path={ROUTES.AIRPORT_DETAIL} element={<AirportDetailPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
