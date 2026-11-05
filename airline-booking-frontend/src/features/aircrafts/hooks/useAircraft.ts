import { useQuery } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import { aircraftQueryKeys } from '../constants/aircraftQueryKeys'
import { isAircraftId } from '../utils/aircraftParams'

export function useAircraft(id?: string) {
  return useQuery({ queryKey: aircraftQueryKeys.detail(id ?? ''), queryFn: () => {
    if (!isAircraftId(id)) throw new Error('ID máy bay không hợp lệ.')
    return aircraftApi.get(id)
  }, enabled: isAircraftId(id) })
}
