# Bài Tập Về Nhà — Buổi 05

## Bài Bắt Buộc — Hợp Đồng Thanh Toán Và Equality Của Product

### Kế Thừa Từ Buổi 04

Tiếp tục repository `HitShop`, các Product P01/P02/P03 và Order O01 gồm P01 × 2, P02 × 1 với tổng `350000`. Thêm các lớp thanh toán vào ứng dụng; không thay mô hình giỏ/đơn đã có.

### Bài Toán

Thêm một phương thức thanh toán có thể thay thế cho phương thức hiện có mà phần gọi thanh toán không phụ thuộc vào class cụ thể. Đồng thời, hai Product có cùng mã được xem là cùng một sản phẩm khi tìm hoặc xóa trong danh sách.

### Class, Method Và Ràng Buộc

- `PaymentMethod` là interface có đúng method `String pay(double amount)`.
- `CashPayment` và `BankTransferPayment` cùng implements `PaymentMethod`.
- `CashPayment.pay(amount)` trả về `Đã nhận tiền mặt: ` nối với biểu diễn chuẩn của `double`; `BankTransferPayment.pay(amount)` trả về `Đã tạo yêu cầu chuyển khoản: ` nối với biểu diễn chuẩn của `double`.
- `HitShop` nhận một biến kiểu `PaymentMethod`; với amount `350000`, mỗi implementation trả đúng nội dung tương ứng. Không chọn payment bằng chuỗi if/else trong nơi gọi method.
- Mở rộng `Product` hiện có, giữ các field/constructor/getter/setter của buổi 3; `equals(Object)` và `hashCode()` cùng dựa trên `id`. Hai object cùng id nhưng khác name/price vẫn bằng nhau; khác id thì không bằng nhau.
- `id` của Product không rỗng và không null.
- Danh sách kiểm tra equality là `ArrayList<Product>`. Tìm/xóa một object Product khác instance nhưng có id đã có phải nhận ra dòng cùng id.
- Không bắt buộc tạo OrderService, database, Set/Map hoặc Stream.

### Dữ Liệu Chuẩn

Hai Product `P01 / Áo thun / 150000` và `P01 / Áo thun bản cập nhật / 160000` được xem là bằng nhau; Product `P02 / Bình nước / 50000` không bằng P01. Amount thanh toán mẫu là `350000.0`; hai chuỗi output chuẩn lần lượt là `Đã nhận tiền mặt: 350000.0` và `Đã tạo yêu cầu chuyển khoản: 350000.0`.
- Hai Product bằng nhau theo id phải có cùng `hashCode()`; Product khác id không bị yêu cầu có hash code khác.

### Nộp Bài

Nộp source interface, hai implementation, Product equality và `HitShop`; kèm output của hai phương thức payment và phép so sánh ba Product chuẩn.

## Mở Rộng/Nâng Cao — Bài Tự Chọn

- Lambda: lọc danh sách theo điều kiện tồn kho hoặc tên; điều kiện và kết quả lọc phải được nêu trong output.
- Generics: class `Box<T>` giữ đúng một giá trị kiểu T, có constructor nhận giá trị và method `getValue()` trả lại cùng giá trị.
- Optional: một phép tìm Product theo id trả về `Optional<Product>`; id không có trong danh sách là `Optional.empty()`.

Đây là tiện ích tìm/lọc độc lập. Không làm phần tự chọn không ảnh hưởng payment, equality hoặc chức năng cốt lõi.
