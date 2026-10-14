# Bài Tập Về Nhà — Buổi 08

## Bài Bắt Buộc — Tách Repository Và Service

### Kế Thừa Từ Buổi 07

Tiếp tục repository `HitShop`, schema `shop_db` và dữ liệu Product P01/P02/P03. Đưa các thao tác JDBC đã có vào `JdbcProductRepository` nhưng giữ tên và ý nghĩa `add`, `findById`, `findAll`, `update`, `deleteById`; menu của `HitShop` tiếp tục sử dụng cùng các chức năng.

### Bài Toán

Tổ chức lại phần JDBC Product từ buổi 7 thành các trách nhiệm model, repository, service và console. Bổ sung khả năng tra cứu Customer và chuẩn hóa trạng thái Order bằng enum.

### Thành Phần Và Hợp Đồng

- `ProductRepository`: chịu trách nhiệm JDBC với bảng `products`; có các thao tác thêm, tìm theo id, liệt kê, cập nhật và xóa. `findById(String id)` trả `Optional<Product>`; danh sách trả về theo thứ tự id tăng dần.
- `JdbcProductRepository`: là triển khai JDBC cụ thể của ProductRepository. Không chứa lời nhắc nhập/xuất console.
- `ProductService`: nhận một ProductRepository qua constructor; cung cấp các thao tác thêm/tìm/liệt kê/cập nhật/xóa cho giao diện console. Quy tắc bắt buộc: mã và tên không rỗng, giá > 0, stock >= 0; dữ liệu sai không được ghi xuống database.
- `Customer`: field private `id`, `name`, `phone`; id là khóa định danh. Bảng `customers` có các cột tương ứng: `id VARCHAR(20) PRIMARY KEY`, `name VARCHAR(100) NOT NULL`, `phone VARCHAR(20) NOT NULL`.
- Customer có constructor nhận đủ ba field, getter cho cả ba và không có setter cho id.
- Tạo bảng customers bằng đúng schema trên; không đổi cấu trúc bảng products buổi 7.
- `CustomerRepository` và `JdbcCustomerRepository`: cùng ranh giới trách nhiệm như ProductRepository, hỗ trợ thêm, tìm id và liệt kê Customer theo id tăng dần.
- Hợp đồng method ProductRepository: `boolean add(Product)`, `Optional<Product> findById(String)`, `List<Product> findAll()`, `boolean update(Product)`, `boolean deleteById(String)`. CustomerRepository: `boolean add(Customer)`, `Optional<Customer> findById(String)`, `List<Customer> findAll()`.
- ProductService có `addProduct(Product): boolean`, `findProductById(String): Optional<Product>`, `findAllProducts(): List<Product>`, `updateProduct(Product): boolean`, `deleteProduct(String): boolean`; method chuyển yêu cầu tới repository sau khi áp dụng quy tắc dữ liệu.
- ProductService có thêm `findAllProductSummaries(): List<ProductSummary>`; mỗi Product được chuyển thành record có cùng id/name/price và không có stock.
- Package cố định: `model` chứa model/enum/record; `repository` chứa interface và JDBC implementation; `service` chứa ProductService; `app` chứa `HitShop`.
- `HitShop` chịu trách nhiệm nhập/xuất console và gọi service/repository; không chứa SQL. File cấu hình kết nối ở ngoài source và không có credential thật.
- Menu `HitShop`: `1` thêm Product, `2` liệt kê Product, `3` tìm Product theo mã, `4` cập nhật Product, `5` xóa Product, `6` thêm Customer, `7` liệt kê Customer, `0` thoát. Lựa chọn khác báo `Lựa chọn không hợp lệ.`
- `OrderStatus` là enum có đúng các giá trị `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`. Field status của Order có kiểu OrderStatus, không còn kiểu String.
- `ProductSummary` là DTO dạng `record` gồm `String id`, `String name`, `double price`; giá trị summary không chứa stock.
- Console chỉ gọi ProductService/CustomerRepository; lớp console không chứa SQL. Repository không in thông báo cho người dùng.

### Dữ Liệu Chuẩn Và Kết Quả

Product chuẩn của buổi 7 tiếp tục giữ nguyên. Customer chuẩn: `C01 / Linh Nguyễn / 0900000001`, `C02 / Minh Trần / 0900000002`. Tra P99 trả trạng thái không có kết quả qua Optional; list Customer hiển thị C01 trước C02. Lần lượt gán OrderStatus phải chỉ nhận bốn giá trị nêu trên.

### Nộp Bài

Nộp source các interface, JDBC repository, services, model/enum/record và script tạo bảng customers; kèm output tra P99 và danh sách Customer chuẩn.

## Mở Rộng — Bài Tự Chọn

Phân loại `SQLException`, `IOException` là checked exception; `IllegalArgumentException` là unchecked exception. Nêu thông báo nghiệp vụ dễ hiểu cho người dùng nhưng giữ nguyên cause kỹ thuật trong log/nội bộ. Đây là cải tiến xử lý lỗi, không phải yêu cầu để CRUD cốt lõi hoạt động.
