# Bài Tập Về Nhà — Buổi 02

## Bài Bắt Buộc — Giỏ Hàng Console Bằng Danh Sách Song Song

### Kế Thừa Từ Buổi 01

Tiếp tục đúng file `HitShop.java` trong repository cũ. Giữ phép tính tiền một mặt hàng; phạm vi mới là áp dụng phép tính đó cho nhiều dòng và thêm các thao tác quản lý giỏ.

### Bài Toán

Mở rộng chương trình buổi 1 thành giỏ hàng console. Mỗi dòng hàng có tên, đơn giá, số lượng; chương trình cho phép thêm, xóa, xem giỏ và xem tổng tiền.

### Ràng Buộc Dữ Liệu Và Chương Trình

- Dùng đúng ba danh sách song song: `ArrayList<String> names`, `ArrayList<Double> prices`, `ArrayList<Integer> quantities`.
- Cùng một vị trí `i` ở cả ba danh sách luôn mô tả cùng một mặt hàng. Sau mọi thao tác, ba danh sách phải có cùng `size()`.
- Mỗi dòng hàng có tên không rỗng, giá lớn hơn 0 và số lượng nguyên dương. Mỗi lần thêm tạo một dòng hàng mới; chưa gộp các sản phẩm trùng tên.
- Có method `addItem(String name, double price, int quantity)`, `removeItem(int index)`, `printItems()` và `calculateTotal()` trả về tổng tiền kiểu `double`.
- Các method trên là `static` để được gọi trực tiếp từ `main`, đúng phạm vi method của buổi học.
- Tên class chính vẫn là `HitShop`; không tạo class chạy chương trình thứ hai.
- Menu có đúng các lựa chọn: `1` thêm mặt hàng; `2` xóa theo số thứ tự đang hiển thị; `3` xem giỏ; `4` xem tổng tiền; `0` kết thúc.
- Số thứ tự hiển thị bắt đầu từ 1; index truyền cho thao tác xóa tương ứng số thứ tự trừ 1. Số thứ tự không tồn tại không làm thay đổi giỏ và phải báo `Không tìm thấy mặt hàng.`
- Lựa chọn menu ngoài các giá trị trên phải báo `Lựa chọn không hợp lệ.` rồi tiếp tục hiển thị menu. Chọn `0` kết thúc.
- Tổng tiền được tính bằng tổng của `price × quantity` trên mọi dòng; giỏ rỗng có tổng bằng 0.

### Dữ Liệu Chuẩn Để Đối Chiếu

Khi lần lượt thêm `P01 / Áo thun / 150000 / 2`, `P02 / Bình nước / 50000 / 1`, danh sách có hai dòng và tổng là `350000`. Xóa dòng số 1 thì còn `P02 / Bình nước / 50000 / 1`, tổng là `50000`.

### Nộp Bài

Nộp source Java và link repository GitHub. Bài bắt buộc chưa dùng class tự tạo, database hoặc file lưu trữ.

### Mốc Bàn Giao Cho Buổi 03

Giữ tên bốn method `addItem`, `removeItem`, `printItems`, `calculateTotal` và giữ cách dùng menu. Buổi 3 thay dữ liệu ba list song song bằng object Product/CartItem; các method tiếp tục phục vụ cùng chức năng giỏ hàng.

## Bài Tự Chọn — Mở Rộng

- Tìm theo tên không phân biệt hoa/thường; kết quả là mọi dòng khớp chính xác với tên tìm kiếm. Không tìm thấy thì in `Không tìm thấy mặt hàng.`
- VAT cố định 10% trên tổng tiền hàng; phí vận chuyển cố định 20000 nếu giỏ có ít nhất một dòng, bằng 0 nếu giỏ rỗng. In riêng tiền hàng, VAT, phí vận chuyển và tổng thanh toán.
- Debug là phần tự chọn: nộp ảnh breakpoint/Variables hoặc ghi ngắn một lỗi index đã phát hiện. Không cần bổ sung chức năng core.

## ERD Và Đặc Tả Dữ Liệu

Buổi 2 chưa yêu cầu database. Phần này để trống để hoàn thiện khi học thiết kế dữ liệu.

### Sơ Đồ ERD

_Bổ sung sau khi chốt mô hình dữ liệu._

### Đặc Tả Bảng

| Tên bảng | Cột | Kiểu dữ liệu | Khóa / ràng buộc | Mô tả |
|---|---|---|---|---|
| _Chưa áp dụng_ |  |  |  |  |
