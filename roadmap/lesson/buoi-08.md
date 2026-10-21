# Buổi 8: Repository, Service, Optional, enum Và Clean Code

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Tách SQL khỏi nghiệp vụ bằng repository interface và JDBC implementation.
- Đặt validation và use case ở Service thay vì View/Repository.
- Trả kết quả tìm kiếm bằng `Optional` thay cho null.
- Dùng `OrderStatus` enum thay chuỗi trạng thái tự do.
- Thêm các chức năng tạo, liệt kê và tìm Customer theo cùng cấu trúc Product.

> **Kết quả cuối buổi:** giao diện console gọi ProductService/CustomerService; service gọi repository interface; status của Order là enum.

## I. Mùi Code Sau Buổi 7

Nếu menu vừa đọc input, vừa kiểm tra giá, vừa viết SQL, một thay đổi nhỏ sẽ kéo theo nhiều trách nhiệm:

```text
case 1 trong menu
  ├── đọc Scanner
  ├── kiểm tra Product
  ├── mở Connection
  ├── chạy INSERT
  └── in thông báo
```

Code này chạy được nhưng khó test và khó đổi giao diện. Ta tách theo lý do thay đổi:

```text
Console: giao tiếp với người dùng
Service: quy tắc/use case
Repository: hợp đồng lưu trữ
JDBC implementation: SQL cụ thể
```

## II. Repository Là Hợp Đồng Truy Cập Dữ Liệu

```java
public interface ProductRepository {
    boolean insert(Product product);
    List<Product> findAll();
    Optional<Product> findById(String id);
    boolean update(Product product);
    boolean deleteById(String id);
}
```

Interface không nhắc MySQL, Connection hoặc câu SQL. Vì vậy Service có thể dùng JDBC implementation hôm nay và memory implementation khi test ở buổi 10.

```java
public class JdbcProductRepository implements ProductRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcProductRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Product> findById(String id) {
        String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";
        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapProduct(result));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể tìm Product " + id, e);
        }
    }
}
```

Repository chuyển row thành object. Nó không quyết định price bao nhiêu là hợp lệ theo nghiệp vụ và không in “không tìm thấy” ra console.

## III. Service Điều Phối Nghiệp Vụ

```java
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public boolean addProduct(Product product) {
        validate(product);
        if (repository.findById(product.getId()).isPresent()) {
            return false;
        }
        return repository.insert(product);
    }

    public Optional<Product> findProductById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    private void validate(Product product) {
        if (product == null || product.getId().isBlank()
                || product.getName().isBlank()
                || product.getPrice() <= 0
                || product.getStock() < 0) {
            throw new IllegalArgumentException("Product không hợp lệ");
        }
    }
}
```

Service phụ thuộc `ProductRepository`, không phụ thuộc `JdbcProductRepository`. Dependency đi vào qua constructor nên rõ ràng, bắt buộc và dễ thay thế.

## IV. Optional Buộc Nơi Gọi Xử Lý Không Tìm Thấy

Thay vì:

```java
Product product = repository.findById(id);
System.out.println(product.getName()); // có thể NullPointerException
```

Dùng:

```java
service.findProductById(id)
        .ifPresentOrElse(
                product -> System.out.println(product),
                () -> System.out.println("Không tìm thấy sản phẩm."));
```

Optional phù hợp với kết quả có thể không tồn tại. Không dùng Optional cho mọi field hoặc tham số; ở đây nó diễn đạt chính xác kết quả tìm theo id.

## V. Làm Lại Cùng Luồng Với Customer

Tạo bảng:

```sql
CREATE TABLE customers (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL
);
```

Tạo `CustomerRepository`, `JdbcCustomerRepository`, `CustomerService` theo cùng cấu trúc. Repository và Service giữ vai trò ổn định; các field và quy tắc dữ liệu thay đổi theo từng entity.

Luồng thêm Customer:

```text
Console đọc KH01/Nguyễn An/0900000001
    → CustomerService kiểm tra field
    → CustomerRepository kiểm tra dữ liệu hiện có và insert
    → Console nhận boolean hoặc lỗi để hiển thị
```

Console không được bỏ qua CustomerService và gọi repository trực tiếp chỉ vì chức năng này đơn giản.

## VI. enum Thay Chuỗi Trạng Thái

Ở buổi 4, status là String nên có thể bị gán sai chính tả. Thay bằng:

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
```

Trong Order:

```java
private OrderStatus status = OrderStatus.PENDING;

public void confirm() {
    if (status == OrderStatus.PENDING) {
        status = OrderStatus.CONFIRMED;
    }
}
```

Khi đọc/ghi database ở buổi 9, dùng `status.name()` để lưu và `OrderStatus.valueOf(text)` để phục hồi. Enum thu hẹp tập giá trị hợp lệ ngay ở hệ thống kiểu.

## VII. DTO/record

JDK 17 hỗ trợ record. Khi màn hình chỉ cần ba field:

```java
public record ProductSummary(String id, String name, double price) {}
```

Record phù hợp để vận chuyển dữ liệu chỉ đọc. Entity biểu diễn dữ liệu và hành vi nghiệp vụ; DTO mang dữ liệu cần thiết qua ranh giới giữa các tầng.

## VIII. Demo Trước Và Sau Khi Tách Tầng

Refactor chức năng “tìm Product theo id” theo ba commit nhỏ:

1. Di chuyển SQL vào `JdbcProductRepository`, kết quả vẫn giữ nguyên.
2. Thêm `ProductService`, console gọi service.
3. Đổi null sang Optional và xử lý nhánh không tìm thấy.

Sau mỗi commit chạy SP01 và mã không tồn tại SP99. Việc giữ test case không đổi chứng minh refactor không làm thay đổi hành vi người dùng.

## IX. Thực Hành Có Hướng Dẫn

1. Hoàn thiện ProductRepository/JdbcProductRepository từ DAO buổi 7.
2. Tạo ProductService và đưa validation ra khỏi menu.
3. Seed KH01/KH02, hoàn thiện CustomerRepository và CustomerService.
4. Đổi status của Order sang enum; chạy lại hai chuỗi chuyển trạng thái buổi 4.
5. Vẽ một mũi tên dependency và chỉ ra interface nằm ở đâu.

## Lỗi thường gặp

| Lỗi | Dấu hiệu | Cách sửa |
|---|---|---|
| Service chứa SQL | Có chuỗi SELECT/INSERT trong service | Chuyển sang JDBC repository |
| Console gọi repository | UI gắn chặt nguồn dữ liệu | Console chỉ gọi service |
| Repository in thông báo | Không tái sử dụng được cho Swing | Trả dữ liệu/trạng thái, để View hiển thị |
| `Optional.get()` không kiểm tra | Có thể ném NoSuchElementException | Dùng `ifPresent`, `orElse`, `orElseThrow` phù hợp |
| status vẫn là String ở một số class | Hợp đồng không nhất quán | Chuyển toàn bộ model/method sang OrderStatus |

## Tổng kết buổi 8

Repository che chi tiết lưu trữ, Service giữ nghiệp vụ, Optional mô tả kết quả có thể vắng mặt và enum bảo vệ tập trạng thái. JOIN và transaction không thuộc trách nhiệm của giao diện.

## Bài tập nhỏ trên lớp — Tách tầng quản lý công việc

Refactor bài `tasks` thành:

- Enum `TaskStatus { TODO, DONE }`.
- Interface `TaskRepository` có `insert`, `findAll`, `findById`, `updateStatus`.
- `JdbcTaskRepository` chứa toàn bộ SQL.
- `TaskService` từ chối title rỗng và không cho đánh dấu một id không tồn tại.
- Class console chỉ gọi TaskService; tìm id vắng mặt được xử lý bằng `Optional`.

**Nghiệm thu:** tìm id không tồn tại không gây NullPointerException, và tìm toàn source không có SQL ngoài JdbcTaskRepository. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 08](../BTVN/buoi-08-btvn.md).
