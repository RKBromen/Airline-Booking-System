import { z } from 'zod'
import { localDateTimeSchema } from '../../../shared/validation/common'

export const flightScheduleCreateSchema = z.object({
  flightId: z.uuid(), aircraftId: z.uuid(), departureTime: localDateTimeSchema,
  arrivalTime: localDateTimeSchema, basePrice: z.number().finite().nonnegative(),
}).refine(value => Date.parse(value.arrivalTime) > Date.parse(value.departureTime), {
  path: ['arrivalTime'], message: 'Giờ đến phải sau giờ khởi hành.',
})
export const flightSeatPriceSchema = z.object({ price: z.number().finite().nonnegative() })
