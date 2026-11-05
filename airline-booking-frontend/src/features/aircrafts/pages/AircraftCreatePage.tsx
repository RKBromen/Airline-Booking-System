import { App, Card } from 'antd'
import { useNavigate } from 'react-router'
import { AircraftForm } from '../components/AircraftForm'
import { useCreateAircraft } from '../hooks/useCreateAircraft'
import { PageHeader } from '../../../components/ui/PageHeader'
import { ROUTES, routeTo } from '../../../app/router/paths'

export function AircraftCreatePage() {
  const mutation = useCreateAircraft()
  const navigate = useNavigate()
  const { message } = App.useApp()
  return <><PageHeader title="Thêm máy bay" description="Nhập số đăng ký để tạo máy bay mới." />
    <Card className="aircraft-form-card"><AircraftForm mode="create" isSubmitting={mutation.isPending}
      onCancel={() => navigate(ROUTES.AIRCRAFTS)} onSubmit={async values => {
        const aircraft = await mutation.mutateAsync(values)
        void message.success('Đã thêm máy bay thành công.')
        navigate(routeTo.aircraftDetail(aircraft.id))
      }} /></Card></>
}
