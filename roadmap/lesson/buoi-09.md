# Buổi 9: JDBC Nhiều Bảng — Loan, LoanItem, JOIN và Thống Kê

---

## Mục tiêu buổi học

- Thiết kế quan hệ một-nhiều giữa loans và loan_items.
- Dùng primary key, foreign key và DATETIME/LocalDateTime.
- Thêm phiếu mượn và các dòng chi tiết bằng getGeneratedKeys.
- Truy vấn phiếu bằng JOIN, sắp xếp bằng ORDER BY và tổng hợp bằng GROUP BY.

> **Cầu nối:** Truy vấn một bảng chưa trả lời được câu hỏi cần dữ liệu từ nhiều bảng có quan hệ. Khóa ngoại, JOIN và GROUP BY giúp liên kết các dòng, tạo kết quả chi tiết hoặc tổng hợp. Buổi này tập trung vào mô hình quan hệ và cách truy vấn đó hoạt động.

## I. Mô Hình Dữ Liệu

Một Loan có nhiều LoanItem; mỗi dòng tham chiếu một Book. Reader có thể gắn với nhiều Loan. Khóa ngoại giữ liên kết hợp lệ.

~~~sql
CREATE TABLE readers (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE books (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE TABLE loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    borrowed_at DATETIME NOT NULL,
    FOREIGN KEY (reader_id) REFERENCES readers(id)
);

CREATE TABLE loan_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    book_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    daily_fee DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loans(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
~~~

Lưu `daily_fee` tại thời điểm cho mượn để lịch sử không đổi nếu biểu phí hiện tại thay đổi. `borrowed_at` ánh xạ sang `LocalDateTime`.

Khóa chính (PK) nhận diện một dòng; khóa ngoại (FK) tham chiếu PK ở bảng khác. Quan hệ 1-nhiều được lưu bằng FK ở phía “nhiều”: một reader có nhiều loan thì `loans.reader_id` trỏ tới `readers.id`; một loan có nhiều loan_item thì `loan_items.loan_id` trỏ tới `loans.id`. FK giúp database từ chối liên kết tới bản ghi không tồn tại. `daily_fee` là giá tại thời điểm giao dịch, khác giá hiện hành của Book.

Khi chọn constraint, phân biệt mục tiêu của chúng: `NOT NULL` bắt buộc phải có giá trị; `PRIMARY KEY` định danh duy nhất; `FOREIGN KEY` bảo vệ quan hệ; `CHECK (quantity > 0)` giới hạn miền giá trị. Ràng buộc đặt ở database bảo vệ dữ liệu từ mọi client; Service cũng kiểm tra sớm để trả thông báo dễ hiểu. Không nên chỉ dựa vào giao diện để giữ dữ liệu hợp lệ.

~~~text
readers (PK id) 1 ─── N loans (PK id, FK reader_id)
                              |
                              1
                              |
                              N
                    loan_items (PK id, FK loan_id, FK book_id)
                              N
                              |
                              1
                         books (PK id)
~~~

## II. Tạo Phiếu Mượn Và Lấy Khóa Sinh Tự Động

Thêm loans trước để lấy ID; sau đó dùng ID đó ghi từng LoanItem. PreparedStatement có tùy chọn trả generated keys.

~~~java
String sql = "INSERT INTO loans(reader_id, status, borrowed_at) VALUES (?, ?, ?)";
try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, readerId);
    ps.setString(2, status.name());
    ps.setTimestamp(3, Timestamp.valueOf(borrowedAt));
    ps.executeUpdate();
    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) loanId = keys.getLong(1);
    }
}
~~~

Đóng mọi resource; giữ SQL parameterized. LoanRepository đọc/ghi bảng; LoanService phối hợp tạo loan và items.

`AUTO_INCREMENT` để MySQL cấp định danh mới; JDBC lấy khóa vừa sinh từ thao tác insert. Vì ID loan cần cho các item nên ghi cha trước, con sau. Việc ghi nhiều bảng có thể dở dang nếu một bước thất bại; transaction là cách bảo vệ tính toàn vẹn đó và được xếp ở phần nâng cao, không phải điều kiện để hiểu JOIN hay generated key.

## III. JOIN, GROUP BY, ORDER BY

- JOIN lấy thông tin loan cùng reader và loan_items/book.
- ORDER BY sắp xếp phiếu theo thời gian hoặc phí.
- GROUP BY tổng hợp doanh thu theo ngày hoặc theo khách hàng.

Ví dụ lấy chi tiết một phiếu mượn cần nối bảng theo khóa ngoại, không dựa vào thứ tự tình cờ của các dòng:

~~~sql
SELECT l.id, r.name AS reader_name, l.status, l.borrowed_at,
       b.id AS book_id, b.name AS book_name,
       li.quantity, li.daily_fee
FROM loans l
JOIN readers r ON r.id = l.reader_id
JOIN loan_items li ON li.loan_id = l.id
JOIN books b ON b.id = li.book_id
WHERE l.id = ?
ORDER BY li.id;
~~~

Một Loan có ba LoanItem thì truy vấn trả ba dòng: thông tin phiếu mượn/bạn đọc lặp lại, còn sách và số lượng khác nhau. Khi tạo object Loan, nhóm các dòng theo `l.id`; không tạo ba Loan trùng nhau. `JOIN` (inner join) chỉ trả các dòng có quan hệ khớp ở hai phía; dùng khi cần các bản ghi liên quan tồn tại ở cả hai bảng.

~~~sql
SELECT DATE(l.borrowed_at) AS loan_day, SUM(li.quantity * li.daily_fee) AS fee_total
FROM loans l
JOIN loan_items li ON li.loan_id = l.id
GROUP BY DATE(l.borrowed_at)
ORDER BY loan_day;
~~~

`GROUP BY DATE(l.borrowed_at)` gom tất cả phiếu theo ngày; `SUM(quantity × daily_fee)` cộng phí ở từng dòng. Báo cáo theo bạn đọc thay biểu thức nhóm bằng `r.id, r.name` và nối `loans` với `readers`. `WHERE` lọc dòng trước khi gom, còn `HAVING` lọc nhóm sau khi tính tổng. Kết quả báo cáo là các dòng tổng hợp, không phải danh sách Loan đầy đủ.

Khi một phiếu có nhiều dòng, kết quả JOIN có thể lặp thông tin Loan; cần gom kết quả theo loan_id khi dựng object.

## Phần Nâng Cao — Tìm Hiểu Thêm

- `LIMIT` giới hạn số dòng; `OFFSET` bỏ qua một số dòng để phân trang. Luôn sắp xếp theo khóa ổn định trước khi phân trang, nếu không cùng một bản ghi có thể xuất hiện ở trang khác nhau giữa các lần truy vấn.
- Transaction gom nhiều lệnh thành một đơn vị: `setAutoCommit(false)` bắt đầu, `commit()` xác nhận khi tất cả bước thành công, `rollback()` hoàn tác khi có bước lỗi. ACID gồm Atomicity (tất cả hoặc không), Consistency (giữ ràng buộc hợp lệ), Isolation (các giao dịch đồng thời không nhìn thấy trạng thái dở dang theo mức cô lập), Durability (đã commit thì kết quả bền vững).
- Batch dùng `addBatch()` để xếp nhiều câu lệnh cùng dạng rồi `executeBatch()` để gửi một lượt; phù hợp nhiều thao tác độc lập/lặp, nhưng cần xem kết quả từng dòng và xử lý lỗi batch.
- RowMapper là một hàm nhận một dòng `ResultSet` và tạo object tương ứng. Tách ánh xạ khỏi vòng lặp truy vấn giúp dùng lại logic và giảm lặp code.

~~~java
connection.setAutoCommit(false);
try {
    insertLoan();
    insertLoanItems();
    connection.commit();
} catch (SQLException e) {
    connection.rollback();
    throw e;
}
~~~

Đây là giả mã trong method có thể ném `SQLException`; cần phục hồi `autoCommit` hoặc đóng connection ở vị trí quản lý vòng đời phù hợp. Mục đích minh họa: nếu ghi các dòng chi tiết thất bại, `rollback()` ngăn database giữ lại một phần phiếu mượn.

**Bài tập tự chọn:** Thử phân trang danh sách; cố ý làm lỗi khi lưu item để rollback transaction; so sánh insert từng dòng với batch. Các nội dung này là nâng cao, không phải điều kiện hoàn thành chức năng nhiều bảng cơ bản.

`JOIN ... ON` ghép dòng dựa trên điều kiện liên kết; nếu một loan có ba item, JOIN tạo ba dòng có thông tin loan lặp lại. `GROUP BY` gom các dòng theo khóa/biểu thức để tính `SUM`, `COUNT`...; `ORDER BY` sắp xếp đầu ra và không thay đổi dữ liệu lưu. Nên chọn cột rõ ràng thay vì `SELECT *` khi ánh xạ sang object. Tính tổng tiền sau JOIN cần chú ý mức chi tiết: nếu JOIN thêm quan hệ một-nhiều khác, một dòng item có thể bị lặp và làm tổng bị nhân lên.

## IV. Thực Hành Trên Lớp

1. Vẽ quan hệ bảng và kiểm tra khóa chính/ngoại.
2. Viết LoanRepository tạo phiếu, lấy generated key, ghi LoanItem.
3. Truy vấn chi tiết phiếu cùng sách/bạn đọc.
4. Viết báo cáo số lượt mượn theo ngày/bạn đọc.
5. Thực hành JOIN, GROUP BY, ORDER BY bằng dữ liệu mẫu.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Không insert được loan_item | loan_id chưa lấy hoặc FK sai | Lấy generated key trước khi ghi chi tiết |
| Lịch sử tiền thay đổi | Chỉ lưu book_id rồi đọc giá hiện tại | Lưu daily_fee tại thời điểm đặt |
| Thông tin phiếu bị lặp sau JOIN | Mỗi dòng item tạo một dòng kết quả | Gom dữ liệu theo loan_id |
| Đóng connection quá sớm | Dùng resource ngoài phạm vi | Bao transaction/resource trong service/repository phù hợp |

## Tổng Kết Buổi 9

- Quan hệ một-nhiều thường đặt foreign key ở phía nhiều; JOIN ghép dòng theo khóa liên kết.
- GROUP BY tạo nhóm để tổng hợp; ORDER BY sắp xếp đầu ra; generated keys cung cấp ID cha cho các dòng con.
- Khi nhiều bảng cùng được cập nhật, cần nghĩ tới tính toàn vẹn của thao tác; transaction là nội dung mở rộng.

### Câu Hỏi Ôn Tập

1. Vì sao loan_items cần foreign key tới loans?
2. Dùng generated keys trong trường hợp nào?
3. JOIN khác GROUP BY thế nào?
4. Vì sao lưu daily_fee trong loan_items?
5. LocalDateTime ánh xạ với loại cột thời gian nào trong lộ trình?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 09](../BTVN/buoi-09-btvn.md).
## Tài Liệu Tham Khảo

- [Java SE API — Connection Transactions](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html)
- [Oracle JDBC — Retrieving Generated Keys](https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html)

- [MySQL Reference Manual — JOIN](https://dev.mysql.com/doc/refman/8.4/en/join.html)
- [MySQL Reference Manual — GROUP BY](https://dev.mysql.com/doc/refman/8.4/en/group-by-handling.html)
- [Oracle JDBC — Using Generated Keys](https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html)
