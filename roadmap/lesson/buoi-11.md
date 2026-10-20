# Buổi 11: Git Flow, Pull Request, Code Review Và Swing Cơ Bản

---

> **Hạn chế từ buổi 10:** code đã được phân tầng nhưng khi nhiều người cùng sửa một repository vẫn dễ đè thay đổi, conflict và thiếu review; giao diện console cũng chưa minh họa cách tái sử dụng Service trong desktop UI.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Làm việc theo nhánh `main`, `develop`, `feature/*` và tạo pull request.
- Xử lý một conflict nhỏ mà không mất thay đổi của người khác.
- Review theo hành vi, kiến trúc và khả năng chạy, không chỉ nhìn format.
- Tạo JFrame, JPanel, JTextField, JButton, JTable và JScrollPane.
- Hiển thị Product từ cùng ProductService của buổi 10 trong Swing.

> **Kết quả cuối buổi:** một feature branch tạo được cửa sổ Swing lấy dữ liệu qua Service và được đưa vào `develop` bằng pull request.

## I. Git Flow Phục Vụ Việc Chia Nhỏ Thay Đổi

Quy ước của lớp:

```text
main       phiên bản ổn định để demo
develop    nơi tích hợp chức năng đã review
feature/*  một thay đổi cụ thể đang làm
```

Tạo nhánh từ develop mới nhất:

```bash
git switch develop
git pull
git switch -c feature/product-ui
```

Sau khi hoàn thành một phần chạy được:

```bash
git status
git add src/main/java/view/ProductManagementFrame.java
git commit -m "feat: add product management window"
git push -u origin feature/product-ui
```

Commit nên mô tả một thay đổi có nghĩa. Không gom file cấu hình bí mật, thư mục build và nhiều chức năng không liên quan vào cùng commit.

## II. Pull Request Và Review Có Tiêu Chí

PR `Add product management window` cần có:

- Mục tiêu và phạm vi.
- Cách chạy.
- Ảnh cửa sổ.
- Test case đã kiểm tra.
- Điểm chưa làm, ví dụ nút Add chưa có event.

Reviewer kiểm tra theo thứ tự:

1. Có build/run trên JDK 17 không?
2. Bảng có lấy dữ liệu qua ProductService không?
3. Swing View có gọi JDBC trực tiếp không?
4. View Swing có làm thay đổi hành vi console cũ không?
5. Tên/format có làm code khó đọc không?

Nhận xét nên chỉ vị trí, vấn đề và kết quả mong đợi. Ví dụ: “View Swing đang tự tạo danh sách dữ liệu trong constructor; hãy inject ProductService và dùng `findAllProducts()` để UI hiển thị từ cùng nguồn.”

## III. Xử Lý Conflict Nhỏ

Khi develop thay đổi trong lúc feature đang làm:

```bash
git fetch origin
git merge origin/develop
```

Git đánh dấu:

```text
  <<<<<<< HEAD
phần trên nhánh feature
  =======
phần trên develop
  >>>>>>> origin/develop
```

Người xử lý phải hiểu cả hai ý định, tạo nội dung cuối đúng rồi xóa marker. Sau đó:

```bash
git add <file-da-xu-ly>
git commit
```

Trước khi push lại, build và chạy test case. Conflict được “resolve” về mặt Git chưa có nghĩa code đúng về mặt nghiệp vụ.

## IV. Swing Và Event Dispatch Thread

Khởi động UI:

```java
SwingUtilities.invokeLater(() -> {
    ProductManagementFrame window = new ProductManagementFrame(productService);
    window.setVisible(true);
});
```

Swing component nên được tạo và cập nhật trên Event Dispatch Thread. Truy vấn database dài không nên chặn thread này trong ứng dụng thật; ở buổi nhập môn, dataset nhỏ và mục tiêu là dựng View đúng kiến trúc.

## V. Dựng Một Cửa Sổ Quản Lý Product

```java
public class ProductManagementFrame extends JFrame {
    private final ProductService productService;
    private final DefaultTableModel tableModel;

    public ProductManagementFrame(ProductService productService) {
        this.productService = productService;

        setTitle("Quản lý sản phẩm");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel formPanel = new JPanel();
        JTextField nameField = new JTextField(15);
        JTextField priceField = new JTextField(10);
        JButton addButton = new JButton("Thêm");
        addButton.setEnabled(false);
        addButton.setToolTipText("Thực hiện ở phần mở rộng");

        formPanel.add(new JLabel("Tên sản phẩm"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Giá"));
        formPanel.add(priceField);
        formPanel.add(addButton);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã", "Tên sản phẩm", "Đơn giá", "Tồn kho"}, 0);
        JTable table = new JTable(tableModel);

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadProducts();
    }
}
```

Nút Add bị disabled trong phần lõi để giao diện không hứa một hành vi chưa tồn tại. Phần mở rộng sẽ kích hoạt nút và gắn listener.

## VI. Nạp Dữ Liệu Qua Service

```java
private void loadProducts() {
    tableModel.setRowCount(0);
    for (Product product : productService.findAllProducts()) {
        tableModel.addRow(new Object[]{
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        });
    }
}
```

Không khởi tạo dữ liệu mẫu ngay trong cửa sổ. Nếu console và Swing dùng hai list khác nhau, hai giao diện sẽ cho kết quả không nhất quán. ProductService là nguồn use case chung; repository phía sau có thể là JDBC hoặc memory khi demo.

Giáo viên chuẩn bị ba Product bất kỳ trong repository demo để kiểm tra bảng. Dữ liệu này chỉ phục vụ buổi học và không phụ thuộc kết quả BTVN của học viên.

## VII. Event Handling Là Phần Mở Rộng Có Kiểm Soát

Khi thêm event:

```java
addButton.addActionListener(event -> handleAddProduct());
```

`handleAddProduct()` đọc text, parse giá, gọi ProductService rồi reload bảng. Không viết INSERT SQL trong listener. Input sai giữ nguyên bảng và hiện thông báo rõ.

Mã P04 không nên được tạo bằng `products.size() + 1` nếu danh sách có thể xóa. Đây là một điểm thảo luận về chiến lược id; bài mở rộng dùng “mã lớn nhất + 1” cho dữ liệu lớp nhỏ.

## VIII. Thực Hành Nhóm

1. Một thành viên tạo layout và commit.
2. Một thành viên nối ProductService và commit riêng.
3. Thành viên khác review PR theo checklist.
4. Giáo viên tạo một thay đổi nhỏ trên develop để nhóm thực hành conflict.
5. Nhóm chạy lại console và Swing trước khi merge.

Mỗi nhóm phải cho thấy `git log --oneline --graph --all` và giải thích nhánh nào chứa thay đổi nào.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| Làm trực tiếp trên develop | Khó review/rollback | Tạo feature branch |
| Resolve conflict bằng cách chọn toàn bộ một phía | Mất thay đổi bên kia | Hiểu ý định, ghép nội dung cuối rồi test |
| Swing tự tạo dữ liệu mẫu | Hai nguồn dữ liệu | Inject ProductService |
| JTable không nằm trong JScrollPane | Khó cuộn/thiếu header | Bọc table bằng JScrollPane |
| Nút hoạt động giả | Người dùng hiểu sai | Disabled cho đến khi có handler |
| SQL trong ActionListener | View gắn chặt database | Gọi Service |

## Tổng kết buổi 11

Git flow làm thay đổi nhỏ, review được và tích hợp có kiểm soát. Swing chỉ là một View mới; kiến trúc đúng cho phép nó dùng lại Service/Repository từ console. Một UI đơn giản nhưng dùng đúng nguồn dữ liệu có giá trị hơn một form nhiều nút nhưng bỏ qua tầng nghiệp vụ.

## Bài tập nhỏ trên lớp — Cửa sổ danh bạ

Thực hiện trên nhánh `feature/contact-window`:

- Tạo JFrame có hai JTextField `Tên`, `Số điện thoại`, một JButton disabled và JTable ba cột `Mã`, `Tên`, `Số điện thoại`.
- Dữ liệu bảng lấy từ `ContactService.findAll()`, không tạo list riêng trong JFrame.
- Bọc JTable trong JScrollPane và bảo đảm form/bảng cùng hiển thị ở kích thước 600×400.
- Commit với message `feat: add contact window` và chuẩn bị mô tả pull request ngắn.

**Nghiệm thu:** cửa sổ hiển thị ít nhất hai contact do Service trả về; không có SQL hoặc dữ liệu hard-code trong View. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 11](../BTVN/buoi-11-btvn.md) bằng PR vào develop. Phần event handler là tự chọn.
