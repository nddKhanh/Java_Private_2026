# Bài Tập Về Nhà — Buổi 10

## Bài Bắt Buộc — Tổ Chức Ứng Dụng Theo MVC Và DIP

### Kế Thừa Từ Buổi 09

Tiếp tục đúng repository `HitShop`, database và các lớp model/repository/service của buổi 8–9. Giữ nguyên chữ ký các method CRUD Product và tạo/xem Order; lần này chỉ phân trách nhiệm giao diện, điều phối và nghiệp vụ theo MVC/DIP.

### Bài Toán

Sắp xếp lại ứng dụng cửa hàng đã có để mỗi phần có trách nhiệm rõ: giao diện, điều phối, nghiệp vụ, truy cập dữ liệu và model. Hành vi CRUD sản phẩm và xem/tạo đơn đã có phải tiếp tục hoạt động sau khi tổ chức lại.

### Cấu Trúc Và Trách Nhiệm Bắt Buộc

- Model: `Product`, `Customer`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`; tiếp tục giữ dữ liệu P01/P02/P03, C01/C02 và Order O01 theo các buổi trước. Chỉ giữ dữ liệu và hành vi nghiệp vụ tương ứng, không in menu hoặc chứa SQL. Field/quan hệ giữ đúng hợp đồng buổi 3, 4 và 9.
- View: `ConsoleView`; hiển thị menu/kết quả và nhận dữ liệu console, không gọi JDBC.
- Controller: `ShopController`; nhận lựa chọn từ View, gọi `ProductService` hoặc `OrderService`, đưa kết quả về View. Controller không chứa SQL.
- Service: `ProductService` và `OrderService`; áp dụng quy tắc nghiệp vụ và gọi repository interface. Không phụ thuộc class JDBC cụ thể.
- Hợp đồng: `ProductRepository`, `OrderRepository`, `OrderItemRepository` là interface; `JdbcProductRepository`, `JdbcOrderRepository`, `JdbcOrderItemRepository` là các triển khai JDBC tương ứng. `OrderService` phụ thuộc OrderRepository và OrderItemRepository qua interface.
- Khởi tạo nối implementation với service/controller ở một điểm tập trung trong class chạy chương trình. Không tạo kết nối database hoặc câu SQL bên trong View.
- Package cố định: `model`, `view`, `controller`, `service`, `repository`, `app`; mỗi class nằm trong package theo đúng trách nhiệm liệt kê trên. `HitShop` nằm trong `app`.
- Hợp đồng method: `ProductService.addProduct(Product): boolean`, `findProductById(String): Optional<Product>`, `findAllProducts(): List<Product>`, `updateProduct(Product): boolean`, `deleteProduct(String): boolean`; `OrderService.createOrder(String, List<OrderItem>, LocalDateTime): long`, `findOrderById(long): Optional<Order>`.
- `OrderRepository.insertOrder(String, OrderStatus, LocalDateTime): long`; `OrderItemRepository.insertAll(long, List<OrderItem>): void` và `findByOrderId(long): List<OrderItem>`; các method report giữ đúng chữ ký đã nêu ở buổi 9.
- `OrderService` còn có `findRevenueByDay(): List<DailyRevenue>` và `findRevenueByCustomer(): List<CustomerRevenue>` theo hợp đồng buổi 9. `HitShop` là class duy nhất chứa `main`.
- Hợp đồng giao diện: `ShopController.run(): void`, `handleChoice(int): void`; `ConsoleView.showMenu(): void`, `readChoice(): int`, `readProduct(): Product`, `readCustomerId(): String`, `readOrderItems(): List<OrderItem>`, `readCreatedAt(): LocalDateTime`, `showProducts(List<Product>): void`, `showOrder(Optional<Order>): void`, `showMessage(String): void`.

### Hành Vi Và Nghiệm Thu

- Product CRUD từ buổi 7 vẫn tạo, liệt kê, tìm, cập nhật, xóa đúng dữ liệu.
- Tạo/xem đơn từ buổi 9 vẫn trả đúng Customer, OrderItem và tổng tiền.
- Một đường gọi CRUD có dạng View → Controller → Service → Repository interface → JDBC implementation; kết quả đi ngược về View.
- Một repository thay thế không dùng JDBC có thể được truyền vào ProductService mà không sửa code nghiệp vụ Service.
- Repository thay thế tên `MemoryProductRepository`, implements ProductRepository, khởi tạo với P01/P02 chuẩn buổi 7 và thực hiện cùng hợp đồng ProductRepository.
- Hướng dẫn chạy nêu database cần có, cấu hình kết nối cần cung cấp và chức năng có thể quan sát được. Nộp kết quả kiểm tra thủ công bằng các bộ dữ liệu chuẩn buổi 7 và 9.

### Nộp Bài

Nộp source theo cấu trúc package cố định, README hướng dẫn chạy và output kiểm tra CRUD Product/tạo-xem Order.

## Bài Tự Chọn — Pattern

- `PaymentMethodFactory` nhận một trong hai mã `CASH`/`BANK` và trả CashPayment/BankTransferPayment tương ứng; mã khác không tạo đối tượng payment.
- Nếu có class Singleton cấu hình, instance duy nhất chỉ chứa cấu hình không bí mật hoặc đọc credential từ bên ngoài; không đưa mật khẩu thật vào source.

Phần pattern là cải tiến tổ chức; bỏ qua không ảnh hưởng CRUD, tạo đơn hay cấu trúc MVC bắt buộc.
