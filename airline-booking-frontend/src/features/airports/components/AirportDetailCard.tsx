import { Card, Descriptions, Tag } from 'antd'
import type { AirportResponse } from '../types/airport.dto'
import { AIRPORT_STATUS_COLORS, AIRPORT_STATUS_LABELS } from '../constants/airport.constants'

export function AirportDetailCard({ airport }: { airport: AirportResponse }) {
  return <Card className="airport-detail-card">
    <div className="airport-detail-heading">
      <div className="airport-monogram" aria-label={`Mã sân bay ${airport.code}`}>{airport.code}</div>
      <div><p className="eyebrow">Thông tin sân bay</p><h1>{airport.name}</h1>
        <p className="muted">{airport.city}, {airport.country}</p></div>
    </div>
    <Descriptions title="Thông tin chi tiết" bordered layout="vertical" column={{ xs: 1, sm: 2, md: 2 }} items={[
      { key: 'code', label: 'Mã sân bay', children: airport.code },
      { key: 'name', label: 'Tên sân bay', children: airport.name },
      { key: 'city', label: 'Thành phố', children: airport.city },
      { key: 'country', label: 'Quốc gia', children: airport.country },
      { key: 'status', label: 'Trạng thái', children:
        <Tag color={AIRPORT_STATUS_COLORS[airport.status]}>{AIRPORT_STATUS_LABELS[airport.status] ?? airport.status}</Tag> },
    ]} />
  </Card>
}
