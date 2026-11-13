import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Input, Space } from 'antd'
import { useNavigate } from 'react-router'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { authApi } from '../../auth/api/auth.api'
import { useAuthStore } from '../../auth/store/auth.store'
import { updateProfileSchema } from '../../users/schemas/user.schema'
import { userApi } from '../../users/api/user.api'
import type { UpdateProfileRequest } from '../../users/types/user.dto'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { normalizeApiError } from '../../../shared/api/apiError'
import { ROUTES } from '../../../app/router/paths'

export function ProfileEditPage() {
  const navigate = useNavigate()
  const client = useQueryClient()
  const query = useQuery({ queryKey: queryKeys.auth.me, queryFn: authApi.me })
  const mutation = useMutation({ mutationFn: (request: UpdateProfileRequest) => userApi.updateMe(request), onSuccess: async user => {
    const current = { id: user.id, email: user.email, fullName: user.fullName, status: user.status, roles: user.roles }
    useAuthStore.getState().setUser(current)
    client.setQueryData(queryKeys.auth.me, current)
    await client.invalidateQueries({ queryKey: queryKeys.auth.me })
    navigate(ROUTES.PROFILE)
  } })
  const { control, handleSubmit, reset, formState } = useForm<UpdateProfileRequest>({
    resolver: zodResolver(updateProfileSchema), defaultValues: { fullName: '' },
  })
  useEffect(() => { if (query.data) reset({ fullName: query.data.fullName }) }, [query.data, reset])
  if (query.isPending) return <Loading label="Đang tải hồ sơ…" />
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  return <>
    <Card className="profile-form-card">
      {mutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(mutation.error).detail} />}
      <form noValidate onSubmit={handleSubmit(values => mutation.mutate(values))}>
        <div className="profile-form-field"><label htmlFor="profile-email">Email</label>
          <Input id="profile-email" value={query.data.email} disabled />
        </div>
        <div className="profile-form-field"><label htmlFor="profile-full-name">Họ và tên</label>
          <Controller name="fullName" control={control} render={({ field }) => <Input {...field} id="profile-full-name" autoComplete="name" disabled={mutation.isPending} aria-invalid={Boolean(formState.errors.fullName)} />} />
          {formState.errors.fullName && <span className="profile-form-error">{formState.errors.fullName.message}</span>}
        </div>
        <div className="profile-form-actions"><Space><Button type="primary" htmlType="submit" loading={mutation.isPending}>Lưu thay đổi</Button>
          <Button onClick={() => navigate(ROUTES.PROFILE)} disabled={mutation.isPending}>Hủy</Button></Space></div>
      </form>
    </Card>
  </>
}
