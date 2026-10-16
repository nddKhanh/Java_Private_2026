# Buổi 11: Git Flow, Pull Request, Code Review và Swing Cơ Bản

---

## Mục tiêu buổi học

- Tổ chức repository nhóm theo main, develop và feature branch.
- Mở pull request, review thay đổi và xử lý conflict cơ bản.
- Dựng giao diện Swing một form và một bảng với dữ liệu học tập mẫu.
- Nhận biết ranh giới giữa giao diện và xử lý dữ liệu; tích hợp service là phần minh họa thêm.

> **Cầu nối:** Thiết kế code tốt cần đi cùng cách phối hợp thay đổi và cách người dùng tương tác với chương trình. Buổi này tìm hiểu quy trình Git theo nhánh/nhóm và những thành phần giao diện Swing cơ bản; hai chủ đề có thể học độc lập.

## I. Git Flow Và Chia Việc

- `main` giữ phiên bản ổn định; `develop` tích hợp phần đã review; mỗi chức năng có feature branch.
- Commit nhỏ, mô tả đúng thay đổi; pull trước khi bắt đầu; push branch và tạo pull request.
- Đặt tên branch ngắn theo chức năng; commit message nêu hành động và đối tượng thay đổi, ví dụ `feat: add score table`.
- Reviewer kiểm tra mục đích, phạm vi, code dễ hiểu và ảnh hưởng tới chức năng cũ.
- Khi conflict, mở từng file, hiểu cả hai thay đổi rồi mới chọn/kết hợp; không xóa marker một cách máy móc.

Branch là nhánh phát triển độc lập để công việc chưa hoàn tất không ảnh hưởng bản ổn định. Commit tạo mốc thay đổi; push đưa commit lên remote; PR là đề xuất tích hợp kèm thảo luận/review, chưa có nghĩa là đã merge. Conflict xảy ra khi Git không thể tự kết hợp chỉnh sửa chồng lấn; cần hiểu ý định cả hai phía rồi kiểm tra kết quả.

~~~text
feature/score-table ────┐
                      ├── PR/merge → develop → PR/merge → main
feature/score-form ────┘
~~~

Đây là luồng làm việc minh họa, không phải cây phân cấp cố định: các branch được tạo từ một commit nền và mỗi nhóm có thể cập nhật branch của mình theo quy ước. PR đề xuất ghép thay đổi vào `develop`; sau khi tích hợp và kiểm tra, nhóm mới đưa phiên bản ổn định sang `main`.

Quy trình: tạo branch → sửa một chức năng → commit/push → mở PR vào develop → review → sửa → merge theo quy ước nhóm.

## II. Swing: Form Và Bảng

Swing có sẵn trong JDK. `JFrame` là cửa sổ; `JPanel` nhóm component; `JTextField` nhận text; `JButton` là nút; `JTable` hiển thị dữ liệu dạng bảng.

~~~java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

public class ScoreWindowDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Theo dõi điểm học tập");
            JPanel panel = new JPanel();
            panel.add(new JLabel("Tên học viên"));
            JTextField nameField = new JTextField(16);
            panel.add(nameField);
            panel.add(new JLabel("Điểm trung bình"));
            JTextField scoreField = new JTextField(10);
            panel.add(scoreField);
            JButton searchButton = new JButton("Tìm");
            panel.add(searchButton);

            DefaultTableModel tableModel = new DefaultTableModel(
                    new Object[][] {{"S01", "Lan", 8.5, 3}},
                    new String[] {"Mã học viên", "Họ tên", "Điểm TB", "Số bài"}
            );
            JTable scoreTable = new JTable(tableModel);

            frame.add(panel, BorderLayout.NORTH);
            frame.add(new JScrollPane(scoreTable), BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 450);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
~~~

`JTable` chỉ trình bày các hàng/cột; dữ liệu hiển thị nằm trong `TableModel`. Dùng `DefaultTableModel` cho ví dụ nhỏ vì dễ nạp dòng; khi cần ràng buộc dữ liệu chặt hoặc đọc trực tiếp từ domain model, có thể viết model riêng. `SwingUtilities.invokeLater` đưa thao tác tạo/cập nhật giao diện lên Event Dispatch Thread (EDT), luồng chuyên xử lý sự kiện UI. Truy vấn lâu không nên chạy trực tiếp trên EDT vì cửa sổ sẽ đứng; xử lý nền là chủ đề concurrency, chỉ cần ghi nhớ giới hạn này.

Ví dụ này dùng dữ liệu mẫu để tập trung vào cấu trúc giao diện. `JTable` hiển thị dữ liệu theo hàng/cột thông qua `TableModel`; bảng không tự lưu hay cập nhật nguồn dữ liệu bên ngoài. Layout manager quyết định cách sắp xếp component khi cửa sổ đổi kích thước. Bắt event nút và nối với một service là phần mở rộng tùy thời lượng, không phải điều kiện để hiểu form/bảng.

`JButton` phát sinh sự kiện khi được nhấn; `JTextField` nhận chuỗi nhập. Lộ trình chỉ yêu cầu dựng form/bảng. `ActionListener` và kết nối thao tác Thêm/Xóa tới service thuộc phần mở rộng, vì vậy phần bắt buộc có thể dùng dữ liệu mẫu để tập trung vào component và cấu trúc giao diện.

**Bài tập mở rộng:** Bắt sự kiện nút Thêm, đọc tên từ JTextField và thêm một dòng vào TableModel; nếu đã có service thì lấy dữ liệu qua service thay vì truy cập JDBC từ giao diện. Có thể bỏ phần này mà form/bảng cơ bản và bài tập lớn vẫn hoàn thành được.

~~~text
+-------------------------- JFrame ------------------------------------+
| JPanel: [Tên học viên ______] [Điểm TB ______] [Tìm]                 |
|----------------------------------------------------------------------|
| JScrollPane                                                          |
|   JTable: Mã học viên | Họ tên | Điểm TB | Số bài                     |
|          ...                                                          |
+------------------------------------------------------------------------+
~~~

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

## Tổng Kết Buổi 11

- Branch giúp tách công việc; commit tạo mốc; pull request hỗ trợ review và trao đổi trước khi merge.
- Swing ghép component trong JFrame; JTable trình bày dữ liệu do TableModel quản lý.
- Bắt sự kiện và nối giao diện với service là bước kế tiếp, được để ở phần mở rộng.

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
