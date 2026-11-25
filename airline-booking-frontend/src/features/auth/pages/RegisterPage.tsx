import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Alert, Button, Card, Divider, Input, Result, Space } from 'antd'
import { Link } from 'react-router'
import { z } from 'zod'
import { authApi } from '../api/auth.api'
import { registerSchema } from '../schemas/auth.schema'
import type { RegisterRequest } from '../types/auth.dto'
import { ROUTES } from '../../../app/router/paths'
import { normalizeApiError } from '../../../shared/api/apiError'

const formSchema = registerSchema.extend({ confirmPassword: z.string() }).refine(
  values => values.password === values.confirmPassword,
  { path: ['confirmPassword'], message: 'Mật khẩu xác nhận chưa khớp.' },
)
type RegisterFormValues = z.infer<typeof formSchema>

export function RegisterPage() {
  const [registered, setRegistered] = useState(false)
  const mutation = useMutation({ mutationFn: (request: RegisterRequest) => authApi.register(request) })
  const { control, handleSubmit, formState } = useForm<RegisterFormValues>({
    resolver: zodResolver(formSchema), defaultValues: { email: '', password: '', fullName: '', confirmPassword: '' },
  })
  if (registered) return <Card className="login-card"><Result status="success" title="Đăng ký thành công"
    subTitle="Tài khoản đã được tạo. Bạn có thể đăng nhập để tiếp tục."
    extra={<Link to={ROUTES.LOGIN}><Button type="primary">Đến trang đăng nhập</Button></Link>} /></Card>

  return <Card className="login-card register-card">
    <h1>Đăng ký</h1><p className="muted">Tạo tài khoản để bắt đầu đặt vé.</p>
    {mutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(mutation.error).detail} />}
    <form className="auth-form" noValidate onSubmit={handleSubmit(async values => {
      try {
        const request: RegisterRequest = { email: values.email, password: values.password, fullName: values.fullName }
        await mutation.mutateAsync(request)
        setRegistered(true)
      } catch { /* Render mutation error above. */ }
    })}>
      <div className="auth-field">
        <label htmlFor="register-name">Họ và tên</label>
        <Controller name="fullName" control={control} render={({ field }) => <Input {...field} id="register-name" className="auth-input" autoComplete="name" disabled={mutation.isPending} />} />
        {formState.errors.fullName && <span className="auth-field-error" role="alert">{formState.errors.fullName.message}</span>}
      </div>
      <div className="auth-field">
        <label htmlFor="register-email">Email</label>
        <Controller name="email" control={control} render={({ field }) => <Input {...field} id="register-email" className="auth-input" type="email" autoComplete="email" disabled={mutation.isPending} />} />
        {formState.errors.email && <span className="auth-field-error" role="alert">{formState.errors.email.message}</span>}
      </div>
      <div className="auth-field">
        <label htmlFor="register-password">Mật khẩu</label>
        <Controller name="password" control={control} render={({ field }) => <Input.Password {...field} id="register-password" className="auth-input" autoComplete="new-password" disabled={mutation.isPending} />} />
        <span className={formState.errors.password ? 'auth-field-error' : 'auth-field-help'} role={formState.errors.password ? 'alert' : undefined}>
          {formState.errors.password?.message ?? 'Tối thiểu 8 ký tự.'}
        </span>
      </div>
      <div className="auth-field">
        <label htmlFor="register-confirm">Xác nhận mật khẩu</label>
        <Controller name="confirmPassword" control={control} render={({ field }) => <Input.Password {...field} id="register-confirm" className="auth-input" autoComplete="new-password" disabled={mutation.isPending} />} />
        {formState.errors.confirmPassword && <span className="auth-field-error" role="alert">{formState.errors.confirmPassword.message}</span>}
      </div>
      <Button block type="primary" htmlType="submit" loading={mutation.isPending} disabled={mutation.isPending}>Tạo tài khoản</Button>
    </form>
    <Divider plain>Hoặc đăng ký nhanh với</Divider>
    <Space direction="vertical" size="middle" className="auth-provider-actions">
      <Button block onClick={() => window.location.assign(authApi.oauthUrl('google'))}>Google</Button>
      <Button block onClick={() => window.location.assign(authApi.oauthUrl('facebook'))}>Facebook</Button>
    </Space>
    <p className="auth-switch">Đã có tài khoản? <Link to={ROUTES.LOGIN}>Đăng nhập</Link></p>
  </Card>
}
