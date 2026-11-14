# Buổi 5: Interface, Abstract Class Và Equality

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Giải thích interface là gì và đọc được cấu trúc khai báo interface.
- Tạo class triển khai interface và sử dụng object qua kiểu interface.
- Giải thích abstract class là gì và đọc được cấu trúc của một abstract class.
- So sánh interface với abstract class để chọn công cụ phù hợp.
- Phân biệt nhanh `==`, `equals()` và `hashCode()` khi làm việc với object.
- Phân tích vì sao interface, abstract class và equality giúp một dự án dễ mở rộng hơn.

> **Kết quả cuối buổi:** học viên hiểu khái niệm trước, sau đó phân tích được cách các khái niệm này giải quyết vấn đề thay đổi và mở rộng trong một dự án Java.

> **Môi trường:** JDK 17; đã học class, kế thừa, `@Override` và đa hình ở buổi 3–4.

## I. Interface: Khái Niệm Và Cấu Trúc

Interface là một kiểu dùng để mô tả **hợp đồng hành vi**.
Nó cho biết object cần làm được gì, nhưng chưa quyết định object thực hiện hành vi đó bằng cách nào.

Ví dụ, nhiều loại thiết bị đều có thể bật:

```java
public interface Switchable {
    void turnOn();
}
```

Ý nghĩa từng phần:

- `public interface`: khai báo một interface có thể được dùng từ package khác.
- `Switchable`: tên hợp đồng.
- `void`: method không trả về giá trị.
- `turnOn`: tên hành vi.
- Dấu `;`: interface mới nêu chữ ký, chưa cung cấp phần thân cho method.

Method trên được hiểu là `public abstract` dù không viết hai từ khóa đó.
Không thể gọi `new Switchable()` vì interface không phải một class hoàn chỉnh.

Class dùng từ khóa `implements` để chấp nhận hợp đồng:

```java
public class Television implements Switchable {
    @Override
    public void turnOn() {
        System.out.println("Tivi đã bật");
    }
}
```

```java
public class Projector implements Switchable {
    @Override
    public void turnOn() {
        System.out.println("Máy chiếu đã bật");
    }
}
```

`@Override` giúp compiler kiểm tra class có triển khai đúng chữ ký trong interface hay không.

Object có thể được sử dụng qua kiểu interface:

```java
static void startDevice(Switchable device) {
    device.turnOn();
}

startDevice(new Television());
startDevice(new Projector());
```

Method `startDevice` không cần biết loại thiết bị cụ thể.
JVM gọi implementation phù hợp với object được truyền vào.
Đây là đa hình qua interface.

Một class có thể implements nhiều interface:

```java
public class SmartPrinter implements Printable, Scannable {
    // triển khai method của cả hai interface
}
```

Interface phù hợp khi nhiều class cần cùng một hợp đồng, kể cả khi chúng không thuộc cùng một cây kế thừa.

### Kiểm tra nhanh

Khi thêm `SmartLight implements Switchable`, method `startDevice` có cần sửa không?
Giải thích dựa trên kiểu tham số của method.

## II. Abstract Class: Khái Niệm Và Cấu Trúc

Abstract class là một class chưa hoàn chỉnh, được dùng làm lớp nền cho các class con.
Nó có thể chứa:

- Field và trạng thái dùng chung.
- Constructor để khởi tạo phần chung.
- Method thường có phần thân.
- Method abstract chưa có phần thân.

Ví dụ:

```java
public abstract class Employee {
    private final String name;

    protected Employee(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên không được trống");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract double calculateSalary();
}
```

`Employee` giữ trạng thái và code dùng chung.
Method `calculateSalary()` chưa có phần thân vì mỗi loại nhân viên tính lương khác nhau.

Class concrete kế thừa và hoàn thiện method abstract:

```java
public class FullTimeEmployee extends Employee {
    private final double monthlySalary;

    public FullTimeEmployee(String name, double monthlySalary) {
        super(name);
        this.monthlySalary = monthlySalary;
    }

    @Override
    public double calculateSalary() {
        return monthlySalary;
    }
}
```

Không thể gọi `new Employee(...)` vì Employee là abstract.
Constructor của abstract class vẫn tồn tại và được class con gọi qua `super(...)`.

Một class chỉ có thể `extends` một class trực tiếp.
Abstract class phù hợp khi các class con thực sự thuộc cùng một họ và cần dùng chung trạng thái hoặc implementation.

## III. So Sánh Interface Và Abstract Class

| Tiêu chí | Interface | Abstract class |
|---|---|---|
| Mục đích chính | Mô tả hợp đồng hoặc vai trò | Làm lớp nền cho một họ class |
| Khởi tạo trực tiếp | Không | Không |
| Trạng thái riêng của object | Không dùng để giữ trạng thái riêng | Có thể có field instance |
| Constructor | Không có constructor cho object | Có constructor để class con gọi |
| Method có phần thân | Có thể có `default`/`static`, nhưng chưa phải trọng tâm | Có thể có method thường |
| Method chưa có phần thân | Method abstract | Method có từ khóa `abstract` |
| Số lượng có thể dùng | Một class có thể implements nhiều interface | Một class chỉ extends một class trực tiếp |
| Khi nên dùng | Nhiều class cần cùng một hợp đồng | Các class cùng họ cần chia sẻ phần nền |

Không nên chọn chỉ bằng cách học thuộc “interface là khả năng, abstract class là IS-A”.
Cả hai đều tạo ra một kiểu mà object có thể thuộc về.

Hãy đặt hai câu hỏi:

1. Các class có cần dùng chung field, constructor hoặc code nền không?
2. Hợp đồng có cần áp dụng cho nhiều class không cùng một cây kế thừa không?

Nếu chỉ cần một hợp đồng thay thế được, interface thường đơn giản hơn.
Nếu có một họ class rõ ràng và phần dùng chung đáng kể, abstract class có thể phù hợp.

Một class concrete cũng có thể vừa `extends` một abstract class, vừa `implements` một hoặc nhiều interface:

```java
public class FilePrinter extends Printer implements Printable {
    // kế thừa phần nền từ Printer và thực hiện hợp đồng Printable
}
```

## IV. Giới Thiệu Nhanh Về Equality

Với object, `==` kiểm tra hai biến có trỏ tới cùng một object hay không.
`equals()` kiểm tra hai object có được xem là bằng nhau về mặt logic hay không.

```java
String first = new String("Java");
String second = new String("Java");

System.out.println(first == second);      // false: khác object
System.out.println(first.equals(second)); // true: cùng nội dung
```

`hashCode()` là một giá trị số đi kèm equality.
Quy tắc cần nhớ: nếu hai object `equals()` nhau thì chúng phải có cùng hash code.

Khi một class có định danh riêng như `id` hoặc `code`, ta có thể override `equals()` và `hashCode()` theo định danh đó.
Không nên dùng field thường xuyên thay đổi như tên, giá hoặc số lượng làm định danh logic.

Equality ảnh hưởng trực tiếp đến một số thao tác với collection:

- `contains(object)` dùng `equals()` để kiểm tra phần tử có tồn tại hay không.
- `remove(object)` dùng `equals()` để tìm phần tử cần xóa.
- Các collection dạng hash còn dùng cả `hashCode()`.

Phần này chỉ giới thiệu mối liên hệ.
Cách override cụ thể sẽ được đặt trong một tình huống dự án ở phần sau.

## V. Từ Rẽ Nhánh Theo Loại Đến Hợp Đồng Chung

Xét một dự án có chức năng thanh toán.
Cách viết ban đầu có thể dựa vào chuỗi loại và `if/else`:

```java
if (type.equals("CASH")) {
    // xử lý tiền mặt
} else if (type.equals("BANK")) {
    // xử lý chuyển khoản
}
```

Cách này chưa sai khi dự án còn nhỏ.
Vấn đề xuất hiện khi dự án có thêm nhiều hình thức thanh toán:

- Nhiều nơi phải biết các chuỗi `CASH`, `BANK`, `QR`.
- Mỗi lần thêm loại mới, các nhánh `if/else` phải được tìm và sửa.
- Code lựa chọn loại bị trộn với code thực hiện thanh toán.
- Dễ bỏ sót một nhánh ở một màn hình hoặc luồng xử lý khác.

Ta tách hành vi chung thành interface:

```java
public interface PaymentMethod {
    String pay(double amount);
}
```

Các class cụ thể tự thực hiện hợp đồng:

```java
public class CashPayment implements PaymentMethod {
    @Override
    public String pay(double amount) {
        return "Đã nhận tiền mặt: " + amount;
    }
}

public class BankTransferPayment implements PaymentMethod {
    @Override
    public String pay(double amount) {
        return "Đã tạo yêu cầu chuyển khoản: " + amount;
    }
}
```

Code sử dụng chỉ phụ thuộc hợp đồng:

```java
static void checkout(PaymentMethod method, double amount) {
    System.out.println(method.pay(amount));
}
```

Khi thêm `QrPayment`, ta bổ sung một implementation mới.
Method `checkout` không cần thêm nhánh theo loại.

Ý nghĩa không nằm ở tên PaymentMethod.
Cùng cách tổ chức có thể áp dụng cho gửi thông báo, xuất báo cáo, lưu file, xác thực người dùng hoặc nhiều bài toán khác.

### Kiểm tra nhanh

Khi thêm một cách thanh toán mới, phần nào của dự án phải biết class cụ thể và phần nào chỉ cần biết PaymentMethod?

## VI. Áp Dụng Cùng Nguyên Tắc Cho Một Hành Vi Khác

Trong cùng một dự án, việc giao hàng cũng có nhiều cách thực hiện:

```java
public interface DeliveryMethod {
    String deliver(Order order);
}
```

```java
public class StandardDelivery implements DeliveryMethod {
    @Override
    public String deliver(Order order) {
        return "Giao tiêu chuẩn cho đơn " + order.getId();
    }
}

public class ExpressDelivery implements DeliveryMethod {
    @Override
    public String deliver(Order order) {
        return "Giao nhanh cho đơn " + order.getId();
    }
}
```

```java
static void printDelivery(DeliveryMethod method, Order order) {
    System.out.println(method.deliver(order));
}
```

PaymentMethod và DeliveryMethod thuộc hai nhóm hành vi khác nhau nhưng giải quyết cùng một vấn đề thiết kế:

- Nơi sử dụng biết hợp đồng.
- Object cụ thể biết cách thực hiện.
- Thêm implementation không buộc code sử dụng phải phân loại lại object.

Ví dụ thứ hai giúp tránh hiểu interface là một cú pháp chỉ dành cho thanh toán.

## VII. Khi Dự Án Cần Một Lớp Nền Dùng Chung

Giả sử các cách giao hàng bắt đầu có phần dữ liệu chung:

- Phí giao hàng.
- Quy tắc phí không được âm.
- Getter để đọc phí.

Nếu lặp lại ba phần này ở mọi class, ta có thể tạo abstract class:

```java
public abstract class BaseDeliveryMethod implements DeliveryMethod {
    private final double fee;

    protected BaseDeliveryMethod(double fee) {
        if (fee < 0) {
            throw new IllegalArgumentException("Phí không được âm");
        }
        this.fee = fee;
    }

    public double getFee() {
        return fee;
    }
}
```

Class concrete kế thừa phần nền và vẫn thực hiện hợp đồng giao hàng:

```java
public class StandardDelivery extends BaseDeliveryMethod {
    public StandardDelivery(double fee) {
        super(fee);
    }

    @Override
    public String deliver(Order order) {
        return "Giao tiêu chuẩn cho đơn " + order.getId();
    }
}
```

Interface và abstract class không loại trừ nhau:

- DeliveryMethod giữ hợp đồng để nơi sử dụng không phụ thuộc class cụ thể.
- BaseDeliveryMethod giữ trạng thái và code dùng chung cho họ class giao hàng.

Không tạo abstract class chỉ để dự án trông “nhiều OOP hơn”.
Chỉ thêm nó khi thật sự có phần nền chung và quan hệ kế thừa hợp lý.

## VIII. Equality Trong Một Dự Án Quản Lý Dữ Liệu

Xét một dự án quản lý sản phẩm.
Hai object có thể được tạo ở hai thời điểm khác nhau nhưng cùng đại diện cho một sản phẩm:

```java
Product oldData = new Product("P01", "Áo thun", 150000, 20);
Product newData = new Product("P01", "Áo thun mới", 160000, 10);
```

Nếu `id` là định danh ổn định, hai object cùng id được xem là cùng sản phẩm:

```java
@Override
public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof Product)) return false;
    Product product = (Product) other;
    return id.equals(product.id);
}

@Override
public int hashCode() {
    return id.hashCode();
}
```

Hai method phải dựa trên cùng `id`.
Tên, giá và tồn kho không phù hợp vì chúng có thể thay đổi.

Sau khi định nghĩa equality, collection có thể tìm object khác instance:

```java
import java.util.ArrayList;
import java.util.List;

List<Product> products = new ArrayList<>();
products.add(oldData);

System.out.println(products.contains(newData)); // true
products.remove(newData);                       // xóa oldData
```

Ý nghĩa trong dự án:

- Model tự định nghĩa thế nào là cùng một thực thể.
- Code tìm kiếm không phải tự so sánh từng field ở nhiều nơi.
- Quy tắc định danh được dùng nhất quán khi kiểm tra hoặc xóa object.

Không phải class nào cũng bắt buộc override equality.
Chỉ làm khi dự án có quy tắc bằng nhau về logic rõ ràng.

## IX. Thực Hành Có Hướng Dẫn

1. Tạo Switchable, Television và Projector; gọi hai object qua cùng method `startDevice(...)`.
2. Thêm SmartLight và xác nhận không sửa thân `startDevice(...)`.
3. Tạo Employee và FullTimeEmployee; chỉ ra field, constructor và method dùng chung.
4. Viết bảng so sánh ngắn giữa interface và abstract class bằng chính hai ví dụ trên.
5. Refactor một nhánh payment `if/else` sang PaymentMethod.
6. Tạo hai Product khác instance cùng id và kiểm tra `==`, `equals()`, `contains()`.

Chạy chương trình sau từng bước để tách lỗi cú pháp khỏi lỗi thiết kế.

## X. Kiểm Tra OOP Cuối Phần

Dành 60 phút cuối để kiểm tra cá nhân các nội dung bắt buộc từ buổi 3–5:

- Đọc mô hình và xác định IS-A/HAS-A.
- Viết một class con có `super(...)` và `@Override`.
- Khai báo một interface và triển khai bằng hai class.
- So sánh interface với abstract class trong một tình huống cụ thể.
- Giải thích ngắn `==`, `equals()` và `hashCode()`.
- Phân tích một đoạn `if/else` theo loại và đề xuất hợp đồng phù hợp.

Bài kiểm tra dùng miền bài toán khác dự án thực hành xuyên suốt.
Lambda, Optional và tự viết generic không nằm trong phạm vi bắt buộc.

## Nội dung mở rộng

- Lambda có thể dùng với một số interface chỉ có một abstract method; chưa yêu cầu tự thiết kế functional interface ở buổi này.
- Generic tự viết là nội dung nâng cao; buổi này chỉ tiếp tục sử dụng collection đã biết.
- Optional chỉ được giới thiệu như hướng biểu diễn kết quả có thể không tồn tại; học bắt buộc ở buổi 8.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| Chỉ học thuộc `implements` | Vẫn phân loại object bằng `if/else` ở nơi gọi | Khai báo tham số bằng kiểu interface |
| Quên `public` khi override method interface | Quyền truy cập hẹp hơn hợp đồng | Khai báo implementation là `public` |
| Dùng abstract class chỉ để gom một method | Tạo quan hệ kế thừa không cần thiết | Dùng interface nếu chỉ cần hợp đồng |
| Nghĩ abstract class không có constructor | Class con không khởi tạo được phần chung | Gọi constructor nền bằng `super(...)` |
| Dùng `==` để so sánh nội dung object | Hai instance cùng định danh vẫn bị xem là khác | Override và gọi `equals()` khi có quy tắc logic |
| Override `equals()` nhưng quên `hashCode()` | Collection dạng hash hoạt động không nhất quán | Dùng cùng field cho cả hai method |
| Sao chép nguyên mẫu Payment vào mọi bài toán | Học thuộc tên class thay vì hiểu vấn đề | Xác định hợp đồng từ hành vi của dự án hiện tại |

## Tổng kết buổi 5

Interface mô tả hợp đồng để nhiều implementation có thể thay thế nhau.
Abstract class làm nền cho một họ class cần chia sẻ trạng thái hoặc code.
Equality mô tả khi nào hai object được xem là cùng một thực thể về logic.

Giá trị của các khái niệm này không nằm ở việc tạo thêm nhiều file hoặc package.
Chúng giúp dự án đặt đúng trách nhiệm, giảm rẽ nhánh theo loại và giữ quy tắc định danh nhất quán.

### Câu hỏi chốt buổi

1. Interface quy định điều gì và implementation chịu trách nhiệm điều gì?
2. Vì sao một biến kiểu interface có thể giữ object của nhiều class khác nhau?
3. Abstract class có thể cung cấp những thành phần dùng chung nào?
4. Khi nào interface phù hợp hơn abstract class và ngược lại?
5. `==` và `equals()` khác nhau thế nào khi so sánh object?
6. Vì sao hai object equal phải có cùng hash code?
7. Khi dự án thêm một loại xử lý mới, dấu hiệu nào cho thấy chuỗi `if/else` nên được thay bằng interface?

## Bài tập nhỏ trên lớp — Hệ thống gửi tài liệu

Tạo mô hình độc lập với dự án thực hành xuyên suốt:

- Interface `DocumentSender` có method `public String send(String recipient, String documentName)`.
- Abstract class `BaseDocumentSender` có field `String senderName`; constructor nhận senderName; getter; method `protected String createPrefix()` trả `senderName + " gửi "`.
- `EmailDocumentSender extends BaseDocumentSender implements DocumentSender`; `send(...)` trả chuỗi có prefix, tên tài liệu, email người nhận và chữ `qua email`.
- `CloudDocumentSender extends BaseDocumentSender implements DocumentSender`; `send(...)` trả chuỗi có prefix, tên tài liệu, tài khoản người nhận và chữ `qua cloud`.
- Class `Document` có field `String code`, `String name`; constructor, getter; override `equals()` và `hashCode()` theo `code`.
- Method `public static void deliver(DocumentSender sender, String recipient, String documentName)` chỉ gọi interface và in kết quả.

Dữ liệu mẫu:

- Người gửi: `Phòng đào tạo`.
- Tài liệu: `DOC01 / Lịch học Java`.
- Người nhận email: `an@example.com`.
- Tài khoản cloud: `student-an`.

Tạo hai Document khác instance cùng code `DOC01` để kiểm tra equality và `contains()`.

**Kết quả mong đợi:** cùng method deliver gửi được bằng hai implementation; output đúng kênh và người nhận; hai Document cùng code được xem là bằng nhau.
Không dùng `if/else` hoặc `instanceof` để phân loại sender trong method deliver.
Thời gian làm khoảng 25 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 05](../BTVN/buoi-05-btvn.md).
