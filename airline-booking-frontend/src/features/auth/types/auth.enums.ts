export const ROLES = ['CUSTOMER', 'STAFF', 'ADMIN'] as const
export type Role = typeof ROLES[number]
export const AUTH_PROVIDERS = ['GOOGLE', 'FACEBOOK'] as const
export type AuthProvider = typeof AUTH_PROVIDERS[number]
