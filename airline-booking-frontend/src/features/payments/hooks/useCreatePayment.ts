import { useMutation, useQueryClient } from '@tanstack/react-query'
import { plannedPaymentApi } from '../api/payment.api'
import type { CreatePaymentRequest } from '../types/payment.dto'
import { createPaymentSchema } from '../schemas/payment.schema'
import { queryKeys } from '../../../shared/constants/queryKeys'

/** Example only, pending backend module 9. Redirect does not imply payment success. */
export function usePlannedCreatePayment(bookingId: string) {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (request: CreatePaymentRequest) => plannedPaymentApi.create(bookingId, createPaymentSchema.parse(request)),
    retry: false,
    onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.payments.history(bookingId) }),
  })
}
