import { AppRouter } from './app/router/AppRouter'
import { AntdProvider } from './app/providers/AntdProvider'
import './App.css'

export default function App() {
  return <AntdProvider><AppRouter /></AntdProvider>
}
