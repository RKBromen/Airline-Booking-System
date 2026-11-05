import { Breadcrumb, Button, Result } from 'antd'
import { Link, useParams } from 'react-router'
import { ROUTES } from '../../../app/router/paths'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { useAirport } from '../hooks/useAirport'
import { AirportDetailCard } from '../components/AirportDetailCard'
import { isAirportId } from '../utils/airportParams'
import { normalizeApiError } from '../../../shared/api/apiError'

export function AirportDetailPage() {
  const { id } = useParams<{ id: string }>()
  const airport = useAirport(id)
  const notFound = !isAirportId(id) || (airport.isError && normalizeApiError(airport.error).status === 404)
  return <>
    <Breadcrumb items={[{ title: <Link to={ROUTES.AIRPORTS}>Sân bay</Link> }, { title: airport.data?.code ?? 'Chi tiết sân bay' }]} />
    <Link className="back-link" to={ROUTES.AIRPORTS}><span aria-hidden="true">← </span>Quay lại danh sách sân bay</Link>
    {notFound ? <Result status="404" title="Không tìm thấy sân bay" subTitle="Sân bay không tồn tại hoặc đường dẫn không hợp lệ."
      extra={<Link to={ROUTES.AIRPORTS}><Button type="primary">Về danh sách sân bay</Button></Link>} />
      : airport.isPending ? <div className="airport-loading-card"><Loading label="Đang tải thông tin sân bay…" skeleton /></div>
        : airport.isError ? <ErrorMessage error={airport.error} onRetry={() => { void airport.refetch() }} retrying={airport.isFetching} />
          : <AirportDetailCard airport={airport.data} />}
  </>
}
