# Bài Tập Về Nhà — Buổi 04

## I. Tổng Kết Bài Tập Về Nhà Buổi 03

Sau bài tập buổi 3, `HitShop` đã thay ba danh sách song song bằng các object `Product`, `Customer`, `CartItem` và một `List<CartItem>`.

### 1. Các Test Case Phải Chạy Được

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC03-01` | Xem giỏ rỗng rồi xem tổng | In khách hàng hiện tại, báo `Giỏ hàng đang trống.`, tổng bằng `0` |
| `TC03-02` | Thêm mã khác `P01`, `P02` | Báo `Không tìm thấy sản phẩm.` và giỏ không đổi |
| `TC03-03` | Thêm `P01` với quantity không dương | Báo `Số lượng phải lớn hơn 0.` và giỏ không đổi |
| `TC03-04` | Thêm `P01 × 2`, `P02 × 1` | Giỏ có hai dòng, tổng bằng `350000` |
| `TC03-05` | Thêm cùng một Product hai lần | Tạo hai `CartItem` riêng, không gộp dòng |
| `TC03-06` | Xóa số thứ tự không tồn tại | Báo `Không tìm thấy mặt hàng.` và giỏ không đổi |

### 2. Cây Thư Mục Sau Buổi 03

Cấu trúc source tối thiểu cần đạt:

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Customer.java
    └── CartItem.java
```

## II. Bài Bắt Buộc — Kế Thừa, Đa Hình Và Trạng Thái Đơn

### 1. Kế Thừa Từ Buổi 03

Tiếp tục repository `HitShop` bằng JDK 17 và các class `Product`, `Customer`, `CartItem`. Giữ sản phẩm P01/P02, khách C01, giỏ P01 × 2 và P02 × 1. `HitShop` tiếp tục cung cấp các chức năng `addItem`, `removeItem`, `printItems`, `calculateTotal`, đồng thời bổ sung `Book` và `Order`.

### 2. Bài Toán

Mở rộng mô hình `Product`, `Customer`, `CartItem` từ buổi 3 để biểu diễn một loại sản phẩm chuyên biệt và một đơn hàng. Ví dụ dùng sách; mọi dữ liệu hiển thị phải đến từ method của object phù hợp, không rẽ nhánh theo tên loại sản phẩm.

### 3. Class, Field Và Quan Hệ

- `Product` giữ `id`, `name`, `price`, `stock` và có method `getDescription()` trả về tên sản phẩm. Các field tiếp tục private; không truy cập trực tiếp từ lớp con.
- `Book extends Product`, có field private `author` và `getAuthor()`. Constructor nhận `id`, `name`, `price`, `stock`, `author`; khởi tạo phần Product cùng dữ liệu đó. Override `getDescription()` trả về đúng mẫu `Tên sách — tác giả: Tên tác giả`.
- Một `List<Product>` chứa cả object Product thường và Book. Phần hiển thị danh sách gọi cùng `getDescription()` cho mọi phần tử.
- `Order` có các field private: `id`, `customer` kiểu `Customer`, `items` kiểu `List<CartItem>` và `status` kiểu `String`. Constructor nhận `id` và `customer`; trạng thái ban đầu là `PENDING`. Order HAS-A Customer và HAS-A nhiều CartItem; mỗi CartItem vẫn HAS-A một Product.
- `Order.addItem(CartItem item)` thêm một dòng và `getTotal()` trả tổng các giá trị `CartItem.getTotal()`. `confirm()` chuyển `PENDING` thành `CONFIRMED`; `cancel()` chuyển `PENDING` thành `CANCELLED`. Khi trạng thái không còn là `PENDING`, `confirm()` và `cancel()` không thay đổi trạng thái.
- Các chữ ký là `void addItem(CartItem item)`, `double getTotal()`, `void confirm()`, `void cancel()`, `String getStatus()`, `String getId()`, `Customer getCustomer()` và `List<CartItem> getItems()`. `getItems()` không được trả null.
- Tên class chạy chương trình vẫn là `HitShop`; giữ menu console và các luồng giỏ hàng đã có ở buổi 2–3, đồng thời bổ sung hiển thị danh sách đa hình và thao tác với Order.

### 4. Sơ Đồ Lớp Và Quan Hệ

![Sơ đồ lớp BTVN buổi 04](assets/buoi-04-class-diagram.svg)

Mũi tên tam giác biểu diễn `Book extends Product`; hình thoi tại `Order` biểu diễn một đơn chứa nhiều `CartItem`.

### 5. Dữ Liệu Chuẩn Và Kết Quả

- Product thường tiếp tục từ buổi 3: `P01 / Áo thun / 150000 / stock 20`; `P02 / Bình nước / 50000 / stock 10`.
- Book mới: `P03 / Java cơ bản / 120000 / stock 5 / An Nguyen`.
- List hiển thị ba mô tả: `Áo thun`, `Bình nước` và `Java cơ bản — tác giả: An Nguyen`.
- Customer `C01 / Linh Nguyễn / 0900000001`; Order `O01` tiếp nhận giỏ đã xây dựng ở buổi trước, gồm `P01 × 2` và `P02 × 1`; tổng đơn là `350000`.
- Trước xác nhận, status là `PENDING`; sau `confirm()`, status là `CONFIRMED`. Lời gọi `cancel()` tiếp theo không đổi status.
- Một Order khác ở trạng thái `PENDING`, khi gọi `cancel()`, chuyển thành `CANCELLED`; gọi lại `cancel()` không đổi trạng thái.

### 6. Nộp Bài

Nộp source các model và `HitShop`; kèm output danh sách mô tả, menu/giỏ hàng đang có, tổng đơn và trạng thái sau xác nhận.

## III. Nâng Cao — Bài Tự Chọn

- Mô tả sự khác nhau giữa overloading và overriding.
- Xác định quan hệ Book–Product là IS-A và Order–CartItem là HAS-A.
- Viết hai overload: `findProducts(String keyword)` trả mọi sản phẩm có tên chứa từ khóa; `findProducts(String keyword, int limit)` trả tối đa `limit` kết quả đầu tiên.
- Lọc các đối tượng `Book` trong `List<Product>` và lấy tên tác giả từ từng đối tượng `Book`.

## IV. Tổng Kết Bài Tập Về Nhà Buổi 04

Sau bài tập buổi 4, `Book` kế thừa `Product`, danh sách sản phẩm hiển thị bằng đa hình và `Order` bảo vệ các bước chuyển trạng thái.

### 1. Các Test Case Phải Chạy Được

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC04-01` | Đưa P01, P02 và Book P03 vào `List<Product>`, gọi `getDescription()` | Lần lượt in `Áo thun`, `Bình nước`, `Java cơ bản — tác giả: An Nguyen` mà không rẽ nhánh theo class |
| `TC04-02` | Tạo O01 với C01, thêm `P01 × 2`, `P02 × 1` | `getItems()` không null và `getTotal()` bằng `350000` |
| `TC04-03` | Gọi `confirm()` trên O01 đang `PENDING` | Trạng thái chuyển thành `CONFIRMED` |
| `TC04-04` | Gọi `cancel()` sau khi O01 đã `CONFIRMED` | Trạng thái vẫn là `CONFIRMED` |
| `TC04-05` | Tạo Order mới, gọi `cancel()` hai lần | Lần đầu chuyển thành `CANCELLED`, lần sau không đổi |
| `TC04-06` | Chạy lại menu giỏ hàng của buổi 3 | Các luồng thêm, xóa, xem giỏ và tính tổng vẫn hoạt động |

### 2. Cây Thư Mục Sau Buổi 04

Cấu trúc source tối thiểu cần đạt:

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
