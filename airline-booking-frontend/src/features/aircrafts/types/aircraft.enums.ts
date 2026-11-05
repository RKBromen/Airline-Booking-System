export const AIRCRAFT_STATUSES = ['ACTIVE', 'BLOCKED', 'DELETED'] as const
export type AircraftStatus = typeof AIRCRAFT_STATUSES[number]
export const SEAT_CLASSES = ['ECONOMY', 'BUSINESS', 'FIRST'] as const
export type SeatClass = typeof SEAT_CLASSES[number]
