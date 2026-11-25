import { z } from 'zod'
import { airportCodeSchema } from '../../../shared/validation/common'

const optionalFilter = <T extends z.ZodType>(schema: T) =>
  z.preprocess(value => value === '' || value == null ? undefined : value, schema.optional())

export const flightSearchSchema = z.object({
  departure: optionalFilter(airportCodeSchema),
  arrival: optionalFilter(airportCodeSchema),
  date: optionalFilter(z.iso.date()),
})
  .refine(value => !value.departure || !value.arrival || value.departure !== value.arrival, {
    path: ['arrival'], message: 'Sân bay đến phải khác sân bay đi.',
  })
