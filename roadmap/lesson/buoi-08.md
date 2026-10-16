# Buổi 8: Repository, Service, Optional, enum và Clean Code

---

## Mục tiêu buổi học

- Tách truy cập database khỏi xử lý nghiệp vụ bằng repository/DAO.
- Chia ứng dụng console thành model, repository, service và view/controller đơn giản.
- Dùng Optional cho kết quả có thể không tồn tại và enum cho một tập trạng thái hữu hạn.
- Nhận biết DTO/record để truyền dữ liệu gọn và áp dụng một số nguyên tắc clean code.

> **Cầu nối:** JDBC cho phép gửi SQL, nhưng trộn câu lệnh lưu trữ với quy tắc nghiệp vụ làm code khó đọc và thay đổi. Repository và service giới thiệu ranh giới trách nhiệm; Optional, enum và DTO/record là các chủ đề riêng giúp diễn tả kết quả, tập giá trị và dữ liệu truyền đi rõ hơn.

## I. Vì Sao Cần Tách Tầng?

Nếu một method vừa nhận yêu cầu, vừa lưu dữ liệu, vừa áp dụng quy tắc và hiển thị kết quả, một thay đổi nhỏ có thể ảnh hưởng nhiều nơi. Repository chịu trách nhiệm lưu/đọc; Service áp dụng quy tắc; View/Controller nhận input và trình bày kết quả.

~~~text
console view -> BookService -> BookRepository -> MySQL
                    |
                 validation
~~~

Tách code không cần framework. Mục tiêu là mỗi class có trách nhiệm dễ gọi tên và method có phạm vi rõ.

Một cách nhận ra code đang trộn trách nhiệm: cùng một method vừa đọc input, vừa kiểm tra quy tắc, viết SQL và in kết quả. Khi đổi giao diện, phần SQL không nên phải sửa; khi đổi database, quy tắc tính toán cũng không nên đổi. Các tầng tạo ranh giới để thay đổi cục bộ, nhưng chương trình nhỏ vẫn cần cấu trúc vừa đủ.

~~~text
View/Controller --> BookService --> BookRepository (interface)
                                            ^
                                            |
                              JdbcBookRepository
                                            |
                                          MySQL
~~~

Service chỉ biết interface; class JDBC là implementation được lắp vào lúc khởi tạo. Dấu mũi tên phụ thuộc thể hiện nơi gọi cần biết hợp đồng, không cần biết chi tiết SQL.

## II. Repository, Service Và Optional

BookRepository chứa các thao tác đọc/ghi. Service gọi repository và áp dụng quy tắc nghiệp vụ.

Đoạn code dưới tập trung vào hợp đồng và cách Service nhận Repository; có thể đặt các interface/model vào file/package riêng theo cấu trúc ở buổi 3. Import và phần triển khai JDBC được lược bỏ để làm rõ luồng phụ thuộc.

~~~java
public interface BookRepository {
    boolean add(Book book);
    Optional<Book> findById(String id);
    List<Book> findAll();
    boolean update(Book book);
    boolean deleteById(String id);
}

public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public Optional<Book> findBookById(String id) {
        return repository.findById(id);
    }
}
~~~

Trong ví dụ thực tế, `BookService.findBookById` có thể trả trực tiếp `repository.findById(id)` nếu chưa có quy tắc nghiệp vụ cần bổ sung. Dùng cùng tên/hợp đồng xuyên bài: Repository tập trung lưu trữ (`add`, `findById`, `findAll`, `update`, `deleteById`); Service cung cấp nghiệp vụ (`addBook`, `findBookById`, `findAllBooks`, `updateBook`, `deleteBook`). Một triển khai JDBC có thể được thay bằng triển khai bộ nhớ trong lúc minh họa, miễn cùng thực hiện hợp đồng.

Optional biểu diễn kết quả có thể vắng mặt. Dùng `isPresent()`, `orElse()` hoặc `orElseThrow()` thay vì trả null không rõ nghĩa. Phần exception ở đây chỉ dùng lại nội dung buổi 6.

Chọn `Optional<Book>` cho phép tìm có thể không có kết quả; dùng `List<Book>` cho không hoặc nhiều kết quả. `Optional` không có nghĩa “không có lỗi”: database vẫn có thể lỗi khi tìm kiếm. Với `orElse`, giá trị dự phòng được tính ngay; `orElseGet` tính nó khi cần; `orElseThrow` phù hợp khi thiếu giá trị khiến thao tác không thể tiếp tục. Không gọi `get()` mà chưa kiểm tra, vì điều đó lại đưa nguy cơ lỗi ẩn về caller. Phép tìm kiếm và lỗi khi truy xuất dữ liệu là hai kết quả khác nhau.

~~~java
import java.util.Optional;

public class OptionalDemo {
    public static void main(String[] args) {
        Optional<String> title = Optional.empty();
        System.out.println(title.orElse("Không tìm thấy")); // Không tìm thấy
    }
}
~~~

Trong code thực tế, trả `null` làm mất lợi ích mà `Optional` cung cấp; nên dùng `orElse` với giá trị dự phòng phù hợp, `orElseGet` nếu tạo mặc định tốn công, hoặc `orElseThrow` nếu thiếu dữ liệu khiến thao tác không thể tiếp tục. Caller có thể dùng `ifPresent` khi chỉ cần làm việc nếu kết quả tồn tại.

## III. enum Và DTO/record

Trạng thái là tập giá trị hữu hạn, nên dùng enum thay vì String tùy ý:

~~~java
public enum LoanStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}
~~~

Mỗi hằng enum là một object thuộc đúng kiểu `LoanStatus`; compiler không cho gán một chuỗi tùy ý như `"CONFIRMEDD"`. Dùng enum khi tập trạng thái hữu hạn và biết trước. Có thể so sánh bằng `==`, dùng trong `switch`, hoặc lấy tên để lưu/hiển thị; `valueOf()` chuyển chuỗi khớp chính xác thành enum và ném `IllegalArgumentException` nếu chuỗi không hợp lệ, nên không gọi trực tiếp với dữ liệu ngoài nếu chưa xử lý lỗi.

Khi truyền dữ liệu cần cho một màn hình hoặc một thao tác, DTO (Data Transfer Object) gom các giá trị liên quan thành một gói có cấu trúc. `record` là cú pháp ngắn để khai báo kiểu dữ liệu chủ yếu dùng giữ và truyền dữ liệu; các thành phần record có accessor và không có setter thông thường. Chọn DTO khi muốn bộc lộ đúng dữ liệu cần truyền thay vì chuyển nguyên object có nhiều trách nhiệm.

~~~java
public record BookSummary(String id, String name, String author) {}
~~~

Record tự sinh constructor, accessor theo tên thành phần (ví dụ `summary.name()`), `equals`, `hashCode` và `toString`. Thành phần record không có setter; tuy nhiên “final” chỉ bảo vệ việc gán lại tham chiếu thành phần, không tự làm object mutable bên trong trở nên bất biến. Không chuyển toàn bộ model sang DTO/record trong buổi này; chỉ nhận diện mục đích và dùng một ví dụ nhỏ.

## IV. Clean Code Trong Thiết Kế

- Dùng tên nói rõ nhiệm vụ như `findById`, `calculateTotal`.
- Giữ method ngắn, một trách nhiệm chính; tránh lặp câu SQL và validate ở nhiều nơi.
- Truyền dependency qua constructor để service dễ thay thế repository.

**Mở rộng — checked và unchecked exception:** Checked exception là loại compiler yêu cầu xử lý hoặc khai báo (ví dụ `IOException`, `SQLException`); unchecked exception kế thừa `RuntimeException` và không bắt buộc khai báo (ví dụ `IllegalArgumentException`). Đây là phần mở rộng để phân loại exception đã gặp, không yêu cầu đổi toàn bộ chữ ký method.

**Bài tập tự chọn:** Phân loại một số tình huống đã gặp (file không đọc được, không tìm thấy Book, truyền số lượng âm) thành lỗi ngoại cảnh hay vi phạm đầu vào/nghiệp vụ; giải thích loại exception phù hợp. Bài phân loại này không làm thay đổi chức năng cốt lõi.

## V. Ví Dụ Luồng Tra Cứu

Khi người dùng yêu cầu tìm sách theo mã, View chuyển mã cho Service; Service gọi Repository; Repository trả `Optional<Book>`. View quyết định cách trình bày kết quả. Nếu không tìm thấy, đó có thể là kết quả bình thường; nếu database không truy cập được, đây là lỗi khác cần báo riêng.

~~~text
View -> BookService.findBookById("B01")
     -> BookRepository.findById("B01")
     <- Optional<Book>
View hiển thị sách hoặc thông báo không tìm thấy
~~~

Đây là ví dụ về ranh giới trách nhiệm, không phải hướng dẫn triển khai một ứng dụng cụ thể. Thực hành: phân loại một thao tác tra cứu thành phần nhận yêu cầu, phần áp dụng quy tắc và phần lưu trữ; nêu điều gì nên thay đổi nếu database được thay bằng bộ nhớ.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| View/controller vẫn gọi JDBC trực tiếp | Chưa phân trách nhiệm | Gọi service từ view/controller |
| Optional.get() báo lỗi | Không kiểm tra giá trị có mặt | Dùng orElse/orElseThrow hoặc kiểm tra trước |
| Status sai chính tả | Dùng String tự do | Dùng hằng enum |
| Service chứa SQL | Tầng nghiệp vụ và lưu trữ bị trộn | Chuyển SQL về repository |

## Tổng Kết Buổi 8

- Repository đặt ranh giới truy cập dữ liệu; Service giữ quy tắc nghiệp vụ; View/Controller nhận và trình bày yêu cầu.
- Optional biểu diễn kết quả có thể vắng mặt; enum giới hạn tập giá trị; DTO/record gom dữ liệu truyền đi.
- Phân tầng giảm phụ thuộc chéo, nhưng với chương trình nhỏ cần giữ cấu trúc vừa đủ và tránh tạo tầng không có trách nhiệm rõ.

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
