import { useQuery } from '@tanstack/react-query'
import { airportApi } from '../api/airport.api'
import type { AirportListParams } from '../types/airport.dto'
import { airportQueryKeys } from '../constants/airportQueryKeys'

export function useAirports(params: AirportListParams = {}) {
  return useQuery({ queryKey: airportQueryKeys.list(params), queryFn: () => airportApi.list(params), staleTime: 5 * 60 * 1000 })
}
