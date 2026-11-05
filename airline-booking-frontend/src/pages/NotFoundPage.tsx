import { Button, Result } from 'antd'
import { Link } from 'react-router'
import { ROUTES } from '../app/router/paths'

export function NotFoundPage() {
  return <Result status="404" title="Không tìm thấy trang" subTitle="Đường dẫn bạn truy cập không tồn tại."
    extra={<Link to={ROUTES.AIRPORTS}><Button type="primary">Về danh sách sân bay</Button></Link>} />
}
