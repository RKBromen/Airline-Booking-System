import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { BookingDetailResponse, CancelBookingResponse, CreateBookingRequest,
  CreateBookingResponse } from '../types/booking.dto'

const urls = endpoints.bookings
/** Design-only. No list envelopes or lookup-by-code response are specified. */
export const plannedBookingApi = {
  create: async (request: CreateBookingRequest) =>
    (await axiosClient.post<CreateBookingResponse>(urls.root, request)).data,
  mine: async () => (await axiosClient.get<unknown>(urls.me)).data,
  get: async (id: string) => (await axiosClient.get<BookingDetailResponse>(urls.detail(id))).data,
  byCode: async (code: string) => (await axiosClient.get<unknown>(urls.code(code))).data,
  cancel: async (id: string) => (await axiosClient.post<CancelBookingResponse>(urls.cancel(id))).data,
  adminList: async () => (await axiosClient.get<unknown>(urls.admin)).data,
}
