# Buổi 5: Interface, Abstract Class Và Equality

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Nhận ra rẽ nhánh theo loại và thay bằng một hợp đồng chung.
- Tạo, triển khai và sử dụng `PaymentMethod`, `DeliveryMethod` qua interface.
- Phân biệt vai trò của abstract class và interface.
- Override `equals()`/`hashCode()` nhất quán dựa trên định danh Product.
- Giải thích vì sao `ArrayList.contains/remove` phụ thuộc equality.

> **Kết quả cuối buổi:** chương trình thay được cách thanh toán và giao hàng mà code gọi không đổi; hai Product cùng id được xem là cùng sản phẩm.

> **Môi trường:** JDK 17, tiếp tục Order và Product của buổi 4.

## I. Từ `if/else` Đến Hợp Đồng

Một cách chọn thanh toán dễ xuất hiện là:

```java
if (type.equals("CASH")) {
    // xử lý tiền mặt
} else if (type.equals("BANK")) {
    // xử lý chuyển khoản
}
```

Khi thêm phương thức mới, nhiều nơi phải biết thêm chuỗi mới. Nơi tạo lựa chọn và nơi thực hiện hành vi bị trộn lẫn. Ta tách phần “mọi cách thanh toán đều làm được gì” thành interface:

```java
public interface PaymentMethod {
    String pay(double amount);
}
```

Hai implementation:

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

Code dùng payment chỉ phụ thuộc hợp đồng:

```java
static void checkout(PaymentMethod paymentMethod, double amount) {
    System.out.println(paymentMethod.pay(amount));
}
```

Gọi `checkout` lần lượt với hai object. Chữ ký và thân của `checkout` không thay đổi khi implementation được thay thế.

## II. Áp Dụng Lần Hai Với DeliveryMethod

Nếu học viên chỉ làm một ví dụ, interface dễ bị hiểu như “một cú pháp riêng cho payment”. Áp dụng lại cùng ý tưởng cho giao hàng:

```java
public interface DeliveryMethod {
    String deliver(Order order);
}

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

PaymentMethod và DeliveryMethod khác miền hành vi nhưng cùng nguyên tắc: code cấp cao dùng abstraction, implementation cụ thể có thể thay thế.

## III. Abstract Class Khác Interface Ở Đâu?

Interface phù hợp khi cần định nghĩa khả năng mà nhiều class khác nhau cùng thực hiện. Abstract class phù hợp khi các lớp con thực sự cùng một họ và cần dùng chung trạng thái hoặc một phần implementation.

Ví dụ mở rộng cho delivery:

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

Class trừu tượng có thể giữ `fee` và constructor dùng chung; interface chỉ nêu hành vi `deliver`. Một class chỉ kế thừa một class nhưng có thể implements nhiều interface.

Product là class concrete khi chương trình cần tạo trực tiếp các đối tượng Product. Một class chỉ nên được khai báo `abstract` khi không cần khởi tạo trực tiếp và các lớp con phải cung cấp hành vi chuyên biệt.

## IV. Product Equality: Bằng Nhau Theo Điều Gì?

Mặc định, hai object khác instance không bằng nhau dù field giống nhau:

```java
Product a = new Product("SP01", "Sổ tay", 25000, 50);
Product b = new Product("SP01", "Sổ tay bìa cứng", 30000, 20);
System.out.println(a.equals(b)); // false nếu chưa override
```

Trong ví dụ quản lý sản phẩm, id là định danh ổn định. Hai Product cùng id đại diện cùng sản phẩm:

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

Quy tắc bắt buộc: nếu `a.equals(b)` là true thì `a.hashCode() == b.hashCode()` cũng phải true. Vì vậy hai method phải dựa trên cùng `id`.

Không đưa name, price hoặc stock vào equality. Các field này có thể thay đổi, trong khi định danh sản phẩm không nên đổi. Nếu equality dựa vào field thay đổi, một Product đang nằm trong collection có thể trở nên “khó tìm” sau khi cập nhật.

## V. Equality Ảnh Hưởng Collection

```java
List<Product> products = new ArrayList<>();
products.add(new Product("SP01", "Sổ tay", 25000, 50));

Product searchKey = new Product("SP01", "Tên không quan trọng", 1, 0);
System.out.println(products.contains(searchKey));
products.remove(searchKey);
```

`contains` và `remove(Object)` dùng `equals`. Sau khi override đúng, object làm khóa tìm kiếm không cần cùng instance với object trong list.

### Kiểm tra biên

- Cùng instance: true.
- Khác instance, cùng id: true.
- Khác id: false.
- So với null: false.
- Hai object equal: cùng hash code.

## VI. Thực Hành Có Hướng Dẫn

1. Viết payment bằng `if/else`, sau đó refactor sang interface và so sánh code gọi.
2. Tạo hai DeliveryMethod và chạy với một Order mẫu `DH01`.
3. Viết equality cho Product, kiểm tra năm ca biên.
4. Dùng một Product khác instance để tìm và xóa SP01 trong `ArrayList`.
5. Gọi payment/delivery từ một class demo, không đưa logic lựa chọn UI vào implementation.

Xác định phần thay đổi khi thêm `QrPayment`: bổ sung implementation mới và cập nhật điểm khởi tạo hoặc lựa chọn; không sửa `checkout`.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| Nơi gọi dùng `instanceof` cho từng payment | Mất lợi ích đa hình | Chỉ gọi method qua interface |
| Override equals nhưng quên hashCode | Collection hash hoạt động không nhất quán | Dùng cùng field id cho cả hai |
| Equality dựa vào price/stock | Cập nhật làm thay đổi định danh logic | Chỉ dùng id bất biến |
| Biến Product thành abstract | Không tạo được các Product thông thường | Giữ Product concrete trong ví dụ hiện tại |
| Interface chứa logic/state dùng chung phức tạp | Hợp đồng khó hiểu | Cân nhắc abstract base class khi có quan hệ họ hàng thật |

## Tổng kết buổi 5

Interface tách “có thể làm gì” khỏi “làm bằng cách nào”. Abstract class chia sẻ trạng thái/implementation cho một họ lớp. Equality biến định danh nghiệp vụ thành quy tắc mà collection hiểu được.

## Bài tập nhỏ trên lớp — Gửi thông báo

Tạo interface `NotificationSender` có method `String send(String recipient, String message)`:

- `EmailSender` và `SmsSender` triển khai hai nội dung trả về khác nhau.
- Viết method `notifyUser(NotificationSender sender, String recipient, String message)` chỉ phụ thuộc interface.
- Gọi method với cả hai implementation.
- Tạo thêm class `NotificationTemplate` có `code`, `title`; override `equals()` và `hashCode()` theo `code`, rồi kiểm tra `contains()` bằng object khác instance.

**Nghiệm thu:** thêm một implementation mới không làm thay đổi `notifyUser`; hai template cùng code được xem là bằng nhau. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 05](../BTVN/buoi-05-btvn.md).
