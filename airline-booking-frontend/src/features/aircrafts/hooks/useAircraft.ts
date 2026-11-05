import { useQuery } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import { queryKeys } from '../../../shared/constants/queryKeys'

export function useAircraft(id: string) {
  return useQuery({ queryKey: queryKeys.aircrafts.detail(id), queryFn: () => aircraftApi.get(id), enabled: Boolean(id) })
}
