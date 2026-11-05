import { useQuery } from '@tanstack/react-query'
import { aircraftApi } from '../api/aircraft.api'
import { aircraftQueryKeys } from '../constants/aircraftQueryKeys'
import { isAircraftId } from '../utils/aircraftParams'

export function useAircraftSeats(aircraftId?: string) {
  return useQuery({ queryKey: aircraftQueryKeys.seats(aircraftId ?? ''), queryFn: () => {
    if (!isAircraftId(aircraftId)) throw new Error('ID máy bay không hợp lệ.')
    return aircraftApi.seats(aircraftId)
  }, enabled: isAircraftId(aircraftId) })
}
