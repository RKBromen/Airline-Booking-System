/** Design + V1 SQL enum; flight.enums.FlightStatus is still an empty class. */
export const FLIGHT_STATUSES = ['ACTIVE', 'INACTIVE'] as const
export type FlightStatus = typeof FLIGHT_STATUSES[number]
