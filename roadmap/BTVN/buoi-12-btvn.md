# Bài Tập Sau Buổi 12 — Capstone

## Bài Bắt Buộc — Ứng Dụng Quản Lý Cửa Hàng

### Kế Thừa Từ Buổi 11

Hoàn thiện repository `HitShop` đã được mở rộng từ buổi 1 đến buổi 11. Giữ tên model, repository, service và method đã chốt; phần capstone nối chúng thành một ứng dụng hoàn chỉnh. Swing/ProductWindow là giao diện tùy chọn, giao diện console là luồng demo bắt buộc.

### Mục Tiêu Sản Phẩm

Hoàn thiện ứng dụng `HitShop` quản lý cửa hàng nhỏ có dữ liệu bền vững trong MySQL. Tất cả nhóm dùng cùng đề tài, chức năng và dữ liệu chuẩn để kết quả có thể đối chiếu thống nhất. Tiếp tục repository code đã tích lũy từ các buổi trước.

### Nhóm Và Repository

- Mỗi nhóm phân công đủ ba trách nhiệm: dữ liệu/JDBC, nghiệp vụ/model, giao diện console/tài liệu; một người có thể kiêm nhiệm nếu quy mô nhóm khác. Thành viên cùng review phần của nhau.
- Repository có nhánh `main`, `develop` và nhánh `feature/<ten-chuc-nang>`; chức năng được tích hợp qua pull request vào `develop`.
- README ghi rõ JDK, MySQL, cách cấu hình kết nối không lộ mật khẩu, schema cần tạo, cách chạy, menu và dữ liệu mẫu.
- Không commit credential hoặc file cục bộ có mật khẩu thật.

### Mô Hình Dữ Liệu Bắt Buộc

- `products(id, name, price, stock)`; `id` là khóa chính.
- `customers(id, name, phone)`; `id` là khóa chính.
- `orders(id, customer_id, status, created_at)`; `id` tự tăng, `customer_id` là khóa ngoại đến customers.
- `order_items(id, order_id, product_id, quantity, unit_price)`; `id` tự tăng; khóa ngoại lần lượt đến orders và products.
- Kiểu cột cố định: products `id VARCHAR(20)`, `name VARCHAR(150)`, `price DECIMAL(12,2)`, `stock INT`; customers `id VARCHAR(20)`, `name VARCHAR(100)`, `phone VARCHAR(20)`; orders `id BIGINT AUTO_INCREMENT`, `customer_id VARCHAR(20)`, `status VARCHAR(20)`, `created_at DATETIME`; order_items `id BIGINT AUTO_INCREMENT`, `order_id BIGINT`, `product_id VARCHAR(20)`, `quantity INT`, `unit_price DECIMAL(12,2)`. Các trường dữ liệu bắt buộc đều NOT NULL.
- Một Customer có nhiều Order; một Order có ít nhất một OrderItem; mỗi OrderItem gắn với một Product.
- `quantity > 0`, `price > 0`, `stock >= 0`; `unit_price` giữ giá lúc đặt hàng. Tiền trong database dùng `DECIMAL(12,2)`.

### Class Và Trách Nhiệm Bắt Buộc

- Model: `Product`, `Customer`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`.
- Field cốt lõi: Product `(id, name, price, stock)`; Customer `(id, name, phone)`; CartItem `(product, quantity)`; Order `(id, customer, status, createdAt, items)`; OrderItem `(id, product, quantity, unitPrice)`.
- Repository: `ProductRepository`, `CustomerRepository`, `OrderRepository`, `OrderItemRepository` cùng các triển khai JDBC tương ứng.
- Các chữ ký Product/Order repository, service và kiểu kết quả báo cáo giống hợp đồng đã chốt ở BTVN buổi 8–10; không đổi tên/kiểu trả về.
- Service: `ProductService`, `OrderService`; nghiệp vụ không đặt trong View và SQL không đặt trong View/Controller.
- View/Controller là ứng dụng console; menu gọi Controller, Controller gọi Service, Service gọi repository interface.
- Menu có các mục: `1` xem sản phẩm, `2` thêm sản phẩm, `3` cập nhật sản phẩm, `4` xóa sản phẩm, `5` thêm khách hàng, `6` xem khách hàng, `7` tạo đơn, `8` xem đơn theo mã, `9` xem báo cáo doanh thu, `0` thoát. Giá trị khác báo lựa chọn không hợp lệ và tiếp tục.
- Thời điểm tạo đơn demo cố định là `2026-01-15 10:00`; không lấy thời gian hệ thống cho bộ kiểm tra chuẩn.
- Tên class chứa `main` là `HitShop`.
- `ProductService` có `addProduct(Product): boolean`, `findAllProducts(): List<Product>`, `findProductById(String): Optional<Product>`, `updateProduct(Product): boolean`, `deleteProduct(String): boolean`. `OrderService` có `createOrder(String, List<OrderItem>, LocalDateTime): long`, `findOrderById(long): Optional<Order>`, `findRevenueByDay(): List<DailyRevenue>`, `findRevenueByCustomer(): List<CustomerRevenue>`. `Order.getTotal()` là tổng các OrderItem.

### Chức Năng Và Kết Quả Bắt Buộc

1. **Quản lý sản phẩm:** thêm, xem danh sách, tìm theo id, cập nhật tên/giá/tồn kho, xóa. Mã trùng bị từ chối; mã không tồn tại báo rõ và không đổi dữ liệu.
2. **Quản lý khách hàng:** thêm khách và xem danh sách theo mã tăng dần.
3. **Tạo đơn:** chọn một Customer và từ 1 đến 10 dòng sản phẩm; số lượng mỗi dòng là số nguyên dương và không vượt tồn kho. Tổng tiền tính theo giá tại thời điểm đặt. Khi tạo đơn thành công, stock giảm đúng quantity.
4. **Xem đơn:** tìm theo id; in Customer, thời gian, trạng thái, từng sản phẩm, quantity, unit price, thành tiền từng dòng và tổng đơn.
5. **Doanh thu:** báo cáo tổng doanh thu theo ngày và theo khách hàng, tính từ OrderItem với `quantity × unit_price`.
6. **Trạng thái:** đơn mới bắt đầu `PENDING`; chỉ cho phép chuyển sang `CONFIRMED` hoặc `CANCELLED`. Khi hủy một đơn PENDING, số lượng đã giữ được trả về stock; trạng thái khác hoặc chuyển lặp không làm đổi dữ liệu.
7. **Dữ liệu tồn tại:** đóng rồi chạy lại ứng dụng không làm mất Products, Customers, Orders hoặc OrderItems đã lưu.

### Dữ Liệu Chuẩn Để Demo

- Products: `P01 / Áo thun / 150000 / stock 20`; `P02 / Bình nước / 50000 / stock 10`.
- Product bổ sung từ buổi 4: `P03 / Java cơ bản / 120000 / stock 5`.
- Customers: `C01 / Linh Nguyễn / 0900000001`; `C02 / Minh Trần / 0900000002`.
- Database khởi đầu có đúng ba Product và hai Customer trên, không có Order/OrderItem; ID tự tăng của Order bắt đầu từ 1.
- Demo đơn: C01 mua P01 × 2 và P02 × 1 vào `2026-01-15 10:00`, trạng thái ban đầu `PENDING`. Tổng `350000`; sau thành công P01 còn 18, P02 còn 9.
- Chuyển đơn demo sang `CONFIRMED`. Lưu lại dữ liệu rồi chạy chương trình lần nữa: đơn id 1, trạng thái, thời điểm, hai dòng hàng, tổng `350000` và tồn kho P01=18/P02=9 vẫn còn.

### Nộp Và Demo

- Nộp URL repository, README, schema/database script, dữ liệu khởi tạo và danh sách pull request.
- Demo một luồng: xem Products → tạo đơn theo dữ liệu chuẩn → xem chi tiết → xem báo cáo → chạy lại để xác nhận dữ liệu còn tồn tại.
- Script reset/seed chạy trước demo để đưa database về đúng dữ liệu ban đầu ở trên; không chạy lại script giữa các lần khởi động ứng dụng. Sau đơn demo, báo cáo ngày `2026-01-15` và khách C01 đều là `350000`.
- Trình bày cấu trúc class, quan hệ bảng, ranh giới View/Controller/Service/Repository, phân chia công việc và một lỗi đã xử lý.

## Phần Nâng Cao — Tự Chọn

Giao dịch tạo Order và toàn bộ OrderItem phải cùng thành công hoặc cùng rollback khi một bước ghi lỗi. Nếu bỏ qua phần này, các chức năng bắt buộc vẫn được chấm trên luồng dữ liệu hợp lệ khi database hoạt động.

## Tiêu Chí Hoàn Thành

- Chức năng và kết quả khớp dữ liệu/đặc tả ở trên.
- OOP có field được đóng gói, quan hệ object đúng; repository/service/view có trách nhiệm riêng.
- JDBC dùng tham số; lỗi input/database có thông báo; tài nguyên được đóng.
- Git có commit theo chức năng, PR và review; README đủ để người khác chạy theo cấu hình của mình.
- Có thể bỏ Swing và các phần nâng cao như transaction, phân trang, batch, Regex hoặc Thread; việc bỏ chúng không làm giảm các yêu cầu core của capstone.
