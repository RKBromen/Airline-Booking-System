# Airline Booking System

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
