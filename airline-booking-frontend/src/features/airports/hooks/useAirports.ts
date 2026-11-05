import { useQuery } from '@tanstack/react-query'
import { airportApi } from '../api/airport.api'
import type { AirportListParams } from '../types/airport.dto'
import { queryKeys } from '../../../shared/constants/queryKeys'

export function useAirports(params: AirportListParams = {}) {
  return useQuery({ queryKey: queryKeys.airports.list(params), queryFn: () => airportApi.list(params) })
}
