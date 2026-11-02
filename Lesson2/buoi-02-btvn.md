# Bài Tập Về Nhà — Buổi 02

## Bài Bắt Buộc — Giỏ Hàng Console Bằng Danh Sách Song Song

### Kế Thừa Từ Buổi 01

Tiếp tục đúng file `HitShop.java` trong repository cũ. Giữ phép tính tiền một mặt hàng; phạm vi mới là áp dụng phép tính đó cho nhiều dòng và thêm các thao tác quản lý giỏ.

### Bài Toán

Mở rộng chương trình buổi 1 thành giỏ hàng console. Mỗi dòng hàng có tên, đơn giá, số lượng; chương trình cho phép thêm, xóa, xem giỏ và xem tổng tiền.

### Ràng Buộc Dữ Liệu Và Chương Trình

- Bài chạy bằng JDK 17 và tiếp tục cùng repository của buổi 1.
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

Khi lần lượt thêm `Áo thun / 150000 / 2`, `Bình nước / 50000 / 1`, danh sách có hai dòng và tổng là `350000`. Xóa dòng số 1 thì còn `Bình nước / 50000 / 1`, tổng là `50000`. Mã `P01`, `P02` được bổ sung ở buổi 3 khi dữ liệu chuyển thành object `Product`.

### Nộp Bài

Nộp source Java và link repository GitHub.

## Bài Tự Chọn — Mở Rộng

- Tìm theo tên không phân biệt chữ hoa và chữ thường; trả về mọi dòng có tên bằng tên cần tìm sau khi bỏ qua khác biệt hoa/thường. Không tìm thấy thì in `Không tìm thấy mặt hàng.`
- VAT cố định 10% trên tổng tiền hàng; phí vận chuyển cố định 20000 nếu giỏ có ít nhất một dòng, bằng 0 nếu giỏ rỗng. In riêng tiền hàng, VAT, phí vận chuyển và tổng thanh toán.
- Nộp ảnh cửa sổ Breakpoints/Variables khi debug hoặc mô tả ngắn một lỗi về index đã phát hiện.
