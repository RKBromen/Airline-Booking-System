import { useQuery } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import { queryKeys } from '../../../shared/constants/queryKeys'

export function useAircrafts() {
  return useQuery({ queryKey: queryKeys.aircrafts.list, queryFn: aircraftApi.list })
}
