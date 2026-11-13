import { Alert, Button, Card, Table, Tag } from 'antd'
import { Link } from 'react-router'
import { useAuth } from '../../auth/hooks/useAuth'
import { ROUTES } from '../../../app/router/paths'

const roleDetails: Record<string, { label: string; description: string }> = {
  CUSTOMER: { label: 'Khách hàng', description: 'Tìm kiếm chuyến bay, đặt vé, thanh toán và quản lý tài khoản của mình.' },
  STAFF: { label: 'Nhân viên', description: 'Truy cập các chức năng vận hành được Hệ thống cấp quyền.' },
  ADMIN: { label: 'Quản trị viên', description: 'Quản lý tài khoản và truy cập các chức năng quản trị được Hệ thống cấp quyền.' },
}

type RoleRow = { key: string; role: string; label: string; description: string }
const roleColumns = [
  { title: 'Vai trò', dataIndex: 'label', key: 'label', width: 180, render: (label: string) => <Tag>{label}</Tag> },
  { title: 'Mô tả', dataIndex: 'description', key: 'description' },
]

function rolesFromJwt(token: string | null): string[] {
  if (!token) return []
  try {
    const encodedPayload = token.split('.')[1]
    if (!encodedPayload) return []
    const base64 = encodedPayload.replace(/-/g, '+').replace(/_/g, '/')
    const binary = atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='))
    const bytes = Uint8Array.from(binary, character => character.charCodeAt(0))
    const claims: unknown = JSON.parse(new TextDecoder().decode(bytes))
    if (!claims || typeof claims !== 'object' || !('roles' in claims) || !Array.isArray(claims.roles)) return []
    return claims.roles.filter((role): role is string => typeof role === 'string' && role.trim().length > 0)
  } catch {
    return []
  }
}

export function RolesPage() {
  const { accessToken } = useAuth()
  const roles = rolesFromJwt(accessToken)
  const rows: RoleRow[] = roles.map(role => ({
    key: role,
    role,
    label: roleDetails[role]?.label ?? role,
    description: roleDetails[role]?.description ?? 'Vai trò được Hệ thống cấp cho tài khoản hiện tại.',
  }))
  return <>
    <div className="management-page-actions"><Link to={ROUTES.ADMIN_USERS}><Button>Danh sách người dùng</Button></Link></div>
    {roles.length > 0
      ? <Card className="roles-table-card"><Table<RoleRow> rowKey="key" columns={roleColumns} dataSource={rows} pagination={false} scroll={{ x: 620 }} /></Card>
      : <Alert className="provider-note" type="warning" showIcon title="Không đọc được role từ access token. Hãy đăng nhập lại để lấy token mới." />}
  </>
}
