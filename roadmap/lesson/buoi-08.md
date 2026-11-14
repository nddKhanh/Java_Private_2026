# Buổi 8: Tách Tầng, Repository, Service, MVC Và Refactor

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Phân biệt trách nhiệm của Model, View, Controller, Service và Repository.
- Khai báo repository interface và truyền implementation vào Service qua constructor.
- Xử lý kết quả tìm kiếm bằng `Optional`; dùng `enum` cho tập trạng thái hợp lệ.
- Giải thích SRP và DIP bằng một thay đổi cụ thể.
- Lắp dependency tại composition root và refactor mà giữ nguyên hành vi.

> **Kết quả cuối buổi:** ví dụ Contact chạy theo luồng View → Controller → Service → Repository và có thể đổi giữa bộ nhớ với JDBC mà không sửa Service.

> **Môi trường:** JDK 17; đã học interface, exception và JDBC một bảng ở buổi 7.
> **Phạm vi:** gộp lý thuyết buổi 8 và buổi 10 cũ. Ví dụ dưới đây dùng danh bạ nhỏ để giải thích khái niệm; áp dụng vào HitShop nằm trong BTVN. Đơn hàng nhiều bảng học ở buổi 9.

## I. Vì Sao Cần Tách Tầng?

Một chương trình có thể đọc bàn phím, kiểm tra dữ liệu, chạy SQL và in kết quả trong cùng `main`. Khi đổi từ console sang cửa sổ, ta phải sửa cả phần truy cập dữ liệu dù database không đổi.

Tách tầng là chia chương trình thành những phần có trách nhiệm rõ, giao tiếp qua method. Một luồng tìm liên hệ có dạng:

```text
Người dùng → View → Controller → Service → Repository interface
                                              ↑ implementation
                                        JDBC hoặc bộ nhớ
```

Mũi tên từ Service đến Repository là lời gọi qua interface; object thực tế thực hiện lời gọi có thể là JDBC hoặc bộ nhớ. Kết quả đi ngược lại qua các tầng để View hiển thị.

| Thành phần | Trách nhiệm | Ví dụ |
|---|---|---|
| Model | Dữ liệu và hành vi của đối tượng | Contact giữ mã, tên, số điện thoại |
| View | Nhập và hiển thị dữ liệu | Đọc mã, in liên hệ hoặc thông báo |
| Controller | Điều phối tương tác | Lấy mã từ View, gọi Service, chuyển kết quả về View |
| Service | Kiểm tra và xử lý nghiệp vụ | Từ chối mã trống, tìm liên hệ |
| Repository | Hợp đồng truy cập dữ liệu | Tìm theo mã, thêm, liệt kê |
| JDBC implementation | SQL và ánh xạ row thành object | PreparedStatement, ResultSet |

MVC là Model–View–Controller. Service và Repository là các phần hỗ trợ nghiệp vụ/lưu trữ trong kiến trúc này, không phải hai chữ bổ sung của MVC. Model không đồng nghĩa với database.

### Kiểm tra nhanh

Đặt bốn việc “đọc mã”, “kiểm tra mã rỗng”, “chạy SELECT” và “in thông báo” vào đúng View, Service, Repository hoặc Controller; giải thích vì sao mỗi việc nằm ở đó.

## II. Repository: Interface Và Implementation

Interface mô tả **cần làm gì**; implementation mô tả **làm bằng cách nào**. Sau đây là ví dụ đầy đủ cho một chức năng tìm kiếm; các class đặt trong file cùng tên và bổ sung import như được chỉ dẫn.

```java
public record Contact(String id, String name, String phone) {}
```

`record` là cách khai báo ngắn một kiểu mang dữ liệu trong JDK 17. Java tạo constructor, accessor `id()`, `name()`, `phone()` và equality theo các thành phần. Không khai báo setter cho các thành phần record. Entity có hành vi thay đổi trạng thái vẫn có thể dùng class thông thường.

```java
import java.util.Optional;

public interface ContactRepository {
    Optional<Contact> findById(String id);
}
```

`Optional<Contact>` biểu diễn kết quả có hoặc không có Contact. Interface không chứa SQL, không đọc bàn phím và không quyết định câu thông báo trên màn hình.

Một implementation lưu trong bộ nhớ, chưa cần database:

```java
import java.util.List;
import java.util.Optional;

public class MemoryContactRepository implements ContactRepository {
    private final List<Contact> contacts;

    public MemoryContactRepository(List<Contact> contacts) {
        this.contacts = List.copyOf(contacts);
    }

    @Override
    public Optional<Contact> findById(String id) {
        for (Contact contact : contacts) {
            if (contact.id().equals(id)) {
                return Optional.of(contact);
            }
        }
        return Optional.empty();
    }
}
```

`implements` buộc class thực hiện method của interface. Với JDBC, một `JdbcContactRepository` cũng implements interface này, đặt tham số vào SQL và chuyển ResultSet thành Contact. `Optional.empty()` chỉ dành cho trường hợp không có dòng phù hợp; lỗi kết nối phải được báo như lỗi truy cập dữ liệu, không giả thành “không tìm thấy”.

## III. Service Và Constructor Injection

Service giữ quy tắc nghiệp vụ và gọi repository để lấy dữ liệu:

```java
import java.util.Optional;

public class ContactService {
    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public Optional<Contact> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mã liên hệ không được trống.");
        }
        return repository.findById(id.trim());
    }
}
```

Truyền dependency qua constructor gọi là constructor injection: Service nhận object cần dùng từ bên ngoài. Kiểu field là interface `ContactRepository`, nên Service không cần biết object đang lưu ở MySQL hay bộ nhớ. Không cần framework để làm việc này.

## IV. Optional: Xử Lý Nhánh Không Tìm Thấy

Import `java.util.Optional` khi dùng kiểu này. Ba thao tác cần biết:

```java
Optional<Contact> result = service.findById("C01");
if (result.isPresent()) {
    Contact contact = result.orElseThrow();
    System.out.println(contact.name());
} else {
    System.out.println("Không tìm thấy liên hệ.");
}
```

Đoạn trên minh họa cách tiêu thụ kết quả; `service` là ContactService đã được khởi tạo.

- `isPresent()` kiểm tra có giá trị hay không.
- `orElse(defaultValue)` trả giá trị có sẵn, hoặc giá trị thay thế khi rỗng.
- `orElseThrow()` lấy giá trị hoặc ném lỗi khi rỗng; chỉ dùng khi đã kiểm tra hoặc chủ ý coi thiếu dữ liệu là lỗi.

Không gọi `get()` vô điều kiện. Không cần dùng Optional cho mọi field hoặc tham số.

## V. View, Controller Và DTO

Ví dụ tiếp nối chức năng tìm Contact:

```java
import java.util.Scanner;

public class ContactView {
    private final Scanner scanner = new Scanner(System.in);

    public String readId() {
        System.out.print("Mã liên hệ: ");
        return scanner.nextLine();
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}
```

View không gọi Service hoặc Repository. Controller điều phối một lượt tìm kiếm:

```java
import java.util.Optional;

public class ContactController {
    private final ContactView view;
    private final ContactService service;

    public ContactController(ContactView view, ContactService service) {
        this.view = view;
        this.service = service;
    }

    public void findContact() {
        String id = view.readId();
        try {
            Optional<Contact> result = service.findById(id);
            if (result.isPresent()) {
                Contact contact = result.orElseThrow();
                view.showMessage(contact.name() + " / " + contact.phone());
            } else {
                view.showMessage("Không tìm thấy liên hệ.");
            }
        } catch (IllegalArgumentException e) {
            view.showMessage(e.getMessage());
        }
    }
}
```

Controller biết thứ tự tương tác; quy tắc mã không được trống vẫn nằm ở Service. Controller không viết SQL.

DTO (Data Transfer Object) là object mang dữ liệu qua ranh giới các tầng. Chẳng hạn `record ContactInput(String name, String phone) {}` mang dữ liệu nhập; View chưa cần tạo một entity hoàn chỉnh. Record là cú pháp Java, còn DTO là vai trò sử dụng; hai khái niệm không đồng nghĩa.

## VI. Composition Root: Nơi Lắp Các Object

Composition root là nơi chọn implementation và nối dependency, thường nằm ở `main`:

```java
import java.util.List;

public class Application {
    public static void main(String[] args) {
        ContactRepository repository = new MemoryContactRepository(List.of(
                new Contact("C01", "Linh", "0900000001")));
        ContactService service = new ContactService(repository);
        ContactView view = new ContactView();
        ContactController controller = new ContactController(view, service);
        controller.findContact();
    }
}
```

Lưu bảy kiểu trong ví dụ vào bảy file `.java`, không khai báo package, rồi chạy `Application.main()`. Nhập C01 phải thấy `Linh / 0900000001`; nhập C99 phải thấy thông báo không tìm thấy.

Khi có `JdbcContactRepository`, chỉ thay bước chọn repository và cấu hình kết nối tại composition root; giữ nguyên Service. `main` lắp object, không trở thành nơi chứa toàn bộ nghiệp vụ.

## VII. SOLID Cơ Bản: SRP Và DIP

SOLID là nhóm năm nguyên tắc thiết kế OOP. Buổi này chỉ thực hành hai nguyên tắc:

- **SRP — Single Responsibility Principle:** nhóm các trách nhiệm có cùng lý do thay đổi. Đổi cách hiển thị sửa View; đổi quy tắc nghiệp vụ sửa Service; đổi SQL sửa JDBC repository. SRP không yêu cầu mỗi class chỉ có một method.
- **DIP — Dependency Inversion Principle:** phần nghiệp vụ phụ thuộc hợp đồng thay vì class lưu trữ cụ thể. `ContactService` nhận `ContactRepository`, không tự `new JdbcContactRepository(...)`.

Constructor injection là cách cung cấp dependency; DIP là nguyên tắc định hướng phụ thuộc. Có constructor nhận thẳng class JDBC vẫn chưa đạt mục tiêu thay nguồn lưu trữ qua interface.

## VIII. enum Cho Tập Giá Trị Hữu Hạn

String trạng thái có thể bị gõ sai như `"CONFIRMD"`. Enum khai báo tập giá trị hợp lệ:

```java
public enum TaskStatus {
    TODO, DONE
}
```

Field có thể khai báo `private TaskStatus status = TaskStatus.TODO;`. Dùng `status == TaskStatus.DONE` để so sánh; `.name()` lấy tên để lưu, `TaskStatus.valueOf(text)` khôi phục từ tên. `valueOf` ném IllegalArgumentException nếu tên không hợp lệ.

Enum ngăn giá trị ngoài tập đã khai báo, nhưng quy tắc chuyển trạng thái vẫn phải do Model hoặc Service kiểm soát.

## IX. Refactor Và Clean Code

Refactor là đổi cấu trúc bên trong mà giữ nguyên hành vi cần bảo toàn. Với chức năng tìm Contact:

1. Ghi input/output trước sửa: C01, C99, mã rỗng.
2. Tách phần lưu trữ vào repository.
3. Chuyển validation vào Service, truyền repository qua constructor.
4. Tách View và Controller.
5. Lắp object tại `main`, chạy lại cùng input/output.

Đặt tên theo vai trò, viết method tập trung vào một việc và giảm lặp bằng method dùng chung. Không đánh giá clean code bằng số lượng package.

## Lỗi thường gặp

| Lỗi | Cách sửa |
|---|---|
| View gọi JDBC hoặc Repository | Chuyển lời gọi qua Controller và Service |
| Service có SELECT/INSERT | Chuyển SQL vào JDBC implementation |
| Service tự tạo repository cụ thể | Nhận repository interface qua constructor |
| Repository in thông báo giao diện | Trả kết quả hoặc báo lỗi để tầng trên xử lý |
| Bắt lỗi database rồi trả Optional.empty() | Phân biệt lỗi truy cập với không tìm thấy |

## X. Thực Hành Có Hướng Dẫn

1. Chạy `MemoryContactRepository` với C01 và C02, kiểm tra C01/C99.
2. Thêm validation mã rỗng trong Service rồi chạy lại ba ca C01, C99 và chuỗi rỗng.
3. Nối View, Controller và Application theo đúng hướng phụ thuộc.
4. Đổi sang một `JdbcContactRepository` có cùng interface; không sửa ContactService.
5. So sánh output trước và sau refactor, rồi chỉ ra nơi duy nhất chứa SQL.

Mỗi bước phải chạy được trước khi chuyển sang bước tiếp theo để lỗi cấu trúc không bị trộn với lỗi JDBC.

## XI. Đọc Thêm — Singleton Và Factory

- Factory gom việc chọn/tạo implementation vào một nơi; ví dụ chọn repository memory hoặc JDBC bằng cấu hình.
- Singleton giới hạn một instance theo thiết kế. Có thể dùng cho cấu hình bất biến, nhưng không mặc định dùng một JDBC Connection toàn cục cho mọi thao tác.
- Không bắt buộc triển khai pattern trong bài chính; ưu tiên hiểu interface và constructor injection.

Checked exception như IOException/SQLException phải được bắt hoặc khai báo `throws`; unchecked exception như IllegalArgumentException không bắt buộc khai báo. Repository có thể bọc SQLException trong lỗi truy cập dữ liệu unchecked và giữ cause; không nuốt lỗi rồi báo thành công. Đây là phần đọc thêm nối với buổi 6–7.

## Tổng kết buổi 8

MVC tổ chức tương tác; Service giữ nghiệp vụ; Repository che cách lưu trữ. Optional diễn đạt kết quả có thể vắng mặt, enum giới hạn trạng thái, còn SRP/DIP giúp đặt trách nhiệm và hướng phụ thuộc rõ ràng.

### Câu hỏi chốt buổi

1. Interface Repository giúp Service độc lập với JDBC như thế nào?
2. Vì sao lỗi kết nối database không được biến thành `Optional.empty()`?
3. Constructor injection khác việc Service tự tạo repository ở điểm nào?
4. Controller được phép điều phối những gì và không được chứa những gì?
5. Điều kiện nào chứng minh một lần refactor đã giữ nguyên hành vi?

## Bài tập nhỏ trên lớp — Tách Chức Năng Tìm Liên Hệ

Dùng bảy kiểu của ví dụ trên, hoàn thiện chức năng tìm kiếm và giải thích đường đi của một yêu cầu. Các record có constructor/accessor do Java tạo; các class nhận dependency qua constructor như ví dụ.

- `Contact`: `String id`, `String name`, `String phone`.
- `ContactRepository`: `Optional<Contact> findById(String id)`.
- `MemoryContactRepository`: `List<Contact> contacts`, constructor nhận danh sách; triển khai `public Optional<Contact> findById(String id)`.
- `ContactService`: `ContactRepository repository`; `public Optional<Contact> findById(String id)` từ chối mã trống rồi tìm theo mã đã trim.
- `ContactView`: `Scanner scanner`; `public String readId()` và `public void showMessage(String message)`.
- `ContactController`: `ContactView view`, `ContactService service`; `public void findContact()` điều phối một lượt tìm.
- `Application`: `public static void main(String[] args)` lắp dependency và gọi Controller.

**Nghiệm thu:** C01 trả đúng dữ liệu; C99 báo không tìm thấy; mã rỗng báo lỗi; View không gọi repository và Service không tự tạo repository. Khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 08](../BTVN/buoi-08-btvn.md), gồm tách tầng, Customer, enum, refactor và chạy cùng ProductService với JDBC/memory repository. Buổi 9 áp dụng kiến trúc này cho đơn hàng nhiều bảng.
