import { useQuery } from '@tanstack/react-query'
import { Breadcrumb, Button, Card, Descriptions, Result, Space } from 'antd'
import { Link, useParams } from 'react-router'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { flightSearchApi } from '../../flight-search/api/flight-search.api'
import { routeTo, ROUTES } from '../../../app/router/paths'
import { queryKeys } from '../../../shared/constants/queryKeys'

function formatDateTime(value: string) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'medium', timeStyle: 'short',
  }).format(date)
}

export function FlightDetailPage() {
  const { scheduleId } = useParams()
  const query = useQuery({
    queryKey: queryKeys.search.detail(scheduleId ?? ''),
    queryFn: () => flightSearchApi.get(scheduleId ?? ''),
    enabled: Boolean(scheduleId),
  })

  if (!scheduleId) return <Result status="404" title="Không tìm thấy chuyến bay" />
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  if (query.isPending) return <Card loading className="flight-detail-card" />

  const flight = query.data
  return <>
    <Breadcrumb items={[
      { title: <Link to={ROUTES.FLIGHTS}>Tìm chuyến bay</Link> },
      { title: flight.flightNumber },
    ]} />
    <section className="page-intro">
      <p className="eyebrow">Chi tiết chuyến bay</p>
      <h1>{flight.flightNumber}</h1>
    </section>
    <Card className="flight-detail-card">
      <div className="flight-detail-route">
        <div><strong>{flight.departure_city}</strong><p className="muted">{flight.departure_airport}</p></div>
        <div className="flight-detail-duration">{flight.duration}</div>
        <div><strong>{flight.arrival_city}</strong><p className="muted">{flight.arrival_airport}</p></div>
      </div>
      <Descriptions bordered column={1} items={[
        { key: 'departure', label: 'Khởi hành', children: formatDateTime(flight.departure_time) },
        { key: 'arrival', label: 'Đến', children: formatDateTime(flight.arrival_time) },
        { key: 'aircraft', label: 'Máy bay', children: flight.aircraft_number },
      ]} />
      <Space style={{ marginTop: 24 }} wrap>
        <Link to={routeTo.flightSeats(flight.id)}><Button type="primary">Chọn ghế</Button></Link>
        <Link to={ROUTES.FLIGHTS}><Button>Quay lại kết quả</Button></Link>
      </Space>
    </Card>
  </>
}
