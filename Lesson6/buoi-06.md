# Buổi 6: Exception, try-with-resources Và Lưu Order Ra File

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Phân biệt lỗi biên dịch, lỗi runtime và lỗi nghiệp vụ.
- Dùng `throw`, `throws`, `try/catch/finally` đúng vai trò và bắt lỗi nhập sai ở đúng phạm vi.
- Dùng `Path`, `Files` và UTF-8 để ghi/đọc text.
- Đóng tài nguyên tự động bằng try-with-resources.
- Lưu Order theo một định dạng text nhỏ, đọc lại và tính tổng từ nội dung file.

> **Kết quả cuối buổi:** một Order mẫu hợp lệ được lưu thành khối trong `orders.txt`; order sai không được ghi; lỗi I/O không bị báo thành công.

> **Môi trường:** JDK 17, tiếp tục model Order của buổi 5; chưa thay file bằng database.

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

Order không có item vẫn là một object Java hợp lệ về cú pháp, nhưng không hợp lệ để lưu như một đơn hàng. Có thể dùng exception có sẵn:

```java
throw new IllegalArgumentException("Order phải có ít nhất một item");
```

Phân loại đúng giúp đặt xử lý đúng chỗ. Không bọc toàn bộ chương trình trong một `catch (Exception)` rồi in “Có lỗi”.

### Kiểm tra nhanh

Phân loại bốn tình huống: gọi sai tên method, nhập `abc` khi cần số lượng, Order không có item và file không có quyền ghi. Với mỗi tình huống, xác định compiler hay code runtime phát hiện và nơi phù hợp để xử lý.

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

## III. Xử Lý Input Sai Ở Ranh Giới Giao Diện

Input từ bàn phím là chuỗi và có thể sai. Chỉ bọc thao tác chuyển đổi có khả năng lỗi, không bọc cả menu:

```java
static Integer parseQuantity(String text) {
    try {
        int quantity = Integer.parseInt(text);
        if (quantity <= 0) {
            System.out.println("Số lượng phải lớn hơn 0.");
            return null;
        }
        return quantity;
    } catch (NumberFormatException e) {
        System.out.println("Số lượng phải là số nguyên.");
        return null;
    }
}
```

`NumberFormatException` là lỗi định dạng input; số nguyên không dương là giá trị sai quy tắc. Hai lỗi có nguyên nhân khác nhau nên thông báo không được đánh đồng. Ở tầng model/validator, quy tắc nghiệp vụ vẫn được bảo vệ bằng `IllegalArgumentException` để code gọi không thể bỏ qua validation.

`finally` chạy dù thành công hay thất bại. Nó phù hợp với thao tác dọn dẹp bắt buộc, nhưng không dùng để in thông báo thành công. Với tài nguyên file, ưu tiên try-with-resources thay vì tự đóng trong `finally`.

## IV. Định Dạng File Càng Nhỏ Càng Dễ Kiểm Tra

Mỗi Order được ghi thành một khối:

```text
ORDER|DH01|KH01|Nguyễn An|PENDING
ITEM|SP01|Sổ tay|2|25000.0
ITEM|SP02|Bút|1|10000.0
END
```

Header giữ dữ liệu order/customer cần hiển thị. Mỗi dòng item giữ giá tại lúc lưu. `END` giúp nhận biết khối đã kết thúc.

Trước khi code, học viên trả lời ba câu:

1. Dòng nào đánh dấu phần bắt đầu của một Order?
2. Dựa vào đâu để phân biệt dòng `ITEM` với dòng `ORDER`?
3. Khi giá của Product thay đổi sau khi lưu, nội dung file cũ có thay đổi không?

## V. Ghi File Bằng try-with-resources

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

## VI. Đọc File Và Báo Lỗi Đúng

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

## VII. Tính Tổng Từ Các Dòng ITEM

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

## VIII. Thực Hành Có Hướng Dẫn

1. Tạo Order DH01 hợp lệ bằng dữ liệu mẫu của buổi học và lưu file.
2. Mở file bằng editor, đối chiếu từng field với object.
3. Đọc file và tính tổng 60000.
4. Thử Order rỗng; xác nhận file không bị ghi lại.
5. Thử quantity 0; xác nhận lỗi nghiệp vụ khác lỗi I/O.
6. Thử một đường dẫn không ghi được; xác nhận không có dòng “Lưu thành công”.

Mỗi nhóm ghi lại loại exception, nơi ném và nơi bắt của bốn ca. Đây là bằng chứng hiểu luồng lỗi, không chỉ chạy được happy path.

## IX. Nội Dung Nâng Cao — Custom Exception

Chỉ học khi phần bắt buộc đã hoàn thành. Có thể tạo `InvalidOrderException extends RuntimeException` để tên kiểu lỗi diễn đạt đúng miền nghiệp vụ, sau đó thay `IllegalArgumentException` trong validator.

Custom exception không tự làm validation tốt hơn. Nó chỉ có ích khi code gọi cần phân biệt lỗi đơn hàng với các lỗi tham số khác và có cách xử lý riêng.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| `catch (Exception)` cho mọi trường hợp | Không biết lỗi nghiệp vụ hay I/O | Bắt loại cụ thể, theo thứ tự cụ thể trước |
| Validate sau khi mở writer | Có thể làm mất file cũ | Validate trước I/O |
| In thành công trong `finally` | Thất bại vẫn báo thành công | Chỉ in sau lời gọi hoàn tất |
| Không chỉ rõ UTF-8 | Tiếng Việt có thể lỗi | Dùng `StandardCharsets.UTF_8` |

## Tổng kết buổi 6

Exception là một luồng điều khiển cho tình huống không thể hoàn tất bình thường. Validator chịu trách nhiệm lỗi nghiệp vụ; lớp file chịu trách nhiệm I/O; lớp gọi quyết định thông báo. Try-with-resources bảo đảm đóng tài nguyên.

### Câu hỏi chốt buổi

1. `throw`, `throws` và `catch` xuất hiện ở ba vị trí khác nhau như thế nào?
2. Vì sao không nên dùng một `catch (Exception)` cho mọi lỗi của menu?
3. Tại sao validator phải chạy trước khi mở writer ghi đè file?
4. Vì sao thông báo thành công không được đặt trong `finally`?
5. Try-with-resources bảo đảm điều gì khi lệnh đọc hoặc ghi ném exception?

## Bài tập nhỏ trên lớp — Đọc file điểm số

Viết chương trình làm việc với `Path path = Path.of("scores.txt")`:

- Mỗi dòng chứa một điểm dạng số; thêm một dòng sai định dạng để kiểm tra xử lý lỗi.
- Class `ScoreFileReader` có `List<Double> readValidScores(Path path) throws IOException`; đọc bằng `Files.newBufferedReader` và try-with-resources.
- Method `double calculateAverage(List<Double> scores)` trả điểm trung bình; danh sách rỗng trả `0`.
- Dùng `Double.parseDouble`; bắt `NumberFormatException` cho từng dòng, báo số dòng sai rồi tiếp tục.
- Tính số dòng hợp lệ và điểm trung bình; file không tồn tại phải được báo bằng thông điệp riêng.

Dữ liệu mẫu gồm bốn dòng: `8.0`, `abc`, `7.5`, `9.0`. Kết quả phải báo dòng 2 sai, có 3 điểm hợp lệ và điểm trung bình xấp xỉ `8.17`.

**Nghiệm thu:** một dòng hỏng không làm mất kết quả của các dòng hợp lệ và chương trình không báo thành công khi đọc file thất bại. Thời gian làm khoảng 15 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 06](buoi-06-btvn.md).
