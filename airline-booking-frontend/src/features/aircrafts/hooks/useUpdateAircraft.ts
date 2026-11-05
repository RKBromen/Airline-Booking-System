import { useMutation, useQueryClient } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import { aircraftSchema } from '../schemas/aircraft.schema'
import type { AircraftRequest } from '../types/aircraft.dto'
import { aircraftQueryKeys } from '../constants/aircraftQueryKeys'

export function useUpdateAircraft(id: string) {
  const client = useQueryClient()
  return useMutation({ mutationFn: (request: AircraftRequest) => aircraftApi.update(id, aircraftSchema.parse(request)),
    onSuccess: async aircraft => {
      client.setQueryData(aircraftQueryKeys.detail(id), aircraft)
      await Promise.all([
        client.invalidateQueries({ queryKey: aircraftQueryKeys.list }),
        client.invalidateQueries({ queryKey: aircraftQueryKeys.detail(id) }),
      ])
    } })
}
