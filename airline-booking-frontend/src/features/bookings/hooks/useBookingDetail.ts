import { useQuery } from '@tanstack/react-query'
import { bookingApi } from '../api/booking.api'

export function useBookingDetail(
    bookingId: string,
    pollWhilePending = false
) {
    return useQuery({
        queryKey: ['bookings', 'detail', bookingId],
        queryFn: () => bookingApi.get(bookingId),
        enabled: Boolean(bookingId),
        retry: false,
        refetchInterval: (query) => {
            if (
                pollWhilePending &&
                query.state.data?.status === 'PENDING'
            ) {
                return 2000
            }

            return false
        },
    })
}
