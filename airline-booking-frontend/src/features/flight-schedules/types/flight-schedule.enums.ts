/** Design + V1 SQL values, not implemented Java enums yet. */
export const FLIGHT_SCHEDULE_STATUSES = ['SCHEDULED', 'BOARDING', 'DEPARTED', 'COMPLETED', 'CANCELLED'] as const
export type FlightScheduleStatus = typeof FLIGHT_SCHEDULE_STATUSES[number]
export const FLIGHT_SEAT_STATUSES = ['AVAILABLE', 'HELD', 'BOOKED'] as const
export type FlightSeatStatus = typeof FLIGHT_SEAT_STATUSES[number]
