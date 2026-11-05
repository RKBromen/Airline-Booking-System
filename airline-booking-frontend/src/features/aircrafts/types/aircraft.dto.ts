import type { AircraftStatus, SeatClass } from './aircraft.enums'

export interface AircraftRequest { registrationNo: string }
export interface AircraftResponse { id: string; registrationNo: string; status: AircraftStatus }
export interface AircraftStatusRequest { status: AircraftStatus }
export interface AircraftSeatRequest { seatNumber: string; seatClass: SeatClass }
export interface AircraftSeatListItem { id: string; seatNumber: string; seatClass: SeatClass }
export interface AircraftSeatResponse { id: string; aircraftId: string; seatNumber: string; seatClass: SeatClass }
