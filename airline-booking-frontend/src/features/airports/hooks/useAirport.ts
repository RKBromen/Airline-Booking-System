import { useQuery } from '@tanstack/react-query'
import { airportApi } from '../api/airport.api'
import { airportQueryKeys } from '../constants/airportQueryKeys'
import { isAirportId } from '../utils/airportParams'

export function useAirport(id?: string) {
  return useQuery({
    queryKey: airportQueryKeys.detail(id ?? ''),
    queryFn: () => {
      if (!isAirportId(id)) throw new Error('Mã định danh sân bay không hợp lệ.')
      return airportApi.get(id)
    },
    enabled: isAirportId(id),
    staleTime: 5 * 60 * 1000,
  })
}
