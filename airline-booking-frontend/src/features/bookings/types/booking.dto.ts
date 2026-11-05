/** Provisional: doc 05-1 module 8. Required fields mirror its example;
 * SQL nullability alone cannot establish HTTP request/response nullability. */
import type { BookingStatus, Gender } from './booking.enums'

export interface BookingPassengerRequest {
  fullName: string
  dateOfBirth: string
  gender: Gender
  documentNumber: string
  flightSeatId: string
}
export interface CreateBookingRequest { flightScheduleId: string; passengers: BookingPassengerRequest[] }
export interface BookingPassengerResponse { id: string; fullName: string; seatNumber: string }
export interface CreateBookingResponse {
  id: string
  bookingCode: string
  flightScheduleId: string
  status: BookingStatus
  totalAmount: number
  expiresAt: string
  passengers: BookingPassengerResponse[]
}
export interface BookingDetailResponse {
  id: string
  bookingCode: string
  status: BookingStatus
  flight: { flightNumber: string; departureAirport: string; arrivalAirport: string; departureTime: string }
  passengers: BookingPassengerResponse[]
  totalAmount: number
  expiresAt: string
}
export interface CancelBookingResponse { id: string; status: 'CANCELLED' }
