# Buổi 10: MVC, SOLID Cơ Bản và Refactor Project

---

## Mục tiêu buổi học

- Tổ chức ứng dụng theo Model, View, Controller trên nền repository/service đã có.
- Áp dụng Single Responsibility và Dependency Inversion ở mức dễ hiểu.
- Dùng interface repository để service không phụ thuộc trực tiếp JDBC.
- Refactor code mà vẫn giữ chức năng CRUD/đơn hàng đã xây.

> **Nối tiếp:** Buổi 9 hoàn thiện đơn hàng nhiều bảng. Project đã có model, repository và service nhưng cần ranh giới nhất quán trước khi kiểm tra và làm nhóm.

## I. MVC Là Cách Sắp Xếp Trách Nhiệm

- Model biểu diễn dữ liệu/domain: Product, Customer, Order.
- View hiển thị console và nhận lựa chọn.
- Controller điều phối input sang service, nhận kết quả và cập nhật view.
- Repository truy cập dữ liệu; Service thực hiện nghiệp vụ.

~~~text
View -> Controller -> Service -> Repository interface -> JDBC implementation -> MySQL
                         |
                       Model
~~~

Không cần framework MVC. Mục tiêu là biết code nào thuộc vai trò nào và tránh SQL trong view.

Model mô tả dữ liệu/khái niệm nghiệp vụ; View trình bày; Controller nhận sự kiện hoặc input và điều phối. Service/Repository hỗ trợ xử lý nghiệp vụ và lưu trữ, không đồng nhất với ba chữ MVC. MVC là cách phân chia trách nhiệm, không phải quy tắc mỗi màn hình chỉ có đúng ba class.

> **Hình minh họa cần bổ sung:** Yêu cầu đi từ View qua Controller, Service, Repository tới DB rồi kết quả quay lại View.

## II. SOLID: Hai Ý Chính

- **Single Responsibility:** class nên có một nhóm trách nhiệm liên quan. OrderService không nên in giao diện hay dựng SQL.
- **Dependency Inversion:** service phụ thuộc abstraction (interface) để có thể thay implementation.

~~~java
public interface OrderRepository {
    long save(Order order);
    Optional<Order> findById(long id);
}

public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
~~~

Constructor nhận dependency giúp test thủ công hoặc thay repository khác mà không đổi logic service.

SRP không có nghĩa “một class chỉ có một method”; nghĩa là nhóm hành vi có một lý do chính để thay đổi. Dependency Inversion ở đây: Service phụ thuộc hợp đồng (interface), còn JDBC là một triển khai. Truyền implementation qua constructor là cách nối cụ thể lúc khởi tạo. Buổi này tập trung SRP và DIP ở mức nền tảng.

## III. Singleton Và Factory Đơn Giản

Singleton đảm bảo một instance dùng chung khi thực sự cần, ví dụ đối tượng cấu hình đọc một lần. Factory gom cách tạo object khi lựa chọn class cụ thể. Giới thiệu cách dùng tối thiểu; không áp dụng pattern chỉ để tăng số class.

~~~java
public class PaymentFactory {
    public static PaymentMethod create(String type) {
        if ("CASH".equals(type)) return new CashPayment();
        if ("BANK".equals(type)) return new BankTransferPayment();
        throw new IllegalArgumentException("Loại thanh toán không hỗ trợ");
    }
}
~~~

Static ở cấp class đã là phần nâng cao buổi 3; nếu lớp chưa nắm, nhắc lại ngắn qua method factory static, không mở rộng lý thuyết.

## IV. Refactor Không Làm Mất Chức Năng

1. Vẽ luồng hiện tại và xác định class đang làm quá nhiều việc.
2. Tách phần hiển thị khỏi điều phối.
3. Cho Service gọi interface repository.
4. Giữ implementation JDBC ở repository cụ thể.
5. Chạy lại thủ công luồng CRUD sản phẩm và tạo/xem đơn trước và sau refactor.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| View viết SQL | Chưa tách tầng | Chuyển query về repository |
| Service tạo JDBC trực tiếp | Phụ thuộc implementation | Nhận interface qua constructor |
| Class quá nhiều method không liên quan | Trách nhiệm chưa rõ | Tách theo lý do thay đổi |
| Dùng Singleton ở mọi nơi | Lạm dụng pattern | Chỉ dùng khi cần một instance dùng chung |

## Tổng Kết Và Mốc Project

- Project có ranh giới View/Controller, Service, Repository, Model rõ ràng.
- Chức năng chính được giữ sau refactor; buổi 11 chuẩn bị Git làm nhóm và giao diện Swing demo.

### Câu Hỏi Ôn Tập

1. MVC chia phần nào của ứng dụng?
2. Vì sao Service không nên chứa SQL trực tiếp?
3. Dependency Inversion thể hiện thế nào qua repository interface?
4. Khi nào Singleton hữu ích và vì sao không nên lạm dụng?
5. Factory có trách nhiệm gì?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 10](../BTVN/buoi-10-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Java Classes and Objects](https://docs.oracle.com/javase/tutorial/java/javaOO/index.html)
- [Oracle — JDBC Basics](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html)
- [Martin Fowler — Dependency Injection](https://martinfowler.com/articles/injection.html)
