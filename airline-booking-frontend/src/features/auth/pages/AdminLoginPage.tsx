import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Alert, Button, Card, Input } from 'antd'
import { Link, Navigate, useNavigate } from 'react-router'
import { useLogin } from '../hooks/useLogin'
import { useAuth } from '../hooks/useAuth'
import { loginSchema } from '../schemas/auth.schema'
import type { LoginRequest } from '../types/auth.dto'
import { ROUTES } from '../../../app/router/paths'
import { normalizeApiError } from '../../../shared/api/apiError'
import { authApi } from '../api/auth.api'
import { useAuthStore } from '../store/auth.store'

export function AdminLoginPage() {
  const login = useLogin()
  const auth = useAuth()
  const navigate = useNavigate()
  const [accessError, setAccessError] = useState<string | null>(null)
  const { control, handleSubmit, formState } = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  if (auth.roles.includes('ADMIN')) return <Navigate to={ROUTES.ADMIN_USERS} replace />

  return (
    <Card className="login-card admin-login-card">
      <p className="eyebrow">Khu vực quản trị</p>
      <h1>Đăng nhập quản trị</h1>
      <p className="muted">Đăng nhập bằng tài khoản có quyền quản trị hệ thống.</p>
      {accessError && <Alert className="form-error" type="error" showIcon title={accessError} />}
      {login.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(login.error).detail} />}
      <form className="auth-form" noValidate onSubmit={handleSubmit(async values => {
        if (login.isPending) return
        setAccessError(null)
        try {
          const session = await login.mutateAsync(values)
          if (!session.roles.includes('ADMIN')) {
            try { await authApi.logout() } catch { /* Clear local state even if the server is unavailable. */ }
            useAuthStore.getState().clearSession()
            setAccessError('Tài khoản này không có quyền quản trị. Hãy đăng nhập bằng tài khoản quản trị.')
            return
          }
          navigate(ROUTES.ADMIN_USERS, { replace: true })
        } catch { /* Render authentication errors above. */ }
      })}>
        <div className="auth-field">
          <label htmlFor="admin-login-email">Email quản trị</label>
          <Controller name="email" control={control} render={({ field }) => <Input {...field} id="admin-login-email" className="auth-input" type="email" autoComplete="username" disabled={login.isPending} />} />
          {formState.errors.email && <span className="auth-field-error" role="alert">{formState.errors.email.message}</span>}
        </div>
        <div className="auth-field">
          <label htmlFor="admin-login-password">Mật khẩu</label>
          <Controller name="password" control={control} render={({ field }) => <Input.Password {...field} id="admin-login-password" className="auth-input" autoComplete="current-password" disabled={login.isPending} />} />
          {formState.errors.password && <span className="auth-field-error" role="alert">{formState.errors.password.message}</span>}
        </div>
        <Button block type="primary" htmlType="submit" loading={login.isPending} disabled={login.isPending}>
          Đăng nhập quản trị
        </Button>
      </form>
      <p className="auth-switch"><Link to={ROUTES.LOGIN}>Đăng nhập tài khoản khách hàng</Link></p>
    </Card>
  )
}
