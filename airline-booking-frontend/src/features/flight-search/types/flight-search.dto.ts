/** Mirrors the current Java native-query projection; its property names follow the Java DTO. */
import type { SeatClass } from '../../aircrafts/types/aircraft.enums'
import type { FlightSeatStatus } from '../../flight-schedules/types/flight-schedule.enums'

export interface FlightSearchParams { departure: string; arrival: string; date: string }
export interface FlightSearchItem {
  id: string
  flight_id: string
  departure_id: string
  arrival_id: string
  departure_time: string
  arrival_time: string
  base_price: number
  seatAvailable: number
}
export interface FlightSeatResponse {
  id: string
  seatNumber: string
  seatClass: SeatClass
  price: number
  status: FlightSeatStatus
}
export interface SeatMapResponse { scheduleId: string; seats: FlightSeatResponse[] }
