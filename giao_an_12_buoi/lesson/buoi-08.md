# Buổi 8: Repository, Service, Optional, enum và Clean Code

---

## Mục tiêu buổi học

- Tách truy cập database khỏi xử lý nghiệp vụ bằng repository/DAO.
- Chia project console thành model, repository, service và view/controller đơn giản.
- Dùng Optional cho kết quả có thể không tồn tại và enum cho trạng thái đơn.
- Nhận biết DTO/record để truyền dữ liệu gọn và áp dụng một số nguyên tắc clean code.

> **Nối tiếp:** Buổi 7 đã có Product CRUD trong JDBC nhưng SQL đang gắn với luồng console. Buổi này tổ chức lại code, thêm CustomerRepository và đưa trạng thái đơn sang enum. Quan hệ nhiều bảng học ở buổi 9.

## I. Vì Sao Cần Tách Tầng?

Nếu menu vừa đọc input, vừa viết SQL, vừa tính nghiệp vụ, một thay đổi nhỏ có thể ảnh hưởng nhiều nơi. Repository chịu trách nhiệm đọc/ghi; Service điều phối nghiệp vụ; View/Controller nhận input và hiển thị.

~~~text
console view -> ProductService -> ProductRepository -> MySQL
                    |
                 validation
~~~

Tách code không cần framework. Mục tiêu là mỗi class có trách nhiệm dễ gọi tên và method có phạm vi rõ.

Một cách nhận ra code đang trộn trách nhiệm: cùng một method vừa đọc input, vừa kiểm tra quy tắc, viết SQL và in kết quả. Khi đổi giao diện, phần SQL không nên phải sửa; khi đổi database, quy tắc tính toán cũng không nên đổi. Các tầng tạo ranh giới để thay đổi cục bộ, nhưng chương trình nhỏ vẫn cần cấu trúc vừa đủ.

> **Hình minh họa cần bổ sung:** Sơ đồ luồng gọi và chiều phụ thuộc giữa View, Service, Repository, Database.

## II. Repository, Service Và Optional

ProductRepository chứa các thao tác đọc/ghi. Service gọi repository và áp dụng quy tắc nghiệp vụ.

~~~java
public interface ProductRepository {
    Optional<Product> findById(String id);
    List<Product> findAll();
    void save(Product product);
}

public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product requireProduct(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
    }
}
~~~

Optional biểu diễn kết quả có thể vắng mặt. Dùng `isPresent()`, `orElse()` hoặc `orElseThrow()` thay vì trả null không rõ nghĩa. Phần exception ở đây chỉ dùng lại nội dung buổi 6.

Repository trả dữ liệu hoặc kết quả vắng mặt; Service diễn đạt chính sách nghiệp vụ. `findById` không tìm thấy là tình huống có thể dự kiến nên Optional làm điều đó rõ ngay ở kiểu trả về. Không nên dùng Optional làm field của mọi object hay thay thế mọi danh sách; trọng tâm là kết quả đơn lẻ có thể không tồn tại. Không gọi `get()` khi chưa biết giá trị có mặt.

## III. enum, Customer Và DTO/record

Trạng thái là tập giá trị hữu hạn, nên dùng enum thay vì String tùy ý:

~~~java
public enum OrderStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}
~~~

Thêm bảng customers với id VARCHAR(20) làm khóa chính, cùng name và phone, rồi tạo CustomerRepository theo cấu trúc của ProductRepository. Bảng orders sẽ tham chiếu customers bằng khóa ngoại ở buổi 9. Khi truyền dữ liệu cần cho một màn hình hoặc một thao tác, DTO gom các field liên quan; record được giới thiệu như cú pháp gọn cho dữ liệu không cần setter.

~~~java
public record ProductSummary(String id, String name, double price) {}
~~~

Không chuyển toàn bộ model sang DTO/record trong buổi này; chỉ nhận diện mục đích và dùng một ví dụ nhỏ.

## IV. Clean Code Trong Project

- Dùng tên nói rõ nhiệm vụ như `findById`, `calculateTotal`.
- Giữ method ngắn, một trách nhiệm chính; tránh lặp câu SQL và validate ở nhiều nơi.
- Truyền dependency qua constructor để service dễ thay thế repository.

**Mở rộng — checked và unchecked exception:** Checked exception là loại compiler yêu cầu xử lý hoặc khai báo (ví dụ `IOException`, `SQLException`); unchecked exception kế thừa `RuntimeException` và không bắt buộc khai báo (ví dụ `IllegalArgumentException`). Đây là phần mở rộng để phân loại exception đã gặp, không yêu cầu đổi toàn bộ chữ ký method.

**Bài tập tự chọn:** Phân loại một số tình huống đã gặp (file không đọc được, không tìm thấy Product, truyền số lượng âm) thành lỗi ngoại cảnh hay vi phạm đầu vào/nghiệp vụ; giải thích loại exception phù hợp. Bài phân loại này không làm thay đổi chức năng cốt lõi.

## V. Refactor Có Thứ Tự

1. Di chuyển SQL Product vào ProductRepository.
2. Tạo ProductService gọi repository.
3. Giữ menu làm view/controller console; menu không tự viết SQL.
4. Tạo CustomerRepository theo cùng ranh giới.
5. Thay status String bằng OrderStatus.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Menu vẫn gọi JDBC trực tiếp | Chưa chuyển trách nhiệm | Gọi service từ view |
| Optional.get() báo lỗi | Không kiểm tra giá trị có mặt | Dùng orElse/orElseThrow hoặc kiểm tra trước |
| Status sai chính tả | Dùng String tự do | Dùng hằng enum |
| Service chứa SQL | Tầng nghiệp vụ và lưu trữ bị trộn | Chuyển SQL về repository |

## Mốc Project Sau Buổi 8

- Có ProductRepository, CustomerRepository, ProductService và OrderStatus.
- Chưa thiết kế quan hệ orders/order_items; buổi 9 bổ sung khóa ngoại, JOIN và lưu đơn nhiều bảng.

### Câu Hỏi Ôn Tập

1. Repository và service khác trách nhiệm ra sao?
2. Optional diễn đạt trường hợp nào?
3. enum giúp tránh lỗi gì so với status String?
4. DTO/record dùng để làm gì?
5. Vì sao view không nên chứa SQL?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 08](../BTVN/buoi-08-btvn.md).
## Tài Liệu Tham Khảo

- [Java SE API — Optional](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html)
- [Java SE API — Enum](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Enum.html)
- [Java SE API — Record](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Record.html)
