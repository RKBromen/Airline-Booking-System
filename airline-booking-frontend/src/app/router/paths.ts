export const ROUTES = { HOME: '/', AIRPORTS: '/airports', AIRPORT_DETAIL: '/airports/:id' } as const
export const routeTo = { airportDetail: (id: string) => `/airports/${encodeURIComponent(id)}` }
