
import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { useBookingDetail } from '../hooks/useBookingDetail'
import { useCreatePayment } from '../hooks/useCreatePayment'
import type { CheckoutPaymentMethod } from '../../payments/types/payment.enums'

export function BookingCheckoutPage() {
    const { bookingId } = useParams<{ bookingId: string }>()

    const [paymentMethod, setPaymentMethod] =
        useState<CheckoutPaymentMethod>('VNPAY')

    const {
        data: booking,
        isPending,
        isError,
    } = useBookingDetail(bookingId ?? '')

    const createPayment = useCreatePayment(bookingId ?? '')

    if (isPending) {
        return <p>Đang tải thông tin thanh toán...</p>
    }

    if (isError || !booking) {
        return (
            <main>
                <h1>Không tìm thấy booking</h1>
                <Link to="/">Quay lại trang chủ</Link>
            </main>
        )
    }

    // Backend sử dụng UTC cho expiresAt (LocalDateTime không có timezone).
    const expiresAt = new Date(
        booking.expiresAt.endsWith('Z')
            ? booking.expiresAt
            : `${booking.expiresAt}Z`
    )

    const isExpired = expiresAt.getTime() <= Date.now()

    const canPay =
        booking.status === 'PENDING' && !isExpired

    const handlePayment = () => {
        if (!canPay || createPayment.isPending) return

        createPayment.mutate(
            { paymentMethod },
            {
                onSuccess: (data) => {
                    if (data.paymentUrl) {
                        window.location.assign(data.paymentUrl)
                    }
                },
            }
        )
    }

    if (!canPay) {
        return (
            <main>
                <h1>Không thể thanh toán booking</h1>

                <p>
                    Mã booking: {booking.bookingCode}
                </p>

                <p>
                    Trạng thái: {booking.status}
                </p>

                <p>
                    {isExpired
                        ? 'Booking đã hết hạn thanh toán.'
                        : 'Booking không còn ở trạng thái chờ thanh toán.'}
                </p>

                <Link to={`/bookings/${booking.id}`}>
                    Quay lại chi tiết booking
                </Link>
            </main>
        )
    }

    return (
        <main className="checkout-page">
            <h1>Thanh toán booking</h1>

            <section>
                <h2>Thông tin booking</h2>

                <p>Mã booking: {booking.bookingCode}</p>

                <p>
                    Tổng tiền:{' '}
                    {booking.totalAmount.toLocaleString('vi-VN')} VND
                </p>

                <p>
                    Hạn thanh toán:{' '}
                    {expiresAt.toLocaleString('vi-VN')}
                </p>
            </section>

            <section>
                <h2>Chọn phương thức thanh toán</h2>

                <label>
                    <input
                        type="radio"
                        name="paymentMethod"
                        value="VNPAY"
                        checked={paymentMethod === 'VNPAY'}
                        onChange={() => setPaymentMethod('VNPAY')}
                    />
                    VNPay
                </label>

                <label>
                    <input
                        type="radio"
                        name="paymentMethod"
                        value="MOMO"
                        checked={paymentMethod === 'MOMO'}
                        onChange={() => setPaymentMethod('MOMO')}
                    />
                    MoMo
                </label>
            </section>

            {createPayment.isError && (
                <p role="alert">
                    Không thể khởi tạo thanh toán. Vui lòng thử lại.
                </p>
            )}

            {createPayment.isSuccess && !createPayment.data.paymentUrl && (
                <p role="alert">
                    Backend không trả về đường dẫn thanh toán.
                </p>
            )}

            <button
                type="button"
                onClick={handlePayment}
                disabled={createPayment.isPending}
            >
                {createPayment.isPending
                    ? 'Đang khởi tạo thanh toán...'
                    : 'Thanh toán ngay'}
            </button>

            <p>
                <Link to={`/bookings/${booking.id}`}>
                    Quay lại chi tiết booking
                </Link>
            </p>
        </main>
    )
}
