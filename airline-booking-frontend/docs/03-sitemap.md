# DOC 03 — Sitemap & Routing
Dự án: Airline Booking System | Frontend: React + Vite + TypeScript + React Router
## 1. Mục đích tài liệu
Tài liệu này xác định cấu trúc trang, đường dẫn URL, quyền truy cập và luồng điều hướng cho toàn bộ frontend của hệ thống đặt vé máy bay.
Dựa trên file API bạn cung cấp, hệ thống có 9 module nghiệp vụ, phục vụ ba vai trò chính: `CUSTOMER`, `STAFF` và `ADMIN`.
Frontend sẽ được chia thành bốn khu vực:

| Khu vực        | Mục đích                                                                          |
| -------------- | --------------------------------------------------------------------------------- |
| Public         | Trang chủ, tìm kiếm chuyến bay, xem thông tin chuyến bay                          |
| Authentication | Đăng nhập, đăng ký, xử lý xác thực                                                |
| Customer       | Đặt vé, thanh toán, lịch sử đặt vé, quản lý tài khoản                             |
| Management     | Quản lý máy bay, sân bay, chuyến bay, lịch bay, người dùng, booking và thanh toán |
## 2. Sitemap tổng thể
```
/
├── login
├── register
├── auth
│   └── callback
│
├── airports
│   └── :id
│
├── flights
│   └── :scheduleId
│       ├── seats
│       └── passengers
│
├── bookings
│   └── :bookingId
│       ├── checkout
│       └── payments
│
├── payment
│   └── result
│
├── my-bookings
├── profile
│   ├── edit
│   ├── password
│   └── linked-accounts
│
├── management
│   ├── aircrafts
│   │   ├── new
│   │   └── :id
│   │       ├── edit
│   │       └── seats
│   │
│   ├── airports
│   │   ├── new
│   │   └── :id
│   │       └── edit
│   │
│   ├── flights
│   │   ├── new
│   │   └── :id
│   │       └── edit
│   │
│   ├── flight-schedules
│   │   ├── new
│   │   └── :id
│   │       ├── edit
│   │       └── seats
│   │
│   ├── bookings
│   │   └── :id
│   │
│   └── payments
│       └── :id
│
├── admin
│   ├── users
│   │   ├── new
│   │   └── :id
│   │       └── edit
│   └── roles
│
├── forbidden
└── *
```
Lưu ý: Các route `new`, `edit`, `seats`, `checkout` là màn hình hoặc bước giao diện, không nhất thiết phải có endpoint backend cùng tên.
Trong thiết kế này, `/flights/:scheduleId` dùng ID của lịch bay thực tế để khách đặt vé; còn `/management/flights/:id` dùng ID của chuyến bay/tuyến bay để quản lý. Hai ID không được sử dụng lẫn nhau.
## 3. Danh sách route chi tiết
### 3.1. Public & Authentication

| Frontend Route               | Chức năng                      | API chính                                | Phân công | Deadline |
| ---------------------------- | ------------------------------ | ---------------------------------------- | --------- | -------- |
| `/`                          | Trang chủ, form tìm chuyến bay | `GET /airports`, `GET /search/flights`   | Lâm       | 11/11    |
| `/login`                     | Đăng nhập                      | `POST /auth/login`                       | Lâm       | 11/11    |
| `/register`                  | Đăng ký                        | `POST /auth/register`                    | Lâm       | 11/11    |
| `/auth/callback`             | Xử lý kết quả OAuth2           |                                          | Lâm       | 11/11    |
| `/airports`                  | Danh sách sân bay              | `GET /airports`                          | Nhi       | 8/11     |
| `/airports/:id`              | Chi tiết sân bay               | `GET /airports/{id}`                     | Nhi       | 8/11     |
| `/flights`                   | Kết quả tìm kiếm chuyến bay    | `GET /search/flights`                    | Tín       | 8/11     |
| `/flights/:scheduleId`       | Chi tiết lịch bay để đặt vé    | `GET /search/flights/{scheduleId}`       | Tín       | 8/11     |
| `/flights/:scheduleId/seats` | Chọn ghế                       | `GET /search/flights/{scheduleId}/seats` | Tín       | 8/11     |
Trong bảng này và các bảng tiếp theo, endpoint API được viết rút gọn, bỏ tiền tố `/api/v1`.
Có thể lưu điều kiện tìm chuyến bay trên URL:
```
/flights?departure=SGN&arrival=HAN&date=2026-11-20
```
Cách này cho phép người dùng tải lại trang hoặc chia sẻ liên kết mà vẫn giữ được điều kiện tìm kiếm.
### 3.2. Customer — Đặt vé và thanh toán

| Frontend Route                    | Chức năng                          | API chính                            | Phân công | Deadline |
| --------------------------------- | ---------------------------------- | ------------------------------------ | --------- | -------- |
| `/flights/:scheduleId/passengers` | Nhập thông tin hành khách          | Chưa có API riêng; dùng dữ liệu form | Nghiệp    | 11/11    |
| `/bookings/:bookingId`            | Chi tiết booking                   | `GET /bookings/{id}`                 | Nghiệp    | 11/11    |
| `/bookings/:bookingId/checkout`   | Xác nhận và khởi tạo thanh toán    | `POST /bookings/{id}/payments`       | Nghiệp    | 11/11    |
| `/bookings/:bookingId/payments`   | Lịch sử giao dịch                  | `GET /bookings/{id}/payments`        | Nghiệp    | 11/11    |
| `/payment/result`                 | Kết quả quay về từ cổng thanh toán | `GET /payments/{id}` và booking      | Nghiệp    | 11/11    |
| `/my-bookings`                    | Lịch sử đặt vé cá nhân             | `GET /bookings/me`                   | Nghiệp    | 11/11    |
| `/profile`                        | Thông tin tài khoản                | `GET /auth/me`                       | Lâm       | 11/11    |
| `/profile/edit`                   | Cập nhật hồ sơ                     | `PUT /users/me`                      | Lâm       | 11/11    |
| `/profile/password`               | Đổi mật khẩu                       | `PUT /users/me/password`             | Lâm       | 11/11    |
| `/profile/linked-accounts`        | Quản lý Google/Facebook liên kết   | `GET /auth/providers`                | Lâm       | 11/11    |
Lưu ý: `/flights/:scheduleId/passengers` không cần API ghi dữ liệu riêng. Khi người dùng nhập xong hành khách và chọn ghế, frontend sẽ gọi `POST /api/v1/bookings` để tạo booking và giữ ghế.
### 3.3. Management — STAFF và ADMIN

| Frontend Route                           | Chức năng                   | Role         | Phân công | Phân công |
| ---------------------------------------- | --------------------------- | ------------ | --------- | --------- |
| `/management/aircrafts`                  | Danh sách máy bay           | ADMIN, STAFF | Nhi       | 13/11     |
| `/management/aircrafts/:id`              | Chi tiết máy bay            | ADMIN, STAFF | Nhi       | 13/11     |
| `/management/aircrafts/:id/seats`        | Xem sơ đồ ghế vật lý        | ADMIN, STAFF | Nhi       | 13/11     |
| `/management/aircrafts/new`              | Thêm máy bay                | ADMIN        | Nhi       | 13/11     |
| `/management/aircrafts/:id/edit`         | Cập nhật máy bay            | ADMIN        | Nhi       | 13/11     |
| `/management/airports`                   | Danh sách sân bay           | ADMIN, STAFF | Tín       | 13/11     |
| `/management/airports/:id`               | Chi tiết sân bay            | ADMIN, STAFF | Tín       | 13/11     |
| `/management/airports/new`               | Thêm sân bay                | ADMIN        | Tín       | 13/11     |
| `/management/airports/:id/edit`          | Cập nhật sân bay            | ADMIN        | Tín       | 13/11     |
| `/management/flights`                    | Danh sách tuyến bay         | ADMIN, STAFF | Tín       | 13/11     |
| `/management/flights/:id`                | Chi tiết tuyến bay          | ADMIN, STAFF | Tín       | 13/11     |
| `/management/flights/new`                | Thêm tuyến bay              | ADMIN        | Tín       | 13/11     |
| `/management/flights/:id/edit`           | Cập nhật tuyến bay          | ADMIN        | Tín       | 13/11     |
| `/management/flight-schedules`           | Danh sách lịch bay          | ADMIN, STAFF | Tín       | 13/11     |
| `/management/flight-schedules/:id`       | Chi tiết lịch bay           | ADMIN, STAFF | Tín       | 13/11     |
| `/management/flight-schedules/new`       | Tạo lịch bay                | ADMIN        | Tín       | 13/11     |
| `/management/flight-schedules/:id/edit`  | Sửa lịch bay                | ADMIN        | Tín       | 13/11     |
| `/management/flight-schedules/:id/seats` | Ghế và giá vé theo lịch bay | ADMIN, STAFF | Tín       | 13/11     |
| `/management/bookings`                   | Booking toàn hệ thống       | ADMIN, STAFF | Nghiệp    | 15/11     |
| `/management/bookings/:id`               | Chi tiết booking            | ADMIN, STAFF | Nghiệp    | 15/11     |
| `/management/payments`                   | Danh sách thanh toán        | ADMIN, STAFF | Nghiệp    | 15/11     |
| `/management/payments/:id`               | Chi tiết giao dịch          | ADMIN, STAFF | Nghiệp    | 15/11     |
STAFF được xem và thực hiện những thao tác nghiệp vụ mà backend cấp phép. Không nên hiển thị chức năng thêm, sửa hoặc xóa dành riêng cho ADMIN.
### 3.4. Admin — Quản lý người dùng và phân quyền

| Frontend Route          | Chức năng             | API chính         | Phân công | Deadline |
| ----------------------- | --------------------- | ----------------- | --------- | -------- |
| `/admin/users`          | Danh sách người dùng  | `GET /users`      | Lâm       | 15/11    |
| `/admin/users/new`      | Thêm tài khoản        | `POST /users`     | Lâm       | 15/11    |
| `/admin/users/:id`      | Chi tiết tài khoản    | `GET /users/{id}` | Lâm       | 15/11    |
| `/admin/users/:id/edit` | Cập nhật tài khoản    | `PUT /users/{id}` | Lâm       | 15/11    |
| `/admin/roles`          | Xem danh sách vai trò | `GET /roles`      | Lâm       | 15/11    |
Trang chi tiết người dùng còn có các thao tác thay đổi trạng thái, gán và gỡ vai trò, thông qua các API `PATCH /users/{id}/status`, `POST /users/{id}/roles` và `DELETE /users/{id}/roles/{roleId}`.
Không cần tạo mỗi thao tác thành một route riêng; có thể sử dụng `Modal`, `Drawer` hoặc form ngay trong trang chi tiết.
### 4. Phân quyền truy cập Route
### 4.1. Quy tắc phân quyền
Hệ thống Airline Booking System có ba vai trò chính:
- `CUSTOMER`: Tìm kiếm chuyến bay, đặt vé, thanh toán, xem và quản lý booking cá nhân.
- `STAFF`: Xem thông tin vận hành, lịch bay, máy bay, booking và giao dịch; thực hiện những thao tác được backend cho phép.
- `ADMIN`: Quản lý toàn bộ hệ thống, bao gồm tài khoản, sân bay, máy bay, tuyến bay, lịch bay và phân quyền.
Frontend sử dụng React Router để kiểm soát quyền truy cập các trang.
Quy ước:

|Ký hiệu|Ý nghĩa|
|---|---|
|Cho phép|Người dùng có thể truy cập route|
|Login|Chuyển hướng đến `/login`|
|Forbidden|Chuyển hướng đến `/forbidden`|
|Redirect|Chuyển hướng đến trang phù hợp nếu đã đăng nhập|
##### 4.2. Bảng phân quyền tổng quát

|Loại Route|Chưa đăng nhập|CUSTOMER|STAFF|ADMIN|
|---|---|---|---|---|
|Public|Cho phép|Cho phép|Cho phép|Cho phép|
|Authentication|Cho phép|Redirect|Redirect|Redirect|
|Customer Booking|Login|Cho phép|Forbidden*|Forbidden*|
|Profile|Login|Cho phép|Cho phép|Cho phép|
|Management Read|Login|Forbidden|Cho phép|Cho phép|
|Management Admin Actions|Login|Forbidden|Forbidden|Cho phép|
|Admin Users & Roles|Login|Forbidden|Forbidden|Cho phép|
STAFF hoặc ADMIN vẫn có thể truy cập Customer Booking nếu tài khoản đồng thời có role `CUSTOMER`. Bảng thể hiện các trường hợp tài khoản chỉ có một role.
##### 4.3. PublicRoute — Các trang công khai
Quy tắc: Không yêu cầu đăng nhập. Tất cả người dùng đều được phép truy cập.

| Route                        | Chức năng                          |
| ---------------------------- | ---------------------------------- |
| `/`                          | Trang chủ                          |
| `/airports`                  | Danh sách sân bay                  |
| `/airports/:id`              | Chi tiết sân bay                   |
| `/flights`                   | Tìm kiếm chuyến bay                |
| `/flights/:scheduleId`       | Xem chi tiết lịch bay để đặt vé    |
| `/flights/:scheduleId/seats` | Xem sơ đồ ghế và chọn ghế tạm thời |
Quy tắc triển khai:
1. Không kiểm tra Access Token trước khi hiển thị trang.
2. Không yêu cầu role.
3. Người dùng chưa đăng nhập vẫn có thể tìm kiếm chuyến bay và xem ghế.
4. Thao tác chọn ghế trên giao diện chưa tạo booking hoặc giữ ghế trong database.
5. Khi người dùng thực hiện đặt vé, nếu chưa đăng nhập thì chuyển đến `/login`.
6. Nếu người dùng đã đăng nhập và có role `CUSTOMER`, cho phép tiếp tục quy trình đặt vé.
Ví dụ:
```
User chưa đăng nhập
    |
    v
/flights
    |
    v
/flights/:scheduleId
    |
    v
/flights/:scheduleId/seats
    |
    v
Bấm "Tiếp tục đặt vé"
    |
    v
Kiểm tra đăng nhập và role CUSTOMER
    |
    ├── Hợp lệ
    |      |
    |      v
    |   /flights/:scheduleId/passengers
    |
    ├── Chưa đăng nhập
    |      |
    |      v
    |   /login
    |
    └── Không có role CUSTOMER
           |
           v
        /forbidden
```
##### 4.4. GuestRoute — Đăng nhập và đăng ký
Quy tắc: Chỉ dành cho người dùng chưa đăng nhập.

|Route|Chức năng|
|---|---|
|`/login`|Đăng nhập|
|`/register`|Đăng ký|
Quy tắc triển khai:
1. Nếu chưa đăng nhập, cho phép truy cập.
2. Nếu đã đăng nhập hợp lệ, không cần hiển thị trang đăng nhập hoặc đăng ký.
3. Chuyển hướng người dùng đã đăng nhập đến trang phù hợp.
4. Sau khi đăng nhập thành công, nếu có URL cần quay lại thì ưu tiên URL đó, với điều kiện URL an toàn và người dùng có quyền truy cập.
5. Nếu không có URL quay lại, sử dụng quy tắc chuyển hướng mặc định.

|Role|Trang mặc định sau đăng nhập|
|---|---|
|CUSTOMER|`/`|
|STAFF|`/management/flight-schedules`|
|ADMIN|`/management/flight-schedules`|
Đây là quy tắc điều hướng đề xuất của frontend, không phải yêu cầu bắt buộc từ API.
Ví dụ:
```
CUSTOMER đã đăng nhập
         |
         v
      /login
         |
         v
   Redirect đến /
```
Route `/auth/callback` không nên xử lý như trang GuestRoute thông thường. Đây là route kỹ thuật phục vụ hoàn tất xác thực OAuth2 và cần triển khai theo phương thức callback mà backend thống nhất.
##### 4.5. Customer Booking — Đặt vé và thanh toán
Quy tắc: Phải đăng nhập và có role `CUSTOMER`.

|Route|Chức năng|Điều kiện|
|---|---|---|
|`/flights/:scheduleId/passengers`|Nhập thông tin hành khách|CUSTOMER|
|`/bookings/:bookingId`|Chi tiết booking cá nhân|OWNER|
|`/bookings/:bookingId/checkout`|Thanh toán booking|OWNER|
|`/bookings/:bookingId/payments`|Lịch sử thanh toán booking|OWNER|
|`/my-bookings`|Lịch sử đặt vé cá nhân|CUSTOMER|
Quy tắc triển khai:
1. Kiểm tra người dùng đã đăng nhập hay chưa.
2. Nếu chưa đăng nhập, chuyển hướng đến `/login`.
3. Nếu đã đăng nhập nhưng không có role `CUSTOMER`, chuyển đến `/forbidden`.
4. Nếu hợp lệ, cho phép truy cập route và gọi API tương ứng.
5. Với các route có `bookingId`, backend phải kiểm tra người dùng có quyền truy cập booking đó hay không.
6. Không cho phép người dùng xem booking của người khác bằng cách thay đổi `bookingId` trên URL.
7. Chỉ cho phép khởi tạo thanh toán khi booking thuộc người dùng, có trạng thái `PENDING` và còn hiệu lực.
8. Nếu booking đã `CONFIRMED`, `CANCELLED` hoặc `EXPIRED`, không cho phép khởi tạo thanh toán mới.
Ví dụ:
```
CUSTOMER truy cập
/bookings/123/checkout
          |
          v
Kiểm tra đăng nhập
          |
          v
Kiểm tra role CUSTOMER
          |
          v
GET /api/v1/bookings/123
          |
          v
Backend kiểm tra OWNER
          |
          ├── Không có quyền → /forbidden
          |
          └── Có quyền
                |
                v
          Kiểm tra status
                |
                ├── PENDING, còn hạn
                |       |
                |       v
                |    Thanh toán
                |
                └── Không hợp lệ
                        |
                        v
                 Thông báo trạng thái
```
Lưu ý: API `GET /bookings/{id}` cũng cho phép STAFF và ADMIN theo tài liệu backend. Tuy nhiên, frontend nên sử dụng `/management/bookings/:id` cho nhân viên và quản trị viên thay vì cho họ truy cập trực tiếp luồng thanh toán của CUSTOMER.
##### 4.6. Profile — Quản lý tài khoản cá nhân
Quy tắc: Tất cả tài khoản đã đăng nhập đều được truy cập.

|Route|Chức năng|Role|
|---|---|---|
|`/profile`|Thông tin tài khoản|AUTHENTICATED|
|`/profile/edit`|Cập nhật hồ sơ cá nhân|AUTHENTICATED|
|`/profile/password`|Đổi mật khẩu|AUTHENTICATED|
|`/profile/linked-accounts`|Quản lý liên kết Google/Facebook|AUTHENTICATED|
Quy tắc triển khai:
1. Kiểm tra trạng thái đăng nhập.
2. Nếu chưa đăng nhập, chuyển đến `/login`.
3. Nếu đã đăng nhập, cho phép truy cập mà không yêu cầu role cụ thể.
4. Chỉ hiển thị và chỉnh sửa thông tin của tài khoản hiện tại.
5. Không cho phép chỉnh sửa thông tin của tài khoản khác thông qua trang Profile.
6. Sử dụng `GET /api/v1/auth/me` để lấy thông tin tài khoản.
7. Các thao tác liên kết hoặc hủy liên kết tài khoản phải tuân theo quy tắc của backend.
##### 4.7. Management Read — Xem thông tin quản lý
Quy tắc: Phải đăng nhập và có ít nhất một trong hai role `STAFF` hoặc `ADMIN`.

|Route|Chức năng|
|---|---|
|`/management/airports`|Danh sách sân bay|
|`/management/airports/:id`|Chi tiết sân bay|
|`/management/aircrafts`|Danh sách máy bay|
|`/management/aircrafts/:id`|Chi tiết máy bay|
|`/management/aircrafts/:id/seats`|Danh sách ghế vật lý|
|`/management/flights`|Danh sách tuyến bay|
|`/management/flights/:id`|Chi tiết tuyến bay|
|`/management/flight-schedules`|Danh sách lịch bay|
|`/management/flight-schedules/:id`|Chi tiết lịch bay|
|`/management/flight-schedules/:id/seats`|Danh sách ghế theo lịch bay|
|`/management/bookings`|Danh sách booking toàn hệ thống|
|`/management/bookings/:id`|Chi tiết booking|
|`/management/payments`|Danh sách giao dịch|
|`/management/payments/:id`|Chi tiết giao dịch|
Quy tắc triển khai:
1. Kiểm tra người dùng đã đăng nhập.
2. Nếu chưa đăng nhập, chuyển đến `/login`.
3. Nếu người dùng có `STAFF` hoặc `ADMIN`, cho phép truy cập.
4. Nếu chỉ có `CUSTOMER`, chuyển đến `/forbidden`.
5. Các trang danh sách được phép thực hiện tìm kiếm, lọc, sắp xếp và phân trang theo API hỗ trợ.
6. Không tự động hiển thị nút thêm hoặc chỉnh sửa cho STAFF nếu chức năng đó chỉ cho phép ADMIN.
7. Riêng chức năng thay đổi trạng thái lịch bay, cả STAFF và ADMIN đều có quyền theo tài liệu API.
Ngoại lệ: Các API `GET /airports` và `GET /airports/{id}` là PUBLIC. Việc giới hạn các trang `/management/airports` cho STAFF và ADMIN là quy tắc giao diện quản trị, không phải giới hạn từ backend.
##### 4.8. Management Admin Actions — Thêm và chỉnh sửa dữ liệu quản lý
Quy tắc: Phải đăng nhập và có role `ADMIN`.

|Route|Chức năng|
|---|---|
|`/management/airports/new`|Thêm sân bay|
|`/management/airports/:id/edit`|Chỉnh sửa sân bay|
|`/management/aircrafts/new`|Thêm máy bay|
|`/management/aircrafts/:id/edit`|Chỉnh sửa máy bay|
|`/management/flights/new`|Thêm tuyến bay|
|`/management/flights/:id/edit`|Chỉnh sửa tuyến bay|
|`/management/flight-schedules/new`|Tạo lịch bay|
|`/management/flight-schedules/:id/edit`|Chỉnh sửa lịch bay|
Ngoài các route trên, một số thao tác không cần route riêng nhưng vẫn yêu cầu `ADMIN`:

|Thao tác|API|
|---|---|
|Thay đổi trạng thái sân bay|`PATCH /airports/{id}/status`|
|Thay đổi trạng thái máy bay|`PATCH /aircrafts/{id}/status`|
|Thêm ghế vật lý|`POST /aircrafts/{id}/seats`|
|Thay đổi trạng thái tuyến bay|`PATCH /flights/{id}/status`|
|Hủy lịch bay|`POST /flight-schedules/{id}/cancel`|
|Cập nhật giá ghế|`PATCH /flight-schedules/{id}/seats/{seatId}/price`|
Quy tắc triển khai:
1. Kiểm tra người dùng đã đăng nhập.
2. Nếu chưa đăng nhập, chuyển đến `/login`.
3. Nếu có role `ADMIN`, cho phép truy cập.
4. Nếu chỉ có role `CUSTOMER` hoặc `STAFF`, chuyển đến `/forbidden`.
5. Chỉ hiển thị các nút Create, Edit, Cancel hoặc Update Price nếu người dùng có quyền thực hiện.
6. Khi cập nhật dữ liệu, frontend phải xử lý validation và lỗi nghiệp vụ từ backend.
7. Không cho phép thao tác vượt quá trạng thái nghiệp vụ hợp lệ của tài nguyên.
Ngoại lệ: `PATCH /api/v1/flight-schedules/{id}/status` cho phép cả `STAFF` và `ADMIN`. Chức năng này cần một quyền riêng, không đặt chung vào nhóm chỉ dành cho ADMIN.
##### 4.9. Admin Users & Roles — Quản lý tài khoản và vai trò
Quy tắc: Chỉ người dùng có role `ADMIN` được truy cập.

|Route|Chức năng|
|---|---|
|`/admin/users`|Danh sách tài khoản|
|`/admin/users/new`|Tạo tài khoản|
|`/admin/users/:id`|Chi tiết tài khoản|
|`/admin/users/:id/edit`|Chỉnh sửa tài khoản|
|`/admin/roles`|Danh sách vai trò|
Các thao tác thay đổi trạng thái, xóa mềm, gán hoặc gỡ vai trò có thể thực hiện ngay tại trang chi tiết người dùng.
Quy tắc triển khai:
1. Kiểm tra đăng nhập.
2. Yêu cầu role `ADMIN`.
3. Nếu không có quyền, chuyển đến `/forbidden`.
4. Cho phép quản lý tài khoản và phân quyền theo API.
5. Không cho phép cấp quyền bằng cách tự chỉnh sửa dữ liệu role ở frontend.
6. Không cho phép thực hiện thao tác trái với quy tắc nghiệp vụ, chẳng hạn gỡ vai trò ADMIN cuối cùng nếu backend đang áp dụng chính sách bảo vệ này.
##### 4.10. System Routes — Các route đặc biệt

|Route|Quy tắc|
|---|---|
|`/forbidden`|Hiển thị lỗi không có quyền truy cập|
|`*`|Hiển thị trang 404 khi frontend route không tồn tại|
|`/auth/callback`|Xử lý callback xác thực từ OAuth2|
|`/payment/result`|Tiếp nhận kết quả điều hướng thanh toán, xác minh bằng backend|
`/payment/result` không được dựa vào query parameters hoặc thông báo từ trình duyệt để kết luận thanh toán thành công. Trạng thái chính thức phải được xác nhận từ backend.
#### 4.11. Bảng áp dụng Guard cho từng nhóm Route

|Nhóm|Guard cần áp dụng|Allowed Roles|
|---|---|---|
|Public|Không cần Guard|Tất cả|
|Authentication|`GuestRoute`|Chưa đăng nhập|
|Customer Booking|`ProtectedRoute` + `RoleGuard`|CUSTOMER|
|Profile|`ProtectedRoute`|Mọi role đã đăng nhập|
|Management Read|`ProtectedRoute` + `RoleGuard`|STAFF, ADMIN|
|Management Admin Actions|`ProtectedRoute` + `RoleGuard`|ADMIN|
|Admin Users & Roles|`ProtectedRoute` + `RoleGuard`|ADMIN|
Đối với các booking và payment cá nhân, cần thêm kiểm tra Ownership ở backend, không thể chỉ sử dụng React Router Guard.
## 5. Luồng điều hướng nghiệp vụ
### 5.1. Đặt vé máy bay

| STT | Chức năng                 | Frontend Route                    | API Backend                             | Mô tả xử lý                                                                                      |
| --- | ------------------------- | --------------------------------- | --------------------------------------- | ------------------------------------------------------------------------------------------------ |
| 1   | Trang chủ                 | `/`                               | —                                       | Hiển thị form tìm kiếm chuyến bay                                                                |
| 2   | Nhập thông tin tìm kiếm   | `/`                               | —                                       | Nhập sân bay đi, sân bay đến và ngày bay                                                         |
| 3   | Tìm chuyến bay            | `/flights`                        | `GET /search/flights`                   | Hiển thị danh sách chuyến bay phù hợp với điều kiện tìm kiếm                                     |
| 4   | Chi tiết chuyến bay       | `/flights/:scheduleId`            | `GET /search/flights/:scheduleId`       | Hiển thị thông tin chi tiết của chuyến bay được chọn                                             |
| 5   | Chọn ghế                  | `/flights/:scheduleId/seats`      | `GET /search/flights/:scheduleId/seats` | Hiển thị sơ đồ ghế, trạng thái ghế và cho phép người dùng chọn ghế khả dụng                      |
| 6   | Nhập thông tin hành khách | `/flights/:scheduleId/passengers` | —                                       | Nhập và kiểm tra thông tin hành khách trước khi đặt vé                                           |
| 7   | Kiểm tra đăng nhập        | `/login`                          | API xác thực                            | Yêu cầu đăng nhập trước khi tạo booking; sau khi đăng nhập thành công, quay lại quy trình đặt vé |
| 8   | Tạo booking               | `/flights/:scheduleId/passengers` | `POST /bookings`                        | Gửi thông tin hành khách và ghế đã chọn để tạo booking                                           |
| 9   | Xác nhận booking          | —                                 | `POST /bookings`                        | Backend kiểm tra tính khả dụng của ghế, giữ ghế và tạo booking với trạng thái `PENDING`          |
| 10  | Thanh toán                | `/bookings/:bookingId/checkout`   | `POST /bookings/:bookingId/payments`    | Hiển thị thông tin booking, số tiền cần thanh toán và thực hiện yêu cầu thanh toán               |
| 11  | Kết quả thanh toán        | `/payment/result`                 | API kiểm tra trạng thái tương ứng       | Hiển thị kết quả thanh toán dựa trên trạng thái thực tế được xác minh từ backend                 |
| 12  | Xem vé đã đặt             | `/my-bookings`                    | API danh sách booking cá nhân           | Cho phép người dùng xem lại các booking, bao gồm booking đã được xác nhận `CONFIRMED`            |
### 5.2. Các trường hợp đặc biệt cần xử lý

|STT|Trường hợp|Điều kiện|Xử lý Frontend|
|---|---|---|---|
|1|Chưa đăng nhập|Người dùng thực hiện tạo booking nhưng chưa xác thực|Chuyển đến `/login`, lưu thông tin quy trình đặt vé và đưa người dùng quay lại sau khi đăng nhập thành công|
|2|Ghế không còn khả dụng|Ghế được chọn đã bị người khác giữ hoặc đặt|Hiển thị thông báo, cập nhật trạng thái ghế và yêu cầu người dùng chọn ghế khác|
|3|Booking hết hạn|Booking có trạng thái `EXPIRED`|Không cho phép tiếp tục thanh toán, hiển thị thông báo booking đã hết hạn|
|4|Thanh toán đang xử lý|Payment có trạng thái `PENDING`|Hiển thị trạng thái đang xử lý, không tự xác nhận thanh toán thành công|
|5|Booking được xác nhận|Booking có trạng thái `CONFIRMED`|Hiển thị thông báo đặt vé thành công và cho phép xem chi tiết trong `/my-bookings`|
|6|Thanh toán thất bại|Backend xác nhận payment thất bại|Hiển thị thông báo lỗi và cho phép thử thanh toán lại nếu booking vẫn hợp lệ|
|7|Lỗi kết nối khi thanh toán|Không nhận được phản hồi từ backend|Kiểm tra lại trạng thái payment trước khi cho phép gửi yêu cầu thanh toán mới, tránh thanh toán trùng|
|8|Người dùng tải lại trang|Người dùng refresh trong quá trình đặt vé|Khôi phục dữ liệu tạm nếu còn hợp lệ và kiểm tra lại trạng thái booking, ghế từ backend|
Lưu ý triển khai: Các bước chọn ghế và nhập hành khách có thể lưu dữ liệu tạm trên frontend, nhưng trạng thái ghế, booking và payment phải luôn được backend xác nhận. Frontend không được tự quyết định booking đã thành công chỉ dựa vào dữ liệu cục bộ hoặc việc chuyển hướng đến trang kết quả thanh toán.
### 5.2. Quản lý máy bay
```
/management/aircrafts
         |
         ├── /new
         |      └── POST /aircrafts
         |
         └── /:id
                ├── GET /aircrafts/:id
                |
                ├── /edit
                |      └── PUT /aircrafts/:id
                |
                └── /seats
                       ├── GET /aircrafts/:id/seats
                       └── POST /aircrafts/:id/seats
```
Các chức năng này cần phân biệt ghế vật lý (`aircraft_seats`) và ghế được mở bán cho từng lịch bay (`flight_seats`). Việc cập nhật giá vé nằm ở module Flight Schedule, không nằm ở module Aircraft.
## 6. Thiết kế React Router
### 6.1. Cấu trúc thư mục đề xuất
```
src/
├── app/
│   ├── router/
│   │   ├── index.tsx
│   │   ├── paths.ts
│   │   └── guards/
│   │       ├── ProtectedRoute.tsx
│   │       ├── GuestRoute.tsx
│   │       └── RoleGuard.tsx
│   │
│   └── layouts/
│       ├── PublicLayout.tsx
│       ├── AuthLayout.tsx
│       ├── CustomerLayout.tsx
│       └── ManagementLayout.tsx
│
├── features/
│   ├── auth/
│   ├── airports/
│   ├── aircrafts/
│   ├── flights/
│   ├── flight-schedules/
│   ├── bookings/
│   ├── payments/
│   ├── profile/
│   └── users/
│
├── pages/
│   ├── ForbiddenPage.tsx
│   └── NotFoundPage.tsx
│
└── main.tsx
```
### 6.2. Quy ước đặt tên Route
Sử dụng một file `paths.ts` để quản lý URL, tránh viết chuỗi đường dẫn rải rác trong các component.
```
export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  AIRPORTS: "/airports",
  FLIGHTS: "/flights",
  FLIGHT_DETAIL: "/flights/:scheduleId",
  FLIGHT_SEATS: "/flights/:scheduleId/seats",
  FLIGHT_PASSENGERS: "/flights/:scheduleId/passengers",
  MY_BOOKINGS: "/my-bookings",
  BOOKING_DETAIL: "/bookings/:bookingId",
  CHECKOUT: "/bookings/:bookingId/checkout",
  PAYMENT_RESULT: "/payment/result",
  PROFILE: "/profile",
  MANAGEMENT: {
    AIRCRAFTS: "/management/aircrafts",
    AIRPORTS: "/management/airports",
    FLIGHTS: "/management/flights",
    FLIGHT_SCHEDULES: "/management/flight-schedules",
    BOOKINGS: "/management/bookings",
    PAYMENTS: "/management/payments",
  },
  ADMIN: {
    USERS: "/admin/users",
    ROLES: "/admin/roles",
  },
  FORBIDDEN: "/forbidden",
} as const;
```
Với những đường dẫn động, nên sử dụng hàm tạo URL để giảm lỗi truyền tham số:
```
export const routeTo = {
  flightDetail: (scheduleId: string) =>
    `/flights/${scheduleId}`,
  flightSeats: (scheduleId: string) =>
    `/flights/${scheduleId}/seats`,
  bookingDetail: (bookingId: string) =>
    `/bookings/${bookingId}`,
  checkout: (bookingId: string) =>
    `/bookings/${bookingId}/checkout`,
  aircraftDetail: (id: string) =>
    `/management/aircrafts/${id}`,
};
```
### 6.3. Quy tắc sử dụng Layout

| Layout             | Áp dụng cho                                    | Thành phần                           |
| ------------------ | ---------------------------------------------- | ------------------------------------ |
| `PublicLayout`     | Trang chủ, tìm chuyến bay, thông tin công khai | Header, Footer, Content              |
| `AuthLayout`       | Login, Register                                | Form đăng nhập/đăng ký               |
| `CustomerLayout`   | Booking, Profile, Checkout                     | Header, Navigation, Content          |
| `ManagementLayout` | Quản trị và vận hành                           | Sidebar, Header, Breadcrumb, Content |
Có thể dùng `ProtectedRoute` và `RoleGuard` với React Router để đặt điều kiện truy cập tại layout cha, sau đó render các route con thông qua `<Outlet />`.
## 7. Quy tắc xử lý điều hướng và trạng thái
### 7.1. Trường hợp chưa đăng nhập
Khi người dùng truy cập route cần xác thực:
```
/bookings/booking-123/checkout
          |
          v
        /login
          |
          v
  Đăng nhập thành công
          |
          v
/bookings/booking-123/checkout
```
Chỉ cho phép redirect về đường dẫn nội bộ hợp lệ để tránh lỗi open redirect.
### 7.2. Trường hợp không có quyền
- API trả `401`: thực hiện quy trình làm mới token nếu hợp lệ; nếu thất bại, chuyển về `/login`.
- API trả `403`: hiển thị `/forbidden`.
- API trả `404`: hiển thị trang không tìm thấy dữ liệu.
- API trả `409`: hiển thị lỗi xung đột nghiệp vụ, ví dụ ghế không còn khả dụng.
- API trả `422`: hiển thị thông tin lỗi nghiệp vụ để người dùng chỉnh sửa.
Phân quyền phía frontend chỉ phục vụ trải nghiệm người dùng. Backend vẫn phải kiểm tra JWT, role và quyền sở hữu tài nguyên.
### 7.3. Dữ liệu cần giữ giữa các bước đặt vé

| Dữ liệu                        | Nơi quản lý đề xuất            |
| ------------------------------ | ------------------------------ |
| Sân bay, ngày bay, bộ lọc      | URL search params              |
| `scheduleId`                   | URL params                     |
| Ghế đang chọn                  | Zustand, dữ liệu tạm           |
| Thông tin hành khách chưa gửi  | Zustand hoặc React Hook Form   |
| `bookingId`                    | URL params                     |
| Booking status, payment status | TanStack Query, lấy từ backend |
| Người dùng và role             | Auth state + `GET /auth/me`    |
Quan trọng: Ghế được chọn trên frontend chưa có nghĩa là ghế đã được giữ. Backend chỉ chính thức giữ ghế khi `POST /api/v1/bookings` thành công.
## 8. Các điểm API cần xác nhận trước khi triển khai
Sau khi đối chiếu tài liệu API, có một số điểm cần làm rõ:

| Vấn đề                       | Chi tiết                                                                                |
| ---------------------------- | --------------------------------------------------------------------------------------- |
| Cập nhật User                | Danh sách API ghi `PUT /users/{id}`, ví dụ chi tiết lại dùng `PATCH /users/{id}`        |
| Cập nhật Airport             | Danh sách ghi `PUT /airports/{id}`, ví dụ ghi `PATCH /airports/{id}`                    |
| Thanh toán VNPay             | Danh sách API ghi webhook VNPay bằng `GET`, phần mô tả lại ghi `POST`                   |
| OAuth2 callback              | Chưa xác định cách frontend nhận phiên xác thực sau callback                            |
| Kết quả thanh toán           | Chưa quy định rõ URL frontend nhận redirect và định danh giao dịch                      |
| Đổi mật khẩu, cập nhật hồ sơ | Có endpoint nhưng cần xác nhận DTO request/response                                     |
| Customer booking             | Chưa có API riêng cho bước nhập hành khách; không phải vấn đề nếu dùng `POST /bookings` |
| Ticket                       | API nhắc đến phát hành vé nhưng chưa mô tả module Ticket                                |
Đây là những điểm cần xác nhận với backend, không nên tự giả định endpoint mới trong frontend.
## 9. Checklist hoàn thành DOC 03
Tiến độ tài liệu
8/10
Xác định sitemap đầy đủ theo 9 module
Phân loại Public, Customer, Staff, Admin
Xác định URL và tham số động cho từng màn hình
Mapping frontend route với backend API
Thiết kế luồng tìm kiếm và đặt vé
Thiết kế luồng thanh toán và kết quả
Thống nhất các trường hợp redirect và lỗi
Tạo file paths.ts quản lý URL
Tạo cấu trúc layout và route guard
Xác nhận các điểm API chưa thống nhất với backend