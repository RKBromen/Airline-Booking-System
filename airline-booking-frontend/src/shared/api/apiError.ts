import axios from 'axios'
import type { ApiError } from '../types/api.types'

const messages: Record<number, string> = {
  400: 'Dữ liệu gửi lên không hợp lệ.',
  401: 'Vui lòng đăng nhập lại.',
  403: 'Bạn không có quyền thực hiện thao tác này.',
  404: 'Không tìm thấy dữ liệu.',
  409: 'Dữ liệu hoặc trạng thái đã thay đổi. Vui lòng tải lại.',
  422: 'Thao tác không đáp ứng điều kiện nghiệp vụ.',
  500: 'Hệ thống đang gặp lỗi. Vui lòng thử lại sau.',
}

export class ApiRequestError extends Error implements ApiError {
  readonly status: number
  readonly title: string
  readonly detail: string
  readonly type?: string
  readonly instance?: string
  readonly errorCode?: string
  readonly fieldErrors?: Record<string, string[]>

  constructor(error: ApiError) {
    super(error.detail)
    this.name = 'ApiRequestError'
    Object.assign(this, error)
    this.status = error.status
    this.title = error.title
    this.detail = error.detail
  }
}

/** Supports both RFC ProblemDetail and the current Java { message } handler. */
export function normalizeApiError(error: unknown): ApiRequestError {
  if (error instanceof ApiRequestError) return error
  const response = axios.isAxiosError(error) ? error.response : undefined
  const status = response?.status ?? 0
  const data: Record<string, unknown> = response?.data && typeof response.data === 'object'
    ? response.data : {}
  const fieldErrors = data.fieldErrors && typeof data.fieldErrors === 'object'
    ? Object.fromEntries(Object.entries(data.fieldErrors).filter(
      (entry): entry is [string, string[]] => Array.isArray(entry[1]) && entry[1].every(value => typeof value === 'string'),
    )) : undefined
  return new ApiRequestError({
    status,
    title: typeof data.title === 'string' ? data.title : (status ? `HTTP ${status}` : 'Network Error'),
    detail: typeof data.detail === 'string' ? data.detail
      : typeof data.message === 'string' ? data.message
        : messages[status] ?? (status ? 'Không thể thực hiện yêu cầu.' : 'Không thể kết nối đến máy chủ.'),
    type: typeof data.type === 'string' ? data.type : undefined,
    instance: typeof data.instance === 'string' ? data.instance : undefined,
    errorCode: typeof data.errorCode === 'string' ? data.errorCode : undefined,
    fieldErrors,
  })
}
