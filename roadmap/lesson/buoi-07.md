# Buổi 7: Maven, MySQL Và JDBC Trong Java

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Giải thích **Maven** là gì, vì sao cần Maven khi sử dụng thư viện bên ngoài.
- Đọc và cấu hình các thành phần cơ bản trong `pom.xml`.
- Hiểu JDBC là gì, phân biệt JDBC API với JDBC Driver.
- Giải thích cấu trúc **JDBC URL**, sử dụng `Connection` để kết nối MySQL.
- Thực hiện bốn thao tác SQL CRUD: thêm, đọc, cập nhật và xóa dữ liệu.
- Phân biệt `Statement` và `PreparedStatement`; hiểu nguyên nhân của SQL Injection.
- Sử dụng `ResultSet` để đọc dữ liệu từ database và chuyển thành object Java.
- Vận dụng `SQLException` và `try-with-resources` đã học ở buổi 6 để xử lý lỗi và đóng tài nguyên.

> **Kết quả cuối buổi:** học viên có thể tạo một Maven project, kết nối Java với MySQL, thực hiện CRUD trên một bảng và biết truyền dữ liệu vào câu SQL an toàn.

> **Môi trường:** JDK 17, Maven, MySQL 8.x; đã học class, method, collection, exception và try-with-resources ở các buổi trước.

## I. Maven: Quản Lý Project Và Thư Viện Trong Java

### 1. Vì sao cần Maven?

Ở các buổi trước, chương trình chủ yếu sử dụng những class có sẵn trong Java, chẳng hạn `ArrayList`, `Scanner` và `Files`.

Khi muốn kết nối với MySQL, chương trình cần thêm một thư viện bên ngoài là MySQL Connector/J. Nếu làm thủ công, lập trình viên phải tự tải file JAR, thêm vào project và bảo đảm những người khác sử dụng đúng phiên bản.

Khi project có thêm nhiều thư viện, việc quản lý thủ công trở nên khó khăn. Một thư viện còn có thể phụ thuộc vào thư viện khác.

**Maven** là công cụ quản lý project, dependency và quá trình build trong Java. Thay vì gửi các file JAR cho người khác, ta khai báo các thư viện cần dùng; Maven có thể tải chúng và các dependency liên quan khi cần.

| Công việc | Làm thủ công | Sử dụng Maven |
|---|---|---|
| Thêm thư viện | Tải JAR và cấu hình classpath | Khai báo dependency trong pom.xml |
| Đổi phiên bản | Tải và thay JAR | Đổi version trong pom.xml |
| Build project | Tự cấu hình biên dịch và đóng gói | Sử dụng các lệnh Maven |
| Chia sẻ project | Người khác phải biết các thư viện cần cài | Dùng cùng pom.xml để Maven giải quyết dependency |

Maven không phải ngôn ngữ lập trình, IDE hay thư viện kết nối database. Maven cũng không tự cài đặt hoặc khởi động MySQL Server.

### 2. Cấu trúc Maven project

```text
jdbc-demo/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/       # Source code Java
│   │   └── resources/  # File cấu hình và tài nguyên
│   └── test/
│       └── java/       # Code kiểm thử
└── target/             # Kết quả build do Maven tạo
```

`src/main/java` là vị trí mặc định của source code. `target` được tạo khi build, không phải nơi viết code.

### 3. File pom.xml

POM (Project Object Model) là mô hình mô tả project của Maven. File `pom.xml` chứa thông tin project, dependency và cấu hình build.

Ví dụ cấu hình project sử dụng Java 17 và MySQL Connector/J:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>vn.edu.hit</groupId>
    <artifactId>jdbc-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.4.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

| Thành phần | Vai trò |
|---|---|
| `groupId` | Định danh nhóm hoặc tổ chức sở hữu project/thư viện |
| `artifactId` | Tên định danh project hoặc thư viện |
| `version` | Phiên bản của project hoặc thư viện |
| `properties` | Khai báo các giá trị cấu hình, ví dụ phiên bản Java |
| `dependencies` | Danh sách thư viện project cần sử dụng |
| `build`, `plugins` | Cấu hình các công cụ tham gia quá trình build |

Bộ ba `groupId`, `artifactId`, `version` giúp Maven xác định một artifact cụ thể. Ví dụ trên khai báo MySQL Connector/J phiên bản 8.4.0.

Khi build hoặc reload project, Maven tìm thư viện trong kho cục bộ (thường là `/.m2/repository). Nếu chưa có, Maven có thể tải từ kho từ xa, chẳng hạn Maven Central.

### 4. Các lệnh Maven cơ bản

Chạy tại thư mục chứa `pom.xml`:

```bash
mvn -version
mvn compile
mvn test
mvn package
mvn clean
```

| Lệnh | Công việc |
|---|---|
| `mvn -version` | Kiểm tra Maven và Java đang được Maven sử dụng |
| `mvn compile` | Biên dịch source code |
| `mvn test` | Biên dịch và chạy các bài kiểm thử đã cấu hình |
| `mvn package` | Thực hiện các giai đoạn cần thiết để đóng gói project, thường thành JAR |
| `mvn clean` | Xóa kết quả build cũ |

Có thể chạy `mvn clean package` để build lại từ đầu. File JAR thông thường không tự chứa tất cả dependency và không nhất thiết chạy được bằng `java -jar`. Trong buổi này, reload Maven rồi chạy method `main` bằng IDE.

### Kiểm tra nhanh

Khi gửi source code và pom.xml cho một người khác, vì sao không cần gửi kèm file JAR của MySQL Connector/J? Maven có thay thế MySQL Server không?

## II. JDBC: Cầu Nối Giữa Java Và Database

Ở buổi 6, dữ liệu được đọc và ghi vào file. Nếu cần tìm một sản phẩm theo mã, chương trình có thể phải đọc từng dòng, tách chuỗi và tự kiểm tra dữ liệu.

Database cho phép lưu dữ liệu có cấu trúc, truy vấn theo điều kiện và áp dụng các ràng buộc. Java không cần tự thực hiện toàn bộ công việc đó, nhưng cần một cơ chế giao tiếp với database.

**JDBC (Java Database Connectivity)** là API chuẩn của Java để làm việc với cơ sở dữ liệu quan hệ. JDBC cung cấp các interface và class để kết nối, gửi SQL và đọc kết quả.

```text
Java Application
       |
       v
JDBC API (java.sql)
       |
       v
JDBC Driver (MySQL Connector/J)
       |
       v
MySQL Server
```

- JDBC API: quy định cách Java sử dụng các thành phần như `Connection`, `Statement` và `ResultSet`.
- JDBC Driver: thư viện triển khai việc giao tiếp với một hệ quản trị database cụ thể.
- MySQL Server: nơi thực thi SQL và lưu trữ dữ liệu.

Các interface JDBC thuộc `java.sql` có sẵn trong JDK. MySQL Connector/J là dependency bổ sung để các lời gọi JDBC có thể giao tiếp với MySQL.

Nếu chuyển từ MySQL sang PostgreSQL, nhiều thao tác qua JDBC API vẫn tương tự, nhưng phải đổi driver, URL và có thể phải sửa cú pháp SQL đặc thù của database.

## III. JDBC URL Và Connection

### 1. JDBC URL: Chương trình cần kết nối đến đâu?

```java
String url = "jdbc:mysql://localhost:3306/shop_db";
```

| Thành phần | Ý nghĩa |
|---|---|
| `jdbc` | Tiền tố của JDBC URL |
| `mysql` | Loại JDBC driver/database |
| `localhost` | Máy đang chạy MySQL Server |
| `3306` | Cổng mặc định của MySQL |
| `shop_db` | Tên database cần sử dụng |

URL mô tả đích kết nối, không tự tạo database và không phải bản thân kết nối. Nếu server chạy trên máy khác hoặc port khác, phải thay thông tin tương ứng.

### 2. Chuẩn bị database

Mở MySQL Workbench hoặc MySQL client, chạy:

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

INSERT INTO products(id, name, price, stock) VALUES
('SP01', 'Sổ tay', 25000, 50),
('SP02', 'Bút', 10000, 100),
('SP03', 'Java nhập môn', 90000, 8);
```

Chạy phần tạo bảng và thêm dữ liệu một lần trên database mới. Nếu chạy lại INSERT với các mã đã tồn tại, MySQL sẽ báo lỗi trùng khóa chính.

### 3. Connection: Mở kết nối

`Connection` là interface JDBC biểu diễn một kết nối với database. Thông qua kết nối, Java có thể tạo đối tượng thực thi SQL.

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/shop_db";
        String user = System.getenv("SHOP_DB_USER");
        String password = System.getenv("SHOP_DB_PASSWORD");

        try (Connection connection =
                     DriverManager.getConnection(url, user, password)) {
            System.out.println("Kết nối thành công");
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại: " + e.getMessage());
        }
    }
}
```

Trước khi chạy, thiết lập `SHOP_DB_USER` và `SHOP_DB_PASSWORD` trong môi trường chạy của IDE, với tài khoản MySQL có quyền trên shop_db. URL cũng có thể đưa vào biến môi trường `SHOP_DB_URL` như yêu cầu BTVN. Không commit mật khẩu thật.

`DriverManager.getConnection(...)` tìm driver phù hợp và yêu cầu mở kết nối. Với MySQL Connector/J hiện đại đã có trong classpath, thông thường không cần tự gọi `Class.forName(...)`.

Một kết nối thành công không có nghĩa là mọi câu SQL sau đó đều thành công. Các thao tác vẫn có thể thất bại vì sai cú pháp, thiếu quyền hoặc vi phạm ràng buộc dữ liệu.

### 4. SQLException và đóng kết nối

`SQLException` là checked exception, nên khi gọi các method JDBC có thể ném lỗi này, phải `catch` hoặc khai báo `throws SQLException`.

`Connection` hỗ trợ `AutoCloseable`, vì vậy dùng `try-with-resources` để Java tự đóng kết nối khi rời khỏi khối try, kể cả khi có exception.

### Kiểm tra nhanh

Nếu pom.xml đã khai báo MySQL Connector/J nhưng MySQL Server chưa chạy, Java có kết nối thành công không? JDBC URL khác Connection ở điểm nào?

## IV. SQL CRUD: Bốn Thao Tác Cơ Bản Với Database

CRUD là viết tắt của Create, Read, Update và Delete. Đây là bốn thao tác làm việc với dữ liệu, không phải bốn câu lệnh Java.

| Thao tác | SQL | Mục đích |
|---|---|---|
| Create | `INSERT` | Thêm một dòng dữ liệu |
| Read | `SELECT` | Đọc dữ liệu |
| Update | `UPDATE` | Thay đổi dữ liệu |
| Delete | `DELETE` | Xóa dữ liệu |

### 1. Create — Thêm dữ liệu

```sql
INSERT INTO products(id, name, price, stock)
VALUES ('SP04', 'Thước kẻ', 15000, 20);
```

### 2. Read — Đọc dữ liệu

```sql
SELECT id, name, price, stock FROM products;

SELECT id, name, price, stock
FROM products WHERE id = 'SP04';
```

`WHERE` giúp lọc những dòng thỏa mãn điều kiện. Nếu không có dòng phù hợp, SELECT trả kết quả rỗng, không nhất thiết phát sinh lỗi.

### 3. Update — Cập nhật dữ liệu

```sql
UPDATE products SET price = 18000 WHERE id = 'SP04';
```

### 4. Delete — Xóa dữ liệu

```sql
DELETE FROM products WHERE id = 'SP04';
```

Nếu bỏ `WHERE` trong UPDATE hoặc DELETE ở các ví dụ này, câu lệnh có thể cập nhật hoặc xóa toàn bộ các dòng trong bảng. Chỉ thử nghiệm trên database học tập.

### Kiểm tra nhanh

Nếu muốn sửa tên sản phẩm SP01 mà không ảnh hưởng tới SP02, cần sử dụng câu SQL nào và điều kiện gì?

## V. Statement: Gửi Câu SQL Từ Java

Sau khi có `Connection`, Java cần một đối tượng để gửi SQL đến database.

**Statement** là interface JDBC cho phép thực thi câu SQL được truyền vào dưới dạng chuỗi.

Các đoạn code JDBC từ phần này được đặt trong method có sẵn `Connection connection` đang mở. Method cần khai báo `throws SQLException` hoặc xử lý exception phù hợp. Các class JDBC sử dụng import từ `java.sql`.

### 1. Đọc dữ liệu

```java
String sql = "SELECT id, name FROM products ORDER BY id";

try (Statement statement = connection.createStatement();
     ResultSet result = statement.executeQuery(sql)) {
    while (result.next()) {
        System.out.println(result.getString("id")
                + " - " + result.getString("name"));
    }
}
```

`connection.createStatement()` tạo Statement. `executeQuery(sql)` gửi câu SELECT và trả về ResultSet.

### 2. Thay đổi dữ liệu

```java
String sql = "UPDATE products SET price = 30000 WHERE id = 'SP01'";

try (Statement statement = connection.createStatement()) {
    int rows = statement.executeUpdate(sql);
    System.out.println("Số dòng bị tác động: " + rows);
}
```

| Method | Khi sử dụng | Kết quả trả về |
|---|---|---|
| `executeQuery(sql)` | Câu SQL trả về dữ liệu dạng bảng, ví dụ SELECT | ResultSet |
| `executeUpdate(sql)` | INSERT, UPDATE, DELETE; có thể dùng cho DDL | Số dòng bị tác động, hoặc 0 với câu lệnh không trả số dòng |

Statement phù hợp với câu SQL cố định không chứa dữ liệu đầu vào cần ghép vào chuỗi SQL.

### Kiểm tra nhanh

Vì sao không sử dụng executeQuery để nhận số dòng đã được UPDATE?

## VI. PreparedStatement Và SQL Injection

### 1. Vấn đề khi ghép chuỗi SQL

Giả sử người dùng nhập tên sản phẩm cần tìm:

```java
String inputName = "Sổ tay";
String sql = "SELECT id, name FROM products WHERE name = '"
        + inputName + "'";
```

Đoạn code có vẻ hoạt động bình thường. Tuy nhiên, nếu tên có dấu nháy đơn, chẳng hạn `O'Reilly`, câu SQL được tạo có thể sai cú pháp.

Vấn đề nghiêm trọng hơn xảy ra khi dữ liệu đầu vào chứa cú pháp SQL và làm thay đổi ý nghĩa của câu lệnh ban đầu.

**SQL Injection** là lỗ hổng xảy ra khi dữ liệu không đáng tin cậy được đưa vào câu SQL theo cách khiến database có thể hiểu nó là một phần của câu lệnh, thay vì chỉ là dữ liệu. Hậu quả có thể là truy cập hoặc thay đổi dữ liệu ngoài phạm vi được phép.

### 2. PreparedStatement: Tách dữ liệu khỏi câu SQL

```java
String sql = "SELECT id, name FROM products WHERE name = ?";

try (PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, inputName);

    try (ResultSet result = statement.executeQuery()) {
        while (result.next()) {
            System.out.println(result.getString("name"));
        }
    }
}
```

Cơ chế hoạt động:

- Viết cấu trúc SQL với dấu `?` tại vị trí cần truyền giá trị.
- Tạo PreparedStatement từ Connection.
- Dùng `setString`, `setInt`, `setDouble`... để gán giá trị vào từng tham số.
- Gọi `executeQuery()` hoặc `executeUpdate()` mà không truyền lại chuỗi SQL.

Vị trí tham số bắt đầu từ **1**, không phải 0. Không đặt dấu nháy đơn bao quanh dấu `?`.

Khi inputName là `O'Reilly`, giá trị vẫn được truyền như dữ liệu văn bản, không làm thay đổi cấu trúc câu SQL.

### 3. So sánh Statement và PreparedStatement

| Tiêu chí | Statement | PreparedStatement |
|---|---|---|
| Tạo đối tượng | `createStatement()` | `prepareStatement(sql)` |
| Truyền SQL | Khi gọi executeQuery/executeUpdate | Khi tạo đối tượng |
| Gán tham số | Không có cơ chế bind `?` | Dùng các method setXxx |
| SQL chứa input | Không nên ghép trực tiếp | Bind tham số thay vì ghép chuỗi |
| Thực thi lặp lại | Gửi lại câu SQL | Có thể dùng lại cùng cấu trúc với giá trị khác |

PreparedStatement có thể giúp tái sử dụng câu SQL; việc chuẩn bị câu lệnh ở phía server và lợi ích hiệu năng còn phụ thuộc driver, cấu hình và database.

PreparedStatement không tự bảo vệ câu SQL nếu lập trình viên vẫn ghép input trực tiếp vào chuỗi trước khi gọi prepareStatement. Dấu `?` chỉ đại diện cho giá trị dữ liệu, không thay thế được tên bảng, tên cột hoặc từ khóa SQL. Nếu cần chọn tên cột động, phải giới hạn lựa chọn bằng danh sách hợp lệ.

### Kiểm tra nhanh

Nếu người dùng nhập một tên chứa dấu nháy đơn, vì sao PreparedStatement vẫn có thể xử lý đúng? Nếu đã dùng PreparedStatement nhưng vẫn nối trực tiếp input vào chuỗi SQL, còn nguy cơ SQL Injection không?

## VII. ResultSet: Đọc Kết Quả Truy Vấn

### 1. ResultSet là gì?

Khi thực hiện SELECT, MySQL có thể trả về nhiều dòng. `ResultSet` là interface JDBC cho phép Java duyệt và đọc dữ liệu của kết quả truy vấn.

Ví dụ:

| id | name | price | stock |
|---|---|---:|---:|
| SP01 | Sổ tay | 25000 | 50 |
| SP02 | Bút | 10000 | 100 |
| SP03 | Java nhập môn | 90000 | 8 |

Khi mới nhận ResultSet, con trỏ nằm **trước dòng đầu tiên**.

```text
Ban đầu:     cursor -> trước dòng SP01
next():      cursor -> SP01      (true)
next():      cursor -> SP02      (true)
next():      cursor -> SP03      (true)
next():      cursor -> hết dữ liệu (false)
```

`next()` chuyển con trỏ sang dòng kế tiếp và trả về true nếu có dòng để đọc. Vì vậy phải gọi next() trước khi lấy dữ liệu.

### 2. Lấy giá trị từ một dòng

```java
while (result.next()) {
    String id = result.getString("id");
    String name = result.getString("name");
    double price = result.getDouble("price");
    int stock = result.getInt("stock");

    System.out.println(id + " - " + name + " - " + price + " - " + stock);
}
```

Có thể đọc theo tên cột hoặc vị trí cột (bắt đầu từ 1). Đọc theo tên cột thường rõ ràng hơn. Ví dụ nhập môn giữ giá kiểu double như model Product hiện có; khi cần tính toán tiền chính xác, ưu tiên BigDecimal và getBigDecimal().

### 3. Đọc một dòng và đọc nhiều dòng

| Cách sử dụng | Khi nào dùng? |
|---|---|
| `if (result.next())` | Truy vấn tối đa một dòng, ví dụ tìm theo primary key |
| `while (result.next())` | Truy vấn có thể trả về nhiều dòng, ví dụ liệt kê toàn bộ |

ResultSet không phải List và JDBC không tự chuyển mỗi dòng thành Product. Lập trình viên cần đọc từng cột và tạo object tương ứng.

### Kiểm tra nhanh

Nếu ResultSet không chứa dòng nào thì lần gọi next() đầu tiên trả về gì? Vì sao không nên gọi getString ngay sau executeQuery mà chưa gọi next()?

## VIII. Vận Dụng JDBC: CRUD Sản Phẩm

Giả sử đã có class Product với bốn thuộc tính `id`, `name`, `price`, `stock`, constructor và các getter tương ứng. Bảng products đã được tạo ở phần III.

Mỗi method dưới đây nhận Connection do nơi gọi mở. Method đóng Statement và ResultSet do mình tạo, **không đóng Connection được truyền vào**.

### 1. Create — Thêm sản phẩm

```java
static boolean add(Connection connection, Product product)
        throws SQLException {
    String sql = "INSERT INTO products(id, name, price, stock) VALUES (?, ?, ?, ?)";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, product.getId());
        statement.setString(2, product.getName());
        statement.setDouble(3, product.getPrice());
        statement.setInt(4, product.getStock());

        return statement.executeUpdate() == 1;
    }
}
```

Thứ tự setXxx phải khớp với thứ tự dấu `?`. Nếu trùng primary key, database có thể ném SQLException thay vì trả 0.

### 2. Read — Tìm sản phẩm theo mã

```java
static Product findById(Connection connection, String id)
        throws SQLException {
    String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
}
```

Không tìm thấy dữ liệu là kết quả hợp lệ của truy vấn. Method trả null để nơi gọi quyết định thông báo phù hợp.

### 3. Read — Liệt kê sản phẩm

```java
static List<Product> findAll(Connection connection) throws SQLException {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT id, name, price, stock FROM products ORDER BY id";

    try (Statement statement = connection.createStatement();
         ResultSet result = statement.executeQuery(sql)) {
        while (result.next()) {
            products.add(new Product(
                    result.getString("id"),
                    result.getString("name"),
                    result.getDouble("price"),
                    result.getInt("stock")));
        }
    }
    return products;
}
```

Cần import thêm `java.util.List` và `java.util.ArrayList`. Câu SQL cố định nên có thể dùng Statement. Trường hợp bảng rỗng, method trả danh sách rỗng.

### 4. Update — Cập nhật sản phẩm

```java
static boolean update(Connection connection, Product product)
        throws SQLException {
    String sql = "UPDATE products SET name = ?, price = ?, stock = ? WHERE id = ?";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, product.getName());
        statement.setDouble(2, product.getPrice());
        statement.setInt(3, product.getStock());
        statement.setString(4, product.getId());

        return statement.executeUpdate() == 1;
    }
}
```

Không cập nhật id trong ví dụ này. Dùng id để xác định đúng sản phẩm cần thay đổi.

### 5. Delete — Xóa sản phẩm

```java
static boolean deleteById(Connection connection, String id)
        throws SQLException {
    String sql = "DELETE FROM products WHERE id = ?";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, id);
        return statement.executeUpdate() == 1;
    }
}
```

Với UPDATE và DELETE theo khóa chính, nếu id không tồn tại thì số dòng bị tác động là 0. Không báo thành công trong trường hợp đó. Riêng UPDATE giá trị không thay đổi, số dòng trả về có thể phụ thuộc cách driver/database tính số dòng khớp hay thực sự thay đổi.

### 6. Xử lý lỗi tại nơi gọi

Giả sử các method trên được đặt trong class ProductJdbc. Nơi gọi mở kết nối, thực hiện thao tác và đóng kết nối khi hoàn tất:

```java
String url = System.getenv("SHOP_DB_URL");
String user = System.getenv("SHOP_DB_USER");
String password = System.getenv("SHOP_DB_PASSWORD");

try (Connection connection =
             DriverManager.getConnection(url, user, password)) {
    Product product = ProductJdbc.findById(connection, "SP01");

    if (product == null) {
        System.out.println("Không tìm thấy sản phẩm.");
    } else {
        System.out.println(product.getName());
    }
} catch (SQLException e) {
    System.out.println("Thao tác database thất bại: " + e.getMessage());
}
```

Đặt SHOP_DB_URL thành `jdbc:mysql://localhost:3306/shop_db` trong cấu hình chạy cục bộ.

Áp dụng kiến thức buổi 6: method JDBC khai báo throws để chuyển lỗi đến nơi có trách nhiệm xử lý. Không bắt lỗi rồi trả null hoặc false cho mọi trường hợp, vì như vậy nơi gọi không phân biệt được lỗi kết nối với không tìm thấy dữ liệu.

| Tài nguyên | Nơi tạo | Nơi đóng trong ví dụ |
|---|---|---|
| Connection | DriverManager | Method gọi ProductJdbc |
| Statement / PreparedStatement | Connection | Method ProductJdbc |
| ResultSet | Statement / PreparedStatement | Method đọc kết quả |

Try-with-resources đóng các tài nguyên khai báo trong cùng khối theo thứ tự ngược với khi tạo. Với các ví dụ trên, ResultSet được đóng trước Statement, sau đó Connection được đóng ở nơi gọi.

## IX. Thực Hành Có Hướng Dẫn

1. Tạo Maven project, thêm dependency MySQL Connector/J, chạy mvn compile.
2. Tạo database shop_db, bảng products và dữ liệu mẫu.
3. Mở Connection thành công; thử sai tên database để quan sát SQLException.
4. Dùng Statement và ResultSet liệt kê các sản phẩm theo mã.
5. Dùng PreparedStatement tìm SP01 và SP99; phân biệt tìm thấy và không tìm thấy.
6. Thêm SP04, cập nhật giá của SP04 rồi xóa SP04. Sau mỗi thao tác, kiểm tra bằng SELECT trong MySQL.
7. Thử thêm lại SP01 để quan sát lỗi trùng khóa; cập nhật hoặc xóa SP99 để quan sát kết quả 0 dòng.
8. Thử truyền tên sản phẩm có dấu nháy đơn qua PreparedStatement và kiểm tra dữ liệu được lưu đúng.

Sau mỗi ca, xác định: **câu SQL nào được thực thi, Java nhận kết quả kiểu gì, tài nguyên nào cần đóng và lỗi được xử lý ở đâu?**

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| Chưa khai báo hoặc reload dependency | Không tìm thấy JDBC driver | Kiểm tra pom.xml và reload Maven |
| Sai host hoặc port, server chưa chạy | Không kết nối được database | Kiểm tra MySQL Server và JDBC URL |
| Sai user/password hoặc thiếu quyền | Access denied | Kiểm tra tài khoản và quyền |
| Sai tên database hoặc bảng | Unknown database/table | Chạy schema đúng database |
| Ghép input trực tiếp vào SQL | Lỗi cú pháp hoặc SQL Injection | Dùng PreparedStatement và bind giá trị |
| Bắt đầu vị trí tham số từ 0 | SQLException do sai index | Bắt đầu từ 1 |
| Dùng executeQuery cho INSERT/UPDATE/DELETE | Không nhận đúng kiểu kết quả | Dùng executeUpdate |
| Đọc ResultSet trước next() | Con trỏ chưa nằm trên dòng hợp lệ | Gọi next() trước khi đọc |
| Quên WHERE khi UPDATE/DELETE | Có thể sửa hoặc xóa cả bảng | Kiểm tra điều kiện trước khi thực thi |
| Quên đóng tài nguyên | Lãng phí hoặc cạn kết nối | Dùng try-with-resources |
| Bắt SQLException rồi luôn trả false/null | Không phân biệt lỗi SQL với không có dữ liệu | Chuyển lỗi tới nơi xử lý phù hợp |

## Tổng kết buổi 7

Maven giúp quản lý thư viện và quá trình build. JDBC là API để Java giao tiếp với database thông qua driver. JDBC URL xác định đích kết nối; Connection biểu diễn kết nối đang sử dụng; Statement và PreparedStatement thực thi SQL; ResultSet cho phép đọc kết quả SELECT.

Trong chương trình thực tế, điều quan trọng là **tách dữ liệu khỏi câu SQL, đọc đúng kết quả, xử lý lỗi đúng nơi và đóng tài nguyên sau khi sử dụng**.

### Câu hỏi chốt buổi

1. **Nếu có thể tự tải và thêm file JAR vào project, vì sao cần Maven?** Maven giải quyết những công việc nào?
2. pom.xml có vai trò gì? groupId, artifactId và version dùng để làm gì?
3. **JDBC và JDBC Driver khác nhau ở điểm nào?** Vì sao đã có java.sql mà vẫn phải thêm MySQL Connector/J?
4. JDBC URL chứa những thông tin gì? URL khác Connection như thế nào?
5. SQL CRUD gồm những thao tác nào? Vì sao cần WHERE trong UPDATE và DELETE?
6. executeQuery và executeUpdate khác nhau về mục đích và kiểu kết quả trả về như thế nào?
7. **Statement và PreparedStatement khác nhau ở đâu?** Khi nào có thể dùng Statement?
8. **SQL Injection bản chất là gì? Vì sao truyền input qua dấu ? có thể ngăn SQL Injection, còn ghép chuỗi lại có nguy cơ?**
9. ResultSet có phải List không? Vì sao phải gọi next() trước khi đọc dữ liệu?
10. Nếu SELECT không tìm thấy dữ liệu hoặc UPDATE trả 0, có phải đã xảy ra SQLException không?
11. **Vì sao cần đóng Connection, Statement và ResultSet?** Nếu method nhận Connection từ nơi gọi thì có nên tự đóng Connection đó không?

## Bài tập nhỏ trên lớp — Quản lý công việc

Tạo bảng tasks trong database học tập:

```sql
CREATE TABLE tasks (
    id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    done BOOLEAN NOT NULL
);
```

Tạo class Task với id, title và done. Viết các method thực hiện:

- Thêm một công việc có trạng thái chưa hoàn thành.
- Liệt kê toàn bộ công việc bằng Statement và ResultSet.
- Tìm công việc theo mã bằng PreparedStatement.
- Đánh dấu công việc đã hoàn thành.
- Xóa công việc theo mã.

Mọi câu SQL nhận input phải bind tham số. Sử dụng try-with-resources để đóng tài nguyên.

**Kết quả mong đợi:** thêm T01 và T02, đánh dấu T01 hoàn thành, xóa T02; bảng còn đúng một dòng T01 với done = true. Tìm T99 trả kết quả không tồn tại.

Thời gian làm khoảng 20 phút. Trọng tâm là hiểu luồng JDBC và sử dụng đúng các thành phần; không yêu cầu xây dựng menu hoặc kiến trúc nhiều tầng trên lớp.

## Bài tập về nhà

Hoàn thành [BTVN buổi 07](../BTVN/buoi-07-btvn.md).
