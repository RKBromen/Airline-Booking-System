import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Alert, Button, Card, Divider, Form, Input, Result, Space } from 'antd'
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

  return <Card className="login-card">
    <h1>Đăng ký</h1><p className="muted">Tạo tài khoản để bắt đầu đặt vé.</p>
    {mutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(mutation.error).detail} />}
    <form noValidate onSubmit={handleSubmit(async values => {
      try {
        const request: RegisterRequest = { email: values.email, password: values.password, fullName: values.fullName }
        await mutation.mutateAsync(request)
        setRegistered(true)
      } catch { /* Render mutation error above. */ }
    })}>
      <Form.Item label="Họ và tên" htmlFor="register-name" validateStatus={formState.errors.fullName ? 'error' : undefined} help={formState.errors.fullName?.message}>
        <Controller name="fullName" control={control} render={({ field }) => <Input {...field} id="register-name" autoComplete="name" disabled={mutation.isPending} />} />
      </Form.Item>
      <Form.Item label="Email" htmlFor="register-email" validateStatus={formState.errors.email ? 'error' : undefined} help={formState.errors.email?.message}>
        <Controller name="email" control={control} render={({ field }) => <Input {...field} id="register-email" type="email" autoComplete="email" disabled={mutation.isPending} />} />
      </Form.Item>
      <Form.Item label="Mật khẩu" htmlFor="register-password" validateStatus={formState.errors.password ? 'error' : undefined} help={formState.errors.password?.message ?? 'Tối thiểu 8 ký tự.'}>
        <Controller name="password" control={control} render={({ field }) => <Input.Password {...field} id="register-password" autoComplete="new-password" disabled={mutation.isPending} />} />
      </Form.Item>
      <Form.Item label="Xác nhận mật khẩu" htmlFor="register-confirm" validateStatus={formState.errors.confirmPassword ? 'error' : undefined} help={formState.errors.confirmPassword?.message}>
        <Controller name="confirmPassword" control={control} render={({ field }) => <Input.Password {...field} id="register-confirm" autoComplete="new-password" disabled={mutation.isPending} />} />
      </Form.Item>
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
