import { Tag } from 'antd'
import type { UserStatus } from '../types/user.enums'

const labels: Record<UserStatus, string> = { ACTIVE: 'Đang hoạt động', BLOCKED: 'Tạm ngưng', DELETED: 'Đã xóa' }
const colors: Record<UserStatus, string> = { ACTIVE: 'success', BLOCKED: 'warning', DELETED: 'default' }

export function UserStatusTag({ status }: { status: UserStatus }) {
  return <Tag color={colors[status]}>{labels[status]}</Tag>
}
