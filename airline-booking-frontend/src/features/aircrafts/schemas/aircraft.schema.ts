import { z } from 'zod'
import { AIRCRAFT_STATUSES } from '../types/aircraft.enums'

export const aircraftSchema = z.object({ registrationNo: z.string().trim().min(1).max(50) })
export const aircraftStatusSchema = z.object({ status: z.enum(AIRCRAFT_STATUSES) })
