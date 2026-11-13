import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, message, Popconfirm, Tag } from 'antd'
import { useSearchParams } from 'react-router'
import { Loading } from '../../../components/ui/Loading'
import { ErrorMessage } from '../../../components/ui/ErrorMessage'
import { authApi } from '../../auth/api/auth.api'
import type { AuthProvider } from '../../auth/types/auth.enums'
import { queryKeys } from '../../../shared/constants/queryKeys'
import { normalizeApiError } from '../../../shared/api/apiError'
import { AuthProviderIcon } from '../../../components/ui/AuthProviderIcon'

const providerLabels: Record<AuthProvider, string> = { GOOGLE: 'Google', FACEBOOK: 'Facebook' }

export function LinkedAccountsPage() {
  const queryClient = useQueryClient()
  const [params, setParams] = useSearchParams()
  const query = useQuery({ queryKey: queryKeys.auth.providers, queryFn: authApi.providers })
  const link = useMutation({
    mutationFn: (provider: AuthProvider) => authApi.linkProvider(provider),
    onSuccess: result => window.location.assign(result.authorizationUrl),
  })
  const unlink = useMutation({
    mutationFn: (provider: AuthProvider) => authApi.unlinkProvider(provider),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.auth.providers })
      void message.success('Đã hủy liên kết tài khoản.')
    },
  })
  const linkedProvider = params.get('linked') as AuthProvider | null
  const oauthFailed = params.get('error') === 'oauth_login_failed'
  const clearOutcome = () => {
    if (linkedProvider || oauthFailed) {
      const next = new URLSearchParams(params)
      next.delete('linked')
      next.delete('error')
      setParams(next, { replace: true })
    }
  }

  return <>
    {linkedProvider && <Alert className="provider-note" type="success" showIcon closable onClose={clearOutcome}
      title={`Đã liên kết tài khoản ${providerLabels[linkedProvider] ?? linkedProvider}.`} />}
    {oauthFailed && <Alert className="provider-note" type="error" showIcon closable onClose={clearOutcome}
      title="Không thể liên kết tài khoản. Tài khoản nhà cung cấp có thể đã được dùng ở nơi khác hoặc chưa cung cấp email đã xác minh." />}
    {link.isError && <Alert className="provider-note" type="error" showIcon closable
      title={normalizeApiError(link.error).detail} />}
    {unlink.isError && <Alert className="provider-note" type="error" showIcon closable
      title={normalizeApiError(unlink.error).detail} />}
    {query.data && !query.data.hasPassword && query.data.providers.filter(provider => provider.linked).length <= 1
      && <Alert className="provider-note" type="info" showIcon title="Hãy thiết lập mật khẩu hoặc liên kết thêm một tài khoản trước khi gỡ phương thức đăng nhập hiện tại." />}
    {query.isPending && <Loading label="Đang tải trạng thái liên kết…" />}
    {query.isError && <ErrorMessage error={query.error} onRetry={() => { void query.refetch() }} retrying={query.isFetching} />}
    {query.data && <Card className="profile-card"><div className="account-data-table-scroll"><table className="account-data-table">
      <tbody>
        <tr><th scope="row">Mật khẩu</th><td><Tag color={query.data.hasPassword ? 'success' : 'default'}>{query.data.hasPassword ? 'Đã thiết lập' : 'Chưa thiết lập'}</Tag></td></tr>
        {query.data.providers.map(item => <tr key={item.provider}>
          <th scope="row"><span className="provider-identity"><AuthProviderIcon provider={item.provider} />{providerLabels[item.provider] ?? item.provider}</span></th>
          <td><div className="provider-status"><Tag color={item.linked ? 'success' : 'default'}>{item.linked ? 'Đã liên kết' : 'Chưa liên kết'}</Tag>
            {item.linked
              ? <Popconfirm title="Hủy liên kết tài khoản này?" description="Bạn vẫn cần ít nhất một cách đăng nhập." okText="Hủy liên kết" cancelText="Quay lại" onConfirm={() => unlink.mutate(item.provider)}>
                  <Button loading={unlink.isPending} disabled={!query.data.hasPassword && query.data.providers.filter(provider => provider.linked).length <= 1}>Hủy liên kết</Button>
                </Popconfirm>
              : <Button loading={link.isPending} onClick={() => link.mutate(item.provider)}>Liên kết</Button>}
          </div></td>
        </tr>)}
      </tbody>
    </table></div></Card>}
  </>
}
