# Bài Tập Về Nhà — Buổi 01

## Bài Bắt Buộc — Tính Tiền Một Mặt Hàng

### Bài Toán

Viết chương trình console nhận thông tin của một mặt hàng và in hóa đơn tạm tính cho mặt hàng đó.

### Dữ Liệu Và Ràng Buộc

- Bài chạy bằng JDK 17.
- Thông tin cần nhập: tên mặt hàng (`String`), đơn giá (`double`) và số lượng (`int`).
- Tên mặt hàng không để trống; đơn giá lớn hơn 0; số lượng là số nguyên dương.
- Thành tiền được xác định duy nhất bằng `đơn giá × số lượng`.
- Chương trình phải có class chứa `main`.
- Tên class chạy chương trình là `HitShop`; file là `HitShop.java`.

### Kết Quả Nghiệm Thu

- In tên mặt hàng, đơn giá, số lượng và thành tiền trên các nhãn dễ đọc.
- Với bộ dữ liệu `Áo thun`, `150000`, `2`, thành tiền là `300000`.
- Nộp một file source Java và link repository GitHub chứa bài làm.

## Mở Rộng — Bài Tự Chọn

- Khai báo hai hằng số `VAT_RATE = 0.10` và `SHIPPING_FEE = 20000`.
- Tính `tổng thanh toán = thành tiền + thành tiền × VAT_RATE + SHIPPING_FEE`.
- Với bộ dữ liệu chuẩn ở trên, tổng thanh toán là `350000`.
