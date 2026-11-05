import type { AuthProvider, Role } from '../../auth/types/auth.enums'
import type { UserStatus } from './user.enums'
import type { PageParams } from '../../../shared/types/pagination.types'

export interface UserResponse {
  id: string
  email: string
  fullName: string
  status: UserStatus
  roles: Role[]
  authProviders: AuthProvider[]
  createdAt: string
}
export interface UserCreateRequest {
  email: string
  password: string
  fullName: string
  roleIds?: string[] | null
}
export interface UserUpdateRequest { fullName: string }
export interface UpdateProfileRequest { fullName: string }
export interface UserStatusRequest { status: UserStatus }
export interface ChangePasswordRequest { currentPassword: string; newPassword: string }
export interface UserListParams extends PageParams {
  status?: UserStatus
  sort?: 'createdAt' | 'email' | 'fullName'
}
export interface AssignRoleRequest { roleId: string }
