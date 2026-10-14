# Bài Tập Về Nhà — Buổi 06

## Bài Bắt Buộc — Kiểm Tra Dữ Liệu Và Lưu Đơn Hàng Văn Bản

### Kế Thừa Từ Buổi 05

Tiếp tục `HitShop`, các model Product/Customer/CartItem/Order và payment method đã có. Dùng lại Order O01 cùng dữ liệu P01 × 2, P02 × 1; bổ sung khả năng lưu và nạp danh sách Order, không tạo mô hình đơn hàng khác.

### Bài Toán

Ứng dụng nhận một đơn gồm khách hàng và một hoặc nhiều dòng hàng. Dữ liệu hợp lệ được lưu thành file text để có thể đọc lại khi chương trình chạy lần sau.

### Dữ Liệu Và Ràng Buộc

- Dùng các class đã có: `Product(id, name, price, stock)`, `Customer(id, name, phone)`, `CartItem(product, quantity)` và `Order(id, customer, items, status)`.
- `Order` có ít nhất một `CartItem`; mỗi quantity là số nguyên dương; mỗi Product được tham chiếu phải tồn tại trong catalog đang nạp. Đơn không hợp lệ không được ghi vào file.
- File có tên `orders.txt`, UTF-8; mỗi dòng biểu diễn một Order theo đúng thứ tự trường: `orderId|customerId|customerName|customerPhone|status|itemList`.
- Class `OrderFileStore` có `void saveOrders(List<Order> orders)` và `List<Order> loadOrders()`; class chạy ví dụ là `HitShop`.
- `itemList` gồm các item cách nhau bằng `;`; mỗi item theo thứ tự `productId,productName,unitPrice,stock,quantity`. Mọi trường trong file đều được giữ lại để tạo lại Customer, Product, CartItem và Order.
- Mỗi lần `saveOrders` ghi lại đúng toàn bộ danh sách được truyền vào, theo thứ tự list; không tự nối dữ liệu cũ. `loadOrders` trả các bản ghi theo thứ tự trong file. Dòng sai định dạng được báo vị trí và bỏ qua; các dòng hợp lệ còn lại vẫn được nạp.
- Dữ liệu chuẩn không chứa `|`, `;` hoặc `,` trong giá trị văn bản.
- `unitPrice` là giá tại lúc tạo đơn; tổng đơn là tổng `quantity × unitPrice` của các item. Không cần thay đổi stock trong bài này.
- Có chức năng ghi các đơn hợp lệ và chức năng đọc lại toàn bộ file rồi in mỗi mã đơn, tên khách, trạng thái, số dòng hàng và tổng tiền.
- File chưa tồn tại được xem là chưa có đơn nào. Lỗi đọc/ghi file phải có thông báo dễ hiểu; không được báo lưu thành công khi ghi thất bại.

### Dữ Liệu Chuẩn Và Kết Quả

Đơn `O01`, khách `C01 / Linh Nguyễn / 0900000001`, status `PENDING`, gồm `P01 × 2` giá `150000` và `P02 × 1` giá `50000`; tổng `350000`. Sau khi đọc lại, thông tin O01 và các object liên kết phải giữ nguyên. Dòng file tương ứng dùng `O01|C01|Linh Nguyễn|0900000001|PENDING|P01,Áo thun,150000,20,2;P02,Bình nước,50000,10,1`.

### Nộp Bài

Nộp source, file `orders.txt` mẫu và ảnh/chụp output đọc lại O01. Không yêu cầu Regex, custom exception hay thread trong phần bắt buộc.

## Mở Rộng/Nâng Cao — Bài Tự Chọn

- **Regex:** định nghĩa một mẫu kiểm tra mã đơn theo định dạng `O` theo sau bởi đúng hai chữ số. Mã hợp lệ `O01`; `01O` không hợp lệ.
- **Custom exception:** lỗi số lượng nhỏ hơn 1 có tên `InvalidQuantityException`; khi lỗi xảy ra, đơn đó không được thêm vào file.
- **Thread:** demo riêng hai thao tác cùng đọc và giảm một stock đang bằng 1; nêu trạng thái sai có thể xảy ra. Không đưa tính năng đa luồng vào yêu cầu lưu/đọc đơn lõi.
