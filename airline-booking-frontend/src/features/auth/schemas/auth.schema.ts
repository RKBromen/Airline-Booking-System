import { z } from 'zod'
import { emailSchema, fullNameSchema, newPasswordSchema } from '../../../shared/validation/common'

export const loginSchema = z.object({ email: emailSchema, password: z.string().min(1).max(72) })
export const registerSchema = z.object({ email: emailSchema, password: newPasswordSchema, fullName: fullNameSchema })
export const oauthExchangeSchema = z.object({ code: z.string().trim().min(1) })
