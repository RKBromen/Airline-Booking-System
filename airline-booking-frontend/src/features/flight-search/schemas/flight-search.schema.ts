import { z } from 'zod'
import { airportCodeSchema } from '../../../shared/validation/common'

export const flightSearchSchema = z.object({ departure: airportCodeSchema, arrival: airportCodeSchema, date: z.iso.date() })
  .refine(value => value.departure !== value.arrival, {
    path: ['arrival'], message: 'Sân bay đến phải khác sân bay đi.',
  })
