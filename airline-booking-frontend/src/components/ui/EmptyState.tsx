import { Empty } from 'antd'

export function EmptyState({ description = 'Chưa có dữ liệu.' }: { description?: string }) {
  return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={description} />
}
