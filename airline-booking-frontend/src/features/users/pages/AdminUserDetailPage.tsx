import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, App, Button, Card, Descriptions, Popconfirm, Result, Space, Tag } from 'antd'
import { Link, useParams } from 'react-router'
import { z } from 'zod'
import { PageHeader } from '../../../components/ui/PageHeader'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { ROUTES, routeTo } from '../../../app/router/paths'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { normalizeApiError } from '../../../shared/api/apiError'
import { userApi } from '../api/user.api'
import { UserStatusTag } from '../components/UserStatusTag'
import { AuthProviderIcon } from '../../../components/ui/AuthProviderIcon'
import type { UserStatus } from '../types/user.enums'

const roleLabels: Record<string, string> = { ADMIN: 'Quản trị viên', STAFF: 'Nhân viên', CUSTOMER: 'Khách hàng' }
const providerLabels: Record<string, string> = { GOOGLE: 'Google', FACEBOOK: 'Facebook' }
const dateFormatter = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' })

export function AdminUserDetailPage() {
  const { id = '' } = useParams()
  const validId = z.uuid().safeParse(id).success
  const client = useQueryClient()
  const { message } = App.useApp()
  const query = useQuery({ queryKey: queryKeys.users.detail(id), queryFn: () => userApi.get(id), enabled: validId })
  const statusMutation = useMutation({ mutationFn: (status: UserStatus) => userApi.updateStatus(id, { status }), onSuccess: async user => {
    client.setQueryData(queryKeys.users.detail(id), user)
    await client.invalidateQueries({ queryKey: queryKeys.users.all })
    void message.success('Trạng thái tài khoản đã được cập nhật.')
  } })
  if (!validId) return <Result status="404" title="Không tìm thấy người dùng" extra={<Link to={ROUTES.ADMIN_USERS}><Button icon={<span aria-hidden="true">←</span>}>Quay lại danh sách</Button></Link>} />
  if (query.isPending) return <Loading label="Đang tải người dùng…" />
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  const user = query.data
  const nextStatus = user.status === 'ACTIVE' ? 'BLOCKED' : user.status === 'BLOCKED' ? 'ACTIVE' : null
  return <>
    <PageHeader eyebrow="Quản trị người dùng" title={user.fullName} description={user.email}
      actions={<Space wrap><Link to={routeTo.adminUserEdit(user.id)}><Button type="primary">Chỉnh sửa</Button></Link>
        {nextStatus && <Popconfirm title={nextStatus === 'BLOCKED' ? 'Tạm ngưng tài khoản này?' : 'Mở lại tài khoản này?'}
          onConfirm={() => statusMutation.mutate(nextStatus)} okText="Xác nhận" cancelText="Hủy">
          <Button loading={statusMutation.isPending}>{nextStatus === 'BLOCKED' ? 'Tạm ngưng' : 'Kích hoạt lại'}</Button>
        </Popconfirm>}
        <Link to={ROUTES.ADMIN_USERS}><Button icon={<span aria-hidden="true">←</span>}>Quay lại danh sách</Button></Link></Space>} />
    {statusMutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(statusMutation.error).detail} />}
    <Card className="profile-card"><Descriptions bordered column={{ xs: 1, sm: 1, md: 2 }}>
      <Descriptions.Item label="Họ và tên">{user.fullName}</Descriptions.Item>
      <Descriptions.Item label="Email">{user.email}</Descriptions.Item>
      <Descriptions.Item label="Trạng thái"><UserStatusTag status={user.status} /></Descriptions.Item>
      <Descriptions.Item label="Ngày tạo">{dateFormatter.format(new Date(user.createdAt))}</Descriptions.Item>
      <Descriptions.Item label="Vai trò"><Space wrap>{user.roles.map(role => <Tag key={role}>{roleLabels[role] ?? role}</Tag>)}</Space></Descriptions.Item>
      <Descriptions.Item label="Liên kết"><Space wrap>{user.authProviders.length
        ? user.authProviders.map(provider => <Tag key={provider}><span className="provider-identity"><AuthProviderIcon provider={provider} />{providerLabels[provider] ?? provider}</span></Tag>) : 'Chưa liên kết provider'}</Space></Descriptions.Item>
    </Descriptions></Card>
  </>
}
