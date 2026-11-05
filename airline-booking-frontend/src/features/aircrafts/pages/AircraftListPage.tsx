import { useState } from 'react'
import { Button, Card } from 'antd'
import { Link } from 'react-router'
import { useAircrafts } from '../hooks/useAircrafts'
import { AircraftTable } from '../components/AircraftTable'
import { AircraftSearch } from '../components/AircraftSearch'
import { AircraftQueryState } from '../components/AircraftQueryState'
import { PageHeader } from '../../../components/ui/PageHeader'
import { useAuth } from '../../auth/hooks/useAuth'
import { ROUTES } from '../../../app/router/paths'

export function AircraftListPage() {
  const query = useAircrafts()
  const { roles } = useAuth()
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(1)
  const data = (query.data ?? []).filter(aircraft => aircraft.registrationNo.toLowerCase().includes(keyword.trim().toLowerCase()))
  const currentPage = Math.min(page, Math.max(1, Math.ceil(data.length / 10)))
  return <>
    <PageHeader title="Danh sách máy bay" description="Tra cứu số đăng ký và cấu hình ghế vật lý."
      actions={roles.includes('ADMIN') && <Link to={ROUTES.AIRCRAFT_NEW}><Button type="primary">Thêm máy bay</Button></Link>} />
    <Card className="aircraft-list-card"><div className="aircraft-toolbar">
      <AircraftSearch value={keyword} onChange={value => { setKeyword(value); setPage(1) }} /></div>
      <AircraftQueryState query={query}><AircraftTable data={data} loading={query.isFetching} page={currentPage} onPageChange={setPage} /></AircraftQueryState>
    </Card>
  </>
}
