import { Tag } from 'antd'
import type { AircraftStatus } from '../types/aircraft.enums'

const labels = { ACTIVE: 'Đang hoạt động', BLOCKED: 'Tạm ngưng', DELETED: 'Đã xóa' }
const colors = { ACTIVE: 'success', BLOCKED: 'warning', DELETED: 'default' }
export function AircraftStatusTag({ status }: { status: AircraftStatus }) {
  return <Tag color={colors[status]}>{labels[status] ?? status}</Tag>
}
