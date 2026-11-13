import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Alert, Button, Card, Form, Input } from 'antd'
import { z } from 'zod'
import { userApi } from '../../users/api/user.api'
import { newPasswordSchema } from '../../../shared/validation/common'
import { normalizeApiError } from '../../../shared/api/apiError'
import type { ChangePasswordRequest } from '../../users/types/user.dto'
import { authApi } from '../../auth/api/auth.api'
import { queryKeys } from '../../../shared/constants/queryKeys'

const passwordFormSchema = z.object({
  currentPassword: z.string().min(1, 'Nhập mật khẩu hiện tại.').refine(value => value.trim().length > 0, 'Nhập mật khẩu hiện tại.'),
  newPassword: newPasswordSchema,
  confirmPassword: z.string(),
}).refine(value => value.newPassword === value.confirmPassword, {
  path: ['confirmPassword'], message: 'Mật khẩu xác nhận chưa khớp.',
})
type PasswordFormValues = z.infer<typeof passwordFormSchema>

export function ChangePasswordPage() {
  const [changed, setChanged] = useState(false)
  const providers = useQuery({ queryKey: queryKeys.auth.providers, queryFn: authApi.providers })
  const mutation = useMutation({ mutationFn: (value: ChangePasswordRequest) => userApi.changePassword(value), onSuccess: () => setChanged(true) })
  const { control, handleSubmit, reset, formState } = useForm<PasswordFormValues>({
    resolver: zodResolver(passwordFormSchema), defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  })
  const passwordFormDisabled = mutation.isPending || providers.isPending || providers.data?.hasPassword === false
  return <>
    <Card className="profile-form-card">
      {providers.data?.hasPassword === false && <Alert className="form-error" type="info" showIcon
        title="Tài khoản này chưa có mật khẩu. Hệ thống hiện chỉ hỗ trợ đổi mật khẩu hiện có." />}
      {changed && <Alert className="form-error" type="success" showIcon title="Mật khẩu đã được cập nhật. Hãy dùng mật khẩu mới cho những lần đăng nhập sau." />}
      {mutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(mutation.error).detail} />}
      <form noValidate onSubmit={handleSubmit(async values => {
        if (providers.data?.hasPassword === false) return
        const request: ChangePasswordRequest = { currentPassword: values.currentPassword, newPassword: values.newPassword }
        try { await mutation.mutateAsync(request); reset() } catch { /* Render mutation error above. */ }
      })}>
        <Form.Item label="Mật khẩu hiện tại" htmlFor="current-password" validateStatus={formState.errors.currentPassword ? 'error' : undefined} help={formState.errors.currentPassword?.message}>
          <Controller name="currentPassword" control={control} render={({ field }) => <Input.Password {...field} id="current-password" autoComplete="current-password" disabled={passwordFormDisabled} />} />
        </Form.Item>
        <Form.Item label="Mật khẩu mới" htmlFor="new-password" validateStatus={formState.errors.newPassword ? 'error' : undefined} help={formState.errors.newPassword?.message ?? 'Tối thiểu 8 ký tự.'}>
          <Controller name="newPassword" control={control} render={({ field }) => <Input.Password {...field} id="new-password" autoComplete="new-password" disabled={passwordFormDisabled} />} />
        </Form.Item>
        <Form.Item label="Xác nhận mật khẩu mới" htmlFor="confirm-new-password" validateStatus={formState.errors.confirmPassword ? 'error' : undefined} help={formState.errors.confirmPassword?.message}>
          <Controller name="confirmPassword" control={control} render={({ field }) => <Input.Password {...field} id="confirm-new-password" autoComplete="new-password" disabled={passwordFormDisabled} />} />
        </Form.Item>
        <Button type="primary" htmlType="submit" loading={mutation.isPending} disabled={passwordFormDisabled}>Cập nhật mật khẩu</Button>
      </form>
    </Card>
  </>
}
