import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { FlightSearchParams, SeatMapResponse } from '../types/flight-search.dto'

/** Design-only. Search list envelope and detail DTO are not specified. */
export const plannedFlightSearchApi = {
  search: async (params: FlightSearchParams) =>
    (await axiosClient.get<unknown>(endpoints.search.root, { params })).data,
  get: async (scheduleId: string) => (await axiosClient.get<unknown>(endpoints.search.detail(scheduleId))).data,
  seats: async (scheduleId: string) =>
    (await axiosClient.get<SeatMapResponse>(endpoints.search.seats(scheduleId))).data,
}
