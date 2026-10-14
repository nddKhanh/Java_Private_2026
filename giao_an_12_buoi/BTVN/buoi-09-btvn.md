# Bài Tập Về Nhà — Buổi 09

## Bài Bắt Buộc — Đơn Hàng Nhiều Bảng

### Kế Thừa Từ Buổi 08

Tiếp tục `HitShop`, ProductRepository/JdbcProductRepository, ProductService, CustomerRepository và các model hiện có. Không đổi hợp đồng Product CRUD; thêm các bảng và repository đơn hàng để `HitShop` dùng cùng Product/Customer khi tạo Order.

### Bài Toán

Mở rộng database shop đã có thành mô hình đơn hàng gồm khách hàng, đơn và các dòng hàng. Có thể tạo đơn, xem chi tiết đơn và xem doanh thu theo ngày/khách hàng.

### Schema Và Quan Hệ Cố Định

- Giữ nguyên `products(id, name, price, stock)` và `customers(id, name, phone)` từ các buổi trước.
- `orders`: `id BIGINT` primary key tự tăng; `customer_id VARCHAR(20)` bắt buộc và là foreign key đến `customers.id`; `status VARCHAR(20)` bắt buộc; `created_at DATETIME` bắt buộc.
- `order_items`: `id BIGINT` primary key tự tăng; `order_id BIGINT` bắt buộc và foreign key đến `orders.id`; `product_id VARCHAR(20)` bắt buộc và foreign key đến `products.id`; `quantity INT` bắt buộc, lớn hơn 0; `unit_price DECIMAL(12,2)` bắt buộc, lớn hơn 0.
- Một Customer có thể có nhiều Order; mỗi Order thuộc đúng một Customer. Một Order có ít nhất một OrderItem; mỗi OrderItem tham chiếu đúng một Product.
- `unit_price` là giá tại thời điểm mua, không lấy thay đổi theo giá hiện tại của Product. Tổng dòng hàng bằng `quantity × unit_price`.
- Thời điểm ứng dụng dùng `LocalDateTime`; cột database tương ứng `DATETIME`.
- Lớp nghiệp vụ: `Order` chứa Customer và danh sách OrderItem; `OrderItem` chứa Product, quantity, unitPrice. `OrderService` nhận phối hợp repository; `OrderRepository` xử lý bảng orders, `OrderItemRepository` xử lý order_items.
- Field Order: `id` kiểu `long` do database sinh, `customer`, `status`, `createdAt`, `items` kiểu `List<OrderItem>`; field OrderItem: `id` kiểu `long` do database sinh, `product`, `quantity`, `unitPrice`. Constructor OrderItem nhận Product, quantity, unitPrice; getter cho các giá trị nghiệp vụ; `getLineTotal()` trả quantity × unitPrice. `Order.addItem(OrderItem)` thêm một dòng; `Order.getTotal()` trả tổng các dòng.
- Hợp đồng xử lý: `OrderService.createOrder(String customerId, List<OrderItem> items, LocalDateTime createdAt): long`; `OrderRepository.insertOrder(String customerId, OrderStatus status, LocalDateTime createdAt): long`; `OrderRepository.findById(long): Optional<Order>`; `OrderItemRepository.insertAll(long orderId, List<OrderItem> items): void`; `OrderItemRepository.findByOrderId(long): List<OrderItem>`; `OrderRepository.findRevenueByDay(): List<DailyRevenue>`; `OrderRepository.findRevenueByCustomer(): List<CustomerRevenue>`.
- `DailyRevenue` gồm `String day` theo dạng `yyyy-MM-dd`, `double total`; `CustomerRevenue` gồm `String customerId`, `String customerName`, `double total`. Báo cáo chỉ gồm ngày/khách có ít nhất một đơn.

### Hành Vi Bắt Buộc

- Tạo Order trước các OrderItem liên quan; OrderItem phải được gắn với id database của Order vừa tạo.
- Xem một Order theo id phải in mã đơn, thông tin khách, thời điểm, trạng thái, các dòng hàng và tổng đơn.
- Truy vấn chi tiết phải ghép đúng tên khách và tên sản phẩm; một đơn nhiều dòng hiển thị một dòng cho mỗi OrderItem.
- Báo cáo doanh thu theo ngày sắp xếp `day` tăng dần; báo cáo khách hàng sắp xếp `customerId` tăng dần. Mỗi báo cáo cộng tổng `quantity × unit_price`.
- Foreign key không cho tạo order với customer không tồn tại, hoặc order_item tham chiếu order/product không tồn tại.
- SQL có dữ liệu đầu vào phải dùng tham số; phần bắt buộc không cần phân trang hay batch.

### Dữ Liệu Chuẩn Và Kết Quả

- Customer: C01/Linh Nguyễn, C02/Minh Trần.
- Product trước khi tạo order: P01/Áo thun giá hiện tại 160000; P02/Bình nước giá 50000. Đây là trạng thái chuẩn đầu vào của bài; P01 được đổi từ giá cũ 150000 lên 160000 trước khi ghi order.
- Order id `1`: C01, `2026-01-15 10:00`, CONFIRMED; P01 × 2 với unit_price 150000 và P02 × 1 với unit_price 50000. Tổng đơn 1: `350000`.
- Order id `2`: C02, `2026-01-15 11:00`, CONFIRMED; P02 × 3 với unit_price 50000. Tổng đơn 2: `150000`.
- Database trống trước khi nạp hai order và id tự tăng bắt đầu từ 1. Báo cáo ngày `2026-01-15` có doanh thu `500000`; C01 `350000`, C02 `150000`. Đơn id 1 vẫn có đơn giá P01 `150000` dù giá hiện tại là `160000`.

### Nộp Bài

Nộp SQL schema, dữ liệu mẫu, source repository/service và output tạo đơn, chi tiết đơn, báo cáo theo ngày/khách hàng.

## Nâng Cao — Bài Tự Chọn

- Phân trang danh sách đơn bằng `LIMIT/OFFSET`; trang rỗng có kết quả rỗng, không lặp đơn giữa các trang.
- Giao dịch tạo một Order và toàn bộ OrderItem là tất cả-cùng-thành-công hoặc không lưu phần nào; khi cố ý làm lỗi một item thì không để lại Order thiếu chi tiết.
- Batch insert cho nhiều OrderItem cho cùng kết quả dữ liệu như insert riêng lẻ.
- Class `OrderItemRowMapper` có `OrderItem map(ResultSet row)`; cùng method ánh xạ được dùng cho truy vấn danh sách dòng hàng và truy vấn chi tiết.

Các phần nâng cao chỉ tối ưu hoặc tăng độ an toàn; bỏ qua không ảnh hưởng tạo đơn, xem chi tiết và báo cáo cơ bản.
