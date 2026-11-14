# Bài Tập Về Nhà — Buổi 04

## I. Điểm Bắt Đầu Từ Buổi 03

Tiếp tục repository `HitShop` bằng JDK 17.
Giữ `Product`, `Customer`, `CartItem`, `List<CartItem>` và các chức năng thêm, xóa, xem giỏ, tính tổng.

### 1. Các Ca Cũ Phải Còn Đạt

| Ca | Kết quả cần giữ |
|---|---|
| Giỏ rỗng | Báo giỏ trống, tổng bằng `0` |
| Thêm `P01 × 2`, `P02 × 1` | Có hai CartItem, tổng bằng `350000` |
| Xóa số thứ tự không tồn tại | Báo không tìm thấy, giỏ không đổi |

### 2. Cây Thư Mục Đầu Buổi

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Customer.java
    └── CartItem.java
```

## II. Ba Bài Bắt Buộc

Ba bài dùng chung dữ liệu và source.
Hoàn thành theo thứ tự; không tạo ba project riêng.

### Bài 1 — Encapsulation Và Trạng Thái Hợp Lệ

Cập nhật `Product`:

- Field private: `String id`, `String name`, `double price`, `int stock`.
- Constructor nhận đủ bốn giá trị; từ chối `id`/`name` rỗng, `price <= 0`, `stock < 0` bằng `IllegalArgumentException`.
- Không có `setId()` và `setStock()`.
- `void setPrice(double price)` chỉ nhận giá dương; lỗi không được làm đổi giá cũ.
- `void decreaseStock(int quantity)` chỉ nhận `0 < quantity <= stock`; lỗi không được làm đổi stock.
- Getter chỉ đọc trạng thái hiện tại.

Dữ liệu chuẩn: `P01 / Áo thun / 150000 / 20` và `P02 / Bình nước / 50000 / 10`.

### Bài 2 — Kế Thừa Và Đa Hình

Cập nhật `Product` và tạo `Book`:

- `Product.getDescription(): String` trả tên sản phẩm.
- `Book extends Product`, thêm field private `String author`.
- Constructor `Book(String id, String name, double price, int stock, String author)` gọi `super(...)`.
- `Book.getAuthor(): String` trả tác giả.
- `Book` override `getDescription()` theo mẫu `Tên sách — tác giả: Tác giả`.
- Tạo `List<Product>` chứa P01, P02 và Book P03; một vòng lặp gọi `getDescription()` cho mọi phần tử.

Dữ liệu Book: `P03 / Java cơ bản / 120000 / 5 / An Nguyen`.

Không dùng `instanceof`, chuỗi loại sản phẩm hoặc `if/else` để chọn cách mô tả.

### Bài 3 — HAS-A Và Trạng Thái Order

Tạo `Order`:

- Field private: `String id`, `Customer customer`, `List<CartItem> items`, `String status`.
- Constructor `Order(String id, Customer customer)` tạo list rỗng và status `PENDING`.
- Getter: `getId()`, `getCustomer()`, `getItems()`, `getStatus()`; `getItems()` không trả `null`.
- `void addItem(CartItem item)` thêm một dòng hợp lệ.
- `double getTotal()` cộng `CartItem.getTotal()`.
- `void confirm()` chỉ chuyển `PENDING → CONFIRMED`.
- `void cancel()` chỉ chuyển `PENDING → CANCELLED`.
- Không có `setStatus(String)`.

Customer chuẩn: `C01 / Linh Nguyễn / 0900000001`.
Order chuẩn: `O01` gồm `P01 × 2`, `P02 × 1`, tổng `350000`.

![Sơ đồ lớp BTVN buổi 04](assets/buoi-04-class-diagram.svg)

## III. Ranh Giới Bài Làm

- Không dùng abstract class, interface, enum, `equals()`/`hashCode()`; các nội dung này thuộc buổi sau.
- Không tự động trừ stock khi thêm CartItem vào Order.
- Không viết thêm Cart, OrderService, repository hoặc database.
- Không dùng downcasting để hoàn thành phần bắt buộc.

## IV. Nộp Bài

Nộp source lên github và commend dưới bài viết
Giữ nguyên menu giỏ hàng của buổi 3.

## V. Nghiệm Thu Buổi 04

### 1. Đúng 18 Test Case

#### Encapsulation

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC04-01` | Tạo P01 hợp lệ | Đủ id, name, price, stock |
| `TC04-02` | Tạo Product có id/name rỗng | Bị từ chối |
| `TC04-03` | Tạo Product có price `0` hoặc âm | Bị từ chối |
| `TC04-04` | Tạo Product có stock âm | Bị từ chối |
| `TC04-05` | Đổi giá P01 thành `160000`, sau đó thử giá `0` | Giá cuối là `160000` |
| `TC04-06` | Giảm stock P01 đi `2`, sau đó thử `0`, số âm và vượt tồn | Stock cuối là `18` |

#### Kế Thừa Và Đa Hình

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC04-07` | Gọi mô tả P01 | `Áo thun` |
| `TC04-08` | Tạo Book P03 | Đọc được dữ liệu Product và author |
| `TC04-09` | Gọi mô tả P03 | `Java cơ bản — tác giả: An Nguyen` |
| `TC04-10` | Gán Book P03 vào biến `Product` rồi gọi mô tả | Vẫn gọi mô tả của Book |
| `TC04-11` | Duyệt P01, P02, P03 trong `List<Product>` | In đúng ba mô tả |
| `TC04-12` | Kiểm tra code hiển thị | Không có `instanceof`/`if` phân loại |

#### HAS-A Và Trạng Thái

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC04-13` | Tạo O01 | Items rỗng, total `0`, status `PENDING` |
| `TC04-14` | Thêm P01 × 2 và P02 × 1 | Hai item, total `350000` |
| `TC04-15` | `confirm()` O01 | Status `CONFIRMED` |
| `TC04-16` | `cancel()` O01 sau confirm | Vẫn `CONFIRMED` |
| `TC04-17` | Hủy một Order mới | `PENDING → CANCELLED` |
| `TC04-18` | Confirm/hủy lại Order đã `CANCELLED` | Vẫn `CANCELLED` |

### 2. Cây Thư Mục Cuối Buổi

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Book.java
    ├── Customer.java
    ├── CartItem.java
    └── Order.java
```
