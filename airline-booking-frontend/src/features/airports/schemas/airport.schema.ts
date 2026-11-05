import { z } from 'zod'
import { airportCodeSchema } from '../../../shared/validation/common'
import { AIRPORT_STATUSES } from '../types/airport.enums'

const fields = {
  name: z.string().trim().min(1).max(150),
  city: z.string().trim().min(1).max(100),
  country: z.string().trim().min(1).max(100),
}
export const airportCreateSchema = z.object({ code: airportCodeSchema, ...fields })
export const airportReplaceSchema = z.object({ code: airportCodeSchema.nullable().optional(), ...fields })
export const airportUpdateSchema = z.object({
  code: airportCodeSchema.nullable().optional(),
  name: fields.name.nullable().optional(), city: fields.city.nullable().optional(), country: fields.country.nullable().optional(),
})
export const airportStatusSchema = z.object({ status: z.enum(AIRPORT_STATUSES) })
