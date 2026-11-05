import { useAuthStore } from '../store/auth.store'

export function useAuth() {
  const { accessToken, user, roles } = useAuthStore()
  return { user, roles, isAuthenticated: Boolean(accessToken), accessToken }
}
