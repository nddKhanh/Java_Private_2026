# Bài Tập Về Nhà — Buổi 10

## I. Điểm Bắt Đầu Từ Buổi 09

Giữ kiến trúc View → Controller → Service → Repository và toàn bộ hành vi console.
Swing là View thứ hai, dùng cùng ProductService.

### 1. Các Ca Cũ Phải Còn Đạt

| Ca | Kết quả cần giữ |
|---|---|
| CRUD Product | Đúng qua console |
| Xem Order 1 | Tổng lịch sử 350000 |
| Đổi JDBC/memory repository | ProductService không đổi |

### 2. Cây Thư Mục Đầu Buổi

Dùng nguyên cây cuối [BTVN Buổi 09](buoi-09-btvn.md).

## II. Bài Bắt Buộc

### 1. Git

- Nhánh: `main`, `develop`, `feature/product-window`.
- Commit bắt đầu bằng `feat:` và chỉ chứa thay đổi liên quan.
- Pull request vào `develop`: tiêu đề `Add product management window`.
- PR có mục tiêu, cách chạy, ảnh cửa sổ, test case và giới hạn chưa làm.
- Xử lý comment review trước khi merge.

### 2. ProductWindow

Tạo `ProductWindow extends JFrame`, nhận `ProductService` qua constructor.

Giao diện:

- Tiêu đề `Quản lý sản phẩm`, kích thước 700×450.
- Đóng cửa sổ bằng `JFrame.EXIT_ON_CLOSE` và căn giữa bằng `setLocationRelativeTo(null)`.
- JPanel phía trên: tên, giá, hai JTextField rỗng, JButton `Thêm` đang disabled.
- JTable trong JScrollPane, đúng bốn cột: `Mã`, `Tên sản phẩm`, `Đơn giá`, `Tồn kho`.
- `private void loadProducts()` gọi `productService.findAllProducts()`.
- Không có SQL, repository hoặc list Product riêng trong Swing View.
- Khởi động bằng `SwingUtilities.invokeLater(...)`.

Dữ liệu hiển thị:

- P01/Áo thun/160000/20.
- P02/Bình nước/50000/10.
- P03/Java cơ bản/120000/5.

![Sơ đồ lớp BTVN buổi 10](assets/buoi-10-class-diagram.svg)

## III. Mở Rộng Tự Chọn — Event Handling

Kích hoạt nút Thêm:

- Đọc tên và giá.
- Gọi ProductService với stock mặc định `0`; không viết SQL trong listener.
- Mã mới tiếp sau mã lớn nhất hiện có.
- Input sai giữ nguyên bảng và hiển thị lỗi.
- Thành công thì reload bảng.

## IV. Nộp Bài

Nộp source, URL PR, ảnh cửa sổ và output chạy lại console.
Không merge nếu chưa được review theo quy ước nhóm.

## V. Nghiệm Thu Buổi 10

### 1. Test Case

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC10-01` | Mở ProductWindow 700×450 | Form/bảng hiển thị đủ |
| `TC10-02` | Kiểm tra trạng thái đầu | Text field rỗng, nút disabled |
| `TC10-03` | Kiểm tra JTable | Đúng bốn cột và có JScrollPane |
| `TC10-04` | Nạp qua ProductService | Đúng P01, P02, P03 |
| `TC10-05` | Đổi dữ liệu repository rồi reload | Bảng phản ánh dữ liệu mới |
| `TC10-06` | Kiểm tra source View | Không SQL/repository/list riêng |
| `TC10-07` | Chạy lại console | Chức năng cũ không đổi |
| `TC10-08` | Kiểm tra Git | Đúng branch, commit, PR, review |

### 2. Cây Thư Mục Cuối Buổi

```text
src/main/java/
├── app/HitShop.java
├── controller/ShopController.java
├── view/
│   ├── ConsoleView.java
│   └── ProductWindow.java
├── dto/OrderItemInput.java
├── service/
├── repository/
│   └── jdbc/
└── model/
```
