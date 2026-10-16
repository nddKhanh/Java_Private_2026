# Buổi 5: Abstract Class, Interface, equals() và hashCode()

---

## Mục tiêu buổi học

- Phân biệt abstract class và interface; biết dùng implements cho hợp đồng hành vi.
- Mô tả các khả năng chung bằng interface thay vì phụ thuộc class cụ thể.
- So sánh object theo nội dung bằng equals() và tuân thủ quy tắc hashCode().
- Thực hành bằng hợp đồng gửi thông báo, xuất báo cáo và equality của Book.

> **Cầu nối:** Kế thừa và đa hình ở buổi trước mô tả các biến thể trong cùng một họ kiểu. Khi những class không cùng một họ vẫn cần cung cấp cùng một khả năng, interface biểu diễn hợp đồng chung mà không buộc chúng dùng chung lớp cha. Buổi này cũng phân biệt equality theo nội dung với cùng tham chiếu object.

## I. Từ if/else Đến Hợp Đồng

Nhiều loại đối tượng có thể cùng thực hiện một khả năng nhưng cách làm khác nhau. Ví dụ nhiều loại thông báo đều có thể `send()`, nhưng email và tin nhắn gửi theo cách khác nhau. Nếu code gọi phụ thuộc từng class cụ thể, thêm một cách làm mới thường kéo theo sửa nhiều nhánh. Interface nêu hợp đồng mà bên gọi cần; class triển khai chịu trách nhiệm về cách thực hiện.

~~~java
public interface MessageSender {
    String send(String message);
}

class EmailSender implements MessageSender {
    @Override
    public String send(String message) {
        return "Gửi email: " + message;
    }
}

class SmsSender implements MessageSender {
    @Override
    public String send(String message) {
        return "Gửi SMS: " + message;
    }
}
~~~

Code gọi qua interface có thể yêu cầu `send` mà không cần biết thông báo được gửi bằng email hay SMS. Interface mô tả khả năng mà phía gọi cần; class triển khai chịu trách nhiệm cách thực hiện.

~~~text
                 MessageSender
                  send(message)
                 /           \
       EmailSender           SmsSender
                 \           /
                  caller gọi send()
~~~

## II. Abstract Class Và Interface

- Abstract class là class chưa hoàn thiện để tạo object trực tiếp; nó có thể chứa field, constructor, method đã cài đặt và method abstract buộc lớp con cung cấp phần còn thiếu.
- Interface định nghĩa hợp đồng hành vi mà class cam kết thực hiện. Class dùng `implements`; một class có thể implement nhiều interface, nhưng chỉ extends một class trực tiếp.
- Chọn abstract class khi các lớp con thật sự cùng một họ, cần chia sẻ trạng thái hoặc code chung; chọn interface khi cần mô tả khả năng/hợp đồng có thể áp dụng cho các class không cùng một lớp cha.
- Không chọn theo quy tắc máy móc “is-a”/“can-do”; hãy xem quan hệ trong miền bài toán, khả năng thay thế và phần code/state thật sự cần chia sẻ. Một interface không bắt buộc chỉ có method abstract trong mọi phiên bản Java, nhưng buổi học tập trung vào hợp đồng method cơ bản.

| Câu hỏi khi thiết kế | Abstract class thường phù hợp | Interface thường phù hợp |
|---|---|---|
| Các kiểu có cùng nền tảng và trạng thái chung không? | Có, ví dụ `Shape` giữ màu và cho biết diện tích | Không nhất thiết; chỉ cần cùng một khả năng |
| Một class cần kết hợp nhiều vai trò không? | Java chỉ cho extends một class | Có thể implements nhiều interface |
| Bên gọi cần biết điều gì? | Có thể dựa vào API và trạng thái chung của lớp cha | Chỉ dựa vào các method trong hợp đồng |

Ví dụ: `Circle extends Shape` nếu Circle là một hình và có thể dùng ở nơi nhận Shape. `EmailSender implements MessageSender` vì class này cung cấp khả năng gửi thông báo; nó không cần kế thừa lớp hình học.

~~~java
public abstract class ReportGenerator {
    public abstract String generate();

    public void printReportTitle() {
        System.out.println("Báo cáo tổng hợp");
    }
}
~~~

Không tạo trực tiếp `ReportGenerator`; class cụ thể kế thừa nó và cài đặt `generate()`. Abstract class phù hợp khi các lớp con thật sự cùng một loại và có phần code chung.

~~~java
public class SummaryReport extends ReportGenerator {
    private final int recordCount;

    public SummaryReport(int recordCount) {
        this.recordCount = recordCount;
    }

    @Override
    public String generate() {
        return "Số bản ghi: " + recordCount;
    }
}
~~~

Trong ví dụ này, class trừu tượng giữ một hành vi chung là in tiêu đề; lớp cụ thể cung cấp dữ liệu báo cáo. Dùng abstract class khi các lớp con thuộc cùng một họ và có phần state/code thực sự dùng chung; nếu chỉ muốn nhiều class có cùng khả năng, interface ít ràng buộc hơn.

~~~java
public interface ExportFormat {
    String export(String content);
}

class TextExport implements ExportFormat {
    @Override
    public String export(String content) {
        return content;
    }
}
~~~

Một class có thể implement các interface khác nhau nếu nó đáp ứng từng hợp đồng. Bên gọi chỉ cần biết hợp đồng; nó không cần phụ thuộc class triển khai.

~~~java
MessageSender sender = new EmailSender();
String result = sender.send("Lịch học bắt đầu lúc 8 giờ");
System.out.println(result);
~~~

Biến có kiểu interface cho phép thay `new EmailSender()` bằng `new SmsSender()` mà lời gọi `sender.send(...)` không đổi. Dùng interface ở ranh giới nơi code cần một khả năng nhưng không nên gắn với một cách thực hiện cụ thể.

## III. equals() Và hashCode()

Với object, `==` kiểm tra hai biến có tham chiếu cùng một object không. `equals()` là method để định nghĩa khi nào hai object được xem là bằng nhau về giá trị/định danh; mặc định kế thừa từ `Object` cũng chỉ so sánh cùng instance, nên class cần override nếu muốn equality theo nội dung.

~~~java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Book)) return false;
    Book other = (Book) obj;
    return id.equals(other.id);
}

@Override
public int hashCode() {
    return id.hashCode();
}
~~~

Đoạn minh họa giả sử `id` được khởi tạo và không bao giờ null, ví dụ field `private final String id` được kiểm tra trong constructor. Điều này quan trọng vì nếu id null, `id.equals(...)` gây NullPointerException. Với Java mới có thể dùng `Objects.equals(id, other.id)` để xử lý null an toàn; với định danh, thường nên bắt buộc có giá trị ngay từ lúc tạo.

Nếu override equals(), cần override hashCode() theo cùng dữ liệu định danh. Trong buổi này ta áp dụng tìm/xóa trong ArrayList bằng mã; chưa học sâu Set/Map.

Hợp đồng quan trọng: equality phải phản xạ (object bằng chính nó), đối xứng, bắc cầu và ổn định khi dữ liệu dùng để so sánh không đổi; `equals(null)` phải false. Nếu `a.equals(b)` là true thì `a.hashCode()` phải bằng `b.hashCode()`. Chiều ngược lại không bắt buộc: hai object khác nhau vẫn có thể trùng hash code. Vì thế hai method phải dựa trên cùng các trường định danh. Nếu trường equality bị đổi khi object đang nằm trong cấu trúc dựa trên hash, việc tìm object có thể không còn như mong muốn; đây là lý do nên chọn khóa định danh ổn định.

Trong phạm vi buổi học, kiểm tra equality theo `id` là một lựa chọn mô hình. Nếu `id` có thể null, phải xử lý null an toàn thay vì gọi `id.equals(...)` trực tiếp.

## IV. Áp Dụng Và Củng Cố

1. Khai báo MessageSender và ExportFormat.
2. Viết EmailSender, SmsSender và TextExport.
3. Viết một method nhận MessageSender làm tham số và gọi send.
4. Override equals/hashCode của Book theo id rồi tìm sách tương ứng trong danh sách.
5. Dành 1 giờ cuối buổi cho kiểm tra OOP.

## Phần Mở Rộng Và Nâng Cao — Tìm Hiểu Thêm

- **Lambda cơ bản (mở rộng):** Lambda viết gọn cách cài đặt một hành vi có một method trừu tượng; `forEach` có thể nhận hành động cho từng phần tử, `removeIf` nhận điều kiện lọc. Ví dụ: `scores.removeIf(score -> score < 0);`. Chỉ giới thiệu cú pháp và trường hợp dùng, không đi vào Stream.
- **Generics (nâng cao):** `Box<T>` hoặc method `<T> T first(List<T> values)` cho phép tái sử dụng logic với kiểu dữ liệu khác nhau mà vẫn kiểm tra kiểu khi biên dịch. `ArrayList<T>` là ví dụ generic đã dùng; tự định nghĩa generic không phải phần bắt buộc.
- **Optional (giới thiệu sớm):** biểu diễn một kết quả có thể vắng mặt. Chỉ nhận diện `Optional.empty()`/`Optional.of(value)`; nội dung sử dụng trong repository được dạy chính thức ở buổi 8.

**Bài tập tự chọn:** Viết lambda lọc danh sách số, rồi tạo `Box<T>` có thao tác lấy/đặt giá trị. Thử `Optional` cho một phép tìm sách nhỏ. Đây là bài luyện khái niệm độc lập, không phải điều kiện để học phần bắt buộc.

### Bài Luyện Tập Khái Niệm

1. Đề xuất một interface trung tính như `Exportable` và hai class không cùng lớp cha nhưng cùng cài đặt hợp đồng đó.
2. So sánh abstract class và interface theo state/code dùng chung, khả năng kết hợp và ràng buộc lớp.
3. Lập các cặp object có cùng nội dung nhưng là hai instance riêng; xác định kết quả của `==` và `equals()`.
4. Kiểm tra hợp đồng `equals()`/`hashCode()` bằng các object có cùng và khác mã định danh.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| Class chưa implement hết method | Thiếu method của interface | Implement đủ hoặc khai báo class abstract |
| equals() luôn false | So sánh tham chiếu hoặc sai field định danh | So sánh mã sách |
| equals đúng nhưng hashCode khác | Override hai method không cùng dữ liệu | Dùng cùng id |
| Caller vẫn kiểm tra loại sender | Caller phụ thuộc class cụ thể | Nhận MessageSender interface |

## Tổng Kết Buổi 5

- Abstract class gom trạng thái/hành vi chung; interface mô tả hợp đồng; class có thể cài nhiều interface.
- `equals()` định nghĩa equality theo miền dữ liệu; `hashCode()` phải nhất quán với equality.
- MessageSender, ExportFormat và Book là các ví dụ minh họa hợp đồng và equality theo nội dung/định danh.
- Buổi 6 sẽ xét cách kiểm soát lỗi khi thao tác thất bại và lưu dữ liệu bền vững.

### Câu Hỏi Ôn Tập

1. Khi nào dùng abstract class, khi nào dùng interface?
2. Một class có thể implement nhiều interface không?
3. == khác equals() với object thế nào?
4. Quy tắc giữa equals() và hashCode() là gì?
5. Interface giúp thay đổi cách gửi thông báo mà không sửa nơi gọi ra sao?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 05](../BTVN/buoi-05-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Abstract Methods and Classes](https://docs.oracle.com/javase/tutorial/java/IandI/abstract.html)
- [Oracle — Defining an Interface](https://docs.oracle.com/javase/tutorial/java/IandI/createinterface.html)
- [Java SE API — Object.equals and hashCode](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html)
