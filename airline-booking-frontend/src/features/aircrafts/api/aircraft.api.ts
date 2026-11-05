import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { AircraftRequest, AircraftResponse, AircraftSeatListItem, AircraftSeatRequest,
  AircraftSeatResponse, AircraftStatusRequest } from '../types/aircraft.dto'

const urls = endpoints.aircrafts
export const aircraftApi = {
  list: async () => (await axiosClient.get<AircraftResponse[]>(urls.root)).data,
  get: async (id: string) => (await axiosClient.get<AircraftResponse>(urls.detail(id))).data,
  create: async (request: AircraftRequest) => (await axiosClient.post<AircraftResponse>(urls.root, request)).data,
  update: async (id: string, request: AircraftRequest) =>
    (await axiosClient.put<AircraftResponse>(urls.detail(id), request)).data,
  updateStatus: async (id: string, request: AircraftStatusRequest) =>
    (await axiosClient.patch<AircraftResponse>(urls.status(id), request)).data,
  seats: async (id: string) => (await axiosClient.get<AircraftSeatListItem[]>(urls.seats(id))).data,
  addSeat: async (id: string, request: AircraftSeatRequest) =>
    (await axiosClient.post<AircraftSeatResponse>(urls.seats(id), request)).data,
}
