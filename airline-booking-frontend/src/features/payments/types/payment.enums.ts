/** V1 SQL values; Java payment enums are still empty classes. */
export const PAYMENT_METHODS = ['VNPAY', 'MOMO', 'CREDIT_CARD', 'BANK_TRANSFER'] as const
export type PaymentMethod = typeof PAYMENT_METHODS[number]
/** Only these two methods have a checkout flow in doc 05-1. */
export const CHECKOUT_PAYMENT_METHODS = ['VNPAY', 'MOMO'] as const
export type CheckoutPaymentMethod = typeof CHECKOUT_PAYMENT_METHODS[number]
export const PAYMENT_STATUSES = ['PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'] as const
export type PaymentStatus = typeof PAYMENT_STATUSES[number]
