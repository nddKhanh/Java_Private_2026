# Bài Tập Về Nhà — Buổi 06

## I. Tổng Kết Bài Tập Về Nhà Buổi 05

Sau bài tập buổi 5, project đã tách hợp đồng thanh toán/giao hàng bằng interface và định nghĩa equality của Product theo `id`.

### 1. Các Test Case Phải Chạy Được

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC05-01` | Thanh toán `350000` bằng Cash và Bank Transfer qua `PaymentMethod` | Hai implementation trả đúng chuỗi chuẩn |
| `TC05-02` | Giao O01 bằng Standard và Express qua `DeliveryMethod` | Hai implementation trả đúng mô tả giao hàng |
| `TC05-03` | So sánh hai Product cùng id P01 nhưng khác dữ liệu còn lại | Hai object bằng nhau và có cùng hash code |
| `TC05-04` | Tìm P01 bằng object khác instance trong `ArrayList<Product>` | `contains()` trả `true` |
| `TC05-05` | Xóa P01 bằng object khác instance | Đúng phần tử P01 bị xóa |

### 2. Cây Thư Mục Sau Buổi 05

Cấu trúc source tối thiểu cần đạt:

```text
src/
├── HitShop.java
├── model/
│   ├── Product.java
│   ├── Book.java
│   ├── Customer.java
│   ├── CartItem.java
│   └── Order.java
├── payment/
│   ├── PaymentMethod.java
│   ├── CashPayment.java
│   └── BankTransferPayment.java
└── delivery/
    ├── DeliveryMethod.java
    ├── StandardDelivery.java
    └── ExpressDelivery.java
```

## II. Bài Bắt Buộc — Kiểm Tra Dữ Liệu Và Lưu Đơn Hàng Ra File

### 1. Kế Thừa Từ Buổi 05

Tiếp tục `HitShop` bằng JDK 17 và dùng nguyên các model Product, Customer, CartItem, Order. Order vẫn được tạo bằng `new Order(id, customer)` rồi thêm item qua `addItem`; không tạo constructor hoặc một model Order khác chỉ để đọc file.

### 2. Bài Toán

Kiểm tra một Order trước khi lưu, ghi nội dung dễ đọc vào `orders.txt`, sau đó đọc lại các dòng để hiển thị.

### 3. Định Dạng File

Mỗi đơn là một khối:

```text
ORDER|O01|C01|Linh Nguyễn|PENDING
ITEM|P01|Áo thun|2|150000.0
ITEM|P02|Bình nước|1|50000.0
END
```

- `ORDER` giữ order id, customer id, customer name và status.
- Mỗi `ITEM` giữ product id, product name, quantity và unit price tại lúc lưu.
- `END` kết thúc một đơn.
- Dữ liệu chuẩn không chứa ký tự `|`.

### 4. Class Và Hành Vi Bắt Buộc

- Tạo `OrderValidator.validate(Order order)`; ném `IllegalArgumentException` nếu `order` là null, danh sách item rỗng, hoặc có `CartItem` chứa Product null hay quantity không dương.
- Tạo `OrderFileStore` với:
  - `void saveOrder(Order order, Path path) throws IOException`;
  - `List<String> loadLines(Path path) throws IOException`.
- `saveOrder` gọi validator trước khi mở file, dùng UTF-8 và ghi lại một khối đơn theo đúng định dạng trên. Bài chỉ lưu một đơn nên có thể ghi đè file cũ.
- Dùng `Files.newBufferedWriter`/`Files.newBufferedReader` trong try-with-resources. Không tự gọi `close()` trong nhiều nhánh.
- Nếu file chưa tồn tại, `loadLines` trả danh sách rỗng và thông báo `Chưa có dữ liệu đơn hàng.`.
- Nếu lưu thất bại, không in thông báo thành công. Thông báo lỗi phải nêu thao tác nào thất bại nhưng không in mật khẩu hay thông tin nhạy cảm.
- Khi đọc, in lần lượt các dòng của file và tính lại tổng từ các dòng `ITEM`.

### 5. Sơ Đồ Lớp Và Quan Hệ

![Sơ đồ lớp BTVN buổi 06](assets/buoi-06-class-diagram.svg)

`OrderFileStore` dùng `OrderValidator` trước khi ghi một `Order`; `loadLines` đọc file và trả về các dòng văn bản. Không tạo thêm một model Order dành riêng cho file.

### 6. Dữ Liệu Nghiệm Thu

Order O01 của C01 gồm P01 × 2 giá 150000 và P02 × 1 giá 50000, trạng thái PENDING. File phải có đúng khối mẫu; tổng đọc lại là `350000.0`.

Kiểm tra thêm ba trường hợp:

1. Order không có item: báo lỗi hợp lệ và file không bị ghi.
2. Một CartItem có quantity 0: báo lỗi hợp lệ và file không bị ghi.
3. Đường dẫn không ghi được: báo lỗi I/O, không báo `Lưu thành công`.

### 7. Nộp Bài

Nộp source, file `orders.txt` mẫu và output của bốn ca kiểm tra.

## III. Mở Rộng — Bài Tự Chọn

- Tạo `InvalidOrderException` riêng thay cho `IllegalArgumentException` và cập nhật chữ ký method phù hợp.
- Viết `OrderFileParser` để chuyển một khối hợp lệ trở lại thành Order bằng catalog Product và danh sách Customer đang có. Dòng sai phải báo số dòng và không làm mất các khối hợp lệ khác.

## IV. Tổng Kết Bài Tập Về Nhà Buổi 06

Sau bài tập buổi 6, Order được kiểm tra trước khi ghi, tài nguyên file được đóng bằng try-with-resources và lỗi nghiệp vụ được phân biệt với lỗi I/O.

### 1. Các Test Case Phải Chạy Được

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC06-01` | Lưu O01 gồm `P01 × 2`, `P02 × 1` | `orders.txt` có đúng một khối ORDER/ITEM/END theo mẫu |
| `TC06-02` | Đọc lại file của `TC06-01` | In đúng các dòng và tính tổng bằng `350000.0` |
| `TC06-03` | Gọi `loadLines()` khi file chưa tồn tại | Trả danh sách rỗng và báo `Chưa có dữ liệu đơn hàng.` |
| `TC06-04` | Lưu Order không có item | Báo lỗi nghiệp vụ và nội dung file cũ không đổi |
| `TC06-05` | Lưu Order có CartItem quantity bằng `0` | Báo lỗi nghiệp vụ và không ghi file |
| `TC06-06` | Lưu vào đường dẫn không ghi được | Báo lỗi I/O và không in `Lưu thành công` |

### 2. Cây Thư Mục Sau Buổi 06

Cấu trúc source tối thiểu cần đạt:

```text
orders.txt
src/
├── HitShop.java
├── model/
│   ├── Product.java
│   ├── Book.java
│   ├── Customer.java
│   ├── CartItem.java
│   └── Order.java
├── payment/
├── delivery/
├── validation/
│   └── OrderValidator.java
└── storage/
    └── OrderFileStore.java
```
