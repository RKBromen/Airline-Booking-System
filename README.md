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
Không cần cài Java, Maven hay MySQL trên máy để chạy theo cách này.

Tại thư mục chứa `compose.yaml`, chạy:

```powershell
docker compose up --build -d
```

Docker sẽ build ứng dụng bằng Java 21 và Maven, khởi động MySQL 8.4,
chờ database sẵn sàng rồi chạy Spring Boot ở cổng 8080.
Flyway tự chạy migration trong `src/main/resources/db/migration`.
Lần đầu cần Internet để tải image và thư viện Maven.

Ứng dụng được truy cập tại http://localhost:8080. Project chưa có trang chủ,
nên truy cập `/` có thể trả về 404 dù ứng dụng đã chạy.

## Swagger UI

Sau khi ứng dụng khởi động, mở http://localhost:8080/swagger-ui.html.
Tài liệu OpenAPI JSON nằm tại http://localhost:8080/v3/api-docs.
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

Dừng các container (giữ dữ liệu MySQL):

```powershell
docker compose down
```

Sau khi thay đổi source, chạy lại `docker compose up --build -d`.
Dockerfile bỏ qua test khi đóng gói vì test Spring Boot hiện cần database.

Vì test hiện có dùng @SpringBootTest, nó khởi động Spring Boot và cần kết nối MySQL. Trong bước build image, MySQL chưa chạy nên test có thể thất bại.
Khi chạy docker compose up --build -d, thứ tự là:
1. Build image ứng dụng, bỏ qua test.
2. Khởi động MySQL và chờ sẵn sàng.
3. Chạy ứng dụng và kết nối MySQL.
## Cấu hình tùy chọn

Cấu hình mặc định phục vụ chạy local. Để đổi cổng hoặc mật khẩu,
tạo `.env` từ file mẫu trước lần chạy đầu:

```powershell
Copy-Item .env.example .env
```

Sửa `APP_PORT`, `DB_PASSWORD`, `DB_ROOT_PASSWORD` và `JWT_SECRET` trong `.env`.
`JWT_SECRET` phải có ít nhất 32 byte ngẫu nhiên; không dùng giá trị mẫu khi triển khai thật.
Ví dụ `APP_PORT=8081` thì truy cập http://localhost:8081.
File `.env` được bỏ qua trong Git và khi build image.

MySQL lưu dữ liệu trong volume `mysql_data`. Các biến khởi tạo database
và mật khẩu chỉ có tác dụng khi volume còn trống; sửa `.env` không đổi
mật khẩu của database đã được khởi tạo.

Trong Docker, ứng dụng kết nối MySQL qua tên dịch vụ `db`, không phải
`localhost`. Compose truyền cấu hình này qua biến môi trường để ghi đè
`application.properties`. MySQL chỉ được mở trong mạng nội bộ Compose.

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
