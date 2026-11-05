export const AIRPORT_STATUSES = ['ACTIVE', 'BLOCKED', 'DELETED'] as const
export type AirportStatus = typeof AIRPORT_STATUSES[number]
