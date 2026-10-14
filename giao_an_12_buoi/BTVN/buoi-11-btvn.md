# Bài Tập Về Nhà — Buổi 11

## Bài Bắt Buộc — Form Và Bảng Product Bằng Swing

### Kế Thừa Từ Buổi 10

Tiếp tục repository `HitShop`, class `HitShop` và `ProductService`/repository đã có. Thêm cửa sổ Swing làm View thay cho màn hình console; lấy Product từ cùng `ProductService`, không tạo danh sách sản phẩm hoặc repository riêng cho giao diện.

### Bài Toán

Tạo một cửa sổ desktop để xem danh sách sản phẩm trong cửa hàng. Phần bắt buộc chỉ dựng bố cục và hiển thị dữ liệu; chưa yêu cầu thao tác thêm/xóa khi bấm nút.

### Cấu Trúc Giao Diện

- Một `JFrame` tiêu đề `Quản lý sản phẩm`, kích thước tối thiểu 700×450 px, đóng cửa sổ thì ứng dụng kết thúc.
- Một `JPanel` ở phía trên chứa nhãn `Tên sản phẩm`, một `JTextField` để trống khi mở ứng dụng, nhãn `Giá`, một `JTextField` để trống và một `JButton` chữ `Thêm`.
- Một `JTable` phía dưới nằm trong `JScrollPane`; có đúng các cột theo thứ tự `Mã`, `Tên sản phẩm`, `Đơn giá`, `Tồn kho`.
- Bảng khi mở hiển thị Product do `ProductService.findAllProducts()` trả về, theo dữ liệu shop hiện có: `P01 | Áo thun | 150000 | 20`, `P02 | Bình nước | 50000 | 10`, `P03 | Java cơ bản | 120000 | 5`. Không tự tạo dữ liệu thay thế cho repository.
- Cửa sổ phải hiển thị đồng thời form và bảng; nhãn/cột không bị cắt ở kích thước quy định.
- Bắt buộc có JFrame, JPanel, JTextField, JButton, JTable; phần này không yêu cầu gắn event handler. Kết nối dữ liệu vẫn dùng repository/service đã có.
- Trong bản bắt buộc, bấm nút `Thêm` không làm thay đổi bảng; tương tác chỉ thuộc phần tự chọn bên dưới.

### Nộp Bài Theo Git

Repository nhóm dùng nhánh `main`, `develop` và nhánh chức năng có tên `feature/product-window`. Thay đổi được commit với message bắt đầu bằng `feat:` và tạo pull request vào `develop`. PR có tiêu đề `Add product management window`, mô tả chức năng và ảnh cửa sổ đang chạy. Xử lý mọi comment review trước khi merge theo quy ước nhóm.

## Mở Rộng — Bài Tự Chọn Event Handling

Mốc bàn giao: giữ `HitShop`, `ProductWindow` và ProductService. Buổi 12 tiếp tục cùng ứng dụng và các hợp đồng repository/service; không khởi tạo project/repository mới.

Nút `Thêm` nhận tên và giá, gọi `ProductService.addProduct` để tạo mã mới tiếp sau mã lớn nhất hiện có (với dữ liệu chuẩn, mã đầu tiên là P04); tồn kho mặc định là 0. Tên rỗng hoặc giá không phải số dương thì bảng không đổi và giao diện báo lỗi. Bài mở rộng này thêm tương tác tiện ích; form/bảng ở phần bắt buộc đã đạt yêu cầu độc lập.
