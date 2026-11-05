/** Provisional: examples in doc 05-1, module 5; no Java DTO yet. */
import type { FlightStatus } from './flight.enums'

export interface FlightRequest { flightNumber: string; departureAirportId: string; arrivalAirportId: string }
export interface FlightResponse {
  id: string
  flightNumber: string
  departureAirport: { id: string; code: string }
  arrivalAirport: { id: string; code: string }
  status: FlightStatus
}
export interface FlightListItem {
  id: string
  flightNumber: string
  departureAirportCode: string
  arrivalAirportCode: string
  status: FlightStatus
}
export interface FlightListParams { departureAirportId?: string; arrivalAirportId?: string }
export interface FlightStatusRequest { status: FlightStatus }
