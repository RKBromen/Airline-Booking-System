import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Alert, Breadcrumb, Button, Card, Result, Tag } from 'antd'
import { Link, useNavigate, useParams } from 'react-router'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { ApiRequestError } from '../../../shared/api/apiError'
import { flightSearchApi } from '../../flight-search/api/flight-search.api'
import type { FlightSeatStatus } from '../../flight-schedules/types/flight-schedule.enums'
import { routeTo, ROUTES } from '../../../app/router/paths'
import { queryKeys } from '../../../shared/constants/queryKeys'

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function seatStatusLabel(status: FlightSeatStatus) {
  if (status === 'AVAILABLE') return 'Còn trống'
  if (status === 'HELD') return 'Đang được giữ'
  return 'Đã đặt'
}

export function FlightSeatSelectionPage() {
  const { scheduleId } = useParams()
  const navigate = useNavigate()
  const [selection, setSelection] = useState<{ scheduleId: string; seatIds: string[] } | null>(null)
  const selectedSeatIds = selection && selection.scheduleId === scheduleId ? selection.seatIds : []
  const query = useQuery({
    queryKey: queryKeys.search.seats(scheduleId ?? ''),
    queryFn: () => flightSearchApi.seats(scheduleId ?? ''),
    enabled: Boolean(scheduleId),
  })
  const selectedSeats = (query.data?.seats ?? [])
    .filter(seat => seat.status === 'AVAILABLE' && selectedSeatIds.includes(seat.id))

  const toggleSeat = (seatId: string) => {
    if (!scheduleId) return
    setSelection(current => {
      const currentIds = current?.scheduleId === scheduleId ? current.seatIds : []
      const seatIds = currentIds.includes(seatId)
        ? currentIds.filter(id => id !== seatId)
        : [...currentIds, seatId]
      return { scheduleId, seatIds }
    })
  }

  if (!scheduleId) return <Result status="404" title="Không tìm thấy lịch bay" />
  if (query.isError && query.error instanceof ApiRequestError && query.error.status === 404) {
    return <Result status="404" title="Không tìm thấy lịch bay"
      subTitle="Lịch bay có thể đã bị xóa hoặc đường dẫn không hợp lệ."
      extra={<Link to={ROUTES.FLIGHTS}><Button>Quay lại tìm chuyến bay</Button></Link>} />
  }
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  if (query.isPending) return <Card loading />

  return <>
    <Breadcrumb items={[
      { title: <Link to={ROUTES.FLIGHTS}>Tìm chuyến bay</Link> },
      { title: <Link to={routeTo.flightDetail(scheduleId)}>Chi tiết</Link> },
      { title: 'Chọn ghế' },
    ]} />
    <section className="page-intro">
      <p className="eyebrow">Sơ đồ ghế</p>
      <h1>Chọn ghế</h1>
      <p className="muted">Chọn một hoặc nhiều ghế còn trống. Ghế đã đặt hoặc đang được giữ không thể chọn.</p>
    </section>
    <Card>
      {query.data.seats.length === 0
        ? <Result status="info" title="Chưa có ghế" subTitle="Lịch bay này chưa có dữ liệu ghế." />
        : <>
          <div className="seat-status-grid">
            {query.data.seats.map(seat => {
              const available = seat.status === 'AVAILABLE'
              const selected = selectedSeatIds.includes(seat.id) && available
              return <button key={seat.id} type="button"
                className={`flight-seat ${available ? 'available' : 'unavailable'}${selected ? ' selected' : ''}`}
                disabled={!available} aria-pressed={selected}
                aria-label={`${seat.seatNumber}, ${seat.seatClass}, ${seatStatusLabel(seat.status)}, ${formatCurrency(seat.price)}`}
                onClick={() => toggleSeat(seat.id)}>
                <strong>{seat.seatNumber}</strong>
                <Tag>{seat.seatClass}</Tag>
                <span>{selected ? 'Đã chọn' : seatStatusLabel(seat.status)}</span>
                <span>{formatCurrency(seat.price)}</span>
              </button>
            })}
          </div>
          <section className="seat-selection-summary" aria-live="polite">
            <strong>{selectedSeats.length ? `Đã chọn ${selectedSeats.length} ghế` : 'Chưa chọn ghế'}</strong>
            {selectedSeats.length > 0 && <>
              <span>Ghế: {selectedSeats.map(seat => seat.seatNumber).join(', ')}</span>
              <span>Tổng tiền: {formatCurrency(selectedSeats.reduce((total, seat) => total + seat.price, 0))}</span>
              <Button onClick={() => setSelection({ scheduleId, seatIds: [] })}>Bỏ chọn tất cả</Button>
              <Button
                type="primary"
                onClick={() => {
                  const target = routeTo.flightPassengers(scheduleId)

                  console.log('scheduleId:', scheduleId)
                  console.log('seatIds:', selectedSeatIds)
                  console.log('target URL:', target)

                  navigate(target, {
                    state: {
                      scheduleId,
                      seatIds: selectedSeatIds,
                    },
                  })
                }}
              >
                Tiếp tục
              </Button>
            </>}
          </section>
          <Alert type="info" showIcon
            title="Lựa chọn ghế chỉ được lưu tạm trên trang; ghế được giữ khi tạo booking." />
        </>}
    </Card>
  </>
}
