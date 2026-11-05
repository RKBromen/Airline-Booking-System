# DOC 04 — Backend API, DTO & Enum Mapping
Dự án: Airline Booking System | React + Vite + TypeScript + Axios + TanStack Query + Zustand
## 1. Mục đích tài liệu
Tài liệu này quy định cách chuyển đổi API Contract từ backend Spring Boot sang frontend React TypeScript, giúp hai phía sử dụng thống nhất cấu trúc dữ liệu.
Các thành phần cần mapping gồm:

|Thành phần Backend|Thành phần Frontend|Mục đích|
|---|---|---|
|REST Controller|API Service|Gọi đúng endpoint và HTTP method|
|Request DTO|TypeScript Request Type|Kiểm soát dữ liệu gửi lên backend|
|Response DTO|TypeScript Response Type|Kiểm soát dữ liệu nhận về|
|Java Enum|TypeScript Union Type|Đồng bộ giá trị trạng thái|
|Pageable Response|`PageResponse<T>`|Xử lý phân trang|
|ProblemDetail|`ApiError`|Chuẩn hóa lỗi API|
|Spring Security Roles|`Role` + Route Guard|Kiểm soát quyền truy cập|
|Business Rules|Validation + UI Rules|Ngăn thao tác không hợp lệ trên giao diện|
Nguyên tắc: Frontend phải tuân theo API Contract của backend, không tự đặt thêm field hoặc giá trị enum rồi coi đó là dữ liệu backend hỗ trợ.
## 2. Cấu trúc thư mục
```
src/
├── shared/
│   ├── api/
│   │   ├── axiosClient.ts
│   │   └── endpoints.ts
│   ├── types/
│   │   ├── api.types.ts
│   │   └── pagination.types.ts
│   └── constants/
│       └── queryKeys.ts
│
├── features/
│   ├── auth/
│   │   ├── api/auth.api.ts
│   │   ├── types/auth.dto.ts
│   │   ├── types/auth.enums.ts
│   │   └── hooks/useAuth.ts
│   │
│   ├── users/
│   ├── airports/
│   ├── aircrafts/
│   ├── flights/
│   ├── flight-schedules/
│   ├── flight-search/
│   ├── bookings/
│   └── payments/
│
└── app/
    └── router/
```
Mỗi module nghiệp vụ nên tách `api`, `types`, `hooks`, `components` và `pages` khi cần.
Ví dụ module Airport:
```
features/airports/
├── api/
│   └── airport.api.ts
├── types/
│   ├── airport.dto.ts
│   └── airport.enums.ts
├── hooks/
│   ├── useAirports.ts
│   └── useCreateAirport.ts
├── components/
│   └── AirportForm.tsx
└── pages/
    ├── AirportListPage.tsx
    └── AirportCreatePage.tsx
```
## 3. Quy tắc mapping
1. `UUID` của Java ánh xạ thành `string` trong TypeScript.
2. `String` ánh xạ thành `string`, `Integer`/`Long`/`BigDecimal` trong JSON số ánh xạ thành `number` khi backend xác nhận cách serialize.

3. `LocalDate` và giá trị ngày giờ JSON dạng ISO ánh xạ thành `string`, sau đó parse khi cần hiển thị.
4. Enum được giữ nguyên giá trị, chẳng hạn `PENDING`, `CONFIRMED`, `CANCELLED`.
5. Phân biệt `field?: string` (có thể vắng mặt) với `field: string | null` (field có mặt nhưng có thể null).
6. Request DTO và Response DTO phải khai báo riêng, không sử dụng tùy tiện cùng một interface.
7. Chỉ dùng `PageResponse<T>` khi endpoint thực sự trả về cấu trúc phân trang.
8. Không tự tạo `id`, `totalAmount`, `status` hoặc `createdAt` ở frontend nếu các giá trị đó do backend quản lý.

### Nội dung tài liệu
- Mapping toàn bộ API của 9 module sang frontend API services.
- Định nghĩa TypeScript DTO cho Request và Response.
- Định nghĩa enum cho role, trạng thái người dùng, ghế, lịch bay, booking và payment.
- Quy tắc mapping kiểu dữ liệu Java sang TypeScript.
- Hướng dẫn tổ chức `features`, Axios Client và TanStack Query.
- Ví dụ code gọi API tạo booking, lấy sân bay và thanh toán.
- Mapping HTTP errors và business validation.
- Checklist kiểm tra hợp đồng API với backend.
Điểm cần lưu ý: File backend bạn gửi là bản đặc tả API, chưa phải toàn bộ Java DTO/Enum thực tế. Mình đã đánh dấu các trường hợp chưa đủ thông tin hoặc không thống nhất trong tài liệu thay vì tự đặt ra dữ liệu.
Bạn có thể đưa file `.md` này trực tiếp vào thư mục `docs/` của dự án frontend, dùng làm tài liệu tham chiếu khi triển khai từng module.