export const ROUTES = {
  HOME: '/', AIRPORTS: '/airports', AIRPORT_DETAIL: '/airports/:id',
  FLIGHTS: '/flights', FLIGHT_DETAIL: '/flights/:scheduleId', FLIGHT_SEATS: '/flights/:scheduleId/seats',
  LOGIN: '/login', REGISTER: '/register', OAUTH_CALLBACK: '/auth/callback', FORBIDDEN: '/forbidden',
  PROFILE: '/profile', PROFILE_EDIT: '/profile/edit', PROFILE_PASSWORD: '/profile/password',
  PROFILE_LINKED_ACCOUNTS: '/profile/linked-accounts',
  ADMIN_USERS: '/admin/users', ADMIN_USER_NEW: '/admin/users/new',
  ADMIN_USER_DETAIL: '/admin/users/:id', ADMIN_USER_EDIT: '/admin/users/:id/edit', ADMIN_ROLES: '/admin/roles',
  AIRCRAFTS: '/management/aircrafts', AIRCRAFT_NEW: '/management/aircrafts/new',
  AIRCRAFT_DETAIL: '/management/aircrafts/:id', AIRCRAFT_EDIT: '/management/aircrafts/:id/edit',
  AIRCRAFT_SEATS: '/management/aircrafts/:id/seats',
} as const
export const routeTo = {
  airportDetail: (id: string) => `/airports/${encodeURIComponent(id)}`,
  flightDetail: (id: string) => `/flights/${encodeURIComponent(id)}`,
  flightSeats: (id: string) => `/flights/${encodeURIComponent(id)}/seats`,
  aircraftDetail: (id: string) => `/management/aircrafts/${encodeURIComponent(id)}`,
  aircraftEdit: (id: string) => `/management/aircrafts/${encodeURIComponent(id)}/edit`,
  aircraftSeats: (id: string) => `/management/aircrafts/${encodeURIComponent(id)}/seats`,
  adminUser: (id: string) => `/admin/users/${encodeURIComponent(id)}`,
  adminUserEdit: (id: string) => `/admin/users/${encodeURIComponent(id)}/edit`,
}
