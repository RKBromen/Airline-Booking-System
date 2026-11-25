import { useQuery } from '@tanstack/react-query'
import { Breadcrumb, Card, Result, Tag } from 'antd'
import { Link, useParams } from 'react-router'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { flightSearchApi } from '../../flight-search/api/flight-search.api'
import { routeTo, ROUTES } from '../../../app/router/paths'
import { queryKeys } from '../../../shared/constants/queryKeys'

export function FlightSeatSelectionPage() {
  const { scheduleId } = useParams()
  const query = useQuery({
    queryKey: queryKeys.search.seats(scheduleId ?? ''),
    queryFn: () => flightSearchApi.seats(scheduleId ?? ''),
    enabled: Boolean(scheduleId),
  })

  if (!scheduleId) return <Result status="404" title="Không tìm thấy lịch bay" />
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
      <p className="muted">Ghế màu xanh là ghế khả dụng.</p>
    </section>
    <Card>
      {query.data.seats.length === 0
        ? <Result status="info" title="Chưa có ghế" subTitle="Lịch bay này chưa có dữ liệu ghế." />
        : <div className="seat-status-grid">
          {query.data.seats.map(seat => {
            const available = seat.status === 'AVAILABLE'
            return <div key={seat.id} className={`flight-seat ${available ? 'available' : 'unavailable'}`}>
              <strong>{seat.seatNumber}</strong>
              <Tag>{seat.seatClass}</Tag>
            </div>
          })}
        </div>}
    </Card>
  </>
}
