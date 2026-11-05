import { z } from 'zod'

export const fullNameSchema = z.string().trim().min(1).max(150)
export const emailSchema = z.email().max(255)
export const newPasswordSchema = z.string().min(8).max(72)
export const airportCodeSchema = z.string().regex(/^[a-zA-Z]{3}$/).transform(value => value.toUpperCase())
export const localDateTimeSchema = z.iso.datetime({ local: true, offset: true })
