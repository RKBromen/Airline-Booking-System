import { App, Card } from 'antd'
import { Link, useNavigate, useParams } from 'react-router'
import { AircraftForm } from '../components/AircraftForm'
import { AircraftQueryState } from '../components/AircraftQueryState'
import { useAircraft } from '../hooks/useAircraft'
import { useUpdateAircraft } from '../hooks/useUpdateAircraft'
import { isAircraftId } from '../utils/aircraftParams'
import { PageHeader } from '../../../components/ui/PageHeader'
import { ROUTES, routeTo } from '../../../app/router/paths'

export function AircraftEditPage() {
  const { id } = useParams()
  const query = useAircraft(id)
  const mutation = useUpdateAircraft(id ?? '')
  const navigate = useNavigate()
  const { message } = App.useApp()
  return <>
    <Link className="back-link" to={isAircraftId(id) ? routeTo.aircraftDetail(id) : ROUTES.AIRCRAFTS}>← Quay lại chi tiết máy bay</Link>
    <PageHeader title="Cập nhật máy bay" description="Chỉnh sửa số đăng ký máy bay." />
    <AircraftQueryState query={query} invalid={!isAircraftId(id)}>{query.data &&
      <Card className="aircraft-form-card"><AircraftForm key={query.data.id} mode="edit" initialValues={query.data}
        isSubmitting={mutation.isPending} onCancel={() => navigate(routeTo.aircraftDetail(query.data!.id))}
        onSubmit={async values => {
          const aircraft = await mutation.mutateAsync(values)
          void message.success('Đã cập nhật máy bay thành công.')
          navigate(routeTo.aircraftDetail(aircraft.id))
        }} /></Card>}
    </AircraftQueryState>
  </>
}
