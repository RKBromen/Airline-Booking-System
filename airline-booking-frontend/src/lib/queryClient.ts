import { QueryClient } from '@tanstack/react-query'
import { ApiRequestError } from '../shared/api/apiError'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: (count, error) => count < 1 && (!(error instanceof ApiRequestError) || error.status === 0 || error.status >= 500),
    },
    // Booking/payment mutations must never be retried automatically.
    mutations: { retry: false },
  },
})
