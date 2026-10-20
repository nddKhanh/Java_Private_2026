# Bài Tập Sau Buổi 12 — Capstone

## Mục Tiêu

Mỗi nhóm chọn một bài toán quản lý phù hợp, ví dụ cửa hàng, thư viện, khóa học, phòng khám nhỏ hoặc đặt lịch. `HitShop` là đề tài tham khảo có thể tiếp tục từ buổi 11, không phải đề tài bắt buộc cho mọi nhóm.

Sản phẩm chạy bằng JDK 17, lưu dữ liệu trong MySQL và chứng minh được toàn bộ chuỗi kiến thức của khóa học: OOP, JDBC, thiết kế nhiều bảng, phân tầng ứng dụng, xử lý lỗi, transaction và Git collaboration.

## 1. Đăng Ký Phạm Vi

Trước khi code, nhóm nộp một trang mô tả:

- Tên đề tài và vấn đề cần giải quyết.
- Người dùng chính và ba chức năng họ cần nhất.
- Danh sách entity, quan hệ giữa các entity và ERD dự kiến.
- Một luồng nghiệp vụ cốt lõi ghi vào ít nhất hai bảng liên quan.
- Phân công thành viên và danh sách feature branch dự kiến.
- Giao diện chọn một trong hai: console hoàn chỉnh hoặc Swing. Không bắt buộc làm cả hai.

Mentor chốt phạm vi theo tiêu chí “demo được, dữ liệu đúng, trách nhiệm rõ”. Nhóm không cộng thêm chức năng khi luồng lõi chưa hoàn thành.

## 2. Yêu Cầu Kỹ Thuật Bắt Buộc

### OOP Và Mô Hình Dữ Liệu

- Có ít nhất ba entity nghiệp vụ, field được đóng gói và quan hệ object rõ ràng.
- Database có ít nhất ba bảng nghiệp vụ và ít nhất một quan hệ một-nhiều bằng foreign key.
- Có quy tắc kiểm tra dữ liệu ở Service, ví dụ số lượng dương, lịch không trùng hoặc trạng thái chuyển hợp lệ.
- Giá trị lịch sử cần giữ nguyên phải được snapshot đúng lúc. Ví dụ đơn hàng giữ `unit_price` thay vì luôn đọc giá Product hiện tại.

### JDBC Và Chức Năng

- Có CRUD đầy đủ cho ít nhất một entity chính.
- Có tạo và xem chi tiết cho luồng nghiệp vụ nhiều bảng.
- Có ít nhất một truy vấn JOIN và một báo cáo dùng `GROUP BY` hoặc phép tổng hợp tương đương.
- SQL nhận input phải dùng `PreparedStatement`; tài nguyên được đóng bằng try-with-resources.
- Dữ liệu vẫn còn sau khi đóng và chạy lại ứng dụng.

### Transaction

Luồng nghiệp vụ ghi nhiều bảng phải chạy trong một transaction. Tất cả bước cùng commit hoặc cùng rollback. Nhóm phải demo một ca cố ý làm bước giữa thất bại và chứng minh database không còn dữ liệu dở dang.

Ví dụ HitShop: insert Order, insert OrderItem và giảm Product.stock cùng transaction. Hủy đơn và hoàn stock cũng phải là một transaction và không được hoàn kho hai lần.

### Kiến Trúc

```text
View → Controller → Service → Repository interface → JDBC implementation → MySQL
```

- View không chứa SQL hoặc quy tắc nghiệp vụ.
- Controller chỉ điều phối request/response.
- Service kiểm tra và điều phối nghiệp vụ, phụ thuộc repository interface.
- JDBC implementation chịu trách nhiệm SQL và mapping.
- Một composition root tạo và nối các dependency.

### Git Và Tài Liệu

- Repository có `main`, `develop`, `feature/<ten-chuc-nang>`; thay đổi chính được đưa qua pull request.
- Mỗi thành viên có commit và ít nhất một lần review phần của người khác.
- README ghi JDK 17, MySQL, cách cấu hình không lộ credential, cách tạo schema/seed, cách chạy, tài khoản hoặc dữ liệu demo và luồng demo.
- Không commit mật khẩu thật, thư mục build hoặc cấu hình IDE cá nhân không cần thiết.

## 3. Bộ Nghiệm Thu Chung

Nhóm tự chuẩn bị một script reset/seed có thể chạy lặp lại. Buổi demo thực hiện theo thứ tự:

1. Reset database và khởi động ứng dụng.
2. Liệt kê dữ liệu seed.
3. Thực hiện CRUD entity chính, gồm cả một input sai bị từ chối.
4. Tạo một giao dịch nhiều bảng hợp lệ và xem chi tiết.
5. Mở báo cáo JOIN/aggregate và đối chiếu bằng phép tính tay.
6. Chạy ca lỗi transaction và chứng minh rollback.
7. Đóng/mở lại ứng dụng để chứng minh dữ liệu bền vững.
8. Trình bày một pull request, một nhận xét review và một lỗi nhóm đã sửa.

Mỗi nhóm phải ghi trước dữ liệu đầu vào và kết quả mong đợi của bảy bước đầu trong README; không dùng dữ liệu ngẫu nhiên hoặc thời gian hệ thống nếu điều đó làm kết quả khó lặp lại.

## 4. Đề Tài Tham Khảo: HitShop

Nhóm tiếp tục HitShop có thể giữ hợp đồng từ buổi 11:

- `products`, `customers`, `orders`, `order_items`.
- Product P01/P02/P03, Customer C01/C02.
- Order dùng id `BIGINT AUTO_INCREMENT`; OrderItem giữ `unit_price`.
- Luồng transaction: tạo order + items + giảm stock; hủy order PENDING + hoàn stock.
- Báo cáo doanh thu theo ngày và khách hàng.
- Giao diện console hoặc ProductWindow/Swing mở rộng.

Đây là bộ khung tham khảo. Nhóm chọn đề tài khác được quyền đặt tên class, bảng và menu theo miền bài toán của mình miễn đáp ứng các yêu cầu chung.

## 5. Hồ Sơ Nộp

- URL repository và danh sách pull request.
- Source code, `schema.sql`, `seed.sql` và README.
- ERD và sơ đồ ngắn luồng View → Controller → Service → Repository.
- Bảng test case gồm input, kết quả mong đợi và kết quả thực tế.
- Slide hoặc tài liệu demo tối đa 8 trang nếu nhóm cần; không bắt buộc slide.

## 6. Rubric 100 Điểm

| Nhóm tiêu chí | Điểm | Điều kiện chính |
|---|---:|---|
| Chức năng và dữ liệu | 30 | CRUD, luồng nhiều bảng, xem chi tiết, báo cáo đúng |
| OOP và kiến trúc | 20 | Model/quan hệ hợp lý, phân tầng và dependency rõ |
| JDBC và transaction | 20 | SQL tham số, đóng tài nguyên, commit/rollback đúng |
| Kiểm thử và xử lý lỗi | 10 | Test case lặp lại được, input sai không phá dữ liệu |
| Git collaboration | 10 | Branch, PR, review và lịch sử commit có ý nghĩa |
| README và demo | 10 | Người khác setup/chạy được, trình bày đúng trọng tâm |

Các phần như tìm kiếm nâng cao, phân trang, batch, export, biểu đồ hoặc giao diện đẹp được tính điểm cộng trong phạm vi từng tiêu chí sau khi yêu cầu lõi đã hoạt động; không bù cho transaction hoặc dữ liệu sai.
