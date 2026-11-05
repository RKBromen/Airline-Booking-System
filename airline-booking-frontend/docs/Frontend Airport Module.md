# DOC 04 — Frontend Airport Module: Danh sách và chi tiết sân bay
## 1. Mục tiêu
Xây dựng hai trang frontend cho phép người dùng xem danh sách sân bay và thông tin chi tiết của từng sân bay.

|Frontend Route|Chức năng|Backend API|
|---|---|---|
|`/airports`|Danh sách sân bay|`GET /api/v1/airports`|
|`/airports/:id`|Chi tiết sân bay|`GET /api/v1/airports/{id}`|
Theo tài liệu Sitemap & Routing đã cung cấp, cả hai trang đều thuộc nhóm Public Route. Người dùng không cần đăng nhập hoặc có role cụ thể để truy cập.
### 1.1. Yêu cầu chung
- Sử dụng React Functional Components và TypeScript.
- Sử dụng Axios để gọi Backend API.
- Sử dụng TanStack Query để quản lý server state.
- Sử dụng Ant Design để xây dựng giao diện.
- Sử dụng React Router để chuyển trang.
- Tách biệt API, TypeScript types, custom hooks, components và pages.
- Xử lý đầy đủ các trạng thái loading, error, empty và success.
- Giao diện responsive, sử dụng được trên desktop và mobile.
## 2. Cấu trúc thư mục đề xuất
Đặt toàn bộ nghiệp vụ sân bay trong `src/features/airports/`, phù hợp với cách chia module trong tài liệu hiện tại.
```
src/
├── app/
│   └── router/
│       ├── index.tsx
│       └── paths.ts
│
├── features/
│   └── airports/
│       ├── api/
│       │   └── airportApi.ts
│       │
│       ├── components/
│       │   ├── AirportTable.tsx
│       │   ├── AirportSearch.tsx
│       │   └── AirportDetailCard.tsx
│       │
│       ├── hooks/
│       │   ├── useAirports.ts
│       │   └── useAirport.ts
│       │
│       ├── pages/
│       │   ├── AirportListPage.tsx
│       │   └── AirportDetailPage.tsx
│       │
│       ├── types/
│       │   └── airport.types.ts
│       │
│       └── constants/
│           └── airportQueryKeys.ts
│
└── shared/
    └── api/
        └── axiosClient.ts
```
Các file có trách nhiệm riêng biệt:

| File                    | Nhiệm vụ                        |
| ----------------------- | ------------------------------- |
| `airportApi.ts`         | Khai báo hàm gọi API sân bay    |
| `airport.types.ts`      | Định nghĩa kiểu dữ liệu sân bay |
| `airportQueryKeys.ts`   | Quản lý TanStack Query keys     |
| `useAirports.ts`        | Hook lấy danh sách sân bay      |
| `useAirport.ts`         | Hook lấy chi tiết một sân bay   |
| `AirportTable.tsx`      | Component bảng danh sách        |
| `AirportSearch.tsx`     | Component tìm kiếm              |
| `AirportDetailCard.tsx` | Component hiển thị chi tiết     |
| `AirportListPage.tsx`   | Trang danh sách sân bay         |
| `AirportDetailPage.tsx` | Trang chi tiết sân bay          |
Nguyên tắc: Page điều phối giao diện, hook quản lý việc truy vấn, API module thực hiện HTTP request. Không gọi Axios trực tiếp trong component hiển thị.
## 3. Task 1 — Xây dựng trang danh sách sân bay
AIRPORT-01
Route: `/airports` · API: `GET /api/v1/airports`
### 3.1. Mô tả chức năng
Trang danh sách sân bay cho phép người dùng xem các sân bay hiện có trong hệ thống, tìm kiếm sân bay và truy cập trang chi tiết.
### 3.2. Giao diện tham khảo
![[Pasted image 20261105202237.png]]
### 3.3. Yêu cầu chức năng

| ID     | Yêu cầu                                       | Mức độ   |
| ------ | --------------------------------------------- | -------- |
| AIR-01 | Gọi API lấy danh sách sân bay khi mở trang    | Bắt buộc |
| AIR-02 | Hiển thị dữ liệu bằng Ant Design Table        | Bắt buộc |
| AIR-03 | Hiển thị loading khi đang gọi API             | Bắt buộc |
| AIR-04 | Hiển thị Empty khi không có sân bay           | Bắt buộc |
| AIR-05 | Hiển thị Error và nút thử lại khi API lỗi     | Bắt buộc |
| AIR-06 | Tìm kiếm theo mã hoặc tên sân bay             | Bắt buộc |
| AIR-07 | Bấm vào sân bay để chuyển đến `/airports/:id` | Bắt buộc |
| AIR-08 | Phân trang danh sách                          | Bắt buộc |
| AIR-09 | Responsive trên desktop và mobile             | Bắt buộc |
### Trách nhiệm từng component

| Component                             | Chức năng                      | Sử dụng ở đâu         |
| ------------------------------------- | ------------------------------ | --------------------- |
| `src/components/ui/Loading.tsx`       | Hiển thị trạng thái đang tải   | Tất cả các trang      |
| `src/components/ui/ErrorMessage.tsx`  | Hiển thị lỗi và nút thử lại    | API lỗi               |
| `src/components/ui/ConfirmDialog.tsx` | Xác nhận thao tác như xóa, hủy | Các chức năng quản lý |
### 3.4. Luồng xử lý
```
User truy cập /airports
        |
        v
AirportListPage
        |
        v
useAirports()
        |
        v
airportApi.list()
        |
        v
GET /api/v1/airports
        |
        v
Backend trả dữ liệu
        |
        v
TanStack Query quản lý dữ liệu
        |
        v
AirportTable hiển thị danh sách
        |
        v
User chọn một sân bay
        |
        v
Navigate /airports/:id
```
### 3.5. Checklist triển khai
Tạo airport.types.ts và xác nhận response DTO
Tạo airportApi.ts với hàm list()
Tạo airportQueryKeys.ts
Tạo hook useAirports()
Xây dựng component AirportTable
Xây dựng component AirportSearch
Xây dựng trang AirportListPage
Xử lý loading, empty, error và retry
Tích hợp tìm kiếm và phân trang
Tích hợp điều hướng đến trang chi tiết
Kiểm tra responsive và test API
## 4. Task 2 — Xây dựng trang chi tiết sân bay
AIRPORT-02
Route: `/airports/:id` · API: `GET /api/v1/airports/{id}`
### 4.1. Mô tả chức năng
Người dùng chọn một sân bay từ danh sách để xem thông tin chi tiết. Trang sử dụng `id` từ URL để gọi backend và hiển thị dữ liệu tương ứng.
### 4.2. Giao diện tham khảo
![[Pasted image 20261105204447.png]]
### 4.3. Yêu cầu chức năng

| ID     | Yêu cầu                                | Mức độ   |
| ------ | -------------------------------------- | -------- |
| AIR-10 | Lấy `id` từ React Router `useParams()` | Bắt buộc |
| AIR-11 | Gọi API lấy sân bay theo ID            | Bắt buộc |
| AIR-12 | Hiển thị thông tin sân bay             | Bắt buộc |
| AIR-13 | Hiển thị loading khi tải dữ liệu       | Bắt buộc |
| AIR-14 | Hiển thị Not Found khi không tìm thấy  | Bắt buộc |
| AIR-15 | Xử lý lỗi API và cho phép thử lại      | Bắt buộc |
| AIR-16 | Có nút quay về `/airports`             | Bắt buộc |
| AIR-17 | Hỗ trợ truy cập trực tiếp URL chi tiết | Bắt buộc |
| AIR-18 | Responsive trên mobile và desktop      | Bắt buộc |
### 4.4. Luồng xử lý
```
User truy cập /airports/:id
        |
        v
AirportDetailPage
        |
        v
useParams() lấy id
        |
        v
useAirport(id)
        |
        v
airportApi.get(id)
        |
        v
GET /api/v1/airports/{id}
        |
        +---- 200 --> Hiển thị chi tiết
        |
        +---- 404 --> Không tìm thấy sân bay
        |
        +---- Lỗi khác --> Hiển thị lỗi + Retry
```
### 4.5. Checklist triển khai
Tạo API get(id)
Tạo custom hook useAirport(id)
Tạo AirportDetailCard
Tạo AirportDetailPage
Lấy và kiểm tra id từ URL
Hiển thị thông tin sân bay từ API
Xử lý loading, error, 404 và retry
Tạo nút quay lại danh sách
Test truy cập trực tiếp /airports/:id
Test responsive
## 5. Thiết kế API và TypeScript
### 5.1. API Contract

| Function  | Method | Endpoint                |
| --------- | ------ | ----------------------- |
| `list()`  | GET    | `/api/v1/airports`      |
| `get(id)` | GET    | `/api/v1/airports/{id}` |
### 5.3. TanStack Query Hooks
`src/features/airports/hooks/useAirports.ts`
```
import { useQuery } from "@tanstack/react-query";
import { airportApi } from "../api/airportApi";
export function useAirports() {
  return useQuery({
    queryKey: ["airports"],
    queryFn: airportApi.getAirports,
    staleTime: 5 * 60 * 1000,
  });
}
```
`src/features/airports/hooks/useAirport.ts`
```
import { useQuery } from "@tanstack/react-query";
import { airportApi } from "../api/airportApi";
export function useAirport(id?: string) {
  return useQuery({
    queryKey: ["airports", id],
    queryFn: () => airportApi.getAirportById(id!),
    enabled: Boolean(id),
    staleTime: 5 * 60 * 1000,
  });
}
```
Sau khi hoàn thiện có thể chuyển các query key sang `airportQueryKeys.ts` để tái sử dụng và tránh sai khác giữa các nơi.
### 5.4. Routing
Bổ sung vào `paths.ts`:
```
export const ROUTES = {
  AIRPORTS: "/airports",
  AIRPORT_DETAIL: "/airports/:id",
} as const;
export const routeTo = {
  airportDetail: (id: string) =>
    `/airports/${encodeURIComponent(id)}`,
};
```
Ví dụ cấu hình route con trong `PublicLayout`:
```
<Route element={<PublicLayout />}>
  <Route
    path="/airports"
    element={<AirportListPage />}
  />
  <Route
    path="/airports/:id"
    element={<AirportDetailPage />}
  />
</Route>
```
Đây là đoạn tích hợp vào router hiện có, không phải thay thế toàn bộ `index.tsx`.
## 6. Xử lý trạng thái giao diện

|Trạng thái|Trang danh sách|Trang chi tiết|
|---|---|---|
|Loading|Table Skeleton hoặc Spin|Skeleton|
|Success|Hiển thị bảng|Hiển thị thông tin sân bay|
|Empty|Ant Design Empty|Không áp dụng với object chi tiết hợp lệ|
|404|Thông báo phù hợp nếu API trả 404|Result 404|
|Network Error|Alert và Retry|Alert và Retry|
|Missing ID|Không áp dụng|Không gọi API khi ID không hợp lệ|
Đối với tìm kiếm, nên sử dụng `Input.Search`. Khi không có kết quả lọc, bảng cần hiển thị trạng thái trống thay vì thông báo lỗi.
## 7. Kiểm thử và tiêu chí nghiệm thu

|Test ID|Kịch bản|Kết quả mong đợi|
|---|---|---|
|TC-01|Mở `/airports`|Danh sách tải thành công|
|TC-02|Backend trả danh sách rỗng|Hiển thị Empty|
|TC-03|Tìm kiếm sân bay tồn tại|Hiển thị kết quả phù hợp|
|TC-04|Tìm kiếm không có kết quả|Hiển thị thông báo không có dữ liệu|
|TC-05|Chọn một sân bay|Điều hướng đúng ID|
|TC-06|Mở URL chi tiết trực tiếp|Hiển thị đúng dữ liệu|
|TC-07|ID không tồn tại|Hiển thị Not Found|
|TC-08|Mất kết nối backend|Hiển thị Error và Retry|
|TC-09|Người dùng chưa đăng nhập|Vẫn truy cập được hai trang|
|TC-10|Mở trên mobile|Không vỡ layout|
|TC-11|Refresh trang chi tiết|Vẫn tải lại dữ liệu|
|TC-12|Click quay lại danh sách|Điều hướng về `/airports`|

## 8. Triển khai trong repository

Hai route `/airports` và `/airports/:id` được đăng ký trong `src/app/router/AppRouter.tsx`
bên trong PublicLayout, không có ProtectedRoute. Trang `/` chuyển về danh sách sân bay;
đường dẫn không tồn tại hiển thị trang 404. Entry point dùng QueryProvider và AntdProvider
với giao diện tiếng Việt.

Giữ tên API hiện có `api/airport.api.ts` và DTO đã đối chiếu với Java. Query key của module
re-export từ nguồn chung trong `shared/constants/queryKeys.ts`, tránh tạo cache khác nhau
giữa đọc dữ liệu và các mutation quản trị sau này. Hai hook đọc dữ liệu có staleTime 5 phút.

`GET /api/v1/airports` trả mảng, không có envelope phân trang. Tìm kiếm gửi `keyword` đến
backend; backend hỗ trợ mã/tên và cả thành phố/quốc gia. Table phân trang phía frontend,
10 sân bay mỗi trang; không gửi page/size đến API này. Từ khóa và trang được lưu trong URL
`?keyword=SGN&page=2`; tìm kiếm mới về trang 1, trang vượt giới hạn được giới hạn theo dữ liệu.

Trang chi tiết kiểm tra UUID trước khi gọi API. ID sai định dạng không phát sinh request;
404 từ API hiển thị Not Found, các lỗi còn lại có nút thử lại. Cả hai trang dùng dữ liệu
backend, không chứa danh sách sân bay giả trong code production. Các ảnh tham khảo được
nhúng theo tên Obsidian trong tài liệu chưa có file ảnh tương ứng trong repository.

Chạy local tại thư mục frontend:

```powershell
npm ci
npm run dev
```

Mở `http://localhost:3000/airports`; Vite chuyển `/api` đến backend ở cổng 8080.
Khi chạy Docker Compose, Nginx đã có fallback index.html nên có thể mở hoặc refresh
trực tiếp `/airports/:id`. Sau khi sửa frontend cần build lại image frontend theo README gốc.

Kiểm tra:

```powershell
npm run build
npm run lint
npm test
npm run test:e2e
```

Bộ E2E dùng Playwright với Chrome đã cài trên máy, tự mở Vite ở cổng 4174 và chặn
request API bằng dữ liệu giả lập. Không cần database và không ghi dữ liệu backend.
Chạy trên desktop và viewport mobile 390 × 844; bao gồm phân trang, tìm kiếm, danh sách
trống, chi tiết trực tiếp/reload, 404, ID sai, lỗi mạng/server và retry. Có thể dùng Edge:

```powershell
$env:PLAYWRIGHT_CHANNEL = 'msedge'
npm run test:e2e
```

Đã xác minh: build và lint thành công, 8 kiểm thử API/schema đạt, 14 kiểm thử trình duyệt
đạt (7 kịch bản × desktop/mobile). Đã xem ảnh chụp danh sách desktop và chi tiết mobile.
Vite có cảnh báo bundle lớn do thư viện UI; không ảnh hưởng kết quả build.
API/backend thật cần được kiểm tra riêng khi Spring Boot/MySQL đang chạy; Docker daemon
chưa chạy trong phiên kiểm tra này.
