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

## 4. Kết quả đối chiếu và triển khai trong repository

Nguồn ưu tiên: controller + DTO + service Java đang có; sau đó mới đến tài liệu 05-1. Các class có tên enum nhưng thân rỗng không được coi là Java enum đã triển khai. Giá trị enum của các module chưa hoàn thiện được tham chiếu thêm từ `src/main/resources/db/migration/V1__initial_schema.sql` và ghi chú ngay trong file TypeScript.

### 4.1. Hợp đồng đã có controller Java

- Auth: register, login, me, logout, refresh-token, providers, hai URL bắt đầu OAuth và `POST /auth/oauth2/exchange`. Callback `/login/oauth2/code/{provider}` thuộc Spring Security, không gọi bằng Axios. `LoginResponse` thực tế có `userId`, `email`, `roles`; không có `refreshToken`. `AuthSession` là record nội bộ: controller chỉ serialize `session.response()`, còn refresh token được đặt trong cookie HttpOnly.
- Users: đủ tám thao tác trong `user.api.ts`. Cập nhật user dùng `PATCH /users/{id}` theo controller, dù bảng tổng hợp cũ ghi PUT. Đổi trạng thái trả đầy đủ `UserResponse`, không chỉ `{ id, status }`. `roleIds` có thể thiếu hoặc null; service mặc định CUSTOMER nếu không có vai trò. Danh sách dùng `PageResponse<UserResponse>`; `page >= 0`, `1 <= size <= 100`, `sort` chỉ nhận `createdAt`, `email`, `fullName` và backend luôn sắp xếp giảm dần. Không gửi chuỗi sort kiểu `createdAt,desc`.
- Airports: GET list/detail, POST create, PUT replace, PATCH partial, PATCH status. List trả `AirportResponse[]`, không có phân trang. PATCH cho phép field thiếu/null và giữ giá trị cũ; PUT bắt buộc name/city/country. Code có thể được gửi lại nhưng phải khớp code hiện có, không cho đổi mã. Không có DELETE airport.
- Aircrafts: GET list/detail, POST create, PUT update, PATCH status, GET/POST seats. List máy bay và list ghế đều là mảng. Ghế trong danh sách dùng `AircraftSeatListItem`, không có `aircraftId`; response thêm ghế dùng `AircraftSeatResponse`, có `aircraftId`. Không tự thêm API xóa máy bay hoặc sửa/xóa ghế.

`UserStatus` lấy từ enum lồng `Users.UserStatus` (`ACTIVE`, `BLOCKED`, `DELETED`), vì file `user/enums/UserStatus.java` hiện rỗng. Role lấy theo chính sách Spring Security và dữ liệu vai trò hệ thống; AuthProvider, AirportStatus, AircraftStatus, SeatClass lấy từ enum Java thực tế.

### 4.2. Hợp đồng dự kiến, chưa có backend thực thi

Các service xuất tên `planned...Api` để tránh hiểu nhầm rằng endpoint đã hoạt động:

- `plannedAuthProviderApi`: link/unlink tài khoản ngoài; Java mới có GET providers. Chưa có response cho link.
- `plannedRoleApi`: list roles, assign role, remove role. Chưa có Role controller và response danh sách.
- `plannedFlightApi`: list/detail/create/update/status, theo module 5. Create có DTO riêng; list item khác response create. Envelope list, detail và body update chưa được mô tả đầy đủ.
- `plannedFlightScheduleApi`: đủ tám thao tác module 6. Chỉ response create và đổi trạng thái có ví dụ cụ thể; update/cancel/list/detail/seat response còn cần xác nhận, body update cũng chưa chốt.
- `plannedFlightSearchApi`: search/detail/seats. DTO phần tử search và seat map được khai báo; chưa suy luận envelope search hay response detail.
- `plannedBookingApi`: create, mine, get, byCode, cancel, adminList. DTO create khác DTO detail; không tự đồng nhất hai loại. List và byCode chưa có response cụ thể. Không tạo API sửa status hoặc cho client chủ động đánh dấu EXPIRED.
- `plannedPaymentApi`: create/history/get/adminList. Chỉ checkout VNPAY/MOMO có luồng được đặc tả. CREDIT_CARD/BANK_TRANSFER tồn tại trong SQL nhưng không được đưa vào `CreatePaymentRequest`. History có ví dụ nhưng quy ước paidAt thiếu hay null chưa rõ, nên tạm giữ toàn bộ response là `unknown`. Detail/admin chưa có DTO.

`unknown` là điểm cần chốt hợp đồng, không phải dữ liệu được phép ép kiểu tùy ý. Các wrapper này chưa được nối vào trang đang chạy. Phải bổ sung Java controller/DTO và xác nhận response rồi mới sử dụng trong luồng thực tế. Schema hành khách tạm theo ví dụ đầy đủ trong 05-1; SQL cho phép null không chứng minh HTTP DTO cũng cho phép null. Các giá/amount `number` chỉ phản ánh ví dụ JSON số của tài liệu, chưa phải xác nhận serialize BigDecimal từ controller chạy thật.

Webhook VNPay/MoMo và URL return do backend/provider xử lý, không xây frontend mutation cho chúng. Tài liệu 05-1 mâu thuẫn giữa GET trong bảng VNPay IPN và POST trong phần diễn giải; cần backend chốt với giao thức provider. Frontend không gửi kết quả SUCCESS, không xác nhận booking và không tự giải phóng ghế.

## 5. Tổ chức và cách sử dụng

`shared/api/axiosClient.ts` là Axios instance duy nhất; `shared/api/endpoints.ts` chứa URL tương đối với `/api/v1` và encode các path parameter. Các đường dẫn cũ `lib/axios.ts`, `types/api.types.ts`, `constants/apiEndpoints.ts`, `constants/roles.ts`, `utils/errorHandler.ts` chỉ re-export nguồn chung để tránh nhiều định nghĩa khác nhau. `*.types.ts` ở các feature cũng re-export DTO/enum mới.

TypeScript bật `strict` để phân biệt null, undefined và field bắt buộc khi kiểm tra kiểu.

Đã thêm QueryProvider ở entry point. Query key chứa filter để không trộn cache giữa các danh sách. Query chỉ retry một lần cho lỗi mạng/5xx; lỗi 4xx không retry. Mutation không retry tự động, đặc biệt booking/payment chưa có hợp đồng idempotency key.

Ví dụ dùng trong component bên trong QueryProvider:

```tsx
const airports = useAirports({ status: 'ACTIVE' })
// airports.data là AirportResponse[], không có .content.
const createAirport = useCreateAirport()
createAirport.mutate({ code: 'sgn', name: 'Tan Son Nhat', city: 'Ho Chi Minh City', country: 'Vietnam' })
```

Ví dụ cho giai đoạn backend booking/payment đã hoàn thiện:

```tsx
const createBooking = usePlannedCreateBooking()
const booking = await createBooking.mutateAsync({
  flightScheduleId,
  passengers: [{ fullName, dateOfBirth, gender, documentNumber, flightSeatId }],
})
// Dùng hook ở cấp cao nhất của component, không gọi hook trong callback.
const createPayment = usePlannedCreatePayment(bookingId)
const payment = await createPayment.mutateAsync({ paymentMethod: 'VNPAY' })
// payment.paymentUrl là URL do backend trả về; return từ cổng thanh toán
// cần đọc lại trạng thái từ backend, không được tự coi là thanh toán thành công.
```

Các tên biến trong ví dụ do form/component cung cấp. Không tạo `id`, `bookingCode`, `totalAmount`, `status`, `createdAt` ở client. Schema booking/payment từ chối field ngoài hợp đồng như totalAmount/amount.

### 5.1. Xác thực, cookie và quyền truy cập

- Access token giữ trong Zustand memory, không persist localStorage. Axios gắn Bearer token, gửi cookie bằng withCredentials và gửi X-XSRF-TOKEN từ cookie XSRF-TOKEN.
- Khi bắt đầu ứng dụng có đăng nhập, gọi và đợi `authApi.refresh()` trước khi mount ProtectedRoute; xử lý 401 bằng trạng thái khách. Bộ khung hiện chưa có router/trang đăng nhập thực tế, nên không tự bật bootstrap auth trên trang Vite mẫu.
- `useLogin` lưu session và xóa query cache cũ; `useLogout` xóa trạng thái/cache ngay khi bắt đầu thao tác. Nếu logout backend lỗi, caller vẫn phải hiển thị lỗi để người dùng biết cookie phía server có thể chưa thu hồi.
- 401 từ API được bảo vệ tự refresh một lần, các request đồng thời dùng chung một refresh. 401 từ login/register/exchange/logout/refresh không tạo vòng lặp refresh. Request vẫn 401 sau khi retry được trả về caller. Refresh thất bại xóa session; response refresh về trễ không khôi phục session đã logout.
- `ProtectedRoute` nhận danh sách Role, chuyển tới `/login` khi thiếu token và `/403` khi thiếu role. Khi xây router cần khai báo hai trang này. Route guard chỉ là kiểm soát giao diện; quyền dữ liệu và ownership do Spring Security/service xác nhận.
- OAuth bắt đầu bằng điều hướng trình duyệt tới `authApi.oauthUrl('google')` hoặc facebook, không gọi XHR tới provider. Callback frontend nhận mã dùng một lần, gửi `authApi.exchange({ code })` rồi lưu LoginResponse; không đưa token vào URL.
- Vite dùng cổng 3000 và proxy `/api`, `/oauth2`, `/login/oauth2` sang localhost:8080 để cookie CSRF đọc được cùng origin. `.env.example` mặc định VITE_API_BASE_URL=/api/v1. Production cần reverse proxy tương ứng. Nếu đổi sang backend khác origin, phải xác nhận CORS và khả năng đọc CSRF cookie; withCredentials không tự làm cookie của origin khác đọc được.
- Backend hiện chỉ buộc tạo CSRF cookie khi login/exchange; chưa có GET bootstrap CSRF riêng. Nếu mất XSRF-TOKEN thì refresh/logout có thể 403; frontend không tắt CSRF hoặc suy diễn 403 thành token hết hạn. Backend cần endpoint bootstrap nếu muốn hỗ trợ trường hợp này.

### 5.2. Lỗi và quy tắc nghiệp vụ

`normalizeApiError` trả ApiRequestError có status/title/detail/errorCode khi có. Hỗ trợ ProblemDetail theo đặc tả, `{ message }` từ ApiExceptionHandler hiện tại, body không phải JSON và lỗi mạng (status 0). Không tự tạo errorCode khi backend chưa cung cấp. fieldErrors chỉ dùng khi payload có đúng cấu trúc Record<string, string[]>; validation Spring hiện chưa có hợp đồng field errors thống nhất.

Schema và các hàm trong `utils/*Rules.ts` giúp form/UI kiểm tra trước:

- Đúng độ dài email/password/họ tên, airport code ba chữ cái và chuẩn hóa in hoa; enum giữ nguyên giá trị backend. Login không áp min 8 của đăng ký lên tài khoản đã có.
- Giới hạn phân trang và danh sách sort của UserService.
- Sân bay đi/đến khác nhau; giờ đến sau giờ đi; giá không âm.
- Booking phải có hành khách, không trùng flightSeatId; không nhận số tiền từ client.
- Chỉ ghế AVAILABLE được chọn/sửa giá; chỉ gợi ý thanh toán khi booking PENDING chưa quá hạn. Các trạng thái kết thúc của schedule không chuyển tiếp; hủy lịch bay phải dùng endpoint cancel để backend xử lý nghiệp vụ liên quan.

Backend vẫn phải xác nhận uniqueness, sân bay/máy bay ACTIVE, lịch bay chồng lấn, ghế thuộc schedule, ownership, ghế chưa bị người khác giữ, thanh toán và transaction. Ngày ISO giữ nguyên dạng string, không tự thêm Z cho LocalDateTime. Hàm kiểm tra giờ địa phương/expiry chỉ là gợi ý UI cho tới khi hai phía chốt timezone; cần đặc biệt chú ý khi triển khai ở múi giờ khác backend.

## 6. Checklist kiểm tra hợp đồng

- [x] Đối chiếu DTO/controller/service đang có cho auth/users/airports/aircrafts.
- [x] Tách request/response và create/detail/list item có cấu trúc khác nhau.
- [x] Chỉ dùng PageResponse cho users; không suy diễn pagination cho API dự kiến.
- [x] Mapping đủ thao tác phía frontend trong chín module, đánh dấu planned cho phần chưa có controller.
- [x] Thêm Axios, ApiError, token store, Role guard, QueryProvider, query keys và hook mẫu.
- [x] Thêm kiểm thử API adapter cho method/URL/response và refresh, schema booking/payment và quy tắc nghiệp vụ.
- [ ] Chốt DTO, envelope, nullability và serialize giá/amount cho các API planned.
- [ ] Triển khai các controller/enum Java còn rỗng, Role API và provider link/unlink.
- [ ] Chốt timezone, CSRF bootstrap, hợp đồng fieldErrors và method VNPay IPN.
- [ ] Kiểm thử tích hợp với Spring Boot và provider thật; không thay bằng kết quả mocked adapter.

Chạy kiểm tra tại thư mục frontend: `npm ci`, `npm run build`, `npm run lint`, `npm test`. Bộ test dùng Node 24 có TypeScript stripping và node:test, không thêm test framework. Kết quả chạy thực tế cần đọc từ terminal; checklist trên không khẳng định backend planned đã hoạt động.
