const segment = (value: string) => encodeURIComponent(value)
const resource = (root: string) => ({
  root,
  detail: (id: string) => `${root}/${segment(id)}`,
  status: (id: string) => `${root}/${segment(id)}/status`,
})

export const endpoints = {
  auth: {
    register: '/auth/register', login: '/auth/login', me: '/auth/me',
    logout: '/auth/logout', refresh: '/auth/refresh-token',
    exchange: '/auth/oauth2/exchange', providers: '/auth/providers',
    oauth: (provider: 'google' | 'facebook') => `/auth/oauth2/${provider}`,
    link: (provider: string) => `/auth/providers/${segment(provider)}/link`,
    unlink: (provider: string) => `/auth/providers/${segment(provider)}`,
  },
  users: {
    ...resource('/users'), me: '/users/me', password: '/users/me/password',
    roles: (id: string) => `/users/${segment(id)}/roles`,
    role: (id: string, roleId: string) => `/users/${segment(id)}/roles/${segment(roleId)}`,
  },
  roles: '/roles',
  airports: resource('/airports'),
  aircrafts: {
    ...resource('/aircrafts'), seats: (id: string) => `/aircrafts/${segment(id)}/seats`,
  },
  flights: resource('/flights'),
  schedules: {
    ...resource('/flight-schedules'),
    cancel: (id: string) => `/flight-schedules/${segment(id)}/cancel`,
    seats: (id: string) => `/flight-schedules/${segment(id)}/seats`,
    price: (id: string, seatId: string) => `/flight-schedules/${segment(id)}/seats/${segment(seatId)}/price`,
  },
  search: {
    root: '/search/flights',
    detail: (id: string) => `/search/flights/${segment(id)}`,
    seats: (id: string) => `/search/flights/${segment(id)}/seats`,
  },
  bookings: {
    ...resource('/bookings'), me: '/bookings/me', admin: '/admin/bookings',
    code: (code: string) => `/bookings/code/${segment(code)}`,
    cancel: (id: string) => `/bookings/${segment(id)}/cancel`,
    payments: (id: string) => `/bookings/${segment(id)}/payments`,
  },
  payments: { ...resource('/payments'), admin: '/admin/payments' },
} as const
