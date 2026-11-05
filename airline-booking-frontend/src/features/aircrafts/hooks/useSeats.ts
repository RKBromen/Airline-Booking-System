import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import type { AircraftSeatRequest } from '../types/aircraft.dto'
import { aircraftSeatSchema } from '../schemas/seat.schema'
import { queryKeys } from '../../../shared/constants/queryKeys'

export function useSeats(aircraftId: string) {
  return useQuery({ queryKey: queryKeys.aircrafts.seats(aircraftId),
    queryFn: () => aircraftApi.seats(aircraftId), enabled: Boolean(aircraftId) })
}
export function useAddSeat(aircraftId: string) {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (request: AircraftSeatRequest) => aircraftApi.addSeat(aircraftId, aircraftSeatSchema.parse(request)),
    onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.aircrafts.seats(aircraftId) }),
  })
}
