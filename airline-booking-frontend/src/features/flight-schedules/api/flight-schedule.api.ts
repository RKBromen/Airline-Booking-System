import { axiosClient } from '../../../shared/api/axiosClient'
import { endpoints } from '../../../shared/api/endpoints'
import type { FlightScheduleRequest, FlightScheduleResponse, FlightScheduleStatusRequest,
  FlightScheduleStatusResponse, FlightSeatPriceRequest } from '../types/flight-schedule.dto'

const urls = endpoints.schedules
/** Design-only; update/cancel/list/seat responses are unspecified. */
export const plannedFlightScheduleApi = {
  list: async () => (await axiosClient.get<unknown>(urls.root)).data,
  get: async (id: string) => (await axiosClient.get<unknown>(urls.detail(id))).data,
  create: async (request: FlightScheduleRequest) =>
    (await axiosClient.post<FlightScheduleResponse>(urls.root, request)).data,
  update: async (id: string, request: unknown) =>
    (await axiosClient.put<unknown>(urls.detail(id), request)).data,
  updateStatus: async (id: string, request: FlightScheduleStatusRequest) =>
    (await axiosClient.patch<FlightScheduleStatusResponse>(urls.status(id), request)).data,
  cancel: async (id: string) => (await axiosClient.post<unknown>(urls.cancel(id))).data,
  seats: async (id: string) => (await axiosClient.get<unknown>(urls.seats(id))).data,
  updateSeatPrice: async (id: string, flightSeatId: string, request: FlightSeatPriceRequest) =>
    (await axiosClient.patch<unknown>(urls.price(id, flightSeatId), request)).data,
}
