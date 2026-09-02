# Skilline — E-Learning Platform

Dự án e-learning full-stack gồm:
- **Backend**: Spring Boot 3.2.5, Java 21, MySQL, Redis, Liquibase
- **Frontend**: Vue 3 + TypeScript + Vite, Element Plus

---

# [Xem Report-Stored-XSS.pdf](Report-Stored-XSS.pdf)

## Mục lục

1. [Yêu cầu công cụ](#1-yêu-cầu-công-cụ)
2. [Clone & checkout nhánh](#2-clone--checkout-nhánh)
3. [Cấu hình Backend](#3-cấu-hình-backend-skilline-server)
4. [Cấu hình Frontend](#4-cấu-hình-frontend-skilline-client)
5. [Khởi tạo dữ liệu](#5-khởi-tạo-dữ-liệu)
6. [Chạy dự án](#6-chạy-dự-án)

---

## 1. Yêu cầu công cụ

Cài đặt đầy đủ các công cụ sau trước khi bắt đầu:

| Công cụ | Phiên bản | Ghi chú |
|---|---|---|
| **JDK** | 21+ | [Download Temurin 21](https://adoptium.net/) |
| **Maven** | 3.9+ | Đã có sẵn qua `mvnw`, không cần cài thêm |
| **Node.js** | 18+ (khuyến nghị 20 LTS) | [Download Node.js](https://nodejs.org/) |
| **npm** | 9+ | Đi kèm với Node.js |
| **MySQL** | 8.0+ | [Download MySQL](https://dev.mysql.com/downloads/) |
| **Redis** | 7+ | [Download Redis](https://redis.io/download/) |
| **FFmpeg** | Bất kỳ | Cần cho xử lý video HLS — [Download FFmpeg](https://ffmpeg.org/download.html) |
| **Git** | Bất kỳ | |

> **Lưu ý FFmpeg (Windows):** Sau khi cài, thêm đường dẫn thư mục `bin` của FFmpeg vào biến môi trường `PATH` để lệnh `ffmpeg` chạy được từ bất kỳ thư mục nào.

---

## 2. Clone & Checkout nhánh

```bash
# Clone repo fe
git clone https://github.com/Trandatdev89/Skilline-frontend.git
cd thư mục

# Xem danh sách các nhánh
git branch -a

# Checkout đúng  nhánh feature/xss
git checkout feature/xss

# Kéo code mới nhất
git pull origin feature/xss

# Clone repo be
git clone https://github.com/Trandatdev89/Skilline-backend.git
cd thư mục

# Xem danh sách các nhánh
git branch -a

# Checkout đúng  nhánh feature/xss
git checkout feature/xss-attack

# Kéo code mới nhất
git pull origin feature/xss-attack
```

## 3. Cấu hình Backend (`skilline-server`)

### 3.1 Chuẩn bị MySQL

Đảm bảo MySQL đang chạy và bạn có user `root` hoặc user khác với đủ quyền. Database sẽ được tạo tự động nhờ Liquibase, bạn chỉ cần tạo user/schema nếu muốn dùng tên khác.

```sql
-- Kết nối MySQL với quyền root rồi chạy lệnh sau (tuỳ chọn nếu muốn đổi tên DB)
CREATE DATABASE IF NOT EXISTS skiline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3.2 Chuẩn bị Redis

Đảm bảo Redis đang chạy trên cổng mặc định `6379`. Trên Windows có thể dùng Redis qua WSL2 hoặc [Memurai](https://www.memurai.com/).

```bash
# Kiểm tra Redis đang chạy
redis-cli ping
# Kết quả: PONG
```

### 3.3 Cấu hình `application.yml`

Mở file `skilline-server/src/main/resources/application.yml` và chỉnh sửa các giá trị phù hợp với môi trường local của bạn:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/skiline?createDatabaseIfNotExist=true
    username: root          # ← Đổi thành username MySQL của bạn
    password: your_password # ← Đổi thành password MySQL của bạn
  data:
    redis:
      host: localhost
      port: 6379
domain:
  server: http://localhost:8080/  # ← Đổi nếu be chạy cổng khác
  client: http://localhost:5173 
```

## 4. Cấu hình Frontend (`skilline-client`)

### 4.1 Tạo file `.env`

Tạo file `.env` tại root của `skilline-client` với nội dung sau:

```env
VITE_API_LOCAL=http://localhost:8080
```

> Đây là URL trỏ tới backend Spring Boot. Nếu backend chạy cổng khác thì thay đổi tương ứng.

---

## 5. Khởi tạo dữ liệu

### 5.1 Liquibase tự động migrate schema

Khi backend khởi động lần đầu, **Liquibase sẽ tự động tạo toàn bộ bảng và cấu trúc database** từ file `db.changelog/db.changelog-master.xml`. Bạn không cần chạy SQL thủ công.

Cấu hình trong `application.yml`:

```yaml
spring:
  liquibase:
    change-log: classpath:db.changelog/db.changelog-master.xml
    enabled: true
  jpa:
    hibernate:
      ddl-auto: none  # Để Liquibase quản lý schema, không để Hibernate tự tạo
```

### 5.2 Thêm dữ liệu mẫu (Seed data)

Sau khi backend khởi động thành công và schema đã được tạo, bạn có thể thêm dữ liệu mẫu bằng cách sử dụng luôn website

Mặc định khi start hệ thống thì đã có tài khoản role ADMIN với username:admin, password:admin.
Truy cập `http://localhost:8080` sau khi server chạy và gọi các API:

1. Cấu hình mail cho hệ thống qua tài khoản admin với `POST /api/config/save-template-mail`
{
   "subject":"Verify Account",
   "htmlContent":"<p>Welcome {{toName}} to website.<br/> You need verify account click link : <a>{{linkUrl}}</a> </p>",
   "type":"VERIFY_ACCOUNT",
   "language":"en",
   "active":true
}.
1. Đăng ký tài khoản học viên qua `POST /auth/register`
2. Đăng nhập qua `POST /auth/login`
3. Tạo category qua `POST /api/category`
4. Tạo course qua `POST /api/course/save`
5. 
---

## 6. Chạy dự án

### 6.1 Chạy Backend

```bash
cd skilline-server

# Dùng Maven Wrapper (không cần cài Maven)
./mvnw spring-boot:run          # Linux/macOS
mvnw.cmd spring-boot:run        # Windows

# Hoặc nếu đã cài Maven
mvn spring-boot:run
```

Server khởi động tại: `http://localhost:8080`

### 6.2 Chạy Frontend

```bash
cd skilline-client

# Cài dependencies (chỉ cần chạy lần đầu)
npm install

# Chạy development server
npm run dev
```

Frontend khởi động tại: `http://localhost:5173`

## Kiểm tra nhanh

Sau khi cả hai service đã chạy, mở trình duyệt và truy cập:

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080

Đăng ký tài khoản mới → đăng nhập → kiểm tra các tính năng: xem khoá học, mua khoá học, xem bài giảng video.

---

## Các vấn đề thường gặp

**Lỗi kết nối MySQL:** Kiểm tra MySQL đang chạy, đúng username/password trong `application.yml`.

**Lỗi Redis connection refused:** Đảm bảo Redis service đang chạy trên cổng 6379.

**FFmpeg not found:** Thêm đường dẫn FFmpeg vào `PATH` rồi restart terminal.

**CORS error ở frontend:** Kiểm tra `domain.client` trong `application.yml` khớp với địa chỉ frontend đang chạy.

**Liquibase migration fail:** Xoá database và tạo lại, để Liquibase chạy migration từ đầu.
