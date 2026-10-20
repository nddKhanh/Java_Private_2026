# Buổi 7: Maven, MySQL Và JDBC CRUD Product

---

> **Hạn chế từ buổi 6:** file text dễ ghi tuần tự nhưng khó tìm, cập nhật một bản ghi và duy trì ràng buộc khi lượng dữ liệu tăng.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Cấu hình Maven cho JDK 17 và thêm MySQL Connector/J.
- Thiết kế bảng `products` tương ứng model Product.
- Mở kết nối JDBC và thực hiện Create, Read, Update, Delete.
- Dùng `PreparedStatement`, `ResultSet` và try-with-resources.
- Tách cấu hình kết nối khỏi source được commit.

> **Kết quả cuối buổi:** chương trình mẫu thực hiện được CRUD Product trong MySQL qua JDBC.

## I. Từ File Text Sang Dữ Liệu Có Cấu Trúc

File buổi 6 phù hợp để học I/O, nhưng tìm Product theo id hoặc cập nhật đúng một dòng sẽ ngày càng khó. Database cung cấp bảng, khóa, ràng buộc và ngôn ngữ truy vấn.

Buổi này chỉ chuyển **catalog Product** sang MySQL. Order vẫn chưa chuyển vào database; việc chia nhỏ giúp học viên hiểu CRUD một bảng trước khi xử lý quan hệ ở buổi 9.

## II. Maven Quản Lý Build Và Dependency

`pom.xml` tối thiểu:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>vn.edu.hit</groupId>
    <artifactId>jdbc-product-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mysql.connector.version>8.4.0</mysql.connector.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>${mysql.connector.version}</version>
        </dependency>
    </dependencies>
</project>
```

Maven đọc dependency, tải thư viện và đưa nó vào classpath. Học viên chạy `mvn -version` để xác nhận Maven đang dùng Java 17, sau đó reload Maven trong IDE.

Nếu IDE chạy bằng JDK khác với Maven, lỗi có thể chỉ xuất hiện khi build ngoài IDE. Vì vậy kiểm tra cả Project SDK và output `mvn -version`.

## III. Từ Product Sang Bảng `products`

Model hiện có:

```text
Product(id, name, price, stock)
```

Schema:

```sql
CREATE DATABASE IF NOT EXISTS shop_db;
USE shop_db;

CREATE TABLE products (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL,
    CHECK (price > 0),
    CHECK (stock >= 0)
);
```

`id` là primary key vì mỗi mã Product phải duy nhất. `DECIMAL` phù hợp hơn FLOAT/DOUBLE cho dữ liệu tiền trong database. Trong Java nhập môn ta vẫn giữ `double` để không mở thêm BigDecimal trong buổi JDBC đầu tiên.

Nạp dữ liệu chuẩn:

```sql
INSERT INTO products(id, name, price, stock) VALUES
('SP01', 'Sổ tay', 25000, 50),
('SP02', 'Bút', 10000, 100),
('SP03', 'Java nhập môn', 90000, 8);
```

## IV. Kết Nối Và Đóng Tài Nguyên

```java
String url = System.getenv("SHOP_DB_URL");
String user = System.getenv("SHOP_DB_USER");
String password = System.getenv("SHOP_DB_PASSWORD");

try (Connection connection = DriverManager.getConnection(url, user, password)) {
    System.out.println("Kết nối thành công");
} catch (SQLException e) {
    System.out.println("Không thể kết nối database: " + e.getMessage());
}
```

Không commit mật khẩu thật. README chỉ ghi tên biến môi trường và ví dụ placeholder.

Trong mỗi thao tác JDBC, Connection, PreparedStatement và ResultSet đều là tài nguyên cần đóng. Try-with-resources cho phép thể hiện vòng đời này ngay trong cấu trúc code.

## V. CRUD Theo Một Mẫu Chung

### Create

```java
String sql = "INSERT INTO products(id, name, price, stock) VALUES (?, ?, ?, ?)";

try (Connection connection = openConnection();
     PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, product.getId());
    statement.setString(2, product.getName());
    statement.setDouble(3, product.getPrice());
    statement.setInt(4, product.getStock());
    return statement.executeUpdate() == 1;
}
```

Dấu `?` tách dữ liệu khỏi câu SQL. Không nối tên Product do người dùng nhập trực tiếp vào SQL.

### Read one

```java
String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";

try (Connection connection = openConnection();
     PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, id);
    try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
            return new Product(
                    result.getString("id"),
                    result.getString("name"),
                    result.getDouble("price"),
                    result.getInt("stock"));
        }
        return null;
    }
}
```

Buổi 8 sẽ thay `null` bằng Optional và tách đoạn code này vào repository.

### Read all

```java
String sql = "SELECT id, name, price, stock FROM products ORDER BY id";
```

Lặp `while (result.next())`, map mỗi row thành Product và thêm vào list. Mapping theo tên cột giúp code dễ đọc hơn index số.

### Update và Delete

```sql
UPDATE products SET name = ?, price = ?, stock = ? WHERE id = ?;
DELETE FROM products WHERE id = ?;
```

`executeUpdate()` trả số row bị ảnh hưởng. `0` nghĩa là không tìm thấy id; không được in “thành công” trong trường hợp đó.

## VI. Demo Theo Nhịp Nhìn Thấy Dữ Liệu

1. Chạy SELECT trong MySQL client để thấy ba Product seed.
2. Chạy Java listAll và đối chiếu cùng ba row.
3. Insert P04 rồi chạy SELECT lại.
4. Update giá SP01 từ 25000 thành 30000; xác nhận đúng một row đổi.
5. Tìm P99; giải thích vì sao không phải lỗi SQL.
6. Delete P04; xác nhận số row bị ảnh hưởng.

Sau mỗi thao tác Java, chạy một SELECT độc lập trong MySQL. Cách đối chiếu này giúp phân biệt lỗi mapping, lỗi SQL và lỗi hiển thị.

## VII. Thực Hành Có Hướng Dẫn

Học viên hoàn thiện năm method trong một class tạm `ProductJdbcDao`: insert, findAll, findById, update, deleteById. Class này là bước trung gian; buổi 8 sẽ đổi nó thành `JdbcProductRepository` theo interface.

Giáo viên kiểm tra hai ca lỗi:

- Insert id SP01 lần hai: database từ chối trùng khóa, chương trình báo lỗi dễ hiểu.
- Update P99: không ném lỗi, trả false vì `executeUpdate()` bằng 0.

## Lỗi thường gặp

| Hiện tượng | Nơi kiểm tra |
|---|---|
| `No suitable driver` | Dependency Maven đã tải và reload chưa |
| Access denied | User/password/quyền database, không sửa bằng cách commit mật khẩu |
| Unknown database/table | Đã chạy schema ở đúng server chưa |
| SQL syntax error | Chạy câu SQL với giá trị mẫu trong client |
| ResultSet sai dữ liệu | Tên cột và kiểu getter khi mapping |
| Chương trình treo kết nối | Tất cả tài nguyên đã nằm trong try-with-resources chưa |

## Tổng kết buổi 7

Maven cung cấp driver, MySQL giữ Product bền vững và JDBC là cầu nối. Mọi thao tác CRUD có cùng nhịp: chuẩn bị SQL, bind tham số, thực thi, đọc kết quả, đóng tài nguyên và diễn giải số row bị ảnh hưởng.

## Bài tập nhỏ trên lớp — JDBC CRUD công việc

Tạo bảng độc lập:

```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    done BOOLEAN NOT NULL
);
```

Viết class `TaskJdbcDao` có bốn thao tác: thêm task, liệt kê theo id, đánh dấu hoàn thành và xóa theo id. Mọi input phải truyền qua `PreparedStatement`; Connection, Statement và ResultSet nằm trong try-with-resources.

**Nghiệm thu:** thêm hai task, đánh dấu task đầu hoàn thành, xóa task thứ hai và SELECT còn đúng một dòng `done=true`. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 07](../BTVN/buoi-07-btvn.md). Giữ chữ ký CRUD để buổi 8 tách repository và service mà không đổi hành vi.
