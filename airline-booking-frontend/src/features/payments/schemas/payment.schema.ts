import { z } from 'zod'
import { CHECKOUT_PAYMENT_METHODS } from '../types/payment.enums'

// strict() rejects client-provided amount/status rather than passing them to the server.
export const createPaymentSchema = z.object({ paymentMethod: z.enum(CHECKOUT_PAYMENT_METHODS) }).strict()
