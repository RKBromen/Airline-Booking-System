import { z } from 'zod'
import { SEAT_CLASSES } from '../types/aircraft.enums'

export const aircraftSeatSchema = z.object({
  seatNumber: z.string().trim().min(1).max(10), seatClass: z.enum(SEAT_CLASSES),
})
