import { z } from 'zod'
import { fullNameSchema } from '../../../shared/validation/common'
import { GENDERS } from '../types/booking.enums'

/** Provisional: optional/null passenger fields must be agreed with backend before use. */
export const createBookingSchema = z.object({
  flightScheduleId: z.uuid(),
  passengers: z.array(z.object({
    fullName: fullNameSchema, dateOfBirth: z.iso.date(), gender: z.enum(GENDERS),
    documentNumber: z.string().trim().min(1).max(100), flightSeatId: z.uuid(),
  }).strict()).min(1),
}).strict().superRefine((value, context) => {
  const seats = new Set<string>()
  value.passengers.forEach((passenger, index) => {
    if (seats.has(passenger.flightSeatId)) context.addIssue({
      code: 'custom', path: ['passengers', index, 'flightSeatId'], message: 'Không thể chọn cùng một ghế cho nhiều hành khách.',
    })
    seats.add(passenger.flightSeatId)
  })
})
