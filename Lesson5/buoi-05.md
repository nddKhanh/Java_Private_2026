# BUỔI 05 — EQUALITY, INTERFACE VÀ ABSTRACT CLASS

**Dự án tham chiếu:** HitShop  
**Môi trường:** JDK 17  
**Kiến thức nền:** Class, object, constructor, encapsulation, inheritance, overriding và polymorphism.

## I. Mục tiêu kiến thức

- Phân biệt so sánh giá trị, so sánh tham chiếu và so sánh bằng nhau về mặt logic trong Java.
- Giải thích vai trò của `equals()`, `hashCode()` và mối liên hệ với các thao tác trên collection.
- Trình bày khái niệm interface, cú pháp `interface` và `implements`, cùng tính đa hình qua kiểu interface.
- Trình bày khái niệm abstract class, constructor, phương thức abstract và khả năng tái sử dụng thành phần chung.
- So sánh interface với abstract class trong tổ chức và mở rộng chương trình.

## II. Equality — `==`, `equals()` và `hashCode()`

### 1. Toán tử `==` và phương thức `equals()`

Trong Java, toán tử `==` có ý nghĩa phụ thuộc vào kiểu dữ liệu:

- **Với kiểu nguyên thủy**, `==` so sánh giá trị của hai toán hạng.
- **Với kiểu tham chiếu**, `==` kiểm tra hai tham chiếu có trỏ đến cùng một object hay không.

`equals()` là phương thức được khai báo trong class `Object`, biểu diễn quan hệ bằng nhau giữa hai object. Nếu không được ghi đè, `Object.equals()` sử dụng so sánh danh tính tương đương `==`. Một số class, chẳng hạn `String`, ghi đè phương thức này để so sánh theo nội dung.

```java
String a = new String("Java");
String b = new String("Java");

System.out.println(a == b);      // false
System.out.println(a.equals(b)); // true
```

Hai biến `a` và `b` tham chiếu đến hai object khác nhau nhưng chứa cùng một chuỗi ký tự. Vì vậy, `==` trả về `false`, còn `String.equals()` trả về `true`.

### 2. Phương thức `hashCode()`

`hashCode()` là phương thức thuộc class `Object`, trả về một giá trị số nguyên dùng trong các cấu trúc dữ liệu dựa trên bảng băm, chẳng hạn `HashMap` và `HashSet`.

Hợp đồng giữa `equals()` và `hashCode()` quy định:

- Nếu `a.equals(b)` là `true`, thì `a.hashCode()` và `b.hashCode()` phải bằng nhau.
- Hai object có cùng hash code không nhất thiết bằng nhau theo `equals()`.

Hai phương thức phục vụ các mục đích có liên hệ nhưng không tương đương: `equals()` xác định tính bằng nhau về logic, còn `hashCode()` hỗ trợ phân nhóm và tra cứu trong cấu trúc băm.

### 3. Quan hệ với `List.contains()` và `List.remove()`

`List` hỗ trợ những thao tác dựa trên phép so sánh bằng nhau giữa các phần tử:

- `contains(Object o)`: xác định danh sách có phần tử bằng `o` theo `equals()` hay không.
- `remove(Object o)`: xóa lần xuất hiện đầu tiên của phần tử bằng `o` theo `equals()` nếu phần tử đó tồn tại.

Khác với collection dựa trên bảng băm, hai thao tác trên của `List` không sử dụng `hashCode()` để xác định phần tử tương đương. `List` cũng có phương thức `remove(int index)` dùng để xóa theo vị trí, với ý nghĩa khác `remove(Object o)`.

## III. Interface — Hợp đồng hành vi

### 1. Khái niệm

Interface định nghĩa một tập hợp các hành vi mà những class triển khai phải tuân thủ. Interface xác định **hợp đồng về phương thức**, trong khi các class triển khai quyết định **cách thực hiện cụ thể**.

Ví dụ: một hệ thống có thể gửi thông báo qua email hoặc SMS.

```java
public interface MessageSender {
    String send(String message);
}
```

```java
public class EmailSender implements MessageSender {
    @Override
    public String send(String message) {
        return "Đã gửi email: " + message;
    }
}

public class SmsSender implements MessageSender {
    @Override
    public String send(String message) {
        return "Đã gửi SMS: " + message;
    }
}
```

Phương thức xử lý có thể nhận tham số là kiểu interface:

```java
public static void notifyUser(MessageSender sender, String message) {
    System.out.println(sender.send(message));
}
```

Các lời gọi sau sử dụng cùng một phương thức với những cách triển khai khác nhau:

```java
notifyUser(new EmailSender(), "Đơn hàng đã được tạo");
notifyUser(new SmsSender(), "Đơn hàng đã được tạo");
```

### 2. Bản chất của hợp đồng hành vi

**Hợp đồng hành vi (behavioral contract)** quy định những thao tác mà một đối tượng thuộc kiểu interface phải cung cấp. Trong ví dụ `MessageSender`, hợp đồng khai báo phương thức `send(String message)` với kiểu trả về `String`. Hợp đồng xác định **hành vi có thể được gọi**, không ấn định nội dung email, cách gửi SMS hay thuật toán xử lý bên trong.

- **Phía interface** khai báo những phương thức mà mã nguồn sử dụng có thể dựa vào, bao gồm tên phương thức, tham số và kiểu trả về.
- **Phía class triển khai** sử dụng `implements` và cung cấp phần thân cho các phương thức abstract của hợp đồng chưa được triển khai sẵn. Một class không abstract phải hoàn thiện các phương thức abstract còn thiếu, kể cả khi việc hoàn thiện được kế thừa từ class cha.
- **Phía mã nguồn sử dụng** có thể làm việc thông qua biến, tham số hoặc kiểu trả về là interface, mà không cần gọi trực tiếp phương thức riêng của từng implementation.

Nhờ đó, cả `EmailSender` lẫn `SmsSender` đều có thể được truyền vào `notifyUser(MessageSender sender, ...)`. Lời gọi `sender.send(message)` được kiểm tra dựa trên hợp đồng `MessageSender` khi biên dịch; khi chương trình thực thi, phương thức của object thực tế được lựa chọn theo cơ chế **đa hình**.

Điểm cốt lõi là sự phân tách giữa **quy định hành vi** và **cách hiện thực hành vi**. Nhiều class không cùng một cây kế thừa vẫn có thể cùng tuân thủ một interface.

### 3. Đặc điểm của interface

- `interface` khai báo một kiểu tham chiếu; `implements` khai báo quan hệ giữa class và interface.
- Phương thức abstract thông thường trong interface mặc định là `public abstract`. Phương thức triển khai tương ứng phải có phạm vi truy cập `public`.
- Interface có thể có phương thức `default`, `static` và `private` với phần thân; do đó interface không đồng nghĩa với một tập hợp chỉ gồm các phương thức không có phần thân.
- Interface không có constructor và không được khởi tạo trực tiếp bằng `new`.
- Interface không chứa field instance; những field được khai báo trong interface mặc định là hằng `public static final`.
- Một class có thể triển khai nhiều interface, trong khi một class chỉ có một class cha trực tiếp.
- Biến hoặc tham số kiểu interface có thể tham chiếu đến object thuộc nhiều class triển khai khác nhau.

## IV. Abstract Class — Lớp nền dùng chung

### 1. Khái niệm và cấu trúc

Abstract class là class được khai báo bằng từ khóa `abstract` và không thể khởi tạo trực tiếp. Abstract class có thể cung cấp thuộc tính, constructor, phương thức đã có phần thân và phương thức abstract chưa có phần thân.

Trong ví dụ gửi thông báo, các implementation có thể cùng sử dụng thông tin người gửi. Phần dữ liệu và logic dùng chung được đặt trong một abstract class:

```java
public abstract class BaseMessageSender implements MessageSender {
    private String senderName;

    public BaseMessageSender(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}
```

Class cụ thể có thể kế thừa lớp nền:

```java
public class EmailSender extends BaseMessageSender {
    public EmailSender(String senderName) {
        super(senderName);
    }

    @Override
    public String send(String message) {
        return getSenderName() + " gửi email: " + message;
    }
}
```

Ở phiên bản này, constructor của `EmailSender` gọi `super(senderName)` để khởi tạo phần trạng thái được kế thừa. `EmailSender` vẫn đáp ứng hợp đồng `MessageSender` thông qua lớp cha `BaseMessageSender`.

Một abstract class cũng có thể khai báo phương thức abstract:

```java
public abstract class Shape {
    public abstract double getArea();
}
```

Một class con không abstract của `Shape` phải cung cấp phần triển khai cho `getArea()`.

### 2. Bản chất của abstract class

**Abstract class** là một kiểu class đóng vai trò **lớp cơ sở chưa hoàn chỉnh** trong quan hệ kế thừa. Nó cho phép các class con chia sẻ trạng thái và cách thực hiện chung, đồng thời quy định những phần hành vi mà các class con phải hoàn thiện.

Có hai loại phương thức cần phân biệt:

- **Phương thức abstract:** được khai báo bằng từ khóa `abstract`, chỉ có phần khai báo và **không có phần thân**. Đây là hành vi chưa được định nghĩa cách thực hiện tại lớp hiện tại. Một class con cụ thể (không abstract) phải triển khai những phương thức abstract còn thiếu.
- **Phương thức thông thường (concrete method):** có phần thân chứa code thực hiện. Class con có thể kế thừa và gọi lại phương thức này; khi điều kiện ghi đè cho phép, class con cũng có thể sử dụng `@Override` để thay đổi cách thực hiện.

Ví dụ, `Shape.getArea()` là phương thức abstract, còn `BaseMessageSender.getSenderName()` là phương thức thông thường. Hai phương thức cùng thuộc class nhưng khác nhau ở việc **đã có hay chưa có cách thực hiện**.

Một abstract class **không nhất thiết phải khai báo phương thức abstract**. Việc đánh dấu `abstract` vẫn có ý nghĩa ngăn khởi tạo trực tiếp class nền. Ngược lại, một class có phương thức abstract phải được khai báo là abstract.

Constructor của abstract class không dùng để tạo trực tiếp object thuộc class abstract mà để khởi tạo phần trạng thái chung khi object của class con được tạo. Trong ví dụ `EmailSender`, `super(senderName)` gọi constructor của `BaseMessageSender`.

### 3. Đặc điểm quan hệ kế thừa

- Class con sử dụng `extends` để kế thừa một abstract class, thuộc cùng hệ phân cấp kiểu với class cha.
- Một class chỉ có thể có **một class cha trực tiếp**, bất kể đó là class thường hay abstract class.
- Abstract class có thể chứa field instance, constructor, phương thức thông thường và phương thức abstract.
- Abstract class có thể `implements` interface; nếu chưa hoàn thiện toàn bộ phương thức abstract thì class đó vẫn có thể là abstract.
- Abstract class và interface có thể kết hợp: interface quy định hợp đồng, abstract class tổ chức trạng thái và phần code dùng chung.

### 4. So sánh interface và abstract class

| Tiêu chí | Interface | Abstract class |
|---|---|---|
| Mục đích chủ yếu | Định nghĩa hợp đồng hành vi | Tổ chức phần nền chung cho các class có quan hệ kế thừa |
| Trạng thái instance | Không có field instance | Có thể có field instance |
| Constructor | Không có | Có |
| Phương thức có phần thân | Có thể khai báo `default`, `static` và `private` method | Có thể khai báo phương thức thường |
| Quan hệ với class triển khai | `implements`, có thể nhiều interface | `extends`, chỉ một class cha trực tiếp |
| Khởi tạo trực tiếp | Không | Không |

Cả interface và abstract class đều có thể được dùng làm **kiểu trừu tượng** để biểu diễn nhiều object và hỗ trợ đa hình. Tuy nhiên, trọng tâm của hai cơ chế khác nhau: interface tập trung vào **hợp đồng có thể thực hiện**, còn abstract class tập trung vào **lớp nền trong quan hệ kế thừa** và phần hiện thực có thể chia sẻ. Sự tồn tại của `default` method trong interface không làm thay đổi khác biệt căn bản này.

## V. Bài tập trên lớp — Interface và Abstract Class

1. Xây dựng interface `MessageSender` cùng hai class `EmailSender`, `SmsSender`; sử dụng phương thức `notifyUser(...)` nhận tham số kiểu `MessageSender`.
2. Bổ sung `AppSender` triển khai cùng interface và sử dụng lại `notifyUser(...)`.
3. Xây dựng abstract class `BaseMessageSender` chứa thuộc tính `senderName`, constructor và getter; điều chỉnh các class gửi thông báo để kế thừa lớp nền.
4. Phân tích sự khác biệt giữa các thành phần được định nghĩa trong `MessageSender` và `BaseMessageSender`.

## VI. Tổng kết và câu hỏi củng cố

- **Equality** xác định các quy tắc so sánh object: `==` đối với tham chiếu kiểm tra danh tính, trong khi `equals()` có thể được định nghĩa để so sánh tính bằng nhau về logic. `hashCode()` duy trì quan hệ nhất quán với `equals()` khi sử dụng cấu trúc dữ liệu băm.
- **Interface** định nghĩa hợp đồng chung cho các class triển khai, cho phép sử dụng đa hình thông qua một kiểu trừu tượng.
- **Abstract class** cung cấp phần nền và các thành phần dùng chung cho những class thuộc cùng một hệ phân cấp kế thừa.
- **Thiết kế hướng đến hợp đồng** giúp tách mã sử dụng hành vi khỏi cách triển khai cụ thể, qua đó giảm sự phụ thuộc vào từng class.

### Câu hỏi củng cố

1. **Interface là gì?** Bản chất của hợp đồng hành vi được thể hiện như thế nào qua interface và các class triển khai?
2. **Abstract class là gì?** Phân biệt phương thức abstract với phương thức thông thường trong abstract class.
3. **Interface và abstract class khác nhau về bản chất ở những điểm nào?** So sánh mục đích sử dụng, khả năng chứa trạng thái và quan hệ kế thừa.

## VII. Mở rộng tư duy thiết kế hệ thống — Từ rẽ nhánh theo loại đến hợp đồng chung

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

## VIII. Code mẫu cho bài tập trên lớp

Ví dụ sau sử dụng một tệp `Main.java` để minh họa cách khai báo interface, kế thừa abstract class và truyền các implementation qua tham số kiểu interface.

```java
interface MessageSender {
    String send(String message);
}

abstract class BaseMessageSender implements MessageSender {
    private String senderName;

    public BaseMessageSender(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}

class EmailSender extends BaseMessageSender {
    public EmailSender(String senderName) {
        super(senderName);
    }

    @Override
    public String send(String message) {
        return getSenderName() + " gửi Email: " + message;
    }
}

class SmsSender extends BaseMessageSender {
    public SmsSender(String senderName) {
        super(senderName);
    }

    @Override
    public String send(String message) {
        return getSenderName() + " gửi SMS: " + message;
    }
}

class AppSender extends BaseMessageSender {
    public AppSender(String senderName) {
        super(senderName);
    }

    @Override
    public String send(String message) {
        return getSenderName() + " gửi thông báo ứng dụng: " + message;
    }
}

public class Main {
    public static void notifyUser(MessageSender sender, String message) {
        System.out.println(sender.send(message));
    }

    public static void main(String[] args) {
        notifyUser(new EmailSender("HitShop"), "Đơn hàng đã được tạo");
        notifyUser(new SmsSender("HitShop"), "Đơn hàng đã được tạo");
        notifyUser(new AppSender("HitShop"), "Đơn hàng đã được tạo");
    }
}
```

Kết quả:

```text
HitShop gửi Email: Đơn hàng đã được tạo
HitShop gửi SMS: Đơn hàng đã được tạo
HitShop gửi thông báo ứng dụng: Đơn hàng đã được tạo
```

Ở đây, `MessageSender` xác định phương thức chung `send()`, `BaseMessageSender` chứa trạng thái `senderName` và constructor, còn các class con cung cấp cách thực hiện riêng. Phương thức `notifyUser()` nhận kiểu `MessageSender`, nên có thể sử dụng cả ba implementation mà không thay đổi phần thân phương thức.
