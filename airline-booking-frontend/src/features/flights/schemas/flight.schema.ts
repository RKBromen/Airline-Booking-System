import { z } from 'zod'

/** Provisional, supported by doc 05-1 and V1 SQL. Uniqueness/active airports remain server checks. */
export const flightCreateSchema = z.object({
  flightNumber: z.string().trim().min(1).max(20), departureAirportId: z.uuid(), arrivalAirportId: z.uuid(),
}).refine(value => value.departureAirportId !== value.arrivalAirportId, {
  path: ['arrivalAirportId'], message: 'Sân bay đến phải khác sân bay đi.',
})
