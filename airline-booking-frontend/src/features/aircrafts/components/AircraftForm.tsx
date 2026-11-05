import { useRef } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Alert, Button, Form, Input, Space } from 'antd'
import { aircraftSchema } from '../schemas/aircraft.schema'
import type { AircraftRequest } from '../types/aircraft.dto'
import { normalizeApiError } from '../../../shared/api/apiError'

export function AircraftForm({ mode, initialValues, isSubmitting = false, onSubmit, onCancel }: {
  mode: 'create' | 'edit'; initialValues?: AircraftRequest; isSubmitting?: boolean
  onSubmit: (values: AircraftRequest) => Promise<void>; onCancel: () => void
}) {
  const lock = useRef(false)
  const { control, handleSubmit, setError, clearErrors, formState } = useForm<AircraftRequest>({
    resolver: zodResolver(aircraftSchema), defaultValues: initialValues ?? { registrationNo: '' },
  })
  const busy = isSubmitting || formState.isSubmitting
  const submitValues = async (values: AircraftRequest) => {
    if (lock.current || isSubmitting) return
    lock.current = true
    clearErrors('root')
    try { await onSubmit(values) } catch (error) {
      const apiError = normalizeApiError(error)
      setError('root.server', { message: apiError.detail })
      const fieldError = apiError.fieldErrors?.registrationNo?.join(' ')
      if (fieldError) setError('registrationNo', { message: fieldError })
    } finally { lock.current = false }
  }
  return <form onSubmit={event => { void handleSubmit(submitValues)(event) }} noValidate>
    {formState.errors.root?.server?.message && <Alert className="form-error" type="error" showIcon
      title={formState.errors.root.server.message} />}
    <Form.Item label="Số đăng ký máy bay" htmlFor="registrationNo" required
      validateStatus={formState.errors.registrationNo ? 'error' : undefined} help={formState.errors.registrationNo?.message}>
      <Controller control={control} name="registrationNo" render={({ field }) =>
        <Input {...field} id="registrationNo" size="large" placeholder="Ví dụ: VN-A321" maxLength={50}
          disabled={busy} aria-invalid={Boolean(formState.errors.registrationNo)} /> } />
    </Form.Item>
    <Space wrap><Button type="primary" htmlType="submit" loading={busy} disabled={busy}>
      {mode === 'create' ? 'Thêm máy bay' : 'Lưu thay đổi'}</Button>
      <Button onClick={onCancel} disabled={busy}>Hủy</Button></Space>
  </form>
}
