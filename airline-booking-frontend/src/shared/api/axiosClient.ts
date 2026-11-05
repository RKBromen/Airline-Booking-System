
import axios, {
  type AxiosRequestConfig,
} from 'axios'

import { useAuthStore } from '../../features/auth/store/auth.store'
import type { LoginResponse } from '../../features/auth/types/auth.dto'
import { endpoints } from './endpoints'
import { normalizeApiError } from './apiError'

declare module 'axios' {
  interface AxiosRequestConfig {
    _authRetried?: boolean
  }
}

export const apiBaseURL =
  import.meta.env.VITE_API_BASE_URL || '/api/v1'

const axiosConfig: AxiosRequestConfig = {
  baseURL: apiBaseURL,
  timeout: 20_000,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
}

export const axiosClient = axios.create(axiosConfig)

// Separate client to prevent recursive refresh.
const sessionClient = axios.create(axiosConfig)

const SESSION_ENDPOINTS = new Set<string>([
  endpoints.auth.login,
  endpoints.auth.register,
  endpoints.auth.refresh,
  endpoints.auth.exchange,
  endpoints.auth.logout,
])

// 2. Token refresh

let refreshPromise: Promise<LoginResponse> | null = null

export function refreshSession(): Promise<LoginResponse> {
  if (refreshPromise) {
    return refreshPromise
  }

  const sessionVersion = useAuthStore.getState().sessionVersion

  refreshPromise = sessionClient
    .post<LoginResponse>(endpoints.auth.refresh)
    .then(({ data }) => {
      const auth = useAuthStore.getState()

      if (auth.sessionVersion !== sessionVersion) {
        throw new Error('Session changed during refresh')
      }

      auth.setSession(data)
      return data
    })
    .catch((error: unknown) => {
      const auth = useAuthStore.getState()

      if (
        auth.sessionVersion === sessionVersion &&
        axios.isAxiosError(error) &&
        [401, 403].includes(error.response?.status ?? 0)
      ) {
        auth.clearSession()
      }

      throw normalizeApiError(error)
    })
    .finally(() => {
      refreshPromise = null
    })

  return refreshPromise
}

// 3. Request interceptor

axiosClient.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState()

  if (accessToken) {
    config.headers.set('Authorization', `Bearer ${accessToken}`)
  }

  return config
})

// 4. Response interceptor

axiosClient.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error)) {
      return Promise.reject(normalizeApiError(error))
    }

    const config = error.config
    const auth = useAuthStore.getState()

    if (!config || error.response?.status !== 401) {
      return Promise.reject(normalizeApiError(error))
    }

    if (
      config._authRetried ||
      SESSION_ENDPOINTS.has(config.url ?? '') ||
      !auth.accessToken
    ) {
      // A retried request still failed with its current token.
      if (
        config?._authRetried &&
        auth.accessToken &&
        config.headers.get('Authorization') ===
          `Bearer ${auth.accessToken}`
      ) {
        auth.clearSession()
      }

      return Promise.reject(normalizeApiError(error))
    }

    // 5. Retry protected request once

    config._authRetried = true

    const currentToken = auth.accessToken
    const sentToken = config.headers.get('Authorization')

    // Another request may have refreshed the token already.
    if (sentToken !== `Bearer ${currentToken}`) {
      config.headers.set('Authorization', `Bearer ${currentToken}`)
      return axiosClient.request(config)
    }

    try {
      const session = await refreshSession()

      // Prevent retry with a stale or replaced session.
      if (
        useAuthStore.getState().accessToken !==
        session.accessToken
      ) {
        throw normalizeApiError(error)
      }

      config.headers.set(
        'Authorization',
        `Bearer ${session.accessToken}`,
      )

      return axiosClient.request(config)
    } catch (refreshError) {
      return Promise.reject(normalizeApiError(refreshError))
    }
  },
)
