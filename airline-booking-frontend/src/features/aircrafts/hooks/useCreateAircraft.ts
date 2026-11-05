import { useMutation, useQueryClient } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import { aircraftSchema } from '../schemas/aircraft.schema'
import type { AircraftRequest } from '../types/aircraft.dto'
import { aircraftQueryKeys } from '../constants/aircraftQueryKeys'

export function useCreateAircraft() {
  const client = useQueryClient()
  return useMutation({ mutationFn: (request: AircraftRequest) => aircraftApi.create(aircraftSchema.parse(request)),
    onSuccess: async aircraft => {
      client.setQueryData(aircraftQueryKeys.detail(aircraft.id), aircraft)
      await client.invalidateQueries({ queryKey: aircraftQueryKeys.list })
    } })
}
