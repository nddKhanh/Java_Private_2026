# Buổi 10: MVC, SOLID Và Refactor

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Chỉ ra trách nhiệm của View, Controller, Service, Repository và Model.
- Refactor một luồng đang chạy mà giữ nguyên kết quả nghiệm thu.
- Áp dụng SRP và DIP vào Product/Order thay vì học SOLID như định nghĩa rời.
- Tạo composition root để nối implementation với abstraction.
- Thay JdbcProductRepository bằng MemoryProductRepository mà không sửa ProductService.

> **Kết quả cuối buổi:** ứng dụng mẫu có luồng View → Controller → Service → Repository interface → JDBC implementation và giữ nguyên hành vi trước refactor.

> **Môi trường:** JDK 17; không đổi schema hoặc dữ liệu chuẩn trong lúc refactor.

## I. Chẩn Đoán Code Trước Refactor

Một case menu điển hình có thể đang làm quá nhiều việc:

```java
case 7 -> {
    String customerId = scanner.nextLine();
    // đọc item
    // tìm Product bằng SQL
    // kiểm tra stock
    // insert Order và OrderItem
    // in kết quả
}
```

Đặt ba câu hỏi:

1. Nếu thay giao diện console bằng Swing, những thành phần nào phải thay đổi?
2. Nếu thay MySQL bằng repository in-memory để kiểm thử, những thành phần nào phải thay đổi?
3. Nếu quy tắc tồn kho thay đổi, tầng nào cần được sửa?

Nếu câu trả lời đều là “sửa case 7”, code chưa phân trách nhiệm.

## II. Kiến Trúc Đích Và Luồng Thật

```text
ConsoleView
    ↓ dữ liệu thô
AppController
    ↓ yêu cầu use case
OrderService
    ↓ gọi hợp đồng
OrderRepository + OrderItemRepository + ProductRepository
    ↓
JdbcXxxRepository
    ↓
MySQL
```

Kết quả đi ngược lại qua cùng các tầng. MVC trong ứng dụng này không phải ba thư mục trang trí; nó là quy tắc ai được gọi ai và ai chịu trách nhiệm điều gì.

## III. View Không Tạo Entity Khi Chưa Có Đủ Ngữ Cảnh

Người dùng chỉ nhập product id và quantity. View không tự tra Product hoặc quyết định unit price. Tạo input record trong package `dto`:

```java
public record OrderItemInput(String productId, int quantity) {}
```

```java
public List<OrderItemInput> readOrderItems() {
    // chỉ đọc id và quantity rồi trả dữ liệu thô
}
```

Controller nhận danh sách này và gọi Service. OrderService mới có repository để tìm Product, kiểm tra tồn kho và chốt giá.

```java
public long createOrder(
        String customerId,
        List<OrderItemInput> inputs,
        LocalDateTime createdAt) {
    // tìm Customer/Product
    // validate quantity
    // tạo OrderItem với product.getPrice()
    // gọi repository
}
```

Ranh giới này ngăn người dùng tự nhập unit price và ngăn View gắn với JDBC.

## IV. Controller Chỉ Điều Phối

```java
public void handleCreateOrder() {
    String customerId = view.readCustomerId();
    List<OrderItemInput> items = view.readOrderItems();
    LocalDateTime createdAt = view.readCreatedAt();

    try {
        long id = orderService.createOrder(customerId, items, createdAt);
        view.showMessage("Đã tạo đơn " + id);
    } catch (IllegalArgumentException e) {
        view.showMessage(e.getMessage());
    }
}
```

Controller biết thứ tự tương tác, nhưng không viết SQL và không tự tính tổng. Nếu controller có vòng lặp giảm stock hoặc JOIN, trách nhiệm đang bị đặt sai.

## V. SRP Qua Một Thay Đổi Cụ Thể

Single Responsibility Principle có thể hiểu bằng lý do thay đổi:

| Thành phần | Thay đổi khi |
|---|---|
| ConsoleView | Cách nhập/hiển thị console đổi |
| AppController | Luồng điều phối màn hình/menu đổi |
| OrderService | Quy tắc tạo/hủy đơn đổi |
| JdbcOrderRepository | Schema hoặc SQL Order đổi |
| Order | Hành vi nội tại/trạng thái Order đổi |

Một class không cần “chỉ có một method”; nó cần có một nhóm trách nhiệm kết dính và một lý do thay đổi chính.

## VI. DIP Qua Repository Interface

Sai hướng phụ thuộc:

```java
public class ProductService {
    private final JdbcProductRepository repository = new JdbcProductRepository();
}
```

ProductService vừa tự tạo dependency, vừa khóa chặt MySQL. Sửa bằng constructor:

```java
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
}
```

Memory implementation:

```java
public class MemoryProductRepository implements ProductRepository {
    private final List<Product> products = new ArrayList<>();

    @Override
    public Optional<Product> findById(String id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    // các method còn lại theo cùng hợp đồng
}
```

Nếu cùng ProductService chạy được với JDBC và memory repository, dependency inversion đã tạo ra lợi ích quan sát được.

## VII. Composition Root

Tất cả việc chọn implementation nằm ở một nơi:

```java
public class Application {
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        ProductRepository productRepository =
                new JdbcProductRepository(connectionFactory);
        CustomerRepository customerRepository =
                new JdbcCustomerRepository(connectionFactory);
        OrderRepository orderRepository =
                new JdbcOrderRepository(connectionFactory);
        OrderItemRepository itemRepository =
                new JdbcOrderItemRepository(connectionFactory);

        ProductService productService = new ProductService(productRepository);
        CustomerService customerService = new CustomerService(customerRepository);
        OrderService orderService = new OrderService(
                orderRepository, itemRepository,
                productRepository, customerRepository);

        ConsoleView view = new ConsoleView();
        AppController controller = new AppController(
                view, productService, customerService, orderService);
        controller.run();
    }
}
```

Class main không xử lý nghiệp vụ; nó lắp ráp object graph.

## VIII. Quy Trình Refactor An Toàn

Không di chuyển toàn bộ project trong một lần. Dùng các bước có thể chạy độc lập:

1. Ghi lại một bộ test Product/Order đang chạy đúng trước khi di chuyển code.
2. Tạo package nhưng chưa đổi hành vi.
3. Tách View, chạy CRUD Product.
4. Tách Controller, chạy lại cùng test.
5. Đổi Service sang repository interface, chạy JDBC.
6. Thêm MemoryProductRepository, chạy cùng Service.
7. Refactor tạo/xem Order và báo cáo.
8. Tìm toàn project để chắc chắn SQL chỉ nằm trong JDBC repository.

Mỗi bước là một commit nhỏ. Khi output sai, phạm vi tìm lỗi chỉ nằm trong bước vừa thực hiện.

## IX. So Sánh Trước Và Sau

### Trước

```text
main → Scanner + validation + SQL + println
```

### Sau

```text
main → lắp dependency
View → nhập/xuất
Controller → điều phối
Service → nghiệp vụ
Repository → hợp đồng dữ liệu
JdbcRepository → SQL
```

Không đánh giá refactor bằng số package nhiều hơn. Đánh giá bằng khả năng thay View/repository, vị trí quy tắc rõ ràng và test case cũ vẫn đúng.

## X. Thực Hành Có Hướng Dẫn

- Nhóm A refactor luồng list Product.
- Nhóm B refactor find Product.
- Nhóm C refactor create Order bằng OrderItemInput.
- Các nhóm review chéo: khoanh mọi dòng SQL ngoài repository và mọi lời gọi repository trực tiếp từ View.
- Cuối buổi, đổi một ProductService sang MemoryProductRepository ngay tại composition root và demo không sửa service.

## Lỗi thường gặp

| Lỗi | Dấu hiệu | Cách sửa |
|---|---|---|
| Chỉ chia package, code vẫn gọi chéo | View import JdbcRepository | Kiểm tra dependency theo sơ đồ |
| Controller chứa nghiệp vụ | Tính tổng/stock trong controller | Chuyển sang Service/Model |
| Service tự tạo `new JdbcProductRepository(...)` | Không thay repository được | Inject interface qua constructor |
| View trả OrderItem đầy đủ | View phải tra Product/chốt giá | Trả OrderItemInput |
| Refactor làm đổi dữ liệu mẫu | Khó biết lỗi do đâu | Giữ bộ nghiệm thu trước/sau |

## Tổng kết buổi 10

MVC phân luồng tương tác; SRP đặt trách nhiệm theo lý do thay đổi; DIP làm nghiệp vụ phụ thuộc hợp đồng. Refactor chỉ thành công khi hành vi cũ vẫn đúng và dependency thực sự đi qua các ranh giới đã định.

## Bài tập nhỏ trên lớp — Refactor danh bạ theo tầng

Từ một chương trình console đang giữ `List<Contact>`, tổ chức lại thành:

- `ContactView` đọc tên/số điện thoại và hiển thị kết quả.
- `ContactController` điều phối thao tác thêm và tìm.
- `ContactService` kiểm tra tên/số điện thoại không rỗng.
- `ContactRepository` là interface; `MemoryContactRepository` lưu bằng ArrayList.
- `Application` là nơi duy nhất khởi tạo và nối các dependency.

**Nghiệm thu:** View không import repository, Service không `new MemoryContactRepository`, và chức năng thêm/tìm vẫn cho kết quả như trước refactor. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 10](../BTVN/buoi-10-btvn.md), kèm README và bằng chứng chạy cả JDBC lẫn memory Product repository.
