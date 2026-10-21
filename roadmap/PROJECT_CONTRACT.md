# Hợp Đồng Xuyên Suốt Dự Án Thực Hành

Dự án bài tập mang tên `HitShop`; mỗi BTVN từ buổi 1 đến buổi 11 mở rộng kết quả của bài trước.

## Nền tảng dùng chung

- JDK 17.
- Mã nguồn lưu trong một repository Git từ buổi 1.
- Tiền dùng `double` trong giai đoạn nhập môn. Khi lưu MySQL, cột tiền dùng `DECIMAL(12,2)`; phần nâng cao có thể chuyển Java sang `BigDecimal`.
- Dữ liệu chuẩn: Product `P01 / Áo thun / 150000 / 20`, `P02 / Bình nước / 50000 / 10`; Customer `C01 / Linh Nguyễn / 0900000001`.
- Buổi 1–8 dùng mã đơn dạng chuỗi như `O01`. Từ buổi 9, khi `orders.id` do MySQL tự tăng, mã đơn trong Java chuyển sang `long`; dữ liệu kiểm thử dùng id `1`, `2`.

## Bàn giao theo từng buổi

| Buổi | Phần được bổ sung | Hợp đồng cần giữ lại |
|---|---|---|
| 1 | Chương trình tuần tự tính tiền một mặt hàng | `HitShop.java`, nhập tên/giá/số lượng, tính `price * quantity` |
| 2 | Menu, điều kiện, vòng lặp, method, ba danh sách song song | `addItem`, `removeItem`, `printItems`, `calculateTotal` |
| 3 | `Product`, `Customer`, `CartItem` | Giỏ là `List<CartItem>`; `CartItem` giữ Product và quantity |
| 4 | `Book extends Product`, `Order`, đa hình và trạng thái | Order giữ Customer và nhiều CartItem; `PENDING → CONFIRMED/CANCELLED` |
| 5 | `PaymentMethod`, `DeliveryMethod`, equality của Product | Nơi sử dụng phụ thuộc interface; Product bằng nhau theo id |
| 6 | Xử lý lỗi và lưu đơn ra `orders.txt` | File chỉ là bước học I/O; model Order không bị thay thế |
| 7 | Maven, MySQL, JDBC CRUD Product | Product chuyển sang lưu trong bảng `products`; câu SQL dùng tham số |
| 8 | Repository, Service, Optional, enum | Thêm luồng tạo/liệt kê/tìm Customer; `OrderStatus` thay chuỗi trạng thái; console gọi Service |
| 9 | Order/OrderItem nhiều bảng, JOIN và báo cáo | `orders.id` chuyển sang `long` tự tăng; `unit_price` giữ giá lúc mua |
| 10 | MVC, SOLID và refactor | View → Controller → Service → Repository interface → JDBC implementation |
| 11 | Git flow và Swing cơ bản | `ProductWindow` lấy dữ liệu từ cùng `ProductService`, không tạo kho dữ liệu riêng |
| 12 | Capstone theo đề tài nhóm tự chọn | HitShop là đề tài tham khảo, không phải đề tài bắt buộc của mọi nhóm |

## Ranh giới trách nhiệm từ buổi 8

```text
View (console/Swing)
        ↓
Controller
        ↓
Service — kiểm tra và điều phối nghiệp vụ
        ↓
Repository interface
        ↓
JDBC implementation — SQL và ánh xạ dữ liệu
        ↓
MySQL
```

- Model không đọc bàn phím, không in menu và không chứa SQL.
- View không gọi JDBC.
- Controller không chứa SQL và không tự tính nghiệp vụ đơn hàng.
- Service phụ thuộc repository interface, không phụ thuộc trực tiếp class JDBC.
- Repository không quyết định quy tắc nghiệp vụ như số lượng mua có vượt tồn kho hay không.

## Quy tắc nhất quán của đơn hàng

- `CartItem` dùng cho giỏ trước khi lưu đơn.
- Từ buổi 9, `OrderItem` là dòng hàng đã chốt, có `unitPrice` riêng để giá lịch sử không đổi khi Product đổi giá.
- Tạo đơn và giảm tồn kho là một nghiệp vụ nhiều bước. Buổi 9 giới thiệu cách làm; đến capstone, các bước ghi liên quan phải nằm trong một transaction.
- Hủy đơn chỉ hoàn tồn kho khi đơn đang ở trạng thái cho phép hủy. Không hoàn kho lần thứ hai khi gọi hủy lặp.

## Phạm vi capstone

Buổi 12 cho phép nhóm chọn miền bài toán khác HitShop. Dù chọn đề tài nào, sản phẩm vẫn phải chứng minh các năng lực cốt lõi của lộ trình: OOP, mô hình dữ liệu có quan hệ, JDBC CRUD, một luồng nghiệp vụ nhiều bảng có transaction, kiến trúc phân tầng, Git/PR và demo chạy được.
