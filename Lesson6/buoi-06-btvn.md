# 🚀 BTVN BUỔI 06: EXCEPTION VÀ ĐỌC/GHI FILE — HITSHOP

## I. Tổng kết buổi trước

Sau buổi 5, chương trình **HitShop** có các thành phần:

- `Product` là abstract class; `GeneralProduct`, `Book` và `Cake` triển khai `isExpensive()`.
- `ProductService`, `OrderService` và các class triển khai quản lý sản phẩm, đơn hàng.
- `PaymentService` có hai cách triển khai: `MoMoPaymentService` và `BankPaymentService`.
- `Product` sử dụng `equals()` và `hashCode()` để so sánh theo `id`.

### Cấu trúc thư mục sau buổi 5

```text
src/
├── HitShop.java
├── Product.java
├── GeneralProduct.java
├── Book.java
├── Cake.java
├── Customer.java
├── Order.java
├── ProductService.java
├── ProductServiceImpl.java
├── OrderService.java
├── OrderServiceImpl.java
├── PaymentService.java
├── MoMoPaymentService.java
└── BankPaymentService.java
```

## II. Giới thiệu bài tập

Tiếp tục phát triển **HitShop** từ bài tập buổi 5 với ba công việc:

- Xử lý trường hợp người dùng nhập lựa chọn menu không phải số nguyên.
- Cập nhật các vị trí kiểm tra dữ liệu và thông báo lỗi bằng Exception.
- Bổ sung chức năng ghi danh sách đơn hàng ra file và đọc nội dung file.

## III. Yêu cầu bài tập

### 1. Xử lý lỗi nhập lựa chọn menu

Cập nhật phần nhập lựa chọn menu trong `HitShop.java`:

- Sử dụng `try/catch` để bắt lỗi khi chuyển dữ liệu nhập thành số nguyên.
- Khi nhập `abc`, `1.5` hoặc chuỗi không phải số nguyên, in `Lựa chọn phải là số nguyên.`.
- Sau thông báo lỗi, hiển thị lại menu và cho phép nhập lựa chọn mới.
- Với số nguyên không thuộc các lựa chọn menu, giữ thông báo `Lựa chọn không hợp lệ.`.

### 2. Cập nhật xử lý lỗi trong các chức năng hiện có

Thay các nhánh kiểm tra và in thông báo lỗi hiện có bằng `throw` tại nơi phát hiện lỗi và `try/catch` tại nơi gọi thao tác.

**Các vị trí cần cập nhật:**

| Trường hợp | Nơi `throw` exception | Nơi `try/catch` và in thông báo |
|---|---|---|
| Không tìm thấy sản phẩm theo ID | `ProductServiceImpl.findById(String id)` | `HitShop.java`, tại chức năng kiểm tra sản phẩm có đắt hay không (lựa chọn `5`), bao quanh lời gọi `productService.findById(id)` |
| Không tìm thấy đơn hàng khi sửa | `OrderServiceImpl.updateOrder(String id)` | `HitShop.java`, tại chức năng sửa đơn hàng (lựa chọn `3`), bao quanh lời gọi `orderService.updateOrder(id)` |
| Không tìm thấy đơn hàng khi xóa | `OrderServiceImpl.deleteOrder(String id)` | `HitShop.java`, tại chức năng xóa đơn hàng (lựa chọn `4`), bao quanh lời gọi `orderService.deleteOrder(id)` |
| Dữ liệu không hợp lệ tại các vị trí đã kiểm tra trước đó | Phương thức đang chịu trách nhiệm kiểm tra dữ liệu trong `ProductServiceImpl` hoặc `OrderServiceImpl` | `HitShop.java`, tại chức năng gọi phương thức tương ứng |

Yêu cầu:

- Giữ `if` để phát hiện điều kiện lỗi và sử dụng `throw` exception phù hợp; bỏ lệnh in thông báo lỗi cũ tại vị trí này.
- Trong `HitShop.java`, đặt `try/catch` quanh lời gọi của từng chức năng tương ứng. `catch` in thông báo lỗi thay cho vị trí in cũ.
- Khi xảy ra exception, không thực hiện phần còn lại của thao tác thất bại; sau khi `catch`, chương trình tiếp tục hiển thị menu.
- Các thao tác hợp lệ giữ nguyên kết quả như buổi 5.

### 3. Bổ sung chức năng ghi và đọc file

Bổ sung hai lựa chọn vào menu HitShop:

```text
7. Lưu danh sách đơn hàng ra file
8. Đọc danh sách đơn hàng từ file
```

**Ghi file**

- Khi chọn `7`, ghi danh sách đơn hàng hiện tại vào file `orders.txt` trong thư mục chạy chương trình.
- Mỗi đơn hàng được ghi trên một dòng, gồm mã đơn hàng, mã khách hàng, mã sản phẩm và số lượng.
- Mỗi lần lưu, nội dung file được cập nhật theo danh sách đơn hàng hiện tại.
- Sử dụng UTF-8 và `try-with-resources`.
- Sau khi ghi thành công, in `Lưu danh sách đơn hàng thành công.`.
- Nếu ghi thất bại, bắt `IOException` và in `Không thể ghi file.`.

Dữ liệu file mẫu:

```text
O01|C01|P01|2
O02|C02|P02|1
```

**Đọc file**

- Khi chọn `8`, đọc file `orders.txt` và in từng dòng ra console.
- Sử dụng UTF-8 và `try-with-resources`.
- Nếu file không tồn tại hoặc không đọc được, hiển thị thông báo lỗi tương ứng.

## IV. Kết quả mong đợi

### Các trường hợp cần kiểm tra

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC06-01` | Nhập `abc` tại menu | In `Lựa chọn phải là số nguyên.` và hiển thị lại menu |
| `TC06-02` | Nhập `1.5` tại menu | In `Lựa chọn phải là số nguyên.` và hiển thị lại menu |
| `TC06-03` | Nhập `99` tại menu | In `Lựa chọn không hợp lệ.` và hiển thị lại menu |
| `TC06-04` | Tìm sản phẩm với ID không tồn tại | Bắt exception, in thông báo không tìm thấy sản phẩm |
| `TC06-05` | Sửa hoặc xóa đơn hàng với ID không tồn tại | Bắt exception, in thông báo không tìm thấy đơn hàng; dữ liệu không đổi |
| `TC06-06` | Nhập sai rồi thực hiện thao tác hợp lệ | Thao tác hợp lệ vẫn thực hiện được |
| `TC06-07` | Lưu hai đơn hàng hợp lệ bằng lựa chọn `7` | File `orders.txt` có hai dòng đúng dữ liệu |
| `TC06-08` | Chọn `8` sau khi lưu | In đúng hai dòng trong file |
| `TC06-09` | Đọc file không tồn tại | Hiển thị thông báo không tìm thấy file |
| `TC06-10` | Thực hiện lại thêm/xem/sửa/xóa, kiểm tra giá và thanh toán | Các chức năng buổi 5 hoạt động đúng |

### Cấu trúc thư mục sau buổi 6

```text
HitShop/
├── orders.txt
└── src/
    ├── HitShop.java             (Cập nhật)
    ├── Product.java
    ├── GeneralProduct.java
    ├── Book.java
    ├── Cake.java
    ├── Customer.java
    ├── Order.java
    ├── ProductService.java
    ├── ProductServiceImpl.java  (Cập nhật)
    ├── OrderService.java
    ├── OrderServiceImpl.java    (Cập nhật)
    ├── PaymentService.java
    ├── MoMoPaymentService.java
    └── BankPaymentService.java
```

## V. Nộp bài

- Source code HitShop sau khi cập nhật.
- File `orders.txt` có ít nhất hai đơn hàng.
- Kết quả chạy các trường hợp kiểm tra tại mục IV.

## VI. Nâng cao — Tách chức năng đọc/ghi file

Tạo class `OrderFileStore.java` để quản lý thao tác file.

Yêu cầu:

- Chuyển toàn bộ phần mở, đọc, ghi và đóng file từ `HitShop.java` sang `OrderFileStore.java`.
- Cung cấp hai phương thức ghi danh sách đơn hàng và đọc các dòng trong file.
- Các phương thức xử lý menu trong `HitShop` gọi đến `OrderFileStore` thay vì trực tiếp sử dụng `BufferedReader`, `BufferedWriter` hoặc `Files`.
- Giữ nguyên định dạng `orders.txt` và kết quả của các trường hợp kiểm tra tại mục IV.
