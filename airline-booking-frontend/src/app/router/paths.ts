export const ROUTES = {
  HOME: '/', AIRPORTS: '/airports', AIRPORT_DETAIL: '/airports/:id',
  LOGIN: '/login', FORBIDDEN: '/forbidden',
  AIRCRAFTS: '/management/aircrafts', AIRCRAFT_NEW: '/management/aircrafts/new',
  AIRCRAFT_DETAIL: '/management/aircrafts/:id', AIRCRAFT_EDIT: '/management/aircrafts/:id/edit',
  AIRCRAFT_SEATS: '/management/aircrafts/:id/seats',
} as const
export const routeTo = {
  airportDetail: (id: string) => `/airports/${encodeURIComponent(id)}`,
  aircraftDetail: (id: string) => `/management/aircrafts/${encodeURIComponent(id)}`,
  aircraftEdit: (id: string) => `/management/aircrafts/${encodeURIComponent(id)}/edit`,
  aircraftSeats: (id: string) => `/management/aircrafts/${encodeURIComponent(id)}/seats`,
}
