import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { FlightListParams, FlightRequest, FlightResponse, FlightStatusRequest } from '../types/flight.dto'

/** Design-only. Unknown means doc 05-1 does not define the full contract. */
export const plannedFlightApi = {
  list: async (params: FlightListParams = {}) =>
    (await axiosClient.get<unknown>(endpoints.flights.root, { params })).data,
  get: async (id: string) => (await axiosClient.get<unknown>(endpoints.flights.detail(id))).data,
  create: async (request: FlightRequest) =>
    (await axiosClient.post<FlightResponse>(endpoints.flights.root, request)).data,
  update: async (id: string, request: unknown) =>
    (await axiosClient.patch<unknown>(endpoints.flights.detail(id), request)).data,
  updateStatus: async (id: string, request: FlightStatusRequest) =>
    (await axiosClient.patch<unknown>(endpoints.flights.status(id), request)).data,
}
