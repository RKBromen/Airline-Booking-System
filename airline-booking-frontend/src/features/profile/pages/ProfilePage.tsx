import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Card, Space, Tag } from 'antd'
import { Link } from 'react-router'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { authApi } from '../../auth/api/auth.api'
import { useAuthStore } from '../../auth/store/auth.store'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { ROUTES } from '../../../app/router/paths'

const statusLabels = { ACTIVE: 'Đang hoạt động', BLOCKED: 'Tạm ngưng', DELETED: 'Đã xóa' } as const

export function ProfilePage() {
  const query = useQuery({ queryKey: queryKeys.auth.me, queryFn: authApi.me })
  const setUser = useAuthStore(state => state.setUser)
  useEffect(() => { if (query.data) setUser(query.data) }, [query.data, setUser])
  if (query.isPending) return <Loading label="Đang tải thông tin tài khoản…" />
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  const user = query.data
  return <>
    <div className="profile-page-actions"><Space wrap><Link to={ROUTES.PROFILE_EDIT}><Button type="primary">Cập nhật hồ sơ</Button></Link>
      <Link to={ROUTES.PROFILE_PASSWORD}><Button>Đổi mật khẩu</Button></Link></Space></div>
    <Card className="profile-card"><div className="account-data-table-scroll"><table className="account-data-table">
      <tbody>
        <tr><th scope="row">{user.roles.includes('ADMIN') ? 'Quản trị viên' : 'Họ và tên'}</th><td>{user.fullName}</td></tr>
        <tr><th scope="row">Email</th><td>{user.email}</td></tr>
        <tr><th scope="row">Trạng thái</th><td><Tag color={user.status === 'ACTIVE' ? 'success' : user.status === 'BLOCKED' ? 'warning' : 'default'}>{statusLabels[user.status]}</Tag></td></tr>
      </tbody>
    </table></div></Card>
    <p className="profile-secondary-link"><Link to={ROUTES.PROFILE_LINKED_ACCOUNTS}>Xem tài khoản đăng nhập đã liên kết</Link></p>
  </>
}
