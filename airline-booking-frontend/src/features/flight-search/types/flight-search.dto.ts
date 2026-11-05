/** Provisional: doc 05-1 module 7. */
import type { SeatClass } from '../../aircrafts/types/aircraft.enums'
import type { FlightSeatStatus } from '../../flight-schedules/types/flight-schedule.enums'

export interface FlightSearchParams { departure: string; arrival: string; date: string }
export interface FlightSearchItem {
  scheduleId: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  basePrice: number
  availableSeats: number
}
export interface FlightSeatResponse {
  id: string
  seatNumber: string
  seatClass: SeatClass
  price: number
  status: FlightSeatStatus
}
export interface SeatMapResponse { scheduleId: string; seats: FlightSeatResponse[] }
