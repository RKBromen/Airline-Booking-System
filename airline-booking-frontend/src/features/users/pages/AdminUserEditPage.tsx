import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Form, Input, Result, Space } from 'antd'
import { Link, useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { PageHeader } from '../../../components/ui/PageHeader'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { userApi } from '../api/user.api'
import { userUpdateSchema } from '../schemas/user.schema'
import type { UserUpdateRequest } from '../types/user.dto'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { normalizeApiError } from '../../../shared/api/apiError'
import { ROUTES, routeTo } from '../../../app/router/paths'

export function AdminUserEditPage() {
  const { id = '' } = useParams()
  const validId = z.uuid().safeParse(id).success
  const navigate = useNavigate()
  const client = useQueryClient()
  const query = useQuery({ queryKey: queryKeys.users.detail(id), queryFn: () => userApi.get(id), enabled: validId })
  const mutation = useMutation({ mutationFn: (request: UserUpdateRequest) => userApi.update(id, request), onSuccess: async user => {
    client.setQueryData(queryKeys.users.detail(id), user)
    await client.invalidateQueries({ queryKey: queryKeys.users.all })
    navigate(routeTo.adminUser(id))
  } })
  const { control, handleSubmit, reset, formState } = useForm<UserUpdateRequest>({
    resolver: zodResolver(userUpdateSchema), defaultValues: { fullName: '' },
  })
  useEffect(() => { if (query.data) reset({ fullName: query.data.fullName }) }, [query.data, reset])
  if (!validId) return <Result status="404" title="Không tìm thấy người dùng" extra={<Link to={ROUTES.ADMIN_USERS}>Quay lại danh sách</Link>} />
  if (query.isPending) return <Loading label="Đang tải người dùng…" />
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  return <>
    <PageHeader eyebrow="Quản trị người dùng" title="Cập nhật người dùng" description={query.data.email} />
    <Card className="profile-form-card">
      {mutation.isError && <Alert className="form-error" type="error" showIcon title={normalizeApiError(mutation.error).detail} />}
      <form noValidate onSubmit={handleSubmit(values => mutation.mutate(values))}>
        <Form.Item label="Email" htmlFor="user-edit-email"><Input id="user-edit-email" value={query.data.email} disabled /></Form.Item>
        <Form.Item label="Họ và tên" htmlFor="user-edit-name" validateStatus={formState.errors.fullName ? 'error' : undefined} help={formState.errors.fullName?.message}>
          <Controller name="fullName" control={control} render={({ field }) => <Input {...field} id="user-edit-name" autoComplete="name" disabled={mutation.isPending} />} />
        </Form.Item>
        <Space><Button type="primary" htmlType="submit" loading={mutation.isPending}>Lưu thay đổi</Button>
          <Button onClick={() => navigate(routeTo.adminUser(id))} disabled={mutation.isPending}>Hủy</Button></Space>
      </form>
    </Card>
  </>
}
