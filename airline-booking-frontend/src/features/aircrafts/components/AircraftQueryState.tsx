import type { ReactNode } from 'react'
import { Result } from 'antd'
import { Navigate, useLocation } from 'react-router'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { normalizeApiError } from '../../../shared/api/apiError'
import { ROUTES } from '../../../app/router/paths'

interface QueryState { isPending: boolean; isError: boolean; error: unknown; isFetching: boolean; refetch: () => unknown }
export function AircraftQueryState({ query, invalid = false, children }: { query: QueryState; invalid?: boolean; children: ReactNode }) {
  const location = useLocation()
  const status = query.isError ? normalizeApiError(query.error).status : 0
  if (invalid || status === 404) return <Result status="404" title="Không tìm thấy máy bay" subTitle="Máy bay không tồn tại hoặc ID không hợp lệ." />
  if (status === 403) return <Navigate to={ROUTES.FORBIDDEN} replace />
  if (status === 401) return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />
  if (query.isPending) return <Loading label="Đang tải thông tin máy bay…" skeleton />
  if (query.isError) return <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />
  return children
}
