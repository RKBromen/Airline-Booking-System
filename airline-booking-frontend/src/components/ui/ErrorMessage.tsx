import { Alert, Button } from 'antd'
import { normalizeApiError } from '../../shared/api/apiError'

export function ErrorMessage({ error, onRetry, retrying = false }: { error: unknown; onRetry: () => void; retrying?: boolean }) {
  return <Alert type="error" showIcon title="Không thể tải dữ liệu"
    description={normalizeApiError(error).detail}
    action={<Button onClick={onRetry} loading={retrying}>Thử lại</Button>} />
}
