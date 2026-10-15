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

Ứng dụng được truy cập tại http://localhost:8080. Hiện project chưa có
controller/trang chủ, nên truy cập `/` có thể trả về 404 dù ứng dụng đã chạy.

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

Sửa `APP_PORT`, `DB_PASSWORD`, `DB_ROOT_PASSWORD` trong `.env`.
Ví dụ `APP_PORT=8081` thì truy cập http://localhost:8081.
File `.env` được bỏ qua trong Git và khi build image.

MySQL lưu dữ liệu trong volume `mysql_data`. Các biến khởi tạo database
và mật khẩu chỉ có tác dụng khi volume còn trống; sửa `.env` không đổi
mật khẩu của database đã được khởi tạo.

Trong Docker, ứng dụng kết nối MySQL qua tên dịch vụ `db`, không phải
`localhost`. Compose truyền cấu hình này qua biến môi trường để ghi đè
`application.properties`. MySQL chỉ được mở trong mạng nội bộ Compose.
