import { useMutation, useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/auth.api'
import { useAuthStore } from '../store/auth.store'

export function useLogout() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: authApi.logout,
    onMutate: async () => {
      useAuthStore.getState().clearSession()
      await client.cancelQueries()
      client.removeQueries()
    },
    onSettled: () => { client.removeQueries() },
  })
}
