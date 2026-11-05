import { z } from 'zod'
import { emailSchema, fullNameSchema, newPasswordSchema } from '../../../shared/validation/common'
import { USER_STATUSES } from '../types/user.enums'

export const userCreateSchema = z.object({
  email: emailSchema, password: newPasswordSchema, fullName: fullNameSchema,
  roleIds: z.array(z.uuid()).nullable().optional(),
})
export const userUpdateSchema = z.object({ fullName: fullNameSchema })
export const updateProfileSchema = z.object({ fullName: fullNameSchema })
export const userStatusSchema = z.object({ status: z.enum(USER_STATUSES) })
export const changePasswordSchema = z.object({
  currentPassword: z.string().min(1).refine(value => value.trim().length > 0), newPassword: newPasswordSchema,
})
export const userListSchema = z.object({
  page: z.int().min(0).optional(), size: z.int().min(1).max(100).optional(),
  sort: z.enum(['createdAt', 'email', 'fullName']).optional(), status: z.enum(USER_STATUSES).optional(),
})
