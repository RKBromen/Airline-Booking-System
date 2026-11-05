import { Button, Result } from 'antd'
import { Link } from 'react-router'
import { ROUTES } from '../app/router/paths'

export function ForbiddenPage() {
  return <Result status="403" title="Bạn không có quyền truy cập"
    subTitle="Tài khoản của bạn không được phép thực hiện chức năng này."
    extra={<Link to={ROUTES.AIRPORTS}><Button type="primary">Về trang sân bay</Button></Link>} />
}
