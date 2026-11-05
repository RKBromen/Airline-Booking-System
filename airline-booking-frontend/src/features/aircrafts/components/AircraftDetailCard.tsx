import { Card, Descriptions } from 'antd'
import type { AircraftResponse } from '../types/aircraft.dto'
import { AircraftStatusTag } from './AircraftStatusTag'

export function AircraftDetailCard({ aircraft }: { aircraft: AircraftResponse }) {
  return <Card><Descriptions title="Thông tin máy bay" bordered column={1} items={[
    { key: 'registration', label: 'Số đăng ký', children: aircraft.registrationNo },
    { key: 'status', label: 'Trạng thái', children: <AircraftStatusTag status={aircraft.status} /> },
  ]} /></Card>
}
