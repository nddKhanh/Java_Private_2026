# Buổi 5: Abstract Class, Interface, equals() và hashCode()

---

## Mục tiêu buổi học

- Phân biệt abstract class và interface; biết dùng implements cho hợp đồng hành vi.
- Tách cách thanh toán và giao hàng khỏi các nhánh if/else.
- So sánh object theo nội dung bằng equals() và tuân thủ quy tắc hashCode().
- Áp dụng vào PaymentMethod, DeliveryMethod và Product trong ứng dụng.

> **Nối tiếp:** Buổi 4 dùng kế thừa để biểu diễn các loại Product. Thanh toán và giao hàng là các khả năng có thể áp dụng cho nhiều loại object; interface phù hợp với hợp đồng này. Cuối buổi dành khoảng 1 giờ cho kiểm tra OOP theo lộ trình.

## I. Từ if/else Đến Hợp Đồng

Nhiều loại đối tượng có thể cùng thực hiện một khả năng nhưng cách làm khác nhau. Ví dụ nhiều loại thông báo đều có thể `send()`, nhưng email và tin nhắn gửi theo cách khác nhau. Nếu code gọi phụ thuộc từng class cụ thể, thêm một cách làm mới thường kéo theo sửa nhiều nhánh. Interface nêu hợp đồng mà bên gọi cần; class triển khai chịu trách nhiệm về cách thực hiện.

~~~java
public interface PaymentMethod {
    String pay(double amount);
}

public class CashPayment implements PaymentMethod {
    @Override
    public String pay(double amount) {
        return "Đã nhận tiền mặt: " + amount;
    }
}
~~~

Đoạn code sử dụng interface có thể gọi `pay` mà không cần biết class cụ thể. Khi chọn implementation, chương trình vẫn phải tạo hoặc nhận một đối tượng cài đặt hợp đồng đó; interface không tự thực hiện công việc.

> **Hình minh họa cần bổ sung:** Sơ đồ `PaymentMethod` được `CashPayment` và `BankTransferPayment` cài đặt; bên gọi phụ thuộc vào interface và gọi chung `pay()`.

## II. Abstract Class Và Interface

- Abstract class là class chưa hoàn thiện để tạo object trực tiếp; nó có thể chứa field, constructor, method đã cài đặt và method abstract buộc lớp con cung cấp phần còn thiếu.
- Interface định nghĩa hợp đồng hành vi mà class cam kết thực hiện. Class dùng `implements`; một class có thể implement nhiều interface, nhưng chỉ extends một class trực tiếp.
- Chọn abstract class khi các lớp con thật sự cùng một họ, cần chia sẻ trạng thái hoặc code chung; chọn interface khi cần mô tả khả năng/hợp đồng có thể áp dụng cho các class không cùng một lớp cha.
- Không chọn theo quy tắc máy móc “is-a”/“can-do”; hãy xem quan hệ trong miền bài toán, khả năng thay thế và phần code/state thật sự cần chia sẻ. Một interface không bắt buộc chỉ có method abstract trong mọi phiên bản Java, nhưng buổi học tập trung vào hợp đồng method cơ bản.

~~~java
public abstract class Promotion {
    public abstract double calculateDiscount(double total);

    public void printPromotionName() {
        System.out.println("Khuyến mãi của cửa hàng");
    }
}
~~~

Không tạo trực tiếp Promotion; class cụ thể kế thừa nó và cài đặt calculateDiscount(). Abstract class phù hợp khi các lớp con thật sự cùng một loại và có phần code chung.

~~~java
public interface DeliveryMethod {
    double calculateFee(double distanceKm);
}

public class StandardDelivery implements DeliveryMethod {
    @Override
    public double calculateFee(double distanceKm) {
        return 15000 + distanceKm * 2000;
    }
}
~~~

Ứng dụng tạo PaymentMethod/DeliveryMethod rồi truyền vào OrderService. Thêm BankTransferPayment hoặc ExpressDelivery không cần sửa thuật toán tổng quát của đơn.

## III. equals() Và hashCode()

Với object, `==` kiểm tra hai biến có tham chiếu cùng một object không. `equals()` là method để định nghĩa khi nào hai object được xem là bằng nhau về giá trị/định danh; mặc định kế thừa từ `Object` cũng chỉ so sánh cùng instance, nên class cần override nếu muốn equality theo nội dung.

~~~java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Product)) return false;
    Product other = (Product) obj;
    return id.equals(other.id);
}

@Override
public int hashCode() {
    return id.hashCode();
}
~~~

Nếu override equals(), cần override hashCode() theo cùng dữ liệu định danh. Trong buổi này ta áp dụng tìm/xóa trong ArrayList bằng mã; chưa học sâu Set/Map.

Hợp đồng quan trọng: equality phải phản xạ (object bằng chính nó), đối xứng, bắc cầu và ổn định khi dữ liệu dùng để so sánh không đổi; `equals(null)` phải false. Nếu `a.equals(b)` là true thì `a.hashCode()` phải bằng `b.hashCode()`. Chiều ngược lại không bắt buộc: hai object khác nhau vẫn có thể trùng hash code. Vì thế hai method phải dựa trên cùng các trường định danh. Nếu trường equality bị đổi khi object đang nằm trong cấu trúc dựa trên hash, việc tìm object có thể không còn như mong muốn; đây là lý do nên chọn khóa định danh ổn định.

Trong phạm vi buổi học, kiểm tra equality theo `id` là một lựa chọn mô hình. Nếu `id` có thể null, phải xử lý null an toàn thay vì gọi `id.equals(...)` trực tiếp.

## IV. Áp Dụng Và Củng Cố

1. Khai báo PaymentMethod và DeliveryMethod.
2. Viết CashPayment, BankTransferPayment và StandardDelivery.
3. Cho OrderService nhận interface qua tham số.
4. Override equals/hashCode Product theo id rồi tìm sản phẩm tương ứng trong danh sách.
5. Dành 1 giờ cuối buổi cho kiểm tra OOP.

## Phần Mở Rộng Và Nâng Cao — Tìm Hiểu Thêm

- **Lambda cơ bản (mở rộng):** Lambda viết gọn cách cài đặt một hành vi có một method trừu tượng; `forEach` có thể nhận hành động cho từng phần tử, `removeIf` nhận điều kiện lọc. Ví dụ: `products.removeIf(p -> p.getPrice() < 0);`. Chỉ giới thiệu cú pháp và trường hợp dùng, không đi vào Stream.
- **Generics (nâng cao):** `Box<T>` hoặc method `<T> T first(List<T> values)` cho phép tái sử dụng logic với kiểu dữ liệu khác nhau mà vẫn kiểm tra kiểu khi biên dịch. `ArrayList<T>` là ví dụ generic đã dùng; tự định nghĩa generic không phải phần bắt buộc.
- **Optional (giới thiệu sớm):** biểu diễn một kết quả có thể vắng mặt. Chỉ nhận diện `Optional.empty()`/`Optional.of(value)`; nội dung sử dụng trong repository được dạy chính thức ở buổi 8.

**Bài tập tự chọn:** Viết lambda lọc một danh sách số hoặc sản phẩm, rồi tạo `Box<T>` có thao tác lấy/đặt giá trị. Thử `Optional` cho một phép tìm kiếm nhỏ. Các bài này chỉ là khám phá thêm, không thêm điều kiện để hoàn thành chức năng chính.

### Bài Luyện Tập Khái Niệm

1. Đề xuất một interface trung tính như `Exportable` và hai class không cùng lớp cha nhưng cùng cài đặt hợp đồng đó.
2. So sánh abstract class và interface theo state/code dùng chung, khả năng kết hợp và ràng buộc lớp.
3. Lập các cặp object có cùng nội dung nhưng là hai instance riêng; xác định kết quả của `==` và `equals()`.
4. Kiểm tra hợp đồng `equals()`/`hashCode()` bằng các object có cùng và khác mã định danh.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| Class chưa implement hết method | Thiếu method của interface | Implement đủ hoặc khai báo class abstract |
| equals() luôn false | So sánh tham chiếu hoặc sai field định danh | So sánh mã sản phẩm |
| equals đúng nhưng hashCode khác | Override hai method không cùng dữ liệu | Dùng cùng id |
| Dịch vụ vẫn kiểm tra loại thanh toán | Order phụ thuộc class cụ thể | Nhận PaymentMethod interface |

## Tổng Kết Và Mốc Project

- Abstract class gom trạng thái/hành vi chung; interface mô tả hợp đồng; class có thể cài nhiều interface.
- `equals()` định nghĩa equality theo miền dữ liệu; `hashCode()` phải nhất quán với equality.
- Thanh toán/giao hàng và Product là ví dụ ứng dụng, không thay thế phần giải thích hợp đồng và equality.
- Ứng dụng vẫn cần chống crash khi nhập sai và lưu dữ liệu qua lần chạy: buổi 6.

### Câu Hỏi Ôn Tập

1. Khi nào dùng abstract class, khi nào dùng interface?
2. Một class có thể implement nhiều interface không?
3. == khác equals() với object thế nào?
4. Quy tắc giữa equals() và hashCode() là gì?
5. Interface giúp thêm payment method mới ra sao?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 05](../BTVN/buoi-05-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Abstract Methods and Classes](https://docs.oracle.com/javase/tutorial/java/IandI/abstract.html)
- [Oracle — Defining an Interface](https://docs.oracle.com/javase/tutorial/java/IandI/createinterface.html)
- [Java SE API — Object.equals and hashCode](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html)
