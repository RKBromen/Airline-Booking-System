import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Card, Select, Space, Table } from 'antd'
import type { TableColumnsType } from 'antd'
import { Link, useSearchParams } from 'react-router'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { userApi } from '../api/user.api'
import type { UserListParams, UserResponse } from '../types/user.dto'
import { USER_STATUSES, type UserStatus } from '../types/user.enums'
import { UserStatusTag } from '../components/UserStatusTag'
import { routeTo, ROUTES } from '../../../app/router/paths'
import { EmptyState } from '../../../components/ui/EmptyState'

const statusOptions = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'BLOCKED', label: 'Tạm ngưng' },
  { value: 'DELETED', label: 'Đã xóa' },
]
const dateFormatter = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' })

export function AdminUsersPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(0, Number(searchParams.get('page') ?? 0) || 0)
  const rawStatus = searchParams.get('status') ?? ''
  const status = USER_STATUSES.includes(rawStatus as UserStatus) ? rawStatus as UserStatus : undefined
  const params: UserListParams = { page, size: 10, sort: 'createdAt', ...(status ? { status } : {}) }
  const query = useQuery({ queryKey: queryKeys.users.list(params), queryFn: () => userApi.list(params) })
  const columns = useMemo<TableColumnsType<UserResponse>>(() => [
    { title: 'Họ và tên', dataIndex: 'fullName', render: (value: string, user) => <Link to={routeTo.adminUser(user.id)}>{value}</Link> },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Trạng thái', dataIndex: 'status', render: (value: UserStatus) => <UserStatusTag status={value} /> },
    { title: 'Ngày tạo', dataIndex: 'createdAt', render: (value: string) => dateFormatter.format(new Date(value)) },
    { title: '', key: 'action', render: (_value, user) => <Link to={routeTo.adminUser(user.id)}>Xem</Link> },
  ], [])
  const setFilter = (value: string) => setSearchParams(previous => {
    const next = new URLSearchParams(previous)
    next.delete('page')
    if (value) next.set('status', value)
    else next.delete('status')
    return next
  })
  const setPage = (nextPage: number) => setSearchParams(previous => {
    const next = new URLSearchParams(previous)
    if (nextPage <= 0) next.delete('page')
    else next.set('page', String(nextPage))
    return next
  })
  return <>
    <div className="management-page-actions"><Space wrap><Link to={ROUTES.ADMIN_ROLES}><Button>Vai trò</Button></Link>
      <Link to={ROUTES.ADMIN_USER_NEW}><Button type="primary">Thêm người dùng</Button></Link></Space></div>
    <Card className="admin-list-card">
      <div className="admin-toolbar"><label htmlFor="user-status-filter">Lọc theo trạng thái</label>
        <Select id="user-status-filter" value={status ?? ''} options={statusOptions} onChange={setFilter} className="admin-status-filter" />
        <span className="result-count">{query.data ? `${query.data.totalElements} tài khoản` : 'Danh sách tài khoản'}</span>
      </div>
      {query.isPending && <Loading label="Đang tải người dùng…" />}
      {query.isError && <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />}
      {query.data && <Table<UserResponse> rowKey="id" columns={columns} dataSource={query.data.content}
        locale={{ emptyText: <EmptyState description="Không tìm thấy người dùng phù hợp." /> }}
        pagination={{ current: page + 1, pageSize: query.data.size, total: query.data.totalElements,
          showSizeChanger: false, onChange: value => setPage(value - 1), responsive: true }} scroll={{ x: 760 }} />}
    </Card>
  </>
}
