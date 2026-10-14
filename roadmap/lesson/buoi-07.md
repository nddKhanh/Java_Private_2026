# Buổi 7: Maven, MySQL và JDBC CRUD Sản Phẩm

---

## Mục tiêu buổi học

- Hiểu project Maven và vai trò pom.xml khi thêm thư viện.
- Tạo MySQL database/table products và kết nối bằng JDBC.
- Thực hiện SELECT, INSERT, UPDATE, DELETE với PreparedStatement/ResultSet.
- Không hard-code mật khẩu database trong source code.

> **Nối tiếp:** Buổi 6 lưu đơn hàng trong file; truy vấn/cập nhật một sản phẩm trong file không tiện. Buổi này chuyển dữ liệu Product sang MySQL. Chỉ làm bảng products; khách hàng và đơn hàng nhiều bảng đến buổi 8–9.

## I. Maven Và pom.xml

Maven quản lý cấu hình build và thư viện ngoài. File pom.xml khai báo tọa độ project và dependency MySQL Connector/J để Maven tải driver.

Maven cần khai báo tọa độ driver trong dependency: groupId là com.mysql, artifactId là mysql-connector-j và version theo phiên bản đã thống nhất cho môi trường lớp. Không hard-code một phiên bản tùy ý nếu giảng viên đang cung cấp cấu hình chung. Sau khi thêm dependency, reload Maven để tải thư viện.

Một dependency được nhận diện bằng tọa độ (groupId, artifactId, version). Maven dùng tọa độ đó để tìm thư viện và các dependency liên quan, đưa chúng vào classpath khi build/chạy. `pom.xml` còn mô tả cấu hình project; nó không thay thế source code và không phải database. Khi lỗi không tìm thấy driver, kiểm tra dependency đã tải và cấu hình URL.

Trong Maven project, `src/main/java` thường chứa mã nguồn, `src/test/java` dành cho kiểm thử và `pom.xml` nằm ở thư mục gốc. Đây là quy ước để Maven biết vị trí build; IDE có thể hiển thị theo dạng package thay vì cây thư mục thô.

> **Hình minh họa cần bổ sung:** Quan hệ pom.xml → Maven repository → classpath lúc biên dịch/chạy.

## II. Thiết Kế Bảng products

~~~sql
CREATE DATABASE shop_db;
USE shop_db;

CREATE TABLE products (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL
);
~~~

DECIMAL phù hợp biểu diễn tiền trong database. Ở khóa này ánh xạ tạm sang kiểu Java đơn giản; không mở rộng thành bài BigDecimal.

## III. Kết Nối JDBC Và CRUD

JDBC là API Java để gửi SQL tới database. Connection mở kết nối; PreparedStatement chuẩn bị câu lệnh và tham số; ResultSet chứa kết quả truy vấn.

Luồng tổng quát là Java tạo kết nối → gửi câu SQL cùng tham số → MySQL thực thi → JDBC trả số dòng bị ảnh hưởng hoặc ResultSet. Với SELECT, `executeQuery()` trả ResultSet; `next()` dịch con trỏ tới dòng tiếp theo và trả false khi hết. Với INSERT/UPDATE/DELETE, `executeUpdate()` trả số dòng bị tác động. Tham số `?` đánh số từ 1 và nên dùng setter phù hợp kiểu dữ liệu.

> **Hình minh họa cần bổ sung:** Sơ đồ vòng đời Connection → PreparedStatement → ResultSet và các nhánh CRUD.

~~~java
String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";
try (Connection connection = DriverManager.getConnection(url, user, password);
     PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, productId);
    try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
            System.out.println(result.getString("name"));
        }
    }
}
~~~

- INSERT/UPDATE/DELETE dùng `executeUpdate()`; SELECT dùng `executeQuery()`.
- Dùng dấu ? và setter tương ứng; không nối input người dùng trực tiếp vào SQL.
- Statement chỉ dùng câu lệnh đơn giản cố định để giới thiệu. Query có input dùng PreparedStatement nhằm tránh SQL injection.
- try-with-resources đóng Connection, PreparedStatement, ResultSet.

JDBC URL cho biết loại database, máy chủ/cổng và database cần kết nối; username/password xác thực người dùng database. Đọc giá trị từ cấu hình cục bộ hoặc biến môi trường thay vì ghi bí mật vào source. `Statement` phù hợp câu SQL cố định; khi có tham số từ input, ưu tiên `PreparedStatement`.

## IV. Thông Tin Kết Nối An Toàn

Đọc URL, username, password từ file cấu hình cục bộ hoặc biến môi trường; thêm file bí mật vào .gitignore. Commit file cấu hình mẫu không chứa mật khẩu thật. Không in credential ra log.

## V. Thực Hành Trên Lớp

1. Tạo Maven project và kiểm tra pom.xml.
2. Tạo database, bảng products và thêm vài dòng dữ liệu.
3. Kết nối bằng Connection; đọc ResultSet và in sản phẩm.
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

## Mốc Project Sau Buổi 7

- Product CRUD hoạt động với một bảng MySQL thay vì dữ liệu lưu file.
- SQL và kết nối đang nằm lẫn trong chương trình; buổi 8 tách repository/service và thêm khách hàng/trạng thái.

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
