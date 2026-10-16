# Buổi 10: MVC, SOLID Cơ Bản và Refactor

---

## Mục tiêu buổi học

- Phân biệt Model, View, Controller với Service và Repository.
- Áp dụng Single Responsibility và Dependency Inversion ở mức dễ hiểu.
- Dùng interface repository để service không phụ thuộc trực tiếp JDBC.
- Refactor một ví dụ mà vẫn giữ nguyên hành vi quan sát được.

> **Cầu nối:** Khi chương trình có nhiều phần cùng thay đổi, trách nhiệm chồng lấn khiến sửa một nơi dễ gây lỗi nơi khác. Nguyên tắc thiết kế và phân tách tầng giúp giới hạn ảnh hưởng, làm code dễ hiểu và dễ thay thế hơn.

## I. MVC Là Cách Sắp Xếp Trách Nhiệm

- Model biểu diễn dữ liệu/domain: Book, Reader, Loan.
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

~~~text
Request: View -> Controller -> Service -> Repository -> Database
Result:  View <- Controller <- Service <- Repository <- Database
~~~

## II. SOLID: Hai Ý Chính

- **Single Responsibility:** class nên có một nhóm trách nhiệm liên quan. LoanService không nên in giao diện hay dựng SQL.
- **Dependency Inversion:** service phụ thuộc abstraction (interface) để có thể thay implementation.

~~~java
public interface LoanRepository {
    long insertLoan(String readerId, LoanStatus status, LocalDateTime borrowedAt);
    Optional<Loan> findById(long id);
}

public class LoanService {
    private final LoanRepository repository;

    public LoanService(LoanRepository repository) {
        this.repository = repository;
    }
}
~~~

Đây là trích đoạn hợp đồng, không phải toàn bộ class chạy độc lập; `LoanStatus`, `LocalDateTime`, `Loan` và `Optional` đến từ các phần model/API đã học trước.

Constructor nhận dependency giúp test thủ công hoặc thay repository khác mà không đổi logic service.

So sánh hai cách gắn repository: nếu Service tự gọi `new JdbcLoanRepository(...)`, nó biết trực tiếp về JDBC và khó thay khi cần lưu trong bộ nhớ; nếu constructor nhận `LoanRepository`, phần khởi tạo chọn triển khai nào còn quy tắc nghiệp vụ chỉ làm việc với hợp đồng. Đây là **Dependency Inversion (DIP)** ở mức thực hành: phần nghiệp vụ không phụ thuộc chi tiết lưu trữ.

~~~java
LoanRepository repository = new JdbcLoanRepository(connection);
LoanService service = new LoanService(repository);
~~~

Hai dòng khởi tạo đặt ở `LibraryApplication` hoặc composition root (điểm tập trung lắp ghép ứng dụng), không đặt trong `LoanService`. Có thể truyền một `MemoryLoanRepository` thay cho demo/test; service không đổi nếu hai class cùng thực hiện interface.

SRP không có nghĩa “một class chỉ có một method”; nghĩa là nhóm hành vi có một lý do chính để thay đổi. Dependency Inversion ở đây: Service phụ thuộc hợp đồng (interface), còn JDBC là một triển khai. Truyền implementation qua constructor là cách nối cụ thể lúc khởi tạo. Buổi này tập trung SRP và DIP ở mức nền tảng.

## III. Singleton Và Factory Đơn Giản

Singleton đảm bảo một instance dùng chung khi thực sự cần, ví dụ đối tượng cấu hình đọc một lần. Factory gom cách tạo object khi lựa chọn class cụ thể. Giới thiệu cách dùng tối thiểu; không áp dụng pattern chỉ để tăng số class.

~~~java
interface Shape {}
class Circle implements Shape {}
class Rectangle implements Shape {}

public class ShapeFactory {
    public static Shape create(String type) {
        if ("CIRCLE".equals(type)) return new Circle();
        if ("RECTANGLE".equals(type)) return new Rectangle();
        throw new IllegalArgumentException("Loại hình không hỗ trợ");
    }
}
~~~

Factory hữu ích khi việc chọn class cụ thể dựa trên input hoặc cấu hình; nó gom quyết định tạo object vào một nơi. Nếu chỉ tạo một class và không có lựa chọn/phức tạp, gọi constructor trực tiếp thường đơn giản hơn.

Singleton giới hạn một class còn một instance được chia sẻ. Nó chỉ đáng dùng khi “một cấu hình dùng chung” là quy tắc thật của ứng dụng; không nên biến mọi service hoặc repository thành Singleton vì sẽ khó thay phụ thuộc và kiểm thử. Kết nối JDBC cũng không nên giữ vĩnh viễn như một Singleton trong bài nhập môn; connection được mở/đóng theo thao tác hoặc được quản lý theo vòng đời phù hợp.

~~~java
public final class DisplaySettings {
    private static final DisplaySettings INSTANCE = new DisplaySettings();
    private final String language = "vi";

    private DisplaySettings() {}
    public static DisplaySettings getInstance() { return INSTANCE; }
    public String getLanguage() { return language; }
}
~~~

Đây là dạng Singleton đơn giản để nhận diện, không phải khuôn mẫu bắt buộc cho mọi thiết lập. Giá trị cố định trong ví dụ chỉ để minh họa; cấu hình có thể cần đọc từ nguồn bên ngoài.

Static ở cấp class đã là phần nâng cao buổi 3; nếu lớp chưa nắm, nhắc lại ngắn qua method factory static, không mở rộng lý thuyết.

## IV. Refactor Giữ Nguyên Hành Vi

1. Mô tả input, output và các bước của chương trình hiện tại.
2. Xác định method nào đang có nhiều lý do thay đổi.
3. Tách phần trình bày, điều phối, quy tắc và lưu trữ thành các trách nhiệm riêng.
4. Thay dependency cụ thể bằng interface khi caller chỉ cần một hợp đồng.
5. So sánh hành vi quan sát được trước và sau khi refactor.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| View viết SQL | Chưa tách tầng | Chuyển query về repository |
| Service tạo JDBC trực tiếp | Phụ thuộc implementation | Nhận interface qua constructor |
| Class quá nhiều method không liên quan | Trách nhiệm chưa rõ | Tách theo lý do thay đổi |
| Dùng Singleton ở mọi nơi | Lạm dụng pattern | Chỉ dùng khi cần một instance dùng chung |

## Tổng Kết Buổi 10

- Model, View, Controller, Service và Repository là các trách nhiệm khác nhau; MVC không đồng nghĩa toàn bộ kiến trúc ứng dụng.
- SRP giúp nhóm các hành vi có cùng lý do thay đổi; DIP để phía nghiệp vụ phụ thuộc abstraction.
- Singleton và Factory là công cụ cho tình huống cụ thể, không phải khuôn mẫu cần áp dụng cho mọi class.

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
