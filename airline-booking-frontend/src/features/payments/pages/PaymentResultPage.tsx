
import { Link, useSearchParams } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { useBookingDetail } from '../../bookings/hooks/useBookingDetail'
import { paymentApi } from '../api/payment.api'

export function PaymentResultPage() {
    const [searchParams] = useSearchParams()

    const bookingId = searchParams.get('bookingId') ?? ''

    const {
        data: booking,
        isPending: bookingLoading,
        isError: bookingError,
    } = useBookingDetail(bookingId, true)

    const {
        data: payments,
        isPending: paymentsLoading,
        isError: paymentsError,
    } = useQuery({
        queryKey: ['payments', 'history', bookingId],
        queryFn: () => paymentApi.history(bookingId),
        enabled: Boolean(bookingId),
        retry: false,
        refetchInterval: (query) => {
            const history = query.state.data

            if (
                history?.some((payment) => payment.status === 'PENDING')
            ) {
                return 2000
            }

            return false
        },
    })

    if (!bookingId) {
        return (
            <main>
                <h1>Không tìm thấy thông tin thanh toán</h1>
                <Link to="/my-bookings">Xem booking của tôi</Link>
            </main>
        )
    }

    if (bookingLoading || paymentsLoading) {
        return (
            <main>
                <h1>Đang kiểm tra thanh toán...</h1>
                <p>Vui lòng chờ trong giây lát.</p>
            </main>
        )
    }

    if (bookingError || paymentsError || !booking || !payments) {
        return (
            <main>
                <h1>Không thể tải kết quả thanh toán</h1>
                <p>Vui lòng thử lại sau.</p>
                <Link to="/my-bookings">Xem booking của tôi</Link>
            </main>
        )
    }

    const latestPayment = [...payments].sort(
        (a, b) =>
            new Date(b.createdAt).getTime() -
            new Date(a.createdAt).getTime()
    )[0]

    const formatMoney = (amount: number) =>
        `${amount.toLocaleString('vi-VN')} VND`

    const formatDate = (date: string | null) =>
        date
            ? new Date(`${date}Z`).toLocaleString('vi-VN')
            : 'Chưa có'

    if (!latestPayment) {
        return (
            <main>
                <h1>Chưa tìm thấy giao dịch thanh toán</h1>
                <p>Mã booking: {booking.bookingCode}</p>
                <Link to={`/bookings/${booking.id}`}>
                    Xem chi tiết booking
                </Link>
            </main>
        )
    }

    const isSuccess =
        latestPayment.status === 'SUCCESS' &&
        booking.status === 'CONFIRMED'

    const isPending =
        latestPayment.status === 'PENDING' ||
        booking.status === 'PENDING'

    return (
        <main className="payment-result-page">
            {isSuccess ? (
                <>
                    <h1>Thanh toán thành công!</h1>
                    <p>Booking của bạn đã được xác nhận.</p>
                </>
            ) : isPending ? (
                <>
                    <h1>Đang xác nhận thanh toán</h1>
                    <p>
                        Giao dịch đang được xử lý. Trang sẽ tự động cập nhật
                        khi nhận được kết quả.
                    </p>
                </>
            ) : (
                <>
                    <h1>Thanh toán không thành công</h1>
                    <p>Giao dịch chưa được xác nhận thành công.</p>
                </>
            )}

            <section>
                <h2>Thông tin giao dịch</h2>

                <p>Mã booking: {booking.bookingCode}</p>
                <p>Mã giao dịch: {latestPayment.id}</p>
                <p>Phương thức: {latestPayment.paymentMethod}</p>
                <p>Số tiền: {formatMoney(latestPayment.amount)}</p>
                <p>Trạng thái giao dịch: {latestPayment.status}</p>
                <p>Trạng thái booking: {booking.status}</p>
                <p>Thời gian tạo: {formatDate(latestPayment.createdAt)}</p>
                <p>Thời gian thanh toán: {formatDate(latestPayment.paidAt)}</p>
            </section>

            <p>
                <Link to={`/bookings/${booking.id}`}>
                    Xem chi tiết booking
                </Link>
            </p>

            <p>
                <Link to="/my-bookings">
                    Danh sách booking của tôi
                </Link>
            </p>
        </main>
    )
}
