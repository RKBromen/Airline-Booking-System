
import { Link, useParams } from 'react-router'
import { useBookingDetail } from '../hooks/useBookingDetail'
import { routeTo } from '../../../app/router/paths'

export function BookingDetailPage() {
    const { bookingId } = useParams<{ bookingId: string }>()

    const {
        data: booking,
        isPending,
        isError,
        error,
    } = useBookingDetail(bookingId ?? '')

    if (isPending) {
        return <p>Đang tải thông tin booking...</p>
    }

    if (isError || !booking) {
        console.error('Get booking detail failed:', error)

        return (
            <main>
                <h1>Không thể tải thông tin booking</h1>
                <p>Booking không tồn tại hoặc bạn không có quyền truy cập.</p>
                <Link to="/">Quay lại trang chủ</Link>
            </main>
        )
    }

    return (
        <main>
            <h1>Chi tiết booking</h1>

            <section>
                <h2>Thông tin booking</h2>
                <p>Mã booking: {booking.bookingCode}</p>
                <p>Trạng thái: {booking.status}</p>
                <p>
                    Tổng tiền: {booking.totalAmount.toLocaleString('vi-VN')} VND
                </p>
                <p>Hạn thanh toán: {booking.expiresAt}</p>
            </section>

            <section>
                <h2>Thông tin chuyến bay</h2>
                <p>Số hiệu chuyến bay: {booking.flight.flight_number}</p>
                <p>Sân bay đi: {booking.flight.departureAirport}</p>
                <p>Sân bay đến: {booking.flight.arrivalAirport}</p>
                <p>Thời gian khởi hành: {booking.flight.departureTime}</p>
            </section>

            <section>
                <h2>Hành khách</h2>

                {booking.passengers.map((passenger, index) => (
                    <div key={passenger.id}>
                        <h3>Hành khách {index + 1}</h3>
                        <p>Họ tên: {passenger.fullName}</p>
                        <p>Số ghế: {passenger.seatNumber}</p>
                    </div>
                ))}
            </section>

            {booking.status === 'PENDING' && (
                <Link to={routeTo.bookingCheckout(booking.id)}>
                    Tiến hành thanh toán
                </Link>
            )}

            <div>
                <Link to={routeTo.bookingPayments(booking.id)}>
                    Xem lịch sử thanh toán
                </Link>
            </div>
        </main>
    )
}
