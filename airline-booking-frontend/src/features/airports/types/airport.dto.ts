import type { AirportStatus } from './airport.enums'

export interface AirportResponse {
  id: string
  code: string
  name: string
  city: string
  country: string
  status: AirportStatus
}
export interface AirportRequest { code: string; name: string; city: string; country: string }
/** Java permits null/omitted fields for PATCH; supplied code must match the stored code. */
export interface UpdateAirportRequest {
  code?: string | null
  name?: string | null
  city?: string | null
  country?: string | null
}
export interface ReplaceAirportRequest { code?: string | null; name: string; city: string; country: string }
export interface AirportStatusRequest { status: AirportStatus }
export interface AirportListParams { keyword?: string; status?: AirportStatus }
