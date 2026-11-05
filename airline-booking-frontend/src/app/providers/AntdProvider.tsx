import { App, ConfigProvider } from 'antd'
import viVN from 'antd/locale/vi_VN'
import type { PropsWithChildren } from 'react'

export function AntdProvider({ children }: PropsWithChildren) {
  return (
    <ConfigProvider locale={viVN} theme={{ token: {
      colorPrimary: '#126a68', colorInfo: '#126a68', borderRadius: 10,
      colorText: '#18302f', colorTextSecondary: '#667775', fontFamily: "'Segoe UI', system-ui, sans-serif",
    } }}><App>{children}</App></ConfigProvider>
  )
}
