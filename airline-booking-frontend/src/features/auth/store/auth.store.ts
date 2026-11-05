import { create } from 'zustand'
import type { CurrentUserResponse, LoginResponse } from '../types/auth.dto'
import type { Role } from '../types/auth.enums'

interface AuthState {
  sessionVersion: number
  accessToken: string | null
  roles: Role[]
  user: CurrentUserResponse | null
  setSession: (session: LoginResponse) => void
  setUser: (user: CurrentUserResponse) => void
  clearSession: () => void
}

// Keep access tokens in memory. Refresh tokens are owned by the browser cookie.
export const useAuthStore = create<AuthState>((set) => ({
  sessionVersion: 0, accessToken: null, roles: [], user: null,
  setSession: session => set(state => ({ sessionVersion: state.sessionVersion + 1,
    accessToken: session.accessToken, roles: session.roles, user: null })),
  setUser: user => set({ user, roles: user.roles }),
  clearSession: () => set(state => ({ sessionVersion: state.sessionVersion + 1,
    accessToken: null, roles: [], user: null })),
}))
