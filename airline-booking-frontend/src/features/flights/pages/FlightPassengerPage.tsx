import { useLocation, Navigate, useParams } from 'react-router'
import { useState } from 'react'
import { ROUTES } from '../../../app/router/paths'
import { useCreateBooking } from '../../bookings/hooks/useCreateBooking'
import type { CreateBookingRequest } from '../../bookings/types/booking.dto'
import { GENDERS } from '../../bookings/types/booking.enums'
import type { Gender } from '../../bookings/types/booking.enums'
import './FlightPassengerPage.css'
interface Passenger {
    seatId: string
    fullName: string
    dateOfBirth: string
    gender: Gender | ''
    documentNumber: string
}

interface NavigationState {
    scheduleId: string
    seatIds: string[]
}

export function FlightPassengerPage() {
    const location = useLocation()
    const { scheduleId } = useParams()

    const state = location.state as NavigationState | null

    // Kiểm tra dữ liệu được truyền từ trang chọn ghế
    if (
        !state ||
        state.scheduleId !== scheduleId ||
        !Array.isArray(state.seatIds) ||
        state.seatIds.length === 0
    ) {
        return <Navigate to={ROUTES.FLIGHTS} replace />
    }

    return (
        <PassengerForm
            scheduleId={state.scheduleId}
            seatIds={state.seatIds}
        />
    )
}

function PassengerForm({ scheduleId, seatIds }: { scheduleId: string, seatIds: string[] }) {
    const createBooking = useCreateBooking()

    const [passengers, setPassengers] = useState<Passenger[]>(
        () =>
            seatIds.map(seatId => ({
                seatId,
                fullName: '',
                dateOfBirth: '',
                gender: '',
                documentNumber: '',
            }))
    )

    const handleChange = (
        index: number,
        field: keyof Omit<Passenger, 'seatId'>,
        value: string
    ) => {
        setPassengers(current =>
            current.map((passenger, i) =>
                i === index
                    ? { ...passenger, [field]: value }
                    : passenger
            )
        )
    }

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()

        const request: CreateBookingRequest = {
            flightScheduleId: scheduleId,
            passengers: passengers.map(passenger => ({
                fullName: passenger.fullName,
                dateOfBirth: passenger.dateOfBirth,
                gender: passenger.gender as Gender,
                documentNumber: passenger.documentNumber,
                flightSeatId: passenger.seatId,
            })),
        }

        createBooking.mutate(request, {
            onSuccess: (data) => {
                console.log('Booking created:', data)
            },
            onError: (error) => {
                console.error('Create booking failed:', error)
            },
        })
    }


    return (
        <main className="passenger-page">
            <header className="passenger-page__header">
                <h1>Thông tin hành khách</h1>
                <p>
                    Vui lòng nhập chính xác thông tin của tất cả hành khách.
                    Số lượng hành khách: {passengers.length}
                </p>
            </header>

            <form className="passenger-form" onSubmit={handleSubmit}>
                {passengers.map((passenger, index) => (
                    <fieldset className="passenger-card" key={passenger.seatId}>
                        <legend>Hành khách {index + 1}</legend>

                        <p className="passenger-card__seat">
                            Mã ghế: {passenger.seatId}
                        </p>

                        <div className="passenger-fields">
                            <div className="passenger-field">
                                <label htmlFor={`fullName-${index}`}>
                                    Họ và tên
                                </label>
                                <input
                                    id={`fullName-${index}`}
                                    type="text"
                                    value={passenger.fullName}
                                    onChange={e =>
                                        handleChange(index, 'fullName', e.target.value)
                                    }
                                    placeholder="Nhập họ và tên"
                                    required
                                />
                            </div>

                            <div className="passenger-field">
                                <label htmlFor={`dateOfBirth-${index}`}>
                                    Ngày sinh
                                </label>
                                <input
                                    id={`dateOfBirth-${index}`}
                                    type="date"
                                    value={passenger.dateOfBirth}
                                    onChange={e =>
                                        handleChange(index, 'dateOfBirth', e.target.value)
                                    }
                                    required
                                />
                            </div>

                            <div className="passenger-field">
                                <label htmlFor={`gender-${index}`}>
                                    Giới tính
                                </label>
                                <select
                                    id={`gender-${index}`}
                                    value={passenger.gender}
                                    onChange={e =>
                                        handleChange(index, 'gender', e.target.value)
                                    }
                                    required
                                >
                                    <option value="">-- Chọn giới tính --</option>
                                    {GENDERS.map(gender => (
                                        <option key={gender} value={gender}>
                                            {gender}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="passenger-field">
                                <label htmlFor={`documentNumber-${index}`}>
                                    Số giấy tờ tùy thân
                                </label>
                                <input
                                    id={`documentNumber-${index}`}
                                    type="text"
                                    value={passenger.documentNumber}
                                    onChange={e =>
                                        handleChange(index, 'documentNumber', e.target.value)
                                    }
                                    placeholder="Nhập số giấy tờ"
                                    required
                                />
                            </div>
                        </div>
                    </fieldset>
                ))}

                {createBooking.isError && (
                    <p className="passenger-error" role="alert">
                        Không thể tạo booking. Vui lòng thử lại.
                    </p>
                )}

                <div className="passenger-form__actions">
                    <button
                        className="passenger-submit"
                        type="submit"
                        disabled={createBooking.isPending}
                    >
                        {createBooking.isPending
                            ? 'Đang tạo booking...'
                            : 'Tiếp tục'}
                    </button>
                </div>
            </form>
        </main>
    )
}
