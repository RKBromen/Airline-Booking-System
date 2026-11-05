import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { AirportListParams, AirportRequest, AirportResponse, AirportStatusRequest,
  ReplaceAirportRequest, UpdateAirportRequest } from '../types/airport.dto'

const urls = endpoints.airports
export const airportApi = {
  list: async (params: AirportListParams = {}) =>
    (await axiosClient.get<AirportResponse[]>(urls.root, { params })).data,
  get: async (id: string) => (await axiosClient.get<AirportResponse>(urls.detail(id))).data,
  create: async (request: AirportRequest) =>
    (await axiosClient.post<AirportResponse>(urls.root, { ...request, code: request.code.toUpperCase() })).data,
  replace: async (id: string, request: ReplaceAirportRequest) =>
    (await axiosClient.put<AirportResponse>(urls.detail(id), request)).data,
  update: async (id: string, request: UpdateAirportRequest) =>
    (await axiosClient.patch<AirportResponse>(urls.detail(id), request)).data,
  updateStatus: async (id: string, request: AirportStatusRequest) =>
    (await axiosClient.patch<AirportResponse>(urls.status(id), request)).data,
}
