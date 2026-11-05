import type { AirportListParams } from '../../features/airports/types/airport.dto'
import type { UserListParams } from '../../features/users/types/user.dto'
import type { FlightListParams } from '../../features/flights/types/flight.dto'
import type { FlightSearchParams } from '../../features/flight-search/types/flight-search.dto'

export const queryKeys = {
  auth: { all: ['auth'] as const, me: ['auth', 'me'] as const, providers: ['auth', 'providers'] as const },
  airports: {
    all: ['airports'] as const, list: (params: AirportListParams) => ['airports', 'list', params] as const,
    detail: (id: string) => ['airports', 'detail', id] as const,
  },
  aircrafts: {
    all: ['aircrafts'] as const, list: ['aircrafts', 'list'] as const,
    detail: (id: string) => ['aircrafts', 'detail', id] as const,
    seats: (id: string) => ['aircrafts', 'seats', id] as const,
  },
  users: {
    all: ['users'] as const, list: (params: UserListParams) => ['users', 'list', params] as const,
    detail: (id: string) => ['users', 'detail', id] as const,
  },
  flights: {
    all: ['flights'] as const,
    list: (params: FlightListParams) => ['flights', 'list', params] as const,
    detail: (id: string) => ['flights', 'detail', id] as const,
  },
  schedules: {
    all: ['flight-schedules'] as const, list: ['flight-schedules', 'list'] as const,
    detail: (id: string) => ['flight-schedules', 'detail', id] as const,
    seats: (id: string) => ['flight-schedules', 'seats', id] as const,
  },
  search: {
    all: ['flight-search'] as const,
    list: (params: FlightSearchParams) => ['flight-search', 'list', params] as const,
    detail: (id: string) => ['flight-search', 'detail', id] as const,
    seats: (id: string) => ['flight-search', 'seats', id] as const,
  },
  bookings: {
    all: ['bookings'] as const, mine: ['bookings', 'me'] as const,
    detail: (id: string) => ['bookings', 'detail', id] as const,
  },
  payments: {
    all: ['payments'] as const, history: (bookingId: string) => ['payments', 'history', bookingId] as const,
    detail: (id: string) => ['payments', 'detail', id] as const,
  },
} as const
