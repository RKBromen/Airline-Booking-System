import { useEffect, useRef } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Card, Result } from 'antd'
import { Link, useSearchParams } from 'react-router'
import { Loading } from '../../../components/ui/Loading'
import { ROUTES } from '../../../app/router/paths'
import { authApi } from '../api/auth.api'
import { useAuthStore } from '../store/auth.store'
import { oauthExchangeSchema } from '../schemas/auth.schema'
import { normalizeApiError } from '../../../shared/api/apiError'

export function OAuthCallbackPage() {
  const [params] = useSearchParams()
  const queryClient = useQueryClient()
  const started = useRef(false)
  const code = params.get('code')
  const providerError = params.get('error')
  const linkedProvider = params.get('linked')
  const linkError = providerError === 'oauth_link_failed'
  const exchange = useMutation({
    mutationFn: (value: string) => authApi.exchange(oauthExchangeSchema.parse({ code: value })),
    onSuccess: async session => {
      await queryClient.cancelQueries()
      queryClient.removeQueries()
      useAuthStore.getState().setSession(session)
      await queryClient.invalidateQueries({ queryKey: ['auth'] })
    },
  })
  const { mutate } = exchange

  useEffect(() => {
    if (!code || started.current) return
    started.current = true
    mutate(code)
  }, [code, mutate])

  if (linkedProvider) return <Card className="login-card"><Result status="success" title="Tài khoản đã được liên kết"
    subTitle="Phương thức đăng nhập mới đã được thêm vào tài khoản của bạn."
    extra={<Link to={ROUTES.PROFILE_LINKED_ACCOUNTS}><Button type="primary">Quản lý tài khoản liên kết</Button></Link>} /></Card>
  if (linkError) return <Card className="login-card"><Result status="error" title="Không thể liên kết tài khoản"
    subTitle="Hãy thử lại hoặc chọn một tài khoản nhà cung cấp khác."
    extra={<Link to={ROUTES.PROFILE_LINKED_ACCOUNTS}><Button type="primary">Quay lại tài khoản liên kết</Button></Link>} /></Card>

  if (exchange.isSuccess) return <Card className="login-card"><Result status="success" title="Đăng nhập thành công"
    subTitle="Bạn có thể tiếp tục tìm chuyến bay." extra={<Link to={ROUTES.HOME}><Button type="primary">Tiếp tục</Button></Link>} /></Card>
  if (exchange.isPending && code) return <Card className="login-card"><Loading label="Đang hoàn tất đăng nhập…" /></Card>
  const error = providerError ? 'Đăng nhập qua nhà cung cấp thất bại. Vui lòng thử lại.'
    : exchange.isError ? normalizeApiError(exchange.error).detail : 'Liên kết đăng nhập thiếu mã xác thực.'
  return <Card className="login-card"><Result status="error" title="Không thể đăng nhập"
    subTitle={error} extra={<Link to={ROUTES.LOGIN}><Button type="primary">Quay lại đăng nhập</Button></Link>} /></Card>
}
