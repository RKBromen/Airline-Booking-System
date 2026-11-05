import type { AuthProvider, Role } from './auth.enums'
import type { UserStatus } from '../../users/types/user.enums'

export interface RegisterRequest { email: string; password: string; fullName: string }
export interface LoginRequest { email: string; password: string }
/** Refresh token is an HttpOnly cookie; AuthSession is never a JSON response. */
export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: string
  email: string
  roles: Role[]
}
export interface CurrentUserResponse {
  id: string
  email: string
  fullName: string
  status: UserStatus
  roles: Role[]
}
export interface RegisteredUserResponse {
  id: string
  email: string
  fullName: string
  status: UserStatus
  roles: Role[]
  createdAt: string
}
export interface OAuthExchangeRequest { code: string }
export interface ProviderListResponse {
  providers: { provider: AuthProvider; linked: boolean }[]
  hasPassword: boolean
}
