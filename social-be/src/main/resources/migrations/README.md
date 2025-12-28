# Database Migrations

Thư mục này chứa các script migration SQL để cập nhật schema database.

## Cách sử dụng

### Chạy migration thủ công
```bash
mysql -u root -p123456 social < src/main/resources/migrations/V1__add_privacy_and_location_to_posts.sql
```

Hoặc trong MySQL console:
```sql
SOURCE src/main/resources/migrations/V1__add_privacy_and_location_to_posts.sql;
```

## Migration History
### V1 - Add privacy and location to posts
- **File**: `V1__add_privacy_and_location_to_posts.sql`
- **Mô tả**: Thêm cột `privacy` (VARCHAR(20), NOT NULL, DEFAULT 'PUBLIC') và `location` (VARCHAR(255), nullable) vào bảng `posts`
- **Ngày**: 2025-01-XX
- **Tính năng**: 
  - Privacy: Cho phép chọn PUBLIC hoặc FRIENDS
  - Location: Lưu địa điểm của bài viết



