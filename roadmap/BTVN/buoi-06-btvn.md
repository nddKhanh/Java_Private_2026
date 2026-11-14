# Bài Tập Về Nhà — Buổi 06

## I. Điểm Bắt Đầu Từ Buổi 05

Giữ PaymentMethod, DeliveryMethod, equality của Product và toàn bộ hành vi Order.

### 1. Các Ca Cũ Phải Còn Đạt

| Ca | Kết quả cần giữ |
|---|---|
| Checkout Cash/Bank | Đúng hai kết quả |
| Giao Standard/Express | Đúng hai kết quả |
| Tìm/xóa P01 bằng object cùng id | Hoạt động theo equals() |

### 2. Cây Thư Mục Đầu Buổi

Dùng nguyên cây cuối [BTVN Buổi 05](buoi-05-btvn.md); chưa đổi package trước khi làm phần file.

## II. Bài Bắt Buộc

### 1. OrderValidator

Tạo `OrderValidator.validate(Order order): void`.

Ném `IllegalArgumentException` khi:

- Order là `null`.
- Order không có item.
- CartItem có Product `null`.
- Quantity không dương.

Validation chạy trước khi mở file.

### 2. Định Dạng File

```text
ORDER|O01|C01|Linh Nguyễn|PENDING
ITEM|P01|Áo thun|2|150000.0
ITEM|P02|Bình nước|1|50000.0
END
```

Mỗi ITEM giữ unit price tại lúc lưu.
Dữ liệu chuẩn không chứa ký tự `|`.

### 3. OrderFileStore

Tạo:

- `void saveOrder(Order order, Path path) throws IOException`.
- `List<String> loadLines(Path path) throws IOException`.
- `double calculateTotal(List<String> lines)`.

Yêu cầu:

- Dùng UTF-8 và try-with-resources.
- Bài chỉ lưu một Order nên `saveOrder` được ghi đè file.
- File chưa tồn tại: `loadLines` trả list rỗng.
- Chỉ in “Lưu thành công” sau khi ghi hoàn tất.
- `calculateTotal` chỉ tính các dòng ITEM.

![Sơ đồ lớp BTVN buổi 06](assets/buoi-06-class-diagram.svg)

## III. Mở Rộng Tự Chọn

Tạo `InvalidOrderException extends RuntimeException` và dùng thay `IllegalArgumentException` trong validator.
Không viết parser dựng lại Order trong phần bắt buộc.

## IV. Nộp Bài

Nộp source, `orders.txt` mẫu và output các ca thành công/lỗi.
Không thay model Order chỉ để đọc file.

## V. Nghiệm Thu Buổi 06

### 1. Test Case

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC06-01` | Lưu O01 chuẩn | File đúng khối ORDER/ITEM/END |
| `TC06-02` | Đọc file chuẩn | Tổng `350000.0` |
| `TC06-03` | Đọc file chưa tồn tại | List rỗng, báo chưa có dữ liệu |
| `TC06-04` | Lưu Order `null` hoặc rỗng | Báo lỗi, file cũ không đổi |
| `TC06-05` | Lưu item có Product null/quantity 0 | Báo lỗi, không ghi file |
| `TC06-06` | Ghi vào đường dẫn không hợp lệ | Báo lỗi I/O, không báo thành công |
| `TC06-07` | Chạy lại payment/delivery/equality | Kết quả Buổi 05 không đổi |

### 2. Cây Thư Mục Cuối Buổi

```text
orders.txt
src/
├── HitShop.java
├── model/
├── payment/
├── delivery/
├── validation/
│   └── OrderValidator.java
└── storage/
    └── OrderFileStore.java
```
