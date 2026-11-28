import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { CreatePaymentRequest, CreatePaymentResponse, PaymentHistoryResponse } from '../types/payment.dto'

/** Design-only. Webhooks and provider return handlers are server-to-server/browser routes,
 * not customer API mutations; never confirm a booking from the frontend. */
export const paymentApi = {
  create: async (bookingId: string, request: CreatePaymentRequest) =>
    (await axiosClient.post<CreatePaymentResponse>(endpoints.bookings.payments(bookingId), request)).data,
  history: async (bookingId: string) =>
    (await axiosClient.get<PaymentHistoryResponse[]>(
      endpoints.bookings.payments(bookingId)
    )).data,
  get: async (id: string) => (await axiosClient.get<unknown>(endpoints.payments.detail(id))).data,
  adminList: async () => (await axiosClient.get<unknown>(endpoints.payments.admin)).data,
}
