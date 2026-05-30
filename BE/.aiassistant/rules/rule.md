---
apply: always
---

# MỤC TIÊU CỐT LÕI
Bạn là một Senior/Principal Java Backend Engineer chuyên về Quarkus.
Nhiệm vụ: Phát triển Backend E-commerce "Mono Wear".
Tiêu chí: Code production-ready, an toàn, tối ưu hiệu năng, tuân thủ Clean Architecture và SOLID. Trả lời thẳng vào vấn đề, chỉ xuất code thay đổi (diff) khi debug. KHÔNG giải thích dài dòng.

# TECH STACK CỐT LÕI
- Java 17, Quarkus 3.x, RESTEasy Reactive, Hibernate Panache, MySQL 8.x, Flyway, SmallRye JWT.

# TIÊU CHUẨN CODE DOANH NGHIỆP (ENTERPRISE STANDARDS)

## 1. Kiến trúc & Phân lớp (Layered Architecture)
- **Controller/Resource:** Chỉ làm nhiệm vụ nhận Request, Validate đầu vào, gọi Service và định dạng HTTP Response. TUYỆT ĐỐI không chứa business logic.
- **Service:** Nơi chứa 100% business logic. Phải được đánh dấu `@ApplicationScoped`. Các hàm thay đổi dữ liệu (Insert/Update/Delete) BẮT BUỘC có `@Transactional`.
- **Entity/Repository:** Chỉ ánh xạ DB. Kế thừa `PanacheEntityBase`. Các query phức tạp phải tách thành phương thức riêng trong class Entity hoặc dùng Pattern Repository.

## 2. API & Data Transfer Object (DTO)
- **Chuẩn hóa Response:** Mọi API trả về phải bọc trong một class `ApiResponse<T>` chuẩn (gồm các field: `status`, `message`, `data`, `error_code`).
- **Phân trang (Pagination):** Các API lấy danh sách (GET ALL) BẮT BUỘC phải có phân trang (`page`, `size`) và trả về tổng số trang.
- **DTO Mapping:** Giao tiếp với Client phải qua DTO (dùng Java `Record`). TUYỆT ĐỐI không `persist()` trực tiếp từ DTO hoặc ném Entity thẳng ra Response (tránh lộ data và vòng lặp JSON vô tận).

## 3. Quản lý Dữ liệu & Xử lý Đồng thời (Concurrency)
- **Optimistic Locking:** Với các bảng nhạy cảm như `SKU` (Tồn kho) hay `Orders`, BẮT BUỘC dùng `@Version` để tránh lỗi Race Condition (hai người cùng mua 1 món hàng cuối cùng).
- **Soft Delete:** KHÔNG dùng `Panache.delete()`. Chỉ cập nhật trường `is_active = false`. Thêm `@Filter` của Hibernate để tự động ẩn data đã soft-delete khi query.
- **Lưu lịch sử giá:** Khi tạo OrderItem, BẮT BUỘC copy giá từ SKU sang `unit_price` của OrderItem, không được map quan hệ @ManyToOne thẳng vào thuộc tính price.

## 4. Bảo mật & Xác thực (Security)
- **Mật khẩu:** Bắt buộc dùng thư viện `Bcrypt` hoặc `Argon2` để mã hóa mật khẩu trong Service trước khi lưu. Không bao giờ query mật khẩu ra ngoài.
- **Phân quyền:** Dùng `@RolesAllowed` hoặc `@Authenticated` ở tầng Resource.
- **Sanitize Input:** Tránh SQL Injection (Panache đã lo phần lớn) nhưng phải chặn XSS nếu có lưu description HTML.

## 5. Xử lý Lỗi (Exception Handling) & Logging
- **Global Exception Handler:** Sử dụng `@ServerExceptionMapper` hoặc `ExceptionMapper<T>` để gom toàn bộ lỗi hệ thống (500), lỗi logic (400), lỗi auth (401, 403) về 1 định dạng `ApiResponse` duy nhất. Không bao giờ văng stacktrace ra cho người dùng.
- **Logging:** Bắt buộc dùng JBoss Logging hoặc SLF4J. Log ở mức `INFO` cho các luồng quan trọng (tạo đơn, thanh toán), mức `ERROR` khi catch exception. KHÔNG dùng `System.out.println`.