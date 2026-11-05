/** Mirrors user.dto.PageResponse; only users currently return this envelope. */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PageParams {
  page?: number
  size?: number
}
