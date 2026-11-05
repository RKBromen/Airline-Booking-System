import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { PageResponse } from '../../../shared/types/pagination.types'
import type { AssignRoleRequest, ChangePasswordRequest, UpdateProfileRequest, UserCreateRequest,
  UserListParams, UserResponse, UserStatusRequest, UserUpdateRequest } from '../types/user.dto'

const urls = endpoints.users
export const userApi = {
  list: async (params: UserListParams = {}) =>
    (await axiosClient.get<PageResponse<UserResponse>>(urls.root, { params })).data,
  get: async (id: string) => (await axiosClient.get<UserResponse>(urls.detail(id))).data,
  create: async (request: UserCreateRequest) => (await axiosClient.post<UserResponse>(urls.root, request)).data,
  update: async (id: string, request: UserUpdateRequest) =>
    (await axiosClient.patch<UserResponse>(urls.detail(id), request)).data,
  updateStatus: async (id: string, request: UserStatusRequest) =>
    (await axiosClient.patch<UserResponse>(urls.status(id), request)).data,
  delete: async (id: string): Promise<void> => { await axiosClient.delete(urls.detail(id)) },
  updateMe: async (request: UpdateProfileRequest) =>
    (await axiosClient.put<UserResponse>(urls.me, request)).data,
  changePassword: async (request: ChangePasswordRequest): Promise<void> => {
    await axiosClient.put(urls.password, request)
  },
}

/** Design-only: no role controller exists; list response remains unknown. */
export const plannedRoleApi = {
  list: async () => (await axiosClient.get<unknown>(endpoints.roles)).data,
  assign: async (id: string, request: AssignRoleRequest): Promise<void> => {
    await axiosClient.post(urls.roles(id), request)
  },
  remove: async (id: string, roleId: string): Promise<void> => {
    await axiosClient.delete(urls.role(id, roleId))
  },
}
