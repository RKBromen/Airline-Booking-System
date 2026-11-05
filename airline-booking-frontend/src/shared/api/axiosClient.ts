import axios from 'axios'
import { useAuthStore } from '../../features/auth/store/auth.store'
import type { LoginResponse } from '../../features/auth/types/auth.dto'
import { endpoints } from './endpoints'
import { normalizeApiError } from './apiError'

declare module 'axios' {
  interface AxiosRequestConfig { _authRetried?: boolean }
}

export const apiBaseURL = import.meta.env?.VITE_API_BASE_URL || '/api/v1'
const options = {
  baseURL: apiBaseURL, timeout: 20_000, withCredentials: true,
  withXSRFToken: true, xsrfCookieName: 'XSRF-TOKEN', xsrfHeaderName: 'X-XSRF-TOKEN',
}
export const axiosClient = axios.create(options)
// No auth interceptors here: a rejected refresh must never refresh recursively.
const sessionClient = axios.create(options)
let refreshing: Promise<LoginResponse> | null = null

export async function refreshSession(): Promise<LoginResponse> {
  if (!refreshing) {
    const previousVersion = useAuthStore.getState().sessionVersion
    refreshing = sessionClient.post<LoginResponse>(endpoints.auth.refresh).then(({ data }) => {
      // Do not resurrect a session cleared/replaced while a request was in flight.
      if (useAuthStore.getState().sessionVersion === previousVersion) useAuthStore.getState().setSession(data)
      return data
    }).catch(error => {
      if (useAuthStore.getState().sessionVersion === previousVersion) useAuthStore.getState().clearSession()
      throw normalizeApiError(error)
    }).finally(() => { refreshing = null })
  }
  return refreshing
}

axiosClient.interceptors.request.use(config => {
  const token = useAuthStore.getState().accessToken
  if (token) config.headers.set('Authorization', `Bearer ${token}`)
  return config
})

axiosClient.interceptors.response.use(response => response, async error => {
  const config = axios.isAxiosError(error) ? error.config : undefined
  const token = useAuthStore.getState().accessToken
  const sessionEndpoints: string[] = [endpoints.auth.login, endpoints.auth.register,
    endpoints.auth.refresh, endpoints.auth.exchange, endpoints.auth.logout]
  // Session endpoints handle their own 401s; retry protected calls at most once.
  if (config && error.response?.status === 401 && token && !sessionEndpoints.includes(config.url ?? '')
      && !config._authRetried) {
    config._authRetried = true
    // A parallel request may already have refreshed while this 401 was in flight.
    const sentToken = config.headers.get('Authorization')
    if (sentToken !== `Bearer ${token}`) {
      config.headers.set('Authorization', `Bearer ${token}`)
      return axiosClient.request(config)
    }
    const session = await refreshSession()
    if (useAuthStore.getState().accessToken !== session.accessToken) throw normalizeApiError(error)
    config.headers.set('Authorization', `Bearer ${session.accessToken}`)
    return axiosClient.request(config)
  }
  if (config?._authRetried && error.response?.status === 401
      && config.headers.get('Authorization') === `Bearer ${useAuthStore.getState().accessToken}`) {
    useAuthStore.getState().clearSession()
  }
  throw normalizeApiError(error)
})
