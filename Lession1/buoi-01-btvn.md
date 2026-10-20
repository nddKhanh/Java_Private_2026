# Bài Tập Về Nhà — Buổi 01

## Bài Bắt Buộc — Tính Tiền Một Mặt Hàng

### Bài Toán

Viết chương trình console nhận thông tin của một mặt hàng được mua và in hóa đơn tạm tính. Đây là một lần tính duy nhất; chưa có menu, danh sách hoặc lưu dữ liệu.

### Dữ Liệu Và Ràng Buộc

- Thông tin cần nhập: tên mặt hàng (`String`), đơn giá (`double`) và số lượng (`int`).
- Tên mặt hàng không để trống; đơn giá lớn hơn 0; số lượng là số nguyên dương. Buổi này chỉ cần nhập dữ liệu hợp lệ, chưa yêu cầu lặp nhập lại khi sai.
- Thành tiền được xác định duy nhất bằng `đơn giá × số lượng`.
- Chương trình phải có class chứa `main`; không bắt buộc tạo thêm class model.
- Tên class chạy chương trình là `HitShop`; file là `HitShop.java`.

### Kết Quả Nghiệm Thu

- In tên mặt hàng, đơn giá, số lượng và thành tiền trên các nhãn dễ đọc.
- Với bộ dữ liệu `Áo thun`, `150000`, `2`, thành tiền là `300000`.
- Không tính VAT/phí vận chuyển trong phần bắt buộc.
- Nộp một file source Java và link repository GitHub chứa bài làm.

## Mở Rộng — Bài Tự Chọn

Thêm hai giá trị `VAT_RATE = 0.10` và `SHIPPING_FEE = 20000`; tổng thanh toán được tính bằng thành tiền + 10% thành tiền + phí vận chuyển. Với bộ dữ liệu chuẩn ở trên, tổng thanh toán là `350000`. Bỏ qua phần này không ảnh hưởng bài bắt buộc.

## Mốc Bàn Giao Cho Buổi 02

Giữ `HitShop.java` và phép tính giá × số lượng; buổi 2 mở rộng chương trình thành giỏ hàng nhiều dòng, không tạo ứng dụng/repository mới.
