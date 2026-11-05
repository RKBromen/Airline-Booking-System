import { Table } from 'antd'
import type { TableColumnsType } from 'antd'
import { Link } from 'react-router'
import type { AircraftResponse } from '../types/aircraft.dto'
import { routeTo } from '../../../app/router/paths'
import { AircraftStatusTag } from './AircraftStatusTag'
import { EmptyState } from '../../../components/ui/EmptyState'

const columns: TableColumnsType<AircraftResponse> = [
  { title: 'Số đăng ký', dataIndex: 'registrationNo', render: (value: string, aircraft) =>
    <Link className="airport-code" to={routeTo.aircraftDetail(aircraft.id)}>{value}</Link> },
  { title: 'Trạng thái', dataIndex: 'status', render: status => <AircraftStatusTag status={status} /> },
  { title: 'Chi tiết', key: 'detail', render: (_, aircraft) =>
    <Link to={routeTo.aircraftDetail(aircraft.id)} aria-label={`Xem máy bay ${aircraft.registrationNo}`}>Xem chi tiết →</Link> },
]
export function AircraftTable({ data, loading, page, onPageChange }: {
  data: AircraftResponse[]; loading: boolean; page: number; onPageChange: (page: number) => void
}) {
  return <Table rowKey="id" columns={columns} dataSource={data} loading={loading} scroll={{ x: 620 }}
    locale={{ emptyText: <EmptyState description="Không có máy bay phù hợp." /> }}
    pagination={{ current: page, pageSize: 10, total: data.length, onChange: onPageChange,
      showSizeChanger: false, responsive: true, showTotal: total => `${total} máy bay` }} />
}
