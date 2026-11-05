import { useMutation, useQueryClient } from '@tanstack/react-query'
import { plannedBookingApi } from '../api/booking.api'
import type { CreateBookingRequest } from '../types/booking.dto'
import { createBookingSchema } from '../schemas/booking.schema'
import { queryKeys } from '../../../shared/constants/queryKeys'

/** Example for when backend module 8 is implemented; not wired to a page. */
export function usePlannedCreateBooking() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateBookingRequest) => plannedBookingApi.create(createBookingSchema.parse(request)),
    retry: false,
    onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.bookings.all }),
  })
}
