# Buổi 7: Maven, MySQL và JDBC CRUD Dữ Liệu

---

## Mục tiêu buổi học

- Hiểu project Maven và vai trò pom.xml khi thêm thư viện.
- Tạo MySQL database/table books và kết nối bằng JDBC.
- Thực hiện SELECT, INSERT, UPDATE, DELETE với PreparedStatement/ResultSet.
- Không hard-code mật khẩu database trong source code.

> **Cầu nối:** File văn bản phù hợp lưu dữ liệu nhỏ theo dạng tuần tự, nhưng tìm kiếm, cập nhật từng bản ghi và ràng buộc quan hệ trở nên khó khi dữ liệu tăng. Database quan hệ giải quyết các nhu cầu đó bằng bảng, truy vấn SQL và ràng buộc; JDBC là cầu nối từ Java tới database.

## I. Maven Và pom.xml

Maven quản lý cấu hình build và thư viện ngoài. File pom.xml khai báo tọa độ project và dependency MySQL Connector/J để Maven tải driver.

Maven cần khai báo tọa độ driver trong dependency: groupId là com.mysql, artifactId là mysql-connector-j và version theo phiên bản đã thống nhất cho môi trường lớp. Không hard-code một phiên bản tùy ý nếu giảng viên đang cung cấp cấu hình chung. Sau khi thêm dependency, reload Maven để tải thư viện.

Một dependency được nhận diện bằng tọa độ (groupId, artifactId, version). Maven dùng tọa độ đó để tìm thư viện và các dependency liên quan, đưa chúng vào classpath khi build/chạy. `pom.xml` còn mô tả cấu hình project; nó không thay thế source code và không phải database. Khi lỗi không tìm thấy driver, kiểm tra dependency đã tải và cấu hình URL.

Trong Maven project, `src/main/java` thường chứa mã nguồn, `src/test/java` dành cho kiểm thử và `pom.xml` nằm ở thư mục gốc. Đây là quy ước để Maven biết vị trí build; IDE có thể hiển thị theo dạng package thay vì cây thư mục thô.

~~~text
pom.xml khai báo dependency
          |
          v
Maven tải thư viện vào local repository
          |
          v
Classpath build/run chứa Connector/J
~~~

## II. Thiết Kế Một Bảng Quan Hệ

~~~sql
CREATE DATABASE library_db;
USE library_db;

CREATE TABLE books (
    id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(120) NOT NULL,
    published_year INT NOT NULL,
    replacement_cost DECIMAL(12,2) NOT NULL CHECK (replacement_cost >= 0)
);
~~~

`PRIMARY KEY` yêu cầu id duy nhất và không null; `NOT NULL` cấm bỏ trống cột; `CHECK` giới hạn miền dữ liệu ngay cả khi có chương trình khác ghi DB. `DECIMAL(12,2)` lưu số thập phân chính xác với tối đa 12 chữ số, trong đó 2 chữ số ở phần thập phân; kiểu này phù hợp số tiền hơn `FLOAT`/`DOUBLE` ở database. Ứng dụng vẫn nên kiểm tra input để trả lỗi dễ hiểu.

## III. Kết Nối JDBC Và CRUD

JDBC là API Java để gửi SQL tới database. Connection mở kết nối; PreparedStatement chuẩn bị câu lệnh và tham số; ResultSet chứa kết quả truy vấn.

Luồng tổng quát là Java tạo kết nối → gửi câu SQL cùng tham số → MySQL thực thi → JDBC trả số dòng bị ảnh hưởng hoặc ResultSet. Với SELECT, `executeQuery()` trả ResultSet; `next()` dịch con trỏ tới dòng tiếp theo và trả false khi hết. Với INSERT/UPDATE/DELETE, `executeUpdate()` trả số dòng bị tác động. Tham số `?` đánh số từ 1 và nên dùng setter phù hợp kiểu dữ liệu.

~~~text
Java app --Connection--> MySQL
    |                         |
    +-- PreparedStatement --->|-- executeQuery() --> ResultSet (SELECT)
    |                         +-- executeUpdate() -> số dòng đổi (INSERT/UPDATE/DELETE)
    +-- close resource khi thao tác kết thúc
~~~

~~~java
String bookId = "B01";
String sql = "SELECT id, title, author, published_year FROM books WHERE id = ?";
try (Connection connection = DriverManager.getConnection(url, user, password);
     PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, bookId);
    try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
            System.out.println(result.getString("title"));
        }
    }
}
~~~

Ví dụ trên thực hiện tìm theo mã: dấu `?` là vị trí tham số, `setString(1, bookId)` gán giá trị đầu tiên, `executeQuery()` chạy SELECT và `next()` kiểm tra có dòng kết quả hay không. Nếu có, các getter của ResultSet đọc từng cột theo tên. Dùng tên cột giúp code dễ đọc hơn số thứ tự cột.

Đoạn dưới là phần thân của method thêm Book; giả sử `connection` và `book` đã được truyền vào method. Với thao tác ghi, dùng `executeUpdate()` vì cần biết bao nhiêu dòng bị ảnh hưởng:

~~~java
double replacementCost = 150000.0;
String insertSql = "INSERT INTO books(id, title, author, published_year, replacement_cost) VALUES (?, ?, ?, ?, ?)";
try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
    ps.setString(1, id);
    ps.setString(2, title);
    ps.setString(3, author);
    ps.setInt(4, publishedYear);
    ps.setDouble(5, replacementCost); // ánh xạ đơn giản theo phạm vi khóa học
    int affectedRows = ps.executeUpdate();
    System.out.println(affectedRows == 1 ? "Đã thêm" : "Không thêm được");
}
~~~

UPDATE và DELETE dùng cùng nguyên tắc: khai báo SQL với dấu `?`, gán mỗi tham số theo thứ tự từ 1, gọi `executeUpdate()`, rồi xem số dòng bị tác động. Ví dụ `UPDATE books SET title = ?, author = ?, published_year = ?, replacement_cost = ? WHERE id = ?` có năm tham số; mã định danh là điều kiện WHERE để tránh sửa/xóa nhầm toàn bộ bảng.

~~~java
String updateSql = "UPDATE books SET title = ? WHERE id = ?";
try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
    ps.setString(1, newTitle);
    ps.setString(2, bookId);
    int changed = ps.executeUpdate();
}

String deleteSql = "DELETE FROM books WHERE id = ?";
try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
    ps.setString(1, bookId);
    int deleted = ps.executeUpdate();
}
~~~

Các biến `newTitle`, `bookId`, `connection`, `id`, `title`, `author`, `publishedYear`, `replacementCost` là tham số/giá trị do method gọi cung cấp. Ví dụ chỉ trích thân thao tác; phần khai báo method được lược để tập trung vào bind tham số và kết quả. Database dùng `DECIMAL` để lưu tiền chính xác; ánh xạ sang `double` trong khóa này chỉ là giản lược nhập môn, không phù hợp tính tiền trong hệ thống thật.

SQL Injection xảy ra khi input được nối trực tiếp vào câu SQL, khiến dữ liệu có thể bị diễn giải như cú pháp. PreparedStatement tách câu lệnh khỏi giá trị bind; nó giảm nguy cơ này nhưng không thay thế validate dữ liệu. Không thể dùng dấu `?` thay tên bảng/cột hoặc từ khóa SQL.

- INSERT/UPDATE/DELETE dùng `executeUpdate()`; SELECT dùng `executeQuery()`.
- Dùng dấu ? và setter tương ứng; không nối input người dùng trực tiếp vào SQL.
- Statement phù hợp câu SQL cố định, không có input; PreparedStatement dùng tham số cho truy vấn có giá trị bên ngoài.
- try-with-resources đóng Connection, PreparedStatement, ResultSet.

~~~java
try (Connection connection = DriverManager.getConnection(url, user, password);
     Statement statement = connection.createStatement();
     ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM books")) {
    if (result.next()) {
        System.out.println("Số sách: " + result.getInt(1));
    }
}
~~~

Đây là câu truy vấn cố định, không nhận dữ liệu ngoài nên `Statement` đủ dùng. Nếu lọc theo mã do người dùng nhập, đổi sang `PreparedStatement` với dấu `?`; không nối input vào SQL.

**Khi nào dùng gì?** `Connection` biểu diễn phiên làm việc với database; `PreparedStatement` dùng khi câu lệnh có dữ liệu đầu vào và thường dùng cả cho SQL cố định; `Statement` chỉ phù hợp câu SQL cố định, không có dữ liệu do người dùng cung cấp. `ResultSet` chỉ tồn tại để đọc kết quả truy vấn, vì thế cần xử lý nó trong phạm vi resource đang mở. `executeUpdate()` trả số dòng bị thay đổi: 0 thường nghĩa là không có bản ghi phù hợp, 1 là đúng một bản ghi, lớn hơn 1 cần xem lại điều kiện.

JDBC URL cho biết loại database, máy chủ/cổng và database cần kết nối; username/password xác thực người dùng database. Đọc giá trị từ cấu hình cục bộ hoặc biến môi trường thay vì ghi bí mật vào source. `Statement` phù hợp câu SQL cố định; khi có tham số từ input, ưu tiên `PreparedStatement`.

## IV. Thông Tin Kết Nối An Toàn

Đọc URL, username, password từ file cấu hình cục bộ hoặc biến môi trường; thêm file bí mật vào .gitignore. Commit file cấu hình mẫu không chứa mật khẩu thật. Không in credential ra log.

## V. Thực Hành Trên Lớp

1. Tạo Maven project và kiểm tra pom.xml.
2. Tạo database, bảng books và thêm vài dòng dữ liệu.
3. Kết nối bằng Connection; đọc ResultSet và in tiêu đề sách.
4. Hoàn thành đủ CRUD bằng PreparedStatement.
5. Thực hành khoảng 1 giờ theo lộ trình; đóng connection bằng try-with-resources.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Driver không tìm thấy | Dependency chưa tải/chưa đúng | Kiểm tra pom.xml, reload Maven |
| Access denied | User/password hoặc quyền sai | Kiểm tra config và quyền database |
| SQL syntax error | Tên cột/bảng hoặc câu SQL sai | Chạy SQL độc lập và đọc vị trí lỗi |
| SQL injection | Nối chuỗi input vào query | Dùng PreparedStatement và tham số ? |
| Connection không đóng | Thiếu đóng resource | Dùng try-with-resources |

## Tổng Kết Buổi 7

- Maven khai báo dependency; JDBC dùng Connection, Statement/PreparedStatement, ResultSet và try-with-resources để giao tiếp với database.
- SELECT trả tập kết quả cần duyệt; INSERT/UPDATE/DELETE báo số dòng bị ảnh hưởng. Tham số hóa ngăn input bị hiểu thành cú pháp SQL.
- Khi một class vừa viết SQL vừa xử lý quy tắc nghiệp vụ, cần tách trách nhiệm; đó là chủ đề kiến trúc buổi 8.

### Câu Hỏi Ôn Tập

1. pom.xml dùng để làm gì?
2. ResultSet và PreparedStatement có vai trò gì?
3. Khi dùng input người dùng, vì sao chọn PreparedStatement?
4. DECIMAL khác FLOAT/DOUBLE ở database thế nào ở mức khái niệm?
5. Vì sao không commit mật khẩu database?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 07](../BTVN/buoi-07-btvn.md).
## Tài Liệu Tham Khảo

- [Apache Maven — Introduction to the POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html)
- [MySQL — Connector/J Developer Guide](https://dev.mysql.com/doc/connector-j/en/)
- [Oracle — JDBC Basics: Prepared Statements](https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html)
