import { Skeleton, Spin } from 'antd'

export function Loading({ label = 'Đang tải dữ liệu…', skeleton = false }: { label?: string; skeleton?: boolean }) {
  return <div className="loading-state" role="status" aria-live="polite" aria-label={label}>
    {skeleton ? <Skeleton active paragraph={{ rows: 5 }} /> : <Spin size="large" />}<p>{label}</p>
  </div>
}
