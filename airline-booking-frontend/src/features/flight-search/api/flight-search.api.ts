import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { FlightSearchItem, FlightSearchParams, SeatMapResponse } from '../types/flight-search.dto'

export const flightSearchApi = {
  search: async (params: FlightSearchParams) =>
    (await axiosClient.get<FlightSearchItem[]>(endpoints.search.root, { params })).data,
  get: async (scheduleId: string) => (await axiosClient.get<unknown>(endpoints.search.detail(scheduleId))).data,
  seats: async (scheduleId: string) =>
    (await axiosClient.get<SeatMapResponse>(endpoints.search.seats(scheduleId))).data,
}
