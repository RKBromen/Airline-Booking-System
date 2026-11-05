import { Input } from 'antd'

export function AircraftSearch({ value, onChange }: { value: string; onChange: (value: string) => void }) {
  return <div className="aircraft-search"><label htmlFor="aircraft-keyword">Tìm máy bay</label>
    <Input.Search id="aircraft-keyword" size="large" value={value} allowClear
      placeholder="Tìm theo số đăng ký…" onChange={event => onChange(event.target.value)} />
  </div>
}
