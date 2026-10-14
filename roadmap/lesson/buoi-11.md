# Buổi 11: Git Flow, Pull Request, Code Review và Swing Cơ Bản

---

## Mục tiêu buổi học

- Tổ chức repository nhóm theo main, develop và feature branch.
- Mở pull request, review thay đổi và xử lý conflict cơ bản.
- Dựng giao diện Swing một form và một bảng cho quản lý sản phẩm.
- Nhận biết ranh giới giữa giao diện và xử lý dữ liệu; tích hợp service là phần minh họa thêm.

> **Nối tiếp:** Buổi 10 refactor ứng dụng theo tầng. Buổi này chuẩn bị làm nhóm và có giao diện đơn giản để demo; trọng tâm là quy trình Git, Swing giới hạn ở một form và một bảng.

## I. Git Flow Và Chia Việc

- `main` giữ phiên bản ổn định; `develop` tích hợp phần đã review; mỗi chức năng có feature branch.
- Commit nhỏ, mô tả đúng thay đổi; pull trước khi bắt đầu; push branch và tạo pull request.
- Đặt tên branch ngắn theo chức năng; commit message nêu hành động và đối tượng thay đổi, ví dụ `feat: add product table`.
- Reviewer kiểm tra mục đích, phạm vi, code dễ hiểu và ảnh hưởng tới chức năng cũ.
- Khi conflict, mở từng file, hiểu cả hai thay đổi rồi mới chọn/kết hợp; không xóa marker một cách máy móc.

Branch là nhánh phát triển độc lập để công việc chưa hoàn tất không ảnh hưởng bản ổn định. Commit tạo mốc thay đổi; push đưa commit lên remote; PR là đề xuất tích hợp kèm thảo luận/review, chưa có nghĩa là đã merge. Conflict xảy ra khi Git không thể tự kết hợp chỉnh sửa chồng lấn; cần hiểu ý định cả hai phía rồi kiểm tra kết quả.

> **Hình minh họa cần bổ sung:** Sơ đồ main/develop/feature với hướng PR và merge.

~~~text
main
  └── develop
        ├── feature/product-table
        └── feature/product-form
~~~

Quy trình: tạo branch → sửa một chức năng → commit/push → mở PR vào develop → review → sửa → merge theo quy ước nhóm.

## II. Swing: Form Và Bảng

Swing có sẵn trong JDK. `JFrame` là cửa sổ; `JPanel` nhóm component; `JTextField` nhận text; `JButton` là nút; `JTable` hiển thị dữ liệu dạng bảng.

~~~java
JFrame frame = new JFrame("Quản lý sản phẩm");
JPanel panel = new JPanel();
JTextField nameField = new JTextField(16);
JButton addButton = new JButton("Thêm");
JTable productTable = new JTable(tableModel);

panel.add(nameField);
panel.add(addButton);
frame.add(panel, BorderLayout.NORTH);
frame.add(new JScrollPane(productTable), BorderLayout.CENTER);
frame.pack();
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.setVisible(true);
~~~

Buổi chính dựng form và bảng, có thể nạp dữ liệu mẫu hoặc đọc qua ProductService. Giao diện không được gọi JDBC trực tiếp.

`JTable` hiển thị dữ liệu theo hàng/cột thông qua `TableModel`; bảng không tự lưu dữ liệu database. Layout manager quyết định cách sắp xếp component khi cửa sổ đổi kích thước. Swing dùng event dispatch thread để cập nhật giao diện; tác vụ lâu không nên chặn luồng này (chỉ lưu ý, chưa đi sâu concurrency). Bắt event nút và nối service là phần mở rộng tùy thời lượng, không phải điều kiện nắm form/bảng.

`JButton` phát sinh sự kiện khi được nhấn; `JTextField` nhận chuỗi nhập. Lộ trình chỉ yêu cầu dựng form/bảng. `ActionListener` và kết nối thao tác Thêm/Xóa tới service thuộc phần mở rộng, vì vậy phần bắt buộc có thể dùng dữ liệu mẫu để tập trung vào component và cấu trúc giao diện.

**Bài tập mở rộng:** Bắt sự kiện nút Thêm, đọc tên từ JTextField và thêm một dòng vào TableModel; nếu đã có service thì lấy dữ liệu qua service thay vì truy cập JDBC từ giao diện. Có thể bỏ phần này mà form/bảng cơ bản và bài tập lớn vẫn hoàn thành được.

> **Hình minh họa cần bổ sung:** Wireframe form nhập sản phẩm phía trên JTable, chú thích JFrame/JPanel/JTable.

## III. Thực Hành Git Nhóm

1. Tạo branch feature riêng cho form và bảng.
2. Hai nhánh sửa cùng file để cố ý tạo conflict.
3. Pull, resolve conflict, chạy lại chức năng và commit kết quả.
4. Mở PR, ghi mục đích và cách review, nhận góp ý rồi cập nhật.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Commit thẳng vào main | Không theo quy ước nhóm | Làm trên feature branch |
| Conflict bị ghi đè | Chọn một phía mà không đọc | So sánh hai thay đổi và kiểm tra kết quả |
| JTable không hiện dữ liệu | TableModel chưa được nạp/cập nhật | Kiểm tra model và gọi refresh phù hợp |
| UI chạy JDBC trực tiếp | Bỏ qua service/repository | Gọi service từ controller/event handler |

## Mốc Project Sau Buổi 11

- Repo có quy trình branch/PR để cùng làm; có form và bảng sản phẩm demo.
- Chức năng Add/Delete bằng nút là phần mở rộng; buổi 12 chốt đề tài, repo và tiêu chí bài tập lớn.

### Câu Hỏi Ôn Tập

1. Feature branch giúp nhóm điều gì?
2. Pull request dùng để làm gì?
3. Nên xử lý conflict theo thứ tự nào?
4. JFrame, JPanel và JTable dùng cho phần nào?
5. Vì sao Swing event không nên tự truy cập JDBC?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 11](../BTVN/buoi-11-btvn.md).
## Tài Liệu Tham Khảo

- [GitHub Docs — About Pull Requests](https://docs.github.com/en/pull-requests/reference/pull-requests)
- [Oracle — Creating a GUI With Swing](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Oracle — How to Use Tables](https://docs.oracle.com/javase/tutorial/uiswing/components/table.html)
