import { useMutation, useQueryClient } from '@tanstack/react-query'
import { airportApi } from '../api/airport.api'
import { airportCreateSchema } from '../schemas/airport.schema'
import type { AirportRequest } from '../types/airport.dto'
import { queryKeys } from '../../../shared/constants/queryKeys'

export function useCreateAirport() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (request: AirportRequest) => airportApi.create(airportCreateSchema.parse(request)),
    onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.airports.all }),
  })
}
