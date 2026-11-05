/** Provisional: doc 05-1 module 9. */
import type { CheckoutPaymentMethod, PaymentStatus } from './payment.enums'

export interface CreatePaymentRequest { paymentMethod: CheckoutPaymentMethod }
export interface CreatePaymentResponse {
  paymentId: string
  bookingId: string
  amount: number
  paymentMethod: CheckoutPaymentMethod
  status: PaymentStatus
  paymentUrl: string
}
// History's paidAt null vs omitted semantics and detail/admin DTOs need confirmation.
// Keep those entire responses unknown instead of inferring JSON from SQL columns.
