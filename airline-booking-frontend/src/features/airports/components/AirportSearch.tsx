import { Input } from 'antd'

export function AirportSearch({ keyword, onSearch }: { keyword: string; onSearch: (value: string) => void }) {
  return <div className="airport-search">
    <label htmlFor="airport-keyword">Tìm sân bay</label>
    <Input.Search key={keyword} id="airport-keyword" size="large" defaultValue={keyword}
      placeholder="Nhập mã hoặc tên sân bay…" allowClear enterButton="Tìm kiếm"
      onSearch={value => onSearch(value.trim())} />
  </div>
}
