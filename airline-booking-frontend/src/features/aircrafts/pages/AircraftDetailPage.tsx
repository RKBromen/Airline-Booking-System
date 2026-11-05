import { Button, Space } from 'antd'
import { Link, useParams } from 'react-router'
import { useAircraft } from '../hooks/useAircraft'
import { AircraftDetailCard } from '../components/AircraftDetailCard'
import { AircraftQueryState } from '../components/AircraftQueryState'
import { isAircraftId } from '../utils/aircraftParams'
import { PageHeader } from '../../../components/ui/PageHeader'
import { ROUTES, routeTo } from '../../../app/router/paths'
import { useAuth } from '../../auth/hooks/useAuth'

export function AircraftDetailPage() {
  const { id } = useParams()
  const query = useAircraft(id)
  const { roles } = useAuth()
  return <>
    <Link className="back-link" to={ROUTES.AIRCRAFTS}>← Quay lại danh sách máy bay</Link>
    <AircraftQueryState query={query} invalid={!isAircraftId(id)}>{query.data && <>
      <PageHeader title={query.data.registrationNo} actions={<Space wrap>
        <Link to={routeTo.aircraftSeats(query.data.id)}><Button>Xem ghế vật lý</Button></Link>
        {roles.includes('ADMIN') && <Link to={routeTo.aircraftEdit(query.data.id)}><Button type="primary">Chỉnh sửa máy bay</Button></Link>}
      </Space>} />
      <AircraftDetailCard aircraft={query.data} />
    </>}</AircraftQueryState>
  </>
}
