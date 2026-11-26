// import { useMutation, useQueryClient } from '@tanstack/react-query'
// import { bookingApi } from '../api/booking.api'
// import type { CreateBookingRequest } from '../types/booking.dto'
// import { createBookingSchema } from '../schemas/booking.schema'
// import { queryKeys } from '../../../shared/constants/queryKeys'

// /** Example for when system module 8 is implemented; not wired to a page. */
// export function useCreateBooking() {
//   const client = useQueryClient()
//   return useMutation({
//     mutationFn: (request: CreateBookingRequest) => bookingApi.create(createBookingSchema.parse(request)),
//     retry: false,
//     onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.bookings.all }),
//   })
// }

import { useMutation } from '@tanstack/react-query'
import { bookingApi } from '../api/booking.api'
import type { CreateBookingRequest } from '../types/booking.dto'

export function useCreateBooking() {
  return useMutation({
    mutationFn: (request: CreateBookingRequest) =>
      bookingApi.create(request),
  })
}