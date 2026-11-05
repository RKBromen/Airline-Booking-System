/** Provisional: doc 05-1 module 6; numeric JSON prices need backend confirmation. */
import type { FlightScheduleStatus } from './flight-schedule.enums'

export interface FlightScheduleRequest {
  flightId: string
  aircraftId: string
  departureTime: string
  arrivalTime: string
  basePrice: number
}
export interface FlightScheduleResponse {
  id: string
  flightId: string
  aircraftId: string
  departureTime: string
  arrivalTime: string
  basePrice: number
  status: FlightScheduleStatus
}
export interface FlightScheduleStatusRequest { status: FlightScheduleStatus }
export interface FlightScheduleStatusResponse { id: string; status: FlightScheduleStatus }
export interface FlightSeatPriceRequest { price: number }
