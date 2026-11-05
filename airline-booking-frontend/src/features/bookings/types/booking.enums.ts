/** Design + V1 SQL values; Java enums/DTOs are not implemented. */
export const BOOKING_STATUSES = ['PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'] as const
export type BookingStatus = typeof BOOKING_STATUSES[number]
export const GENDERS = ['MALE', 'FEMALE', 'OTHER'] as const
export type Gender = typeof GENDERS[number]
