# Airline Booking System

## Aircraft Management

Open `http://localhost:8080/swagger-ui.html` and expand **Aircrafts**. All seven
operations use **bearerAuth** via Swagger's **Authorize** button. ADMIN and STAFF
may read; only ADMIN may create or change aircraft and seats. Login first to
obtain an access token; Swagger sends the CSRF cookie/header automatically.

- `GET /api/v1/aircrafts`: list aircraft, sorted by registration.
- `GET /api/v1/aircrafts/{id}`: aircraft details.
- `POST /api/v1/aircrafts`: create with `{"registrationNo":"VN-A321"}`;
  returns `201`, a generated UUID, and status `ACTIVE`.
- `PUT /api/v1/aircrafts/{id}`: update using `{"registrationNo":"VN-A322"}`;
  preserves the ID, status and physical seats.
- `PATCH /api/v1/aircrafts/{id}/status`: `{"status":"BLOCKED"}`.
  Statuses match the existing database: `ACTIVE`, `BLOCKED`, `DELETED`.
- `GET /api/v1/aircrafts/{id}/seats`: list `{id, seatNumber, seatClass}`;
  an existing aircraft with no seats returns `[]`.
- `POST /api/v1/aircrafts/{id}/seats`: add a seat with
  `{"seatNumber":"12A","seatClass":"ECONOMY"}`. Returns `201` with
  `{id, aircraftId, seatNumber, seatClass}`.

Registration numbers and seat numbers are trimmed and stored uppercase.
Registrations must be unique; seat numbers are unique within an aircraft but may
repeat on other aircraft. Seat classes accepted by the API are exactly
`ECONOMY`, `BUSINESS`, `FIRST`. Registration and seat number limits follow the
existing schema (50 and 10 characters). No database migration is required;
existing unique constraints also protect concurrent requests.

Invalid input returns `400`, missing aircraft `404`, and duplicate registration
or seat number `409`. Anonymous access returns `401`; insufficient roles `403`.

```powershell
./mvnw.cmd "-Dtest=AircraftServiceTest,AircraftApiTest" test
```

`AircraftPersistenceIntegrationTest` additionally checks persistence and unique
constraints against the configured MySQL database; its fixtures roll back.

## Airport Management

- `GET /api/v1/airports?keyword=Ho%20Chi%20Minh&status=ACTIVE`: public list;
  optional case-insensitive keyword search across code, name, city and country,
  combined with an optional status filter. Omitted filters return all airports,
  sorted by code. Search treats `%` and `_` as literal characters.
- `GET /api/v1/airports/{id}`: public details, including the current status.
- `POST /api/v1/airports`: ADMIN only; JSON fields `code`, `name`, `city`, `country`.
  Returns `201 Created` with a `Location` header. Codes contain exactly three
  ASCII letters, are stored uppercase, and are unique. Status defaults to `ACTIVE`.
- `PUT /api/v1/airports/{id}`: ADMIN only; requires `name`, `city`, `country`.
- `PATCH /api/v1/airports/{id}`: ADMIN only; updates supplied fields among
  `name`, `city`, `country`, preserving omitted fields. Blank values are rejected.
- `PATCH /api/v1/airports/{id}/status`: ADMIN only; JSON such as
  `{"status":"BLOCKED"}`. Supported statuses match the database:
  `ACTIVE`, `BLOCKED`, `DELETED`.

All airport responses contain `id`, `code`, `name`, `city`, `country`, `status`.
Airport codes cannot be changed after creation; an update supplying a different
code returns `400`. Multiple airports may share a city, country or name.
Invalid requests return `400`, missing airports `404`, duplicate codes `409`.
Admin writes use the existing authentication and CSRF protection: supply the
admin access token and the CSRF cookie/header pair used by the application.

To test in Swagger, open `http://localhost:8080/swagger-ui.html` and expand
**Airports**. Public GET operations can be executed immediately. For admin writes,
execute `POST /api/v1/auth/login` using an existing ADMIN account, copy the
`accessToken` from its response, click **Authorize**, and paste the token into
**bearerAuth** (without the `Bearer` prefix). Swagger sends the CSRF cookie/header
automatically after login. Registering a new account creates a CUSTOMER, so it
cannot create or update airports until an administrator assigns the ADMIN role.
After source changes, rebuild the running app with
`docker compose up --build -d --no-deps app`, then refresh Swagger.

Run the airport service and API/security tests with Java 21 and Maven:

```powershell
./mvnw.cmd "-Dtest=AirportServiceTest,AirportApiTest" test
```

## Chạy bằng Docker

Yêu cầu: Docker Desktop đang chạy với Linux containers và có Docker Compose.
Không cần cài Java, Maven, Node.js hay MySQL trên máy để chạy theo cách này.
Lần đầu cần Internet để tải image, thư viện Maven và npm.

### 1. Tạo file cấu hình môi trường

Mở PowerShell tại thư mục gốc `Airline-Booking-System` chứa `compose.yaml`.
Tạo hai file `.env` từ file mẫu nếu chưa có; các lệnh dưới đây giữ nguyên file đã tồn tại:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
if (-not (Test-Path airline-booking-frontend/.env)) {
    Copy-Item airline-booking-frontend/.env.example airline-booking-frontend/.env
}
```

File **`.env` ở thư mục gốc** được Docker Compose đọc để cấu hình cổng,
database và backend. Các giá trị cho môi trường local:

```dotenv
APP_PORT=8080
FRONTEND_PORT=3000
DB_PASSWORD=airline_local
DB_ROOT_PASSWORD=root_local
JWT_SECRET=replace-with-a-random-secret-at-least-32-bytes-long
JWT_EXPIRATION_SECONDS=3600
REFRESH_TOKEN_EXPIRY_DAYS=30
AUTH_COOKIE_SECURE=false
FRONTEND_ORIGIN=http://localhost:3000
OAUTH2_FRONTEND_REDIRECT_URI=http://localhost:3000/auth/callback
```

Thay `JWT_SECRET` bằng khóa ngẫu nhiên trước khi chạy. Có thể tạo khóa bằng
PowerShell, rồi chép kết quả vào dòng `JWT_SECRET=` trong `.env`:

```powershell
$jwtBytes = New-Object byte[] 32
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $jwtRng.GetBytes($jwtBytes) } finally { $jwtRng.Dispose() }
[Convert]::ToBase64String($jwtBytes)
```

Compose yêu cầu `JWT_SECRET` có giá trị; backend yêu cầu khóa ít nhất 32 byte.
Các biến Google/Facebook trong file mẫu chỉ cần thay bằng thông tin thật khi sử dụng OAuth.
Giữ `AUTH_COOKIE_SECURE=false` khi chạy local qua HTTP.

File **`airline-booking-frontend/.env`** được Vite đọc khi build frontend:

```dotenv
VITE_API_BASE_URL=/api/v1
```

Giữ URL tương đối này khi chạy Compose: trình duyệt gọi
`http://localhost:3000/api/v1/...`, Nginx chuyển tiếp sang `http://app:8080/api/v1/...`
trong mạng Docker. Tên `app` chỉ dùng bên trong Docker, không dùng làm URL API
trong trình duyệt. Các biến `VITE_*` được đóng gói vào JavaScript công khai,
không đặt JWT secret, mật khẩu database hoặc client secret trong file frontend.

Hai file `.env` có vai trò riêng: thêm `VITE_API_BASE_URL` vào `.env` gốc không
tự truyền biến đó vào bước build frontend. Sửa biến Vite cần build lại image frontend;
chỉ restart container không cập nhật nội dung đã build. Không commit file `.env`.

### 2. Build và khởi động

Chạy từ thư mục gốc:

```powershell
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

Compose khởi động các dịch vụ:

- `db`: MySQL 8.4; Flyway tự chạy migration trong `src/main/resources/db/migration` khi backend khởi động.
- `kafka`: Kafka dùng cho producer/consumer của backend.
- `app`: build bằng Java 21 và Maven, chờ MySQL healthy rồi chạy Spring Boot ở cổng nội bộ 8080.
- `frontend`: build React/Vite bằng Node.js 22 rồi phục vụ file tĩnh bằng Nginx ở cổng nội bộ 80.
- `kafka-ui`: giao diện xem Kafka.

Frontend phụ thuộc vào việc container `app` đã bắt đầu chạy, chưa đợi backend
sẵn sàng nhận request. Nếu API báo 502 ngay sau khi khởi động, xem log backend
và đợi quá trình khởi động hoàn tất.

### 3. Truy cập ứng dụng

- Frontend: [http://localhost:3000](http://localhost:3000).
- Backend/Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).
- Kafka UI: [http://localhost:8081](http://localhost:8081).

Trang `/` của backend tại cổng 8080 có thể trả 404; mở cổng 3000 để xem frontend.
MySQL không mở cổng ra máy host, chỉ được truy cập trong mạng Compose.

Nếu cổng 3000 đã được dùng, ví dụ đổi `FRONTEND_PORT=3001`, đồng thời cập nhật
`FRONTEND_ORIGIN=http://localhost:3001` và
`OAUTH2_FRONTEND_REDIRECT_URI=http://localhost:3001/auth/callback` trong `.env` gốc,
rồi chạy lại `docker compose up -d`. Khi đó mở frontend tại cổng 3001.
Nếu đổi `APP_PORT`, dùng cổng mới khi truy cập trực tiếp backend/Swagger;
Nginx vẫn gọi `app:8080`, không cần đổi `VITE_API_BASE_URL`.
Cổng 8081 đang dành cho Kafka UI, nên không đặt `APP_PORT=8081` nếu chưa đổi
mapping cổng của `kafka-ui` trong `compose.yaml`.

## Swagger UI

Sau khi ứng dụng khởi động, mở [Swagger UI](http://localhost:8080/swagger-ui.html).
Tài liệu [OpenAPI JSON](http://localhost:8080/v3/api-docs) cũng được phục vụ bởi backend.
Nếu đổi `APP_PORT`, thay cổng trong các địa chỉ tương ứng.

Trong nhóm **Kafka Test**, chọn `POST /api/kafka/test`, bấm **Try it out**,
nhập body rồi bấm **Execute**:

```json
{"message": "Hello Kafka!"}
```

API trả `202 Accepted` khi đã gọi producer gửi bất đồng bộ; kiểm tra log
producer/consumer để xác nhận gửi và nhận thành công. Message trống trả `400`.

Sau khi thêm hoặc cập nhật Swagger, build lại bằng `docker compose up --build -d app`.

## Quản lý container

Xem trạng thái và log:

```powershell
docker compose ps
docker compose logs -f app
```

Xem log frontend hoặc các dịch vụ còn lại:

```powershell
docker compose logs -f frontend
docker compose logs -f db kafka kafka-ui
```

Sau khi sửa frontend hoặc `airline-booking-frontend/.env`, build lại frontend:

```powershell
docker compose up --build -d --no-deps frontend
```

Sau khi sửa Java, build lại backend:

```powershell
docker compose up --build -d --no-deps app
```

Các lệnh `--no-deps` dùng khi những dịch vụ phụ thuộc đã chạy. Để build và cập nhật
toàn bộ hệ thống, dùng `docker compose up --build -d` tại thư mục gốc.

Dừng các container (giữ dữ liệu MySQL):

```powershell
docker compose down
```

`docker compose down` cũng giữ dữ liệu Kafka. Dockerfile backend đóng gói với
`-DskipTests` vì các bài test tích hợp cần database; Dockerfile frontend chạy
`npm run build` để kiểm tra TypeScript và tạo bản build Vite, không chạy `npm test`.

### Lưu dữ liệu và xử lý lỗi cấu hình

MySQL lưu dữ liệu trong volume `mysql_data`. Các biến khởi tạo database
và mật khẩu chỉ có tác dụng khi volume còn trống; sửa `.env` không đổi
mật khẩu của database đã được khởi tạo. Nếu backend báo lỗi đăng nhập MySQL
sau khi đổi `DB_PASSWORD`, cần dùng lại mật khẩu hiện tại hoặc chủ động đổi
mật khẩu user trong database cho khớp.

Trong Docker, ứng dụng kết nối MySQL qua tên dịch vụ `db`, không phải
`localhost`. Compose truyền cấu hình này qua biến môi trường để ghi đè
`application.yml`. Nếu Compose báo thiếu `JWT_SECRET`, kiểm tra file `.env`
gốc và bảo đảm đang chạy lệnh tại thư mục chứa `compose.yaml`.

## Authentication và User Management

Flyway tạo role `CUSTOMER`, `ADMIN`, `STAFF` và các bảng refresh token/provider.
Tài khoản đăng ký mới nhận `CUSTOMER`. Tạo tài khoản quản trị ban đầu và cấp role
`ADMIN` trực tiếp qua quy trình quản trị database của môi trường; API user quản trị
đều yêu cầu JWT có role `ADMIN`.

Login trả Access Token trong JSON và đặt refresh token đã lưu dạng hash vào cookie
HttpOnly `refresh_token`. Login và endpoint đổi mã OAuth cũng đặt cookie CSRF
`XSRF-TOKEN`; các request refresh/logout gửi kèm header `X-XSRF-TOKEN` và cookie
credentials. CORS chỉ cho phép origin cấu hình trong `FRONTEND_ORIGIN`. OAuth được bắt đầu tại
`/api/v1/auth/oauth2/google` hoặc `/api/v1/auth/oauth2/facebook`; đăng ký callback URL
cho provider là `http://localhost:8080/login/oauth2/code/google` hoặc
`http://localhost:8080/login/oauth2/code/facebook`. Sau callback, frontend nhận mã
dùng một lần tại `OAUTH2_FRONTEND_REDIRECT_URI` và đổi mã qua
`POST /api/v1/auth/oauth2/exchange`.

Để bật đăng nhập provider thật, đặt `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`,
`FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET` trong `.env`. Giá trị mặc định chỉ
giúp ứng dụng khởi động local, không xác thực được với Google/Facebook.

Nginx frontend hiện chỉ proxy `/api/`. Để chạy trọn luồng OAuth bắt đầu từ
frontend, cần bổ sung proxy `/oauth2/` và `/login/oauth2/` về backend, đồng thời
chốt callback URL và forwarded headers tương ứng. Cấu hình hiện tại chưa bao
gồm các proxy OAuth này.
