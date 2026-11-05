import type { PropsWithChildren } from 'react'
import { useQuery } from '@tanstack/react-query'
import { authApi } from '../../../features/auth/api/auth.api'
import { useAuth } from '../../../features/auth/hooks/useAuth'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { normalizeApiError } from '../../../shared/api/apiError'

/** Attempt cookie-based restoration before the guards decide whether to redirect. */
export function SessionBoundary({ children }: PropsWithChildren) {
  const auth = useAuth()
  const restore = useQuery({ queryKey: ['auth', 'restore'], queryFn: authApi.refresh,
    enabled: !auth.isAuthenticated, retry: false, staleTime: Infinity })
  if (!auth.isAuthenticated && restore.isPending) return <Loading label="Đang kiểm tra phiên đăng nhập…" />
  if (!auth.isAuthenticated && restore.isError && ![401, 403].includes(normalizeApiError(restore.error).status)) {
    return <ErrorMessage error={restore.error} onRetry={() => { void restore.refetch() }} retrying={restore.isFetching} />
  }
  return children
}
