import { Link, useParams } from 'react-router'
import { useAircraftSeats } from '../hooks/useAircraftSeats'
import { AircraftSeatMap } from '../components/AircraftSeatMap'
import { AircraftQueryState } from '../components/AircraftQueryState'
import { isAircraftId } from '../utils/aircraftParams'
import { PageHeader } from '../../../components/ui/PageHeader'
import { ROUTES, routeTo } from '../../../app/router/paths'

export function AircraftSeatsPage() {
  const { id } = useParams()
  const query = useAircraftSeats(id)
  return <>
    <Link className="back-link" to={isAircraftId(id) ? routeTo.aircraftDetail(id) : ROUTES.AIRCRAFTS}>← Quay lại chi tiết máy bay</Link>
    <PageHeader title="Ghế vật lý" description="Xem cấu hình ghế của máy bay." />
    <AircraftQueryState query={query} invalid={!isAircraftId(id)}>{query.data && <AircraftSeatMap seats={query.data} />}</AircraftQueryState>
  </>
}
