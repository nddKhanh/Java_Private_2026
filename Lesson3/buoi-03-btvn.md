# Bài Tập Về Nhà — Buổi 03

## I. Tổng Kết Bài Tập Về Nhà Buổi 02

Sau bài tập buổi 2, chương trình `HitShop` đã quản lý được nhiều dòng hàng bằng ba `ArrayList` song song.

Chương trình có menu lặp để thêm mặt hàng, xóa theo số thứ tự, xem giỏ, tính tổng tiền và thoát.

### 1. Các Thành Phần Phải Có

- Ba danh sách `ArrayList<String> names`, `ArrayList<Double> prices`, `ArrayList<Integer> quantities` luôn có cùng số phần tử.
- Bốn method `addItem(String name, double price, int quantity)`, `removeItem(int index)`, `printItems()` và `calculateTotal()`.
- Menu gồm các lựa chọn `1`, `2`, `3`, `4`, `0`; lựa chọn khác phải báo `Lựa chọn không hợp lệ.`.
- Số thứ tự hiển thị bắt đầu từ `1`; index trong `ArrayList` bắt đầu từ `0`.

### 2. Các Test Case Phải Chạy Được

Mỗi test case bắt đầu từ một lần chạy mới, trừ khi phần thao tác ghi rõ các bước liên tiếp.

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC02-01` | Khi giỏ đang rỗng, chọn `4` | Tổng tiền bằng `0` |
| `TC02-02` | Chọn `1`, thêm `Áo thun / 150000 / 2`, sau đó chọn `3` | Giỏ có một dòng `Áo thun`, đơn giá `150000`, số lượng `2` |
| `TC02-03` | Thêm lần lượt `Áo thun / 150000 / 2` và `Bình nước / 50000 / 1`, sau đó chọn `4` | Tổng tiền bằng `350000` |
| `TC02-04` | Từ giỏ của `TC02-03`, chọn `2` và xóa dòng số `1` | Chỉ còn `Bình nước / 50000 / 1`, tổng tiền bằng `50000` |
| `TC02-05` | Khi giỏ có hai dòng, yêu cầu xóa số thứ tự không tồn tại | In `Không tìm thấy mặt hàng.` và giỏ không thay đổi |
| `TC02-06` | Nhập một lựa chọn menu khác `0`, `1`, `2`, `3`, `4` | In `Lựa chọn không hợp lệ.` rồi tiếp tục hiển thị menu |
| `TC02-07` | Chọn `0` | Chương trình kết thúc |

### 3. Cây Thư Mục Sau Buổi 02

Cấu trúc source tối thiểu sau khi hoàn thành bài tập buổi 2:

```text
src/
└── HitShop.java
```

Buổi 3 tiếp tục trên chính chương trình này, không tạo một project mới.

## II. Bài Bắt Buộc — Mô Hình Hóa Mặt Hàng Và Giỏ Hàng Bằng Object

### 1. Bài Toán

Thay ba danh sách song song của buổi 2 bằng các object có dữ liệu và trách nhiệm rõ ràng.

Chương trình vẫn chạy trên console bằng JDK 17 và giữ các chức năng thêm, xóa, xem giỏ, tính tổng tiền.

Phạm vi bài gồm hai sản phẩm có sẵn, một khách hàng hiện tại và một giỏ hàng.

Tên class chạy chương trình vẫn là `HitShop`.

### 2. Các Class Cần Có

#### Class `Product`

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | `String` | Mã sản phẩm |
| `name` | `String` | Tên sản phẩm |
| `price` | `double` | Đơn giá |
| `stock` | `int` | Số lượng đang có trong kho |

Tất cả thuộc tính đều là `private`.

Constructor nhận đủ bốn thuộc tính:

```java
public Product(String id, String name, double price, int stock)
```

Cần có getter cho cả bốn thuộc tính; chỉ `name`, `price`, `stock` có setter.

Ngoài ra, class có phương thức:

| Phương thức | Ý nghĩa |
|---|---|
| `public String toString()` | Trả về thông tin sản phẩm theo dạng `P01 \| Áo thun \| 150000.0 \| tồn: 20` |

#### Class `Customer`

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | `String` | Mã khách hàng |
| `name` | `String` | Tên khách hàng |
| `phone` | `String` | Số điện thoại |

Tất cả thuộc tính đều là `private`.

Constructor nhận đủ ba thuộc tính:

```java
public Customer(String id, String name, String phone)
```

Cần có getter cho cả ba thuộc tính; chỉ `name`, `phone` có setter.

Ngoài ra, class có phương thức:

| Phương thức | Ý nghĩa |
|---|---|
| `public String toString()` | Trả về thông tin khách hàng theo dạng `C01 \| Linh Nguyễn \| 0900000001` |

#### Class `CartItem`

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `product` | `Product` | Sản phẩm được giữ trong dòng giỏ hàng |
| `quantity` | `int` | Số lượng khách muốn mua |

Tất cả thuộc tính đều là `private`.

Constructor nhận đủ hai thuộc tính:

```java
public CartItem(Product product, int quantity)
```

Cần có `getProduct()`, `getQuantity()` và `setQuantity(int quantity)`.

Ngoài ra, class có phương thức:

| Phương thức | Ý nghĩa |
|---|---|
| `public double getTotal()` | Trả về `product.getPrice() * quantity` |

`CartItem` HAS-A `Product`: mỗi dòng giỏ hàng giữ tham chiếu đến đúng một object `Product`.

Thay đổi `quantity` của `CartItem` không được tự làm thay đổi `stock` của `Product`.

#### Class `HitShop`

`HitShop` nằm ngoài package `model` và giữ hai field chính:

```java
private static final Customer currentCustomer =
        new Customer("C01", "Linh Nguyễn", "0900000001");
private static final List<CartItem> cart = new ArrayList<>();
```

Chương trình giữ đúng một biến `currentCustomer` và một `List<CartItem>`.

Không tạo class `Cart` và không lưu số lượng mua trong `Product`.

Giữ đúng bốn method sau:

| Phương thức | Ý nghĩa |
|---|---|
| `static void addItem(Product product, int quantity)` | Tạo một `CartItem` mới và thêm vào giỏ |
| `static void removeItem(int index)` | Xóa dòng giỏ hàng tại index được truyền vào |
| `static void printItems()` | In khách hàng hiện tại và các dòng trong giỏ |
| `static double calculateTotal()` | Trả về tổng thành tiền của toàn bộ giỏ |

### 3. Cấu Trúc Source Và Package

Tổ chức source tối thiểu như sau:

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Customer.java
    └── CartItem.java
```

Ba file trong thư mục `model` phải có dòng:

```java
package model;
```

`HitShop.java` import các class và collection cần dùng:

```java
import model.Product;
import model.Customer;
import model.CartItem;
import java.util.List;
import java.util.ArrayList;
```

### 4. Sơ Đồ Lớp Và Quan Hệ

![Sơ đồ lớp BTVN buổi 03](assets/buoi-03-class-diagram.svg)

Hình thoi tại `HitShop` thể hiện chương trình giữ nhiều `CartItem`.

Mỗi `CartItem` tham chiếu đúng một `Product`.

### 5. Dữ Liệu Ban Đầu

| Object | Mã | Tên | Giá / Số điện thoại | Tồn kho |
|---|---|---|---|---|
| Product 1 | `P01` | `Áo thun` | `150000` | `20` |
| Product 2 | `P02` | `Bình nước` | `50000` | `10` |
| Customer | `C01` | `Linh Nguyễn` | `0900000001` | — |

Tạo hai object `Product` đúng một lần khi chương trình bắt đầu.

Khi thêm sản phẩm vào giỏ, phải tái sử dụng đúng object `P01` hoặc `P02` đã tạo.

### 6. Menu Và Hành Vi Bắt Buộc

Menu tiếp tục có các lựa chọn:

```text
1. Thêm mặt hàng
2. Xóa mặt hàng
3. Xem giỏ hàng
4. Xem tổng tiền
0. Thoát
```

1. **Thêm mặt hàng**

   - In mã và tên của hai sản phẩm có sẵn.
   - Yêu cầu nhập mã sản phẩm và số lượng.
   - Có thể dùng `if/else` hoặc `switch` để chọn object theo mã.
   - Mã khác `P01`, `P02` phải in `Không tìm thấy sản phẩm.` và không thay đổi giỏ.
   - Số lượng nhỏ hơn hoặc bằng `0` phải in `Số lượng phải lớn hơn 0.` và không thay đổi giỏ.
   - Mỗi lần thêm tạo một `CartItem` mới; chưa gộp các dòng dùng cùng một `Product`.

2. **Xóa mặt hàng**

   - Hiển thị số thứ tự của từng dòng trong giỏ, bắt đầu từ `1`.
   - Nhận số thứ tự người dùng nhập và gọi `removeItem(displayNumber - 1)`.
   - Số thứ tự không tồn tại phải in `Không tìm thấy mặt hàng.` và không thay đổi giỏ.

3. **Xem giỏ hàng**

   - In `currentCustomer` trước, sau đó in các dòng giỏ hàng.
   - Mỗi dòng phải có số thứ tự, mã, tên, đơn giá, số lượng và thành tiền.
   - Nếu giỏ rỗng, in `Giỏ hàng đang trống.`.

4. **Xem tổng tiền**

   - In tổng do `calculateTotal()` trả về.
   - Giỏ rỗng có tổng bằng `0`.

5. **Lựa chọn khác và thoát**

   - Lựa chọn ngoài `0`, `1`, `2`, `3`, `4` phải in `Lựa chọn không hợp lệ.` rồi tiếp tục menu.
   - Chọn `0` để kết thúc chương trình.

### 7. Kết Quả Mẫu

Với giỏ gồm `P01 × 2` và `P02 × 1`, output tối thiểu là:

```text
Khách hàng: C01 | Linh Nguyễn | 0900000001
1. P01 | Áo thun | 150000.0 | SL: 2 | Thành tiền: 300000.0
2. P02 | Bình nước | 50000.0 | SL: 1 | Thành tiền: 50000.0
Tổng tiền: 350000.0
```

### 8. Phạm Vi Chưa Thực Hiện Trong Buổi 03

- Chưa kiểm tra `quantity` có vượt `stock`.
- Chưa trừ tồn kho khi thêm vào giỏ.
- Chưa gộp các dòng có cùng `Product`.
- Chưa tạo class `Cart`.

Các quy tắc cập nhật và bảo vệ tồn kho sẽ được phát triển ở buổi sau.

### 9. Nộp Bài

Nộp toàn bộ source của `HitShop` và ba class trong package `model`.

Nộp link repository GitHub và ảnh chụp hoặc nội dung output của giỏ chuẩn có tổng `350000`.

## III. Bài Tự Chọn — Mở Rộng

1. Bổ sung field `private static int count` trong `Product` để đếm số object `Product` đã tạo.

   Constructor tăng `count` sau mỗi lần tạo object.

   Method `public static int getCount()` trả về giá trị hiện tại.

   Sau khi tạo `P01` và `P02`, `Product.getCount()` phải trả về `2`.

2. Thêm kiểm tra trong setter của `Product`:

   - `name` không được rỗng.
   - `price` phải lớn hơn `0`.
   - `stock` không được âm.

   Phần bắt buộc chưa yêu cầu xử lý exception cho dữ liệu nhập từ bàn phím.

3. Lập bảng so sánh `private`, default, `protected`, `public` khi thành viên được dùng trong cùng class, cùng package, class con khác package hoặc nơi khác.

   Các field bắt buộc của ba class model vẫn phải là `private`.

## IV. Tổng Kết Bài Tập Về Nhà Buổi 03

Sau bài tập buổi 3, dữ liệu của chương trình không còn nằm trong ba danh sách song song.

Mỗi loại dữ liệu được biểu diễn bằng class riêng; `HitShop` quản lý một khách hàng và một `List<CartItem>`.

### 1. Các Test Case Phải Chạy Được

Mỗi test case bắt đầu từ một lần chạy mới, trừ khi phần thao tác ghi rõ các bước liên tiếp.

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC03-01` | Khi giỏ đang rỗng, chọn `3`, sau đó chọn `4` | In khách hàng hiện tại, in `Giỏ hàng đang trống.`, tổng tiền bằng `0` |
| `TC03-02` | Chọn `1`, nhập mã không phải `P01` hoặc `P02` | In `Không tìm thấy sản phẩm.` và giỏ vẫn rỗng |
| `TC03-03` | Chọn `1`, nhập `P01` với số lượng `0` hoặc số âm | In `Số lượng phải lớn hơn 0.` và giỏ vẫn rỗng |
| `TC03-04` | Thêm lần lượt `P01 × 2` và `P02 × 1`, sau đó chọn `3` và `4` | Giỏ có đúng hai dòng theo output mẫu; tổng tiền bằng `350000` |
| `TC03-05` | Thêm `P01 × 1` hai lần | Giỏ có hai `CartItem` riêng, mỗi dòng tham chiếu sản phẩm `P01`; không gộp thành một dòng |
| `TC03-06` | Khi giỏ có hai dòng, yêu cầu xóa số thứ tự không tồn tại | In `Không tìm thấy mặt hàng.` và hai dòng trong giỏ không thay đổi |
| `TC03-07` | Từ giỏ của `TC03-04`, xóa dòng số `1` | Chỉ còn `P02 × 1`; tổng tiền bằng `50000` |
| `TC03-08` | Nhập một lựa chọn menu khác `0`, `1`, `2`, `3`, `4` | In `Lựa chọn không hợp lệ.` rồi tiếp tục hiển thị menu |
| `TC03-09` | Chọn `0` | Chương trình kết thúc |

Ngoài các test case console, cần kiểm tra `Product.toString()`, `Customer.toString()` và `CartItem.getTotal()` trả đúng định dạng hoặc giá trị đã mô tả.

Khi gọi `setQuantity(...)` trên một `CartItem`, giá trị `stock` của `Product` mà dòng đó tham chiếu phải giữ nguyên.

### 2. Cây Thư Mục Sau Buổi 03

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Customer.java
    └── CartItem.java
```
