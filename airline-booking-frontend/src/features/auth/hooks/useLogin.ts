import { useMutation, useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/auth.api'
import { useAuthStore } from '../store/auth.store'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { loginSchema } from '../schemas/auth.schema'
import type { LoginRequest } from '../types/auth.dto'

export function useLogin() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (request: LoginRequest) => authApi.login(loginSchema.parse(request)),
    onSuccess: async session => {
      await client.cancelQueries()
      client.removeQueries()
      useAuthStore.getState().setSession(session)
      await client.invalidateQueries({ queryKey: queryKeys.auth.all })
    },
  })
}
