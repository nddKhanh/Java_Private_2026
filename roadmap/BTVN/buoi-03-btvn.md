# Bài Tập Về Nhà — Buổi 03

## Bài Bắt Buộc — Mô Hình Hóa Mặt Hàng Và Giỏ Hàng Bằng Object

### Kế Thừa Từ Buổi 02

Tiếp tục `HitShop.java`, menu và dữ liệu giỏ của repository cũ bằng JDK 17. Bổ sung mã `P01`, `P02` khi tạo Product. Giữ tên bốn method `addItem`, `removeItem`, `printItems`, `calculateTotal`; đổi tham số và dữ liệu bên trong từ ba list song song sang `Product`/`CartItem`.

### Bài Toán

Thay dữ liệu rời của giỏ hàng buổi 2 bằng các object có trách nhiệm riêng. Chương trình vẫn chạy trên console; phạm vi là một danh sách sản phẩm có sẵn và một giỏ hàng của một khách hàng.

### Các Class Và Quan Hệ Bắt Buộc

- `Product`: field `id`, `name`, `price`, `stock`, đều private. Constructor nhận đủ bốn giá trị. Có getter cho cả bốn field; chỉ `name`, `price`, `stock` có setter. `toString()` trả dạng `P01 | Áo thun | 150000.0 | tồn: 20`.
- `Customer`: field `id`, `name`, `phone`, đều private. Constructor nhận đủ ba giá trị; có getter cho cả ba và setter cho `name`, `phone`. `toString()` trả dạng `C01 | Linh Nguyễn | 0900000001`.
- `CartItem`: field `product` kiểu `Product` và `quantity` kiểu `int`, đều private. Constructor nhận hai giá trị; có `getProduct()`, `getQuantity()`, `setQuantity(int quantity)` và `getTotal()` trả `product.getPrice() × quantity`.
- Quan hệ: `CartItem` HAS-A `Product`; giỏ hàng là `List<CartItem>`. Chương trình giữ đúng một biến `currentCustomer` kiểu Customer và một list giỏ hàng; không tạo class Cart hoặc lưu số lượng mua trong Product.
- Tạo package `model` cho ba class trên. Class chạy chương trình nằm ngoài package model và sử dụng các object thông qua public constructor/method.
- Tên class chạy chương trình vẫn là `HitShop`; giỏ được giữ trong một `List<CartItem>`.

### Sơ Đồ Lớp Và Quan Hệ

![Sơ đồ lớp BTVN buổi 03](assets/buoi-03-class-diagram.svg)

Hình thoi tại `HitShop` thể hiện chương trình giữ nhiều `CartItem`; mỗi `CartItem` tham chiếu đúng một `Product`. Không tạo thêm class `Cart`.

### Dữ Liệu Chuẩn Và Kết Quả

- Product: `P01 / Áo thun / 150000 / 20`; `P02 / Bình nước / 50000 / 10`.
- Customer: `C01 / Linh Nguyễn / 0900000001`.
- Giỏ mẫu: `P01 × 2`, `P02 × 1`. Khi in giỏ, mỗi dòng phải có mã/tên sản phẩm, số lượng và thành tiền; tổng giỏ là `350000`.
- Một `CartItem` chỉ chứa đúng một `Product` và một số lượng. Khi thay đổi quantity của CartItem, stock của Product không tự đổi.
- Nộp toàn bộ source theo đúng package.

### Nộp Bài

Nộp source của `HitShop` và ba class model; kèm output giỏ chuẩn có tổng `350000`.

## Nâng Cao — Bài Tự Chọn

- Bổ sung field `static int count` để đếm số đối tượng `Product` đã tạo. Constructor tăng `count` sau mỗi lần tạo đối tượng; method tĩnh `getCount()` trả về giá trị hiện tại. Sau khi tạo P01 và P02, `Product.getCount()` trả về `2`.
- Lập bảng phạm vi truy cập của các class, field và method khi được gọi trong cùng package hoặc từ package khác; các field bắt buộc của model vẫn là `private`.
