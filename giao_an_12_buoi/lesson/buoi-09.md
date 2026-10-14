# Buổi 9: JDBC Nhiều Bảng — Order, OrderItem, JOIN và Thống Kê

---

## Mục tiêu buổi học

- Thiết kế quan hệ một-nhiều giữa orders và order_items.
- Dùng primary key, foreign key và DATETIME/LocalDateTime.
- Thêm đơn và chi tiết đơn trong JDBC bằng getGeneratedKeys.
- Truy vấn đơn bằng JOIN, sắp xếp bằng ORDER BY và tổng hợp bằng GROUP BY.

> **Nối tiếp:** Buổi 8 đã có repository/service và OrderStatus. Chưa có liên kết giữa đơn hàng, khách hàng, sản phẩm và các dòng chi tiết. Buổi này hoàn thiện OrderRepository/OrderService bằng JDBC nhiều bảng; không dạy Hibernate.

## I. Mô Hình Dữ Liệu

Một Order có nhiều OrderItem; mỗi dòng tham chiếu một Product. Customer có thể gắn với nhiều Order. Khóa ngoại giữ liên kết hợp lệ.

~~~sql
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
~~~

Lưu unit_price tại thời điểm đặt hàng để lịch sử đơn không đổi khi giá sản phẩm thay đổi. created_at ánh xạ sang LocalDateTime.

Khóa chính (PK) nhận diện một dòng; khóa ngoại (FK) tham chiếu PK ở bảng khác. Quan hệ 1-nhiều được lưu bằng FK ở phía “nhiều”: một customer có nhiều order thì `orders.customer_id` trỏ tới `customers.id`; một order có nhiều order_item thì `order_items.order_id` trỏ tới `orders.id`. FK giúp database từ chối liên kết tới bản ghi không tồn tại. `unit_price` là giá tại thời điểm giao dịch, khác giá hiện hành của Product.

> **Hình minh họa cần bổ sung:** ERD customers 1—N orders 1—N order_items N—1 products; đánh dấu PK/FK.

## II. Tạo Đơn Và Lấy Khóa Sinh Tự Động

Thêm orders trước để lấy ID; sau đó dùng ID đó ghi từng OrderItem. PreparedStatement có tùy chọn trả generated keys.

~~~java
String sql = "INSERT INTO orders(customer_id, status, created_at) VALUES (?, ?, ?)";
try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, customerId);
    ps.setString(2, status.name());
    ps.setTimestamp(3, Timestamp.valueOf(createdAt));
    ps.executeUpdate();
    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) orderId = keys.getLong(1);
    }
}
~~~

Đóng mọi resource; giữ SQL parameterized. OrderRepository đọc/ghi bảng; OrderService phối hợp tạo order và items.

`AUTO_INCREMENT` để MySQL cấp định danh mới; JDBC lấy khóa vừa sinh từ thao tác insert. Vì ID order cần cho các item nên ghi cha trước, con sau. Nếu bước ghi item lỗi, database không nên giữ một order thiếu chi tiết: đây là lý do transaction nhiều bước hữu ích. `commit` xác nhận; `rollback` hủy các thay đổi chưa commit.

## III. JOIN, GROUP BY, ORDER BY

- JOIN lấy thông tin order cùng customer và order_items/product.
- ORDER BY sắp xếp đơn theo thời gian hoặc tổng tiền.
- GROUP BY tổng hợp doanh thu theo ngày hoặc theo khách hàng.

~~~sql
SELECT DATE(o.created_at) AS order_day, SUM(oi.quantity * oi.unit_price) AS revenue
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
GROUP BY DATE(o.created_at)
ORDER BY order_day;
~~~

Khi một đơn có nhiều dòng, kết quả JOIN có thể lặp thông tin order; cần gom kết quả theo order_id khi dựng object.

## Phần Nâng Cao — Tìm Hiểu Thêm

- `LIMIT` giới hạn số dòng; `OFFSET` bỏ qua một số dòng, thường dùng phân trang cùng ORDER BY ổn định.
- Transaction gom nhiều lệnh thành một đơn vị: `setAutoCommit(false)`, `commit()` khi hoàn tất hoặc `rollback()` khi lỗi. ACID là bốn thuộc tính giao dịch, chỉ cần nhận diện khái niệm.
- `addBatch/executeBatch` gửi nhiều thao tác theo lô; RowMapper là ý tưởng gom việc đọc một dòng ResultSet thành object.

**Bài tập tự chọn:** Thử phân trang danh sách; cố ý làm lỗi khi lưu item để rollback transaction; so sánh insert từng dòng với batch. Các nội dung này là nâng cao, không phải điều kiện hoàn thành chức năng nhiều bảng cơ bản.

`JOIN ... ON` ghép dòng dựa trên điều kiện liên kết; nếu một order có ba item, JOIN tạo ba dòng có thông tin order lặp lại. `GROUP BY` gom các dòng theo khóa/biểu thức để tính `SUM`, `COUNT`...; `ORDER BY` sắp xếp đầu ra và không thay đổi dữ liệu lưu. Nên chọn cột rõ ràng thay vì `SELECT *` khi ánh xạ sang object.

## IV. Thực Hành Trên Lớp

1. Vẽ quan hệ bảng và kiểm tra khóa chính/ngoại.
2. Viết OrderRepository tạo đơn, lấy generated key, ghi OrderItem.
3. Truy vấn chi tiết đơn cùng sản phẩm/khách hàng.
4. Viết báo cáo doanh thu theo ngày/khách hàng.
5. Thực hành JOIN, GROUP BY, ORDER BY bằng dữ liệu mẫu.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Không insert được order_item | order_id chưa lấy hoặc FK sai | Lấy generated key trước khi ghi chi tiết |
| Lịch sử tiền thay đổi | Chỉ lưu product_id rồi đọc giá hiện tại | Lưu unit_price tại thời điểm đặt |
| JOIN nhân đôi đơn | Mỗi dòng item tạo một dòng kết quả | Gom dữ liệu theo order_id |
| Đóng connection quá sớm | Dùng resource ngoài phạm vi | Bao transaction/resource trong service/repository phù hợp |

## Mốc Project Sau Buổi 9

- Có orders và order_items, tạo đơn, xem chi tiết và thống kê qua JDBC.
- Cần tổ chức code rõ hơn để chuẩn bị kiểm tra và bài tập lớn; buổi 10 refactor theo MVC/SOLID cơ bản.

### Câu Hỏi Ôn Tập

1. Vì sao order_items cần foreign key tới orders?
2. Dùng generated keys trong trường hợp nào?
3. JOIN khác GROUP BY thế nào?
4. Vì sao lưu unit_price trong order_items?
5. LocalDateTime ánh xạ với loại cột thời gian nào trong lộ trình?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 09](../BTVN/buoi-09-btvn.md).
## Tài Liệu Tham Khảo

- [Java SE API — Connection Transactions](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html)
- [Oracle JDBC — Retrieving Generated Keys](https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html)

- [MySQL Reference Manual — JOIN](https://dev.mysql.com/doc/refman/8.4/en/join.html)
- [MySQL Reference Manual — GROUP BY](https://dev.mysql.com/doc/refman/8.4/en/group-by-handling.html)
- [Oracle JDBC — Using Generated Keys](https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html)
