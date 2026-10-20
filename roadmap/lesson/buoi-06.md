# Buổi 6: Exception, try-with-resources Và Lưu Order Ra File

---

> **Hạn chế từ buổi 5:** chương trình mới tập trung vào mô hình và đa hình; dữ liệu sai hoặc lỗi I/O vẫn có thể làm luồng chạy dừng đột ngột, và dữ liệu trong bộ nhớ mất khi tắt ứng dụng.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Phân biệt lỗi biên dịch, lỗi runtime và lỗi nghiệp vụ.
- Dùng `throw`, `throws` và `try/catch` đúng vai trò; nhận biết custom exception như phần mở rộng.
- Dùng `Path`, `Files` và UTF-8 để ghi/đọc text.
- Đóng tài nguyên tự động bằng try-with-resources.
- Lưu Order theo một định dạng text nhỏ, đọc lại và tính tổng từ nội dung file.

> **Kết quả cuối buổi:** một Order mẫu hợp lệ được lưu thành khối trong `orders.txt`; order sai không được ghi; lỗi I/O không bị báo thành công.

## I. Ba Nhóm Lỗi Trong Một Chương Trình Quản Lý

### 1. Lỗi biên dịch

```java
double total = order.getTotals();
```

Nếu Order chỉ có `getTotal()`, compiler dừng trước khi chương trình chạy. Hãy đọc lỗi đầu tiên, sửa tên method rồi compile lại; các lỗi sau có thể chỉ là hệ quả.

### 2. Lỗi runtime kỹ thuật

```java
Files.readAllLines(Path.of("orders.txt"));
```

File có thể không tồn tại hoặc không đọc được. Đây là tình huống môi trường mà code phải xử lý tại ranh giới I/O.

### 3. Lỗi nghiệp vụ

Order không có item vẫn là một object Java hợp lệ về cú pháp, nhưng không hợp lệ để lưu như một đơn hàng. Trong phần lõi, dùng exception có sẵn:

```java
throw new IllegalArgumentException("Order phải có ít nhất một item");
```

Phân loại đúng giúp đặt xử lý đúng chỗ. Không bọc toàn bộ chương trình trong một `catch (Exception)` rồi in “Có lỗi”.

## II. `throw`, `throws` Và `catch`

Validator phát hiện và ném lỗi:

```java
public class OrderValidator {
    public static void validate(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order không được null");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order phải có ít nhất một item");
        }
        for (CartItem item : order.getItems()) {
            if (item.getProduct() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item không hợp lệ");
            }
        }
    }
}
```

- `throw` tạo luồng lỗi tại nơi phát hiện vi phạm.
- `throws` công bố cho code gọi biết method có thể thất bại.
- `catch` xử lý ở nơi có đủ ngữ cảnh để báo cho người dùng hoặc chọn hành động tiếp theo.

Không vừa `catch` rồi bỏ qua vừa tiếp tục như thành công. Nếu chưa xử lý được, hãy để exception đi lên tầng gọi.

## III. Định Dạng File Càng Nhỏ Càng Dễ Kiểm Tra

Buổi này không xây một serializer tổng quát. Mỗi Order được ghi thành khối:

```text
ORDER|DH01|KH01|Nguyễn An|PENDING
ITEM|SP01|Sổ tay|2|25000.0
ITEM|SP02|Bút|1|10000.0
END
```

Header giữ dữ liệu order/customer cần hiển thị. Mỗi dòng item giữ giá tại lúc lưu. `END` giúp nhận biết khối đã kết thúc.

Trước khi code, học viên trả lời ba câu:

1. Dòng nào biểu diễn bắt đầu một Order?
2. Làm sao phân biệt item với header?
3. Khi Product đổi giá, file cũ có đổi không?

Câu trả lời thứ ba chuẩn bị cho `unitPrice` trong OrderItem ở buổi 9.

## IV. Ghi File Bằng try-with-resources

```java
public void saveOrder(Order order, Path path)
        throws IOException {
    OrderValidator.validate(order);

    try (BufferedWriter writer = Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8)) {
        writer.write("ORDER|" + order.getId()
                + "|" + order.getCustomer().getId()
                + "|" + order.getCustomer().getName()
                + "|" + order.getStatus());
        writer.newLine();

        for (CartItem item : order.getItems()) {
            Product product = item.getProduct();
            writer.write("ITEM|" + product.getId()
                    + "|" + product.getName()
                    + "|" + item.getQuantity()
                    + "|" + product.getPrice());
            writer.newLine();
        }

        writer.write("END");
        writer.newLine();
    }
}
```

Tài nguyên được đóng cả khi một lệnh write ném exception. Không cần `finally` để tự close writer trong ví dụ này.

Validator chạy trước khi mở file. Vì vậy order sai không xóa nội dung cũ rồi mới phát hiện lỗi.

## V. Đọc File Và Báo Lỗi Đúng

```java
public List<String> loadLines(Path path) throws IOException {
    if (Files.notExists(path)) {
        return new ArrayList<>();
    }

    try (BufferedReader reader = Files.newBufferedReader(
            path,
            StandardCharsets.UTF_8)) {
        return reader.lines().toList();
    }
}
```

`Stream.toList()` dùng được trên JDK 17. Nếu muốn thực hành vòng lặp, có thể đọc từng dòng bằng `readLine()`.

Code giao diện xử lý thông báo:

```java
try {
    store.saveOrder(order, Path.of("orders.txt"));
    System.out.println("Lưu thành công");
} catch (IllegalArgumentException e) {
    System.out.println("Đơn không hợp lệ: " + e.getMessage());
} catch (IOException e) {
    System.out.println("Không thể ghi file đơn hàng: " + e.getMessage());
}
```

Thông báo thành công chỉ nằm sau lời gọi đã hoàn tất. Không đặt nó trong `finally`, vì finally chạy cả khi thất bại.

## VI. Tính Tổng Từ Các Dòng ITEM

Mỗi dòng ITEM có năm phần khi tách bằng `split("\\|")`. Quantity ở index 3, unit price ở index 4:

```java
double total = 0;
for (String line : lines) {
    if (line.startsWith("ITEM|")) {
        String[] parts = line.split("\\|");
        int quantity = Integer.parseInt(parts[3]);
        double unitPrice = Double.parseDouble(parts[4]);
        total += quantity * unitPrice;
    }
}
```

Phần lõi dùng file do chính chương trình tạo nên có thể giả định định dạng đúng. Parse file hỏng và phục hồi object graph là phần mở rộng, tránh biến buổi exception/I/O thành một bài parser quá lớn.

## VII. Thực Hành Có Hướng Dẫn

1. Tạo Order DH01 hợp lệ bằng dữ liệu mẫu của buổi học và lưu file.
2. Mở file bằng editor, đối chiếu từng field với object.
3. Đọc file và tính tổng 60000.
4. Thử Order rỗng; xác nhận file không bị ghi lại.
5. Thử quantity 0; xác nhận lỗi nghiệp vụ khác lỗi I/O.
6. Thử một đường dẫn không ghi được; xác nhận không có dòng “Lưu thành công”.

Mỗi nhóm ghi lại loại exception, nơi ném và nơi bắt của bốn ca. Đây là bằng chứng hiểu luồng lỗi, không chỉ chạy được happy path.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| `catch (Exception)` cho mọi trường hợp | Không biết lỗi nghiệp vụ hay I/O | Bắt loại cụ thể, theo thứ tự cụ thể trước |
| Validate sau khi mở writer | Có thể làm mất file cũ | Validate trước I/O |
| In thành công trong `finally` | Thất bại vẫn báo thành công | Chỉ in sau lời gọi hoàn tất |
| Không chỉ rõ UTF-8 | Tiếng Việt có thể lỗi | Dùng `StandardCharsets.UTF_8` |
| Bắt buộc phục hồi mọi object | Quá tải phạm vi buổi học | Lõi chỉ đọc dòng và tính tổng |

## Tổng kết buổi 6

Exception là một luồng điều khiển cho tình huống không thể hoàn tất bình thường. Validator chịu trách nhiệm lỗi nghiệp vụ; lớp file chịu trách nhiệm I/O; lớp gọi quyết định thông báo. Try-with-resources bảo đảm đóng tài nguyên và định dạng file nhỏ giúp học viên tập trung đúng mục tiêu. Custom exception được để ở phần mở rộng đúng với lộ trình.

## Bài tập nhỏ trên lớp — Đọc file điểm số

Viết chương trình làm việc với `scores.txt`:

- Mỗi dòng chứa một điểm dạng số; giáo viên chuẩn bị cả một dòng sai định dạng.
- Đọc file bằng `Files.newBufferedReader` và try-with-resources.
- Dùng `Double.parseDouble`; bắt `NumberFormatException` cho từng dòng, báo số dòng sai rồi tiếp tục.
- Tính số dòng hợp lệ và điểm trung bình; file không tồn tại phải được báo bằng thông điệp riêng.

**Nghiệm thu:** một dòng hỏng không làm mất kết quả của các dòng hợp lệ và chương trình không báo thành công khi đọc file thất bại. Thời gian làm khoảng 15 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 06](../BTVN/buoi-06-btvn.md). File này chỉ là bước học I/O; từ buổi 7, Product bắt đầu chuyển sang MySQL.
