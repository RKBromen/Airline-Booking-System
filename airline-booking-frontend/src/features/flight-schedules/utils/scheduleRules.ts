import type { FlightScheduleStatus, FlightSeatStatus } from '../types/flight-schedule.enums'

const transitions: Record<FlightScheduleStatus, readonly FlightScheduleStatus[]> = {
  SCHEDULED: ['BOARDING', 'CANCELLED'], BOARDING: ['DEPARTED', 'CANCELLED'],
  DEPARTED: ['COMPLETED'], COMPLETED: [], CANCELLED: [],
}
/** UI hints from doc 05-1; the backend must validate again. */
export const canTransitionSchedule = (current: FlightScheduleStatus, next: FlightScheduleStatus) =>
  transitions[current].includes(next)
export const canEditSeatPrice = (status: FlightSeatStatus) => status === 'AVAILABLE'
