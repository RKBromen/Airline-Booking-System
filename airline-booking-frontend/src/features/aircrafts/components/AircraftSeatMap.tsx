import { Card, Table, Tag } from 'antd'
import type { AircraftSeatListItem } from '../types/aircraft.dto'
import { EmptyState } from '../../../components/ui/EmptyState'

const labels = { ECONOMY: 'Phổ thông', BUSINESS: 'Thương gia', FIRST: 'Hạng nhất' }
export function AircraftSeatMap({ seats }: { seats: AircraftSeatListItem[] }) {
  return <Card title={`Danh sách ghế vật lý (${seats.length})`}>
    <p className="muted seat-note">Ghế được liệt kê theo cấu hình máy bay. Dữ liệu hiện chưa cung cấp vị trí hàng, cột hoặc lối đi.</p>
    <Table<AircraftSeatListItem> rowKey="id" dataSource={seats} scroll={{ x: 320 }}
      locale={{ emptyText: <EmptyState description="Máy bay chưa có ghế vật lý." /> }}
      columns={[
        { title: 'Số ghế', dataIndex: 'seatNumber' },
        { title: 'Hạng ghế', dataIndex: 'seatClass', render: (value: AircraftSeatListItem['seatClass']) => <Tag>{labels[value] ?? value}</Tag> },
      ]} pagination={{ pageSize: 20, showSizeChanger: false, responsive: true }} />
  </Card>
}
