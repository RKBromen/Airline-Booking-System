import { z } from 'zod'
import { AIRCRAFT_STATUSES } from '../types/aircraft.enums'

export const aircraftSchema = z.object({ registrationNo: z.string().trim()
  .min(1, 'Vui lòng nhập số đăng ký máy bay.').max(50, 'Số đăng ký không quá 50 ký tự.') })
export const aircraftStatusSchema = z.object({ status: z.enum(AIRCRAFT_STATUSES) })
