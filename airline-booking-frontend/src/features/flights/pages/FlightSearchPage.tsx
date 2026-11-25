import { useEffect, useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Alert, Button, Card, Empty, Form, Input, List, Select, Space, Tag } from 'antd'
import { useSearchParams } from 'react-router'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { airportApi } from '../../airports/api/airport.api'
import type { AirportResponse } from '../../airports/types/airport.dto'
import { flightSearchApi } from '../../flight-search/api/flight-search.api'
import { flightSearchSchema } from '../../flight-search/schemas/flight-search.schema'
import type { FlightSearchParams } from '../../flight-search/types/flight-search.dto'
import { queryKeys } from '../../../shared/constants/queryKeys'

function todayLocal() {
  const now = new Date()
  return [now.getFullYear(), String(now.getMonth() + 1).padStart(2, '0'), String(now.getDate()).padStart(2, '0')].join('-')
}

function airportLabel(airport: AirportResponse) {
  return `${airport.code} · ${airport.city} — ${airport.name}`
}

function formatDateTime(value: string) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'medium', timeStyle: 'short',
  }).format(date)
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

export function FlightSearchPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [validationMessage, setValidationMessage] = useState<string | null>(null)
  const [form] = Form.useForm<FlightSearchParams>()
  const rawParams: FlightSearchParams = {
    departure: searchParams.get('departure') ?? '',
    arrival: searchParams.get('arrival') ?? '',
    date: searchParams.get('date') ?? '',
  }
  const parsed = flightSearchSchema.safeParse(rawParams)
  const searching = parsed.success && Object.values(parsed.data).some(Boolean)
  const searchRequest = parsed.success ? parsed.data : {}
  useEffect(() => {
    form.setFieldsValue(rawParams)
  }, [form, rawParams.departure, rawParams.arrival, rawParams.date])
  const airports = useQuery({
    queryKey: queryKeys.airports.list({}), queryFn: () => airportApi.list(), staleTime: 5 * 60_000,
  })
  const results = useQuery({
    queryKey: queryKeys.search.list(searchRequest),
    queryFn: () => flightSearchApi.search(searchRequest), enabled: parsed.success, retry: false,
  })
  const airportOptions = useMemo(() => (airports.data ?? []).filter(item => item.status === 'ACTIVE')
    .map(item => ({ value: item.code, label: airportLabel(item) })), [airports.data])
  const selectedDeparture = (airports.data ?? []).find(item => item.code === rawParams.departure)
  const selectedArrival = (airports.data ?? []).find(item => item.code === rawParams.arrival)

  const submit = (values: FlightSearchParams) => {
    const value = flightSearchSchema.safeParse(values)
    if (!value.success) {
      setValidationMessage(value.error.issues[0]?.message ?? 'Vui lòng kiểm tra thông tin tìm kiếm.')
      return
    }
    setValidationMessage(null)
    setSearchParams(value.data)
  }

  return <>
    <section className="page-intro flight-home-intro">
      <p className="eyebrow">Airline Booking</p>
      <h1>Tìm chuyến bay phù hợp</h1>
      <p className="muted">Xem tất cả chuyến bay hoặc chọn bộ lọc để tìm hành trình phù hợp.</p>
    </section>
    <Card className="flight-search-card">
      <Form form={form} layout="vertical" onFinish={submit}>
        <div className="flight-search-fields">
          <Form.Item label="Điểm đi" name="departure">
            <Select allowClear showSearch optionFilterProp="label" placeholder="Chọn sân bay đi" options={airportOptions} loading={airports.isPending} />
          </Form.Item>
          <Form.Item label="Điểm đến" name="arrival">
            <Select allowClear showSearch optionFilterProp="label" placeholder="Chọn sân bay đến" options={airportOptions} loading={airports.isPending} />
          </Form.Item>
          <Form.Item label="Ngày khởi hành" name="date">
            <Input type="date" />
          </Form.Item>
          <Button type="primary" htmlType="submit" className="flight-search-submit">Tìm chuyến bay</Button>
        </div>
        <Button onClick={() => {
          form.resetFields()
          setValidationMessage(null)
          setSearchParams({})
        }}>Xóa bộ lọc</Button>
      </Form>
      {(validationMessage || !parsed.success) && <Alert type="warning" showIcon
        title={validationMessage ?? 'Bộ lọc trên đường dẫn không hợp lệ. Vui lòng chọn lại hoặc xóa bộ lọc.'} />}
      {airports.isError && <div className="flight-search-error"><ErrorMessage error={airports.error} onRetry={() => { void airports.refetch() }} retrying={airports.isFetching} /></div>}
    </Card>

    <section className="flight-results-section" aria-live="polite">
      <div className="flight-results-heading"><div><h2>{searching ? 'Kết quả tìm kiếm' : 'Tất cả chuyến bay'}</h2><p className="muted">
        {searching ? [selectedDeparture?.city ?? rawParams.departure, selectedArrival?.city ?? rawParams.arrival, rawParams.date].filter(Boolean).join(' · ') : 'Các chuyến bay đang mở. Bạn có thể lọc theo điểm đi, điểm đến hoặc ngày khởi hành.'}
      </p></div>{results.data && <Tag>{results.data.length} chuyến bay</Tag>}</div>
      {results.isError && <ErrorMessage error={results.error} onRetry={() => { void results.refetch() }} retrying={results.isFetching} />}
      {parsed.success && results.isPending && <Card loading className="flight-result-card" />}
      {results.data && (results.data.length === 0
        ? <Card><Empty description="Không tìm thấy chuyến bay phù hợp với lựa chọn này." /></Card>
        : <List className="flight-result-list" dataSource={results.data} renderItem={flight => <List.Item>
          <Card className="flight-result-card" bordered={false}>
            <div className="flight-result-route">
              <div><span className="flight-time">{formatDateTime(flight.departure_time)}</span>
                <strong>{flight.departure_id}</strong></div>
              <span className="flight-route-line" aria-hidden="true">→</span>
              <div><span className="flight-time">{formatDateTime(flight.arrival_time)}</span>
                <strong>{flight.arrival_id}</strong></div>
              <div className="flight-result-price"><span>{flight.flight_id}</span>
                <strong>{formatCurrency(flight.base_price)}</strong><span>{flight.seatAvailable} ghế còn</span></div>
              <Link className="flight-result-action" to={routeTo.flightDetail(flight.id)}>
                <Button type="primary">Xem chuyến bay</Button>
              </Link>
            </div>
          </Card>
        </List.Item>} />)}
    </section>
  </>
}
