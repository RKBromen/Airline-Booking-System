import { axiosClient, apiBaseURL, refreshSession } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { CurrentUserResponse, LoginRequest, LoginResponse, OAuthExchangeRequest,
  ProviderLinkResponse, ProviderListResponse, RegisteredUserResponse, RegisterRequest } from '../types/auth.dto'

export const authApi = {
  register: async (request: RegisterRequest) =>
    (await axiosClient.post<RegisteredUserResponse>(endpoints.auth.register, request)).data,
  login: async (request: LoginRequest) =>
    (await axiosClient.post<LoginResponse>(endpoints.auth.login, request)).data,
  me: async () => (await axiosClient.get<CurrentUserResponse>(endpoints.auth.me)).data,
  logout: async (): Promise<void> => { await axiosClient.post(endpoints.auth.logout) },
  refresh: refreshSession,
  exchange: async (request: OAuthExchangeRequest) =>
    (await axiosClient.post<LoginResponse>(endpoints.auth.exchange, request)).data,
  providers: async () => (await axiosClient.get<ProviderListResponse>(endpoints.auth.providers)).data,
  linkProvider: async (provider: 'GOOGLE' | 'FACEBOOK') =>
    (await axiosClient.post<ProviderLinkResponse>(endpoints.auth.link(provider))).data,
  unlinkProvider: async (provider: 'GOOGLE' | 'FACEBOOK'): Promise<void> => {
    await axiosClient.delete(endpoints.auth.unlink(provider))
  },
  // OAuth starts with a browser navigation, not an XHR to a provider's login page.
  oauthUrl: (provider: 'google' | 'facebook') => `${apiBaseURL.replace(/\/$/, '')}${endpoints.auth.oauth(provider)}`,
}
