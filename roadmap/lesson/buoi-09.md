# Buổi 9: JDBC Nhiều Bảng — Order, OrderItem, JOIN Và Báo Cáo

---

> **Hạn chế từ buổi 8:** repository một bảng chưa biểu diễn được quan hệ Order–OrderItem, khóa sinh tự động, truy vấn JOIN hoặc báo cáo tổng hợp.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Thiết kế quan hệ Customer–Order–OrderItem–Product bằng khóa ngoại.
- Lấy generated key khi insert Order và dùng cho OrderItem.
- Tách giá hiện tại của Product khỏi unit price lịch sử.
- Nạp chi tiết Order qua nhiều repository và ghép ở Service.
- Viết JOIN, GROUP BY, ORDER BY cho chi tiết và báo cáo.
- Giải thích rủi ro ghi nhiều bước và vai trò của transaction.

> **Kết quả cuối buổi:** chương trình mẫu tạo/xem được đơn nhiều dòng và lập báo cáo doanh thu; Order dùng id `long` do MySQL tự tăng.

> **Môi trường:** JDK 17, Maven và MySQL.

## I. Chọn Khóa Cho Dữ Liệu Nhiều Bảng

Trong ví dụ của buổi học, `Product` và `Customer` dùng mã nghiệp vụ dạng chuỗi; `Order` dùng khóa số do database sinh. Vì vậy model Order dùng `long id`, tương ứng `BIGINT AUTO_INCREMENT` trong MySQL.

Quy ước của ví dụ:

- Model Order dùng `long id`.
- Product và Customer dùng mã chuỗi như `SP01`, `KH01`.
- Code lấy khóa Order bằng `getGeneratedKeys()` sau câu INSERT.
- Không đoán khóa mới bằng `MAX(id)`.

Mỗi entity chỉ nên có một quy ước định danh rõ ràng trong cùng mô hình demo.

## II. Mô Hình Quan Hệ

```text
customers 1 ─── n orders 1 ─── n order_items n ─── 1 products
```

```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

Không lưu danh sách Product vào một cột text trong orders. Bảng `order_items` biểu diễn mỗi dòng hàng và giải quyết quan hệ nhiều-nhiều theo thời gian giữa Order và Product.

## III. Vì Sao OrderItem Cần `unitPrice`?

Product SP01 có thể tăng từ 25000 lên 30000. Order đã mua hôm qua phải giữ tổng cũ.

```java
public class OrderItem {
    private long id;
    private Product product;
    private int quantity;
    private double unitPrice;

    public double getLineTotal() {
        return quantity * unitPrice;
    }
}
```

Khi tạo OrderItem, Service chụp `product.getPrice()` vào `unitPrice`. Khi xem lịch sử, tính bằng field này; không gọi giá hiện tại từ Product để tính lại.

Đây là sự khác biệt với CartItem buổi 3: CartItem là giỏ tạm và đọc giá hiện tại; OrderItem là dòng giao dịch đã chốt.

## IV. Insert Order Và Lấy Generated Key

```java
String sql = "INSERT INTO orders(customer_id, status, created_at) VALUES (?, ?, ?)";

try (Connection connection = connectionFactory.open();
     PreparedStatement statement = connection.prepareStatement(
             sql, Statement.RETURN_GENERATED_KEYS)) {
    statement.setString(1, customerId);
    statement.setString(2, status.name());
    statement.setTimestamp(3, Timestamp.valueOf(createdAt));

    if (statement.executeUpdate() != 1) {
        throw new DataAccessException("Không tạo được Order");
    }

    try (ResultSet keys = statement.getGeneratedKeys()) {
        if (!keys.next()) {
            throw new DataAccessException("Không nhận được Order id");
        }
        return keys.getLong(1);
    }
}
```

Không dùng `SELECT MAX(id)` vì một người dùng khác có thể insert giữa hai câu lệnh. Generated key thuộc chính lệnh insert vừa thực hiện.

OrderItemRepository nhận id đó:

```java
for (OrderItem item : items) {
    statement.setLong(1, orderId);
    statement.setString(2, item.getProduct().getId());
    statement.setInt(3, item.getQuantity());
    statement.setDouble(4, item.getUnitPrice());
    statement.executeUpdate();
}
```

## V. Nạp Một Order Mà Không Trộn Trách Nhiệm

`OrderRepository.findById(id)` nạp header và Customer, tạo Order với list rỗng. `OrderItemRepository.findByOrderId(id)` nạp item bằng JOIN Product. `OrderService` ghép hai kết quả:

```java
public Optional<Order> findOrderById(long id) {
    Optional<Order> found = orderRepository.findById(id);
    if (found.isEmpty()) {
        return Optional.empty();
    }

    Order order = found.get();
    List<OrderItem> items = orderItemRepository.findByOrderId(id);
    for (OrderItem item : items) {
        order.addItem(item);
    }
    return Optional.of(order);
}
```

View chỉ nhận Order đã hoàn chỉnh. Không để View tự gọi hai repository rồi ghép object.

Query item:

```sql
SELECT oi.id,
       oi.quantity,
       oi.unit_price,
       p.id AS product_id,
       p.name,
       p.price,
       p.stock
FROM order_items oi
JOIN products p ON p.id = oi.product_id
WHERE oi.order_id = ?
ORDER BY oi.id;
```

Alias giúp tránh nhầm các cột cùng tên `id`.

## VI. JOIN Và GROUP BY Cho Báo Cáo

### Doanh thu theo ngày

```sql
SELECT DATE(o.created_at) AS order_day,
       SUM(oi.quantity * oi.unit_price) AS revenue
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
GROUP BY DATE(o.created_at)
ORDER BY order_day;
```

### Doanh thu theo khách hàng

```sql
SELECT c.id AS customer_id,
       c.name AS customer_name,
       SUM(oi.quantity * oi.unit_price) AS revenue
FROM customers c
JOIN orders o ON o.customer_id = c.id
JOIN order_items oi ON oi.order_id = o.id
GROUP BY c.id, c.name
ORDER BY c.id;
```

Trước khi chạy, học viên tự tính hai Order mẫu: 60000 + 30000 = 90000. Nếu SQL trả kết quả khác, hãy kiểm tra JOIN có nhân bản row hoặc seed bị chạy lặp.

## VII. Rủi Ro Của Ghi Nhiều Bước

Luồng tạo đơn hiện có ít nhất hai bước:

```text
INSERT orders
INSERT order_items...
```

Nếu insert item thứ hai lỗi sau khi Order đã lưu, database có Order thiếu chi tiết. Transaction giải quyết bằng một Connection dùng chung, `setAutoCommit(false)`, `commit()` khi tất cả thành công và `rollback()` khi có lỗi.

Buổi này giáo viên demo transaction và cho làm ở phần nâng cao để học viên tập trung trước vào generated key/JOIN. Đến capstone, transaction là bắt buộc cho luồng nhiều bảng và cập nhật stock.

## VIII. Demo Dữ Liệu Chuẩn

1. Seed KH01/KH02, SP01/SP02; đổi giá hiện tại SP01 từ 25000 thành 30000.
2. Tạo Order 1 cho KH01: SP01 × 2 với unit price 25000 và SP02 × 1 với unit price 10000.
3. Tạo Order 2: KH02 mua SP02 × 3 với unit price 10000.
4. Xem Order 1; xác nhận tổng 60000 dù Product SP01 hiện tại là 30000.
5. Chạy hai báo cáo; đối chiếu tổng ngày 90000, KH01 60000, KH02 30000.

## IX. Thực Hành Có Hướng Dẫn

- Vẽ các khóa trước khi viết SQL.
- Hoàn thiện insert Order và in generated id.
- Insert hai OrderItem theo id vừa nhận.
- Viết query item riêng và xác nhận số row.
- Ghép Order ở Service, sau đó mới làm query báo cáo.
- Cuối buổi, cố ý làm một item tham chiếu P99 để quan sát vì sao cần rollback.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| Dùng MAX(id) | Có thể lấy id của request khác | Dùng generated keys |
| Tính lịch sử bằng Product.price | Tổng đơn cũ thay đổi | Dùng OrderItem.unitPrice |
| JOIN thiếu điều kiện | Row bị nhân bản | Kiểm tra từng khóa nối |
| Repository trả Order thiếu item mà View không biết | Hiển thị tổng 0 | Service chịu trách nhiệm hydrate đầy đủ |
| Tự đoán id Order bằng `MAX(id)` | Có thể lấy khóa của request khác | Dùng generated key của chính lệnh INSERT |

## Tổng kết buổi 9

Thiết kế nhiều bảng yêu cầu hiểu khóa, lịch sử giá và thứ tự ghi. Generated key nối Order với OrderItem; JOIN khôi phục object; GROUP BY tạo báo cáo; transaction bảo vệ tính toàn vẹn khi nhiều bước cùng tạo một nghiệp vụ.

## Bài tập nhỏ trên lớp — Đăng ký sinh viên vào lớp học

Thiết kế ba bảng `students`, `courses`, `enrollments`:

- `enrollments` có khóa ngoại tới student và course.
- Viết JDBC tạo một course, lấy generated id rồi thêm hai enrollment.
- Viết một JOIN hiển thị mã sinh viên, tên sinh viên và tên khóa học.
- Viết GROUP BY đếm số sinh viên của từng khóa, sắp xếp theo course id.

**Nghiệm thu:** course có hai enrollment phải xuất hiện một lần trong báo cáo với số lượng 2; JOIN chi tiết có đúng hai dòng. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 09](../BTVN/buoi-09-btvn.md). Transaction là phần nâng cao ở bài này nhưng sẽ là yêu cầu bắt buộc của capstone.
