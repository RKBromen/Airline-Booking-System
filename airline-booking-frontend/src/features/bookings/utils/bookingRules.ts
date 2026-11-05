import type { BookingStatus } from '../types/booking.enums'

/** UI prechecks only; payment/seat ownership and concurrent changes require server validation.
 * Date parsing assumes the browser and backend agree on timezone for offset-free ISO values. */
export const canStartPayment = (status: BookingStatus, expiresAt: string, now = Date.now()) =>
  status === 'PENDING' && Date.parse(expiresAt) > now
export const canCancelPendingBooking = (status: BookingStatus) => status === 'PENDING'
