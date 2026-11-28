import { useMutation, useQueryClient } from '@tanstack/react-query'
import { paymentApi } from '../api/payment.api'
import type { CreatePaymentRequest } from '../types/payment.dto'
import { createPaymentSchema } from '../schemas/payment.schema'
import { queryKeys } from '../../../shared/constants/queryKeys'

/** Example only, pending system module 9. Redirect does not imply payment success. */
export function useCreatePayment(bookingId: string) {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (request: CreatePaymentRequest) => paymentApi.create(bookingId, createPaymentSchema.parse(request)),
    retry: false,
    onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.payments.history(bookingId) }),
  })
}
