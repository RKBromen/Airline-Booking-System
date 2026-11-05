import { Breadcrumb, Card } from 'antd'
import { useSearchParams } from 'react-router'
import { useAirports } from '../hooks/useAirports'
import { AirportSearch } from '../components/AirportSearch'
import { AirportTable } from '../components/AirportTable'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { AIRPORT_PAGE_SIZE } from '../constants/airport.constants'
import { airportPage } from '../utils/airportParams'

export function AirportListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword')?.trim() ?? ''
  const airports = useAirports(keyword ? { keyword } : {})
  const data = airports.data ?? []
  // Backend returns a full array; only the table paginates it.
  const page = Math.min(airportPage(searchParams.get('page')), Math.max(1, Math.ceil(data.length / AIRPORT_PAGE_SIZE)))
  const search = (value: string) => {
    setSearchParams(previous => {
      const next = new URLSearchParams(previous)
      if (value) next.set('keyword', value)
      else next.delete('keyword')
      next.delete('page')
      return next
    })
  }
  const changePage = (value: number) => {
    setSearchParams(previous => {
      const next = new URLSearchParams(previous)
      if (value === 1) next.delete('page')
      else next.set('page', String(value))
      return next
    })
  }
  return <>
    <Breadcrumb items={[{ title: 'Tra cứu' }, { title: 'Sân bay' }]} />
    <section className="page-intro"><p className="eyebrow">Khám phá điểm đến</p><h1>Danh sách sân bay</h1>
      <p className="muted">Tra cứu mã sân bay, thành phố và thông tin điểm đến cho hành trình của bạn.</p></section>
    <Card className="airport-list-card">
      <div className="airport-toolbar">
        <AirportSearch keyword={keyword} onSearch={search} />
        <p className="result-count" aria-live="polite">{airports.isPending ? 'Đang tải danh sách…'
          : airports.isError ? 'Chưa tải được danh sách' : `${data.length} sân bay${keyword ? ' phù hợp' : ' trong hệ thống'}`}</p>
      </div>
      {airports.isError && <div className="airport-error"><ErrorMessage error={airports.error}
        onRetry={() => { void airports.refetch() }} retrying={airports.isFetching} /></div>}
      {(!airports.isError || airports.data) && <AirportTable airports={data}
        loading={airports.isFetching} page={page} onPageChange={changePage} hasSearch={Boolean(keyword)} />}
    </Card>
  </>
}
