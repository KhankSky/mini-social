# Hướng dẫn chạy ứng dụng MiniSocial

## Bước 1: Khởi động Backend

Mở terminal và chạy lệnh:

```powershell
cd c:\workspace\mini-social\social-be
.\mvnw.cmd spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:9090`

> **Lưu ý**: Đảm bảo MySQL đang chạy với database `social` đã được tạo.

---

## Bước 2: Khởi động Frontend

Mở terminal mới và chạy lệnh:

```powershell
cd c:\workspace\mini-social\social-fe
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:5173`

---

## Bước 3: Truy cập ứng dụng

1. Mở trình duyệt và truy cập: `http://localhost:5173`
2. Bạn sẽ **tự động được chuyển** đến trang **Login** (vì chưa đăng nhập)

---

## Test Authentication Flow

### 1. Đăng ký tài khoản mới

1. Từ trang Login, click **"Create Account"**
2. Điền thông tin:
   - **Email**: `test@example.com`
   - **Username**: `testuser` (tùy chọn)
   - **Password**: `123456` (tối thiểu 6 ký tự)
3. Click **"Create Account"**
4. Nếu thành công → chuyển về trang Login

### 2. Đăng nhập

1. Điền thông tin:
   - **Email or Username**: `test@example.com`
   - **Password**: `123456`
2. Click **"Sign In"**
3. Nếu thành công → chuyển đến **Feed page** (trang chủ)

### 3. Kiểm tra Route Protection

1. Sau khi đăng nhập thành công, mở DevTools (F12)
2. Vào tab **Application** → **Local Storage**
3. Xem `social_app_token` đã được lưu
4. Refresh page → vẫn ở Feed page (không bị logout)
5. Xóa `social_app_token` và refresh → tự động redirect về Login

---

## Tính năng đã hoàn thành

### Frontend
✅ **Modern UI Design**
- Glassmorphism effects
- Gradient backgrounds
- Animated blob elements
- Smooth transitions
- Loading states
- Error animations

✅ **Route Protection**
- Tự động redirect về Login nếu chưa authenticated
- Token được lưu trong localStorage
- Auto-include token trong API requests

### Backend
✅ **User Management**
- Đăng ký với email, username, password
- Validation: email unique, username unique
- Password được hash (bcrypt)

✅ **Authentication**
- JWT token generation
- Login endpoint
- Protected endpoints
- LastLogin tracking

---

## Troubleshooting

### Backend không khởi động?
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra database `social` đã tạo chưa
- Kiểm tra credentials trong `application.yml`

### Frontend không kết nối được backend?
- Kiểm tra backend đang chạy tại port 9090
- Kiểm tra CORS đã được config đúng
- Xem console log để biết lỗi cụ thể

### Bị lỗi "User already exists"?
- Email hoặc username đã được sử dụng
- Đổi sang email/username khác

---

## API Endpoints

### Public (không cần token)
- `POST /api/users` - Đăng ký
- `POST /api/auth/login` - Đăng nhập

### Protected (cần token)
- `GET /api/auth/me` - Lấy thông tin user hiện tại
- `GET /api/users` - Danh sách users
- `PUT /api/users` - Cập nhật user

---

Chúc bạn test thành công! 🎉
