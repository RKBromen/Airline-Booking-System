import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Form, Input, Space } from 'antd'
import { useNavigate } from 'react-router'
import { PageHeader } from '../../../components/ui/PageHeader'
import { userCreateSchema } from '../schemas/user.schema'
import { userApi } from '../api/user.api'
import type { UserCreateRequest } from '../types/user.dto'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { normalizeApiError } from '../../../shared/api/apiError'
import { ROUTES, routeTo } from '../../../app/router/paths'

export function AdminUserCreatePage() {
  const client = useQueryClient()
  const navigate = useNavigate()
  const mutation = useMutation({ mutationFn: (request: UserCreateRequest) => userApi.create(request), onSuccess: async user => {
    await client.invalidateQueries({ queryKey: queryKeys.users.all })
    navigate(routeTo.adminUser(user.id))
  } })
  const { control, handleSubmit, formState } = useForm<UserCreateRequest>({
    resolver: zodResolver(userCreateSchema), defaultValues: { email: '', fullName: '', password: '' },
  })
  return <>
    <PageHeader eyebrow="Quản trị người dùng" title="Thêm người dùng" description="Tạo tài khoản mới trong hệ thống." />
    <Card className="profile-form-card">
      <Alert className="form-error" type="info" showIcon title="Tài khoản mới được gán vai trò Khách hàng mặc định." />
      {mutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(mutation.error).detail} />}
      <form noValidate onSubmit={handleSubmit(values => mutation.mutate({ email: values.email, password: values.password, fullName: values.fullName }))}>
        <Form.Item label="Họ và tên" htmlFor="user-full-name" validateStatus={formState.errors.fullName ? 'error' : undefined} help={formState.errors.fullName?.message}>
          <Controller name="fullName" control={control} render={({ field }) => <Input {...field} id="user-full-name" autoComplete="name" disabled={mutation.isPending} />} />
        </Form.Item>
        <Form.Item label="Email" htmlFor="user-email" validateStatus={formState.errors.email ? 'error' : undefined} help={formState.errors.email?.message}>
          <Controller name="email" control={control} render={({ field }) => <Input {...field} id="user-email" type="email" autoComplete="email" disabled={mutation.isPending} />} />
        </Form.Item>
        <Form.Item label="Mật khẩu tạm thời" htmlFor="user-password" validateStatus={formState.errors.password ? 'error' : undefined} help={formState.errors.password?.message ?? 'Tối thiểu 8 ký tự.'}>
          <Controller name="password" control={control} render={({ field }) => <Input.Password {...field} id="user-password" autoComplete="new-password" disabled={mutation.isPending} />} />
        </Form.Item>
        <Space><Button type="primary" htmlType="submit" loading={mutation.isPending}>Tạo tài khoản</Button>
          <Button onClick={() => navigate(ROUTES.ADMIN_USERS)} disabled={mutation.isPending}>Hủy</Button></Space>
      </form>
    </Card>
  </>
}
