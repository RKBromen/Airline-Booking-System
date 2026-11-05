import type { AirportStatus } from '../types/airport.enums'

export const AIRPORT_STATUS_LABELS: Record<AirportStatus, string> = {
  ACTIVE: 'Đang hoạt động', BLOCKED: 'Tạm ngưng', DELETED: 'Đã xóa',
}
export const AIRPORT_STATUS_COLORS: Record<AirportStatus, string> = {
  ACTIVE: 'success', BLOCKED: 'warning', DELETED: 'default',
}
export const AIRPORT_PAGE_SIZE = 10
