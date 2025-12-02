# Mini Social — Hướng dẫn chạy dự án (BE + FE)

Hướng dẫn ngắn để chạy backend (Spring Boot) và frontend (React + Vite) trong workspace này.

## Yêu cầu trước
- JDK 17 (backend)  
- Node.js (>=16) + npm (frontend)  
- Docker (tuỳ chọn, để chạy MySQL local nhanh)  
- Maven (hoặc dùng Maven Wrapper có sẵn)

## Backend (social-be)

Files tham chiếu:
- Cấu hình: [social-be/src/main/resources/application.yml](social-be/src/main/resources/application.yml)  
- Entrypoint: [`com.example.social.SocialApplication`](social-be/src/main/java/com/example/social/SocialApplication.java)  
- Maven: [social-be/pom.xml](social-be/pom.xml)  
- Wrapper scripts: [social-be/mvnw](social-be/mvnw) và [social-be/mvnw.cmd](social-be/mvnw.cmd)

1. Chuẩn bị database (MySQL)
- Tạo database `social` và user hoặc dùng Docker nhanh:
```sh
docker run --name social-db -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=social -p 3306:3306 -d mysql:8.0
```
- Mặc định cấu hình DB đang nằm ở [social-be/src/main/resources/application.yml](social-be/src/main/resources/application.yml):
  - url: `jdbc:mysql://localhost:3306/social`
  - username: `root`
  - password: `123456`
- Bạn có thể chỉnh trực tiếp file trên hoặc override bằng biến môi trường:
  - SPRING_DATASOURCE_URL
  - SPRING_DATASOURCE_USERNAME
  - SPRING_DATASOURCE_PASSWORD

2. Chế độ tạo bảng
- Ứng dụng dùng Spring Data JPA và `spring.jpa.hibernate.ddl-auto: update` nên sẽ tự tạo/ cập nhật schema khi chạy. (xem [application.yml](social-be/src/main/resources/application.yml))

3. Chạy backend
- Linux / macOS:
```sh
cd social-be
./mvnw spring-boot:run
```
- Windows:
```ps1
cd social-be
.\mvnw.cmd spring-boot:run
```
- Hoặc build jar rồi chạy:
```sh
cd social-be
./mvnw -DskipTests package
java -jar target/social-0.0.1-SNAPSHOT.jar
```
- Server mặc định lắng nghe ở port `9090` (theo [application.yml](social-be/src/main/resources/application.yml)).

4. Kiểm tra kết nối DB khi lỗi
- Kiểm tra logs Spring Boot để xem chi tiết lỗi kết nối (host, port, cred).
- Đảm bảo MySQL đang chạy và có thể truy cập từ máy dev.
- Kiểm tra biến môi trường nếu bạn override cấu hình.

## Frontend (social-fe)

Files tham chiếu:
- Package & scripts: [social-fe/package.json](social-fe/package.json)  
- Vite config: [social-fe/vite.config.js](social-fe/vite.config.js)  
- Entry: [social-fe/src/main.jsx](social-fe/src/main.jsx)  
- HTML: [social-fe/index.html](social-fe/index.html)

1. Cài dependency và chạy dev
```sh
cd social-fe
npm install
npm run dev
```
- Vite thường chạy ở `http://localhost:5173` (kiểm tra output terminal).

2. Build & preview
```sh
npm run build
npm run preview
```

3. Ghi chú
- Frontend dùng Tailwind (cấu hình tại [social-fe/tailwind.config.js](social-fe/tailwind.config.js)) và React + Vite.
- Nếu cần gọi API backend, sửa base URL trong code frontend để trỏ tới `http://localhost:9090` (hoặc proxy tuỳ cấu hình).

## Troubleshooting nhanh
- Lỗi kết nối DB: kiểm tra MySQL đang chạy, thông tin trong [social-be/src/main/resources/application.yml](social-be/src/main/resources/application.yml) hoặc các biến môi trường đã set đúng.
- Port conflict: thay port backend trong `application.yml` hoặc frontend Vite port (cấu hình Vite nếu cần).
- Lỗi build backend liên quan Lombok: đảm bảo IDE và build tool hỗ trợ Lombok (dependency đã có trong [social-be/pom.xml](social-be/pom.xml)).

Nếu cần thêm hướng dẫn deploy (Docker / docker-compose) hoặc cấu hình proxy cho frontend, báo để cung cấp file mẫu.

---
## Commit convention

Sử dụng chuẩn Conventional Commits để commit rõ ràng, dễ đọc và dễ tạo changelog.

Format:
type(scope?): subject
[BLANK LINE]
body (tuỳ chọn)
[BLANK LINE]
footer (tuỳ chọn, ví dụ: BREAKING CHANGE: ... hoặc closes #123)

Common types:
- feat: thêm tính năng
- fix: sửa lỗi
- docs: tài liệu
- style: format/code style không ảnh hưởng logic
- refactor: refactor code (không thêm tính năng, không sửa lỗi)
- perf: tối ưu hiệu năng
- test: thêm/sửa test
- chore: công việc không ảnh hưởng src (build, config)
- ci: thay đổi cấu hình CI

Ví dụ:
- feat(auth): add JWT refresh token
- fix(user): handle null pointer in UserController
- docs: update README for DB setup
- chore: bump maven wrapper

Ghi chú:
- Viết subject ở dạng câu lệnh (imperative), tối đa ~72 ký tự.
- Nếu cần mô tả chi tiết, thêm body.
- Đóng issue sử dụng footer: "closes #<issue>".

## Branching (quy ước tạo nhánh)

Branch chính:
- main: mã production luôn ổn định
- develop: tích hợp feature, chuẩn bị release

Branch tạm thời:
- feature/<your_name>/short-desc
  - Ví dụ: feature/khank/login
- fix/<your_name>-short-desc
  - Ví dụ: fix/khank/null-user
- hotfix/<your_name>/short-desc
  - Dùng khi sửa gấp trên main

Quy trình cơ bản:
1. Tạo branch từ develop (hoặc từ main cho hotfix):
2. Làm việc, commit theo convention ở trên.
3. Push và tạo Pull Request vào develop (hoặc main cho hotfix/release).
4. PR phải có mô tả, liên kết issue (nếu có) và review trước khi merge.
5. Sau merge feature vào develop, xóa branch remote khi xong:
   git push origin --delete feature/123-short-desc


Kết thúc.