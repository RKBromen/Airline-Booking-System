import { Table, Tag } from 'antd'
import type { TableColumnsType } from 'antd'
import { Link } from 'react-router'
import type { AirportResponse } from '../types/airport.dto'
import { routeTo } from '../../../app/router/paths'
import { AIRPORT_PAGE_SIZE, AIRPORT_STATUS_COLORS, AIRPORT_STATUS_LABELS } from '../constants/airport.constants'
import { EmptyState } from '../../../components/ui/EmptyState'

const columns: TableColumnsType<AirportResponse> = [
  { title: 'Mã sân bay', dataIndex: 'code', width: 120,
    render: (code: string, airport) => <Link className="airport-code" to={routeTo.airportDetail(airport.id)}>{code}</Link> },
  { title: 'Tên sân bay', dataIndex: 'name', width: 320,
    render: (name: string, airport) => <Link className="airport-name" to={routeTo.airportDetail(airport.id)}>{name}</Link> },
  { title: 'Thành phố', dataIndex: 'city', width: 170 },
  { title: 'Quốc gia', dataIndex: 'country', width: 150 },
  { title: 'Trạng thái', dataIndex: 'status', width: 165,
    render: (status: AirportResponse['status']) => <Tag color={AIRPORT_STATUS_COLORS[status]}>{AIRPORT_STATUS_LABELS[status] ?? status}</Tag> },
  { title: 'Chi tiết', key: 'detail', width: 110,
    render: (_, airport) => <Link aria-label={`Xem chi tiết sân bay ${airport.code}`} to={routeTo.airportDetail(airport.id)}>Xem chi tiết <span aria-hidden="true">↗</span></Link> },
]

interface AirportTableProps {
  airports: AirportResponse[]
  loading: boolean
  page: number
  onPageChange: (page: number) => void
  hasSearch: boolean
}

export function AirportTable({ airports, loading, page, onPageChange, hasSearch }: AirportTableProps) {
  return <Table<AirportResponse> rowKey="id" columns={columns} dataSource={airports} loading={loading}
    scroll={{ x: 1035 }} locale={{ emptyText: <EmptyState description={hasSearch
      ? 'Không tìm thấy sân bay phù hợp. Hãy thử từ khóa khác.' : 'Chưa có sân bay trong hệ thống.'} /> }}
    pagination={{ current: page, pageSize: AIRPORT_PAGE_SIZE, total: airports.length,
      showSizeChanger: false, hideOnSinglePage: false, responsive: true,
      onChange: onPageChange, showTotal: total => `${total} sân bay` }} />
}
