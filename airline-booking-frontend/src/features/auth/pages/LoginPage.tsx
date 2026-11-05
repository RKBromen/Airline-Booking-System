import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Alert, Button, Card, Form, Input } from 'antd'
import { Navigate, useLocation, useNavigate } from 'react-router'
import { useLogin } from '../hooks/useLogin'
import { useAuth } from '../hooks/useAuth'
import { loginSchema } from '../schemas/auth.schema'
import type { LoginRequest } from '../types/auth.dto'
import { ROUTES } from '../../../app/router/paths'
import { normalizeApiError } from '../../../shared/api/apiError'

export function LoginPage() {
  const login = useLogin()
  const auth = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const from = location.state?.from?.pathname
  const destination = typeof from === 'string' && from.startsWith('/management/') ? from : ROUTES.AIRCRAFTS
  const { control, handleSubmit, formState } = useForm<LoginRequest>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } })
  if (auth.isAuthenticated) return <Navigate to={destination} replace />
  return <Card className="login-card"><h1>Đăng nhập</h1><p className="muted">Đăng nhập để truy cập khu vực quản lý.</p>
    {login.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(login.error).detail} />}
    <form noValidate onSubmit={handleSubmit(async values => {
      if (login.isPending) return
      try { await login.mutateAsync(values); navigate(destination, { replace: true }) } catch { /* Render mutation error above. */ }
    })}>
      <Form.Item label="Email" htmlFor="login-email" validateStatus={formState.errors.email ? 'error' : undefined} help={formState.errors.email?.message}>
        <Controller name="email" control={control} render={({ field }) => <Input {...field} id="login-email" type="email" autoComplete="username" disabled={login.isPending} />} />
      </Form.Item>
      <Form.Item label="Mật khẩu" htmlFor="login-password" validateStatus={formState.errors.password ? 'error' : undefined} help={formState.errors.password?.message}>
        <Controller name="password" control={control} render={({ field }) => <Input.Password {...field} id="login-password" autoComplete="current-password" disabled={login.isPending} />} />
      </Form.Item>
      <Button block type="primary" htmlType="submit" loading={login.isPending} disabled={login.isPending}>Đăng nhập</Button>
    </form>
  </Card>
}
