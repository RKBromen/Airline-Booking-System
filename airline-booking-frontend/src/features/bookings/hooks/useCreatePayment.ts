import { useMutation } from '@tanstack/react-query'
import { paymentApi } from '../../payments/api/payment.api'
import type { CreatePaymentRequest } from '../../payments/types/payment.dto'

export function useCreatePayment(bookingId: string) {
    return useMutation({
        mutationFn: (request: CreatePaymentRequest) =>
            paymentApi.create(bookingId, request),
        retry: false,
    })
}
