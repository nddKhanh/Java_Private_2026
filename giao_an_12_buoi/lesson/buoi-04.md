# Buổi 4: Encapsulation Chuyên Sâu, Kế Thừa và Đa Hình

---

## Mục tiêu buổi học

- Kiểm soát việc thay đổi giá, số lượng và trạng thái qua method.
- Dùng extends, constructor lớp cha và super.
- Override method đúng mục đích; dùng tham chiếu Product để làm việc với các loại sản phẩm con.
- Refactor điều kiện theo loại thành hành vi đa hình.

> **Nối tiếp kiến thức:** Buổi 3 giới thiệu class/object và field private. Buổi này phát triển cách mô hình hóa những kiểu có phần chung nhưng có trạng thái/hành vi đặc thù: encapsulation, kế thừa và đa hình. Abstract class/interface chuyển sang buổi 5.

## I. Kiểu Chung Và Biến Thể Cụ Thể

Trong nhiều mô hình, một nhóm đối tượng chia sẻ thuộc tính/hành vi nhưng mỗi loại cụ thể có phần khác nhau. Ví dụ các hình học cùng có phép tính diện tích nhưng công thức riêng; các loại phương tiện cùng có thông tin cơ bản nhưng cách tính phí khác nhau. Nếu dồn mọi biến thể vào một class với field không liên quan và các nhánh kiểm tra kiểu, mỗi lần thêm loại mới phải sửa nhiều nơi.

~~~java
if (type.equals("BOOK")) { /* in tác giả */ }
else if (type.equals("ELECTRONICS")) { /* in bảo hành */ }
~~~

Kế thừa biểu diễn quan hệ lớp con “là một loại của” lớp cha và tái sử dụng phần chung. Đa hình cho phép code làm việc qua kiểu chung, trong khi method được chọn theo object thực tế lúc chạy. Đây là hai ý liên quan nhưng khác nhau: `extends` tạo quan hệ lớp; overriding cung cấp hành vi thay thế; dispatch động chọn bản cài đặt phù hợp.

## II. Encapsulation Và Kiểm Soát Dữ Liệu

Encapsulation gom trạng thái và hành vi quản lý trạng thái trong class, đồng thời giới hạn cách code bên ngoài truy cập. Mục tiêu không chỉ là “đổi field thành private”, mà là bảo đảm object luôn giữ được các điều kiện hợp lệ (invariant), chẳng hạn giá dương hoặc tồn kho không âm. Nếu mọi nơi có thể gán field tùy ý, class không thể bảo đảm quy tắc của chính nó.

~~~java
public boolean setPrice(double price) {
    if (price <= 0) {
        return false;
    }
    this.price = price;
    return true;
}
~~~

Method trả `false` khi yêu cầu không hợp lệ và chỉ đổi trạng thái khi đầu vào đạt quy tắc. Trong chương trình lớn hơn có thể chọn cơ chế báo lỗi phù hợp; exception học ở buổi 6. Tách thao tác nghiệp vụ như `decreaseStock(quantity)` để kiểm tra số lượng dương và đủ hàng trước khi trừ. Trạng thái chỉ đổi qua các chuyển tiếp hợp lệ như `confirm()` hoặc `cancel()` thay vì gán tùy tiện.

Không phải mọi thuộc tính đều cần setter. Chỉ cung cấp thao tác cập nhật mà mô hình thật sự cho phép; nếu muốn tạo object hợp lệ một lần rồi giữ nguyên giá trị, có thể chỉ cung cấp constructor và getter.

## III. Kế Thừa Với extends Và super

Lớp con kế thừa thành viên truy cập được của lớp cha, có thể bổ sung field/method riêng và ghi đè method instance phù hợp. Constructor không được kế thừa như method thường: khi khởi tạo object lớp con, constructor lớp con gọi constructor lớp cha bằng `super(...)`. Nếu không viết lời gọi tường minh, Java chỉ chèn `super()` khi lớp cha có constructor không tham số dùng được.

~~~java
public class Product {
    private String id;
    private String name;
    private double price;

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }

    public String getDescription() { return name; }
}

public class Book extends Product {
    private String author;

    public Book(String id, String name, double price, String author) {
        super(id, name, price);
        this.author = author;
    }

    @Override
    public String getDescription() {
        return getName() + " — tác giả: " + author;
    }
}
~~~

`extends` khai báo lớp con; `super(...)` gọi constructor lớp cha và phải là lệnh đầu tiên trong constructor; `@Override` giúp compiler kiểm tra method được viết lại.

Field `private` của lớp cha không được truy cập trực tiếp từ lớp con; lớp con dùng method public/protected phù hợp do lớp cha cung cấp. Giữ dữ liệu private giúp lớp cha kiểm soát invariant của nó thay vì để subclass sửa trạng thái tùy tiện. Kế thừa chỉ phù hợp khi quan hệ “là một loại” phản ánh đúng mô hình; dùng kế thừa chỉ để tiết kiệm vài dòng code thường tạo ràng buộc không cần thiết.

## IV. Đa Hình Trong Danh Sách Sản Phẩm

Trước hết xét ví dụ trung tính: mọi phương tiện có mô tả chung, còn loại cụ thể có thể mô tả chi tiết hơn.

~~~java
class Vehicle {
    String description() { return "Phương tiện"; }
}

class Bicycle extends Vehicle {
    @Override
    String description() { return "Xe đạp"; }
}

Vehicle vehicle = new Bicycle();
System.out.println(vehicle.description()); // gọi bản cài đặt của Bicycle
~~~

Kiểu biến `Vehicle` quyết định những method nào có thể được gọi khi biên dịch; object thực tế `Bicycle` quyết định phiên bản override nào chạy lúc runtime. Đây là dispatch động. Một list kiểu cha có thể chứa các object con nếu chúng là subtype hợp lệ.

~~~java
List<Product> products = new ArrayList<>();
products.add(new Product("P01", "Bình nước", 50000));
products.add(new Book("P02", "Java cơ bản", 120000, "An Nguyen"));

for (Product product : products) {
    System.out.println(product.getDescription());
}
~~~

List có kiểu Product nhưng mỗi phần tử giữ object con cụ thể. Khi gọi `getDescription()`, Java chạy method override của object đó. Không cần kiểm tra type bằng if/else.

> **Hình minh họa cần bổ sung:** Sơ đồ `Vehicle → Bicycle`; kèm mũi tên từ biến kiểu `Vehicle` tới object `Bicycle` và lời gọi `description()` được dispatch.

## V. Order Chứa CartItem

Phần này là ví dụ ngắn để nối khái niệm HAS-A đã học với kiểm soát trạng thái; trọng tâm buổi học vẫn là encapsulation, kế thừa và đa hình.

Order có danh sách CartItem (HAS-A). Thêm Order và trạng thái đơn giản để các thao tác chuyển trạng thái qua method. Buổi này dùng String để biểu diễn status; enum thuộc buổi 8.

~~~java
public class Order {
    private final List<CartItem> items = new ArrayList<>();
    private String status = "PENDING";

    public void addItem(CartItem item) { items.add(item); }
    public void confirm() { status = "CONFIRMED"; }
    public String getStatus() { return status; }
}
~~~

## Phần Nâng Cao — Tìm Hiểu Thêm

- Overloading là nhiều method cùng tên nhưng khác danh sách tham số; compiler chọn overload dựa trên kiểu và số lượng đối số. Overriding là lớp con thay phần cài đặt method kế thừa; runtime dispatch chọn bản cài đặt theo object thực tế.
- HAS-A biểu diễn một object chứa/sử dụng object khác; composition nhấn mạnh vòng đời/thành phần thuộc về đối tượng chứa. IS-A biểu diễn quan hệ subtype và chỉ nên dùng khi lớp con thay thế được lớp cha.
- Upcasting chuyển tham chiếu lớp con sang lớp cha, thường tự động và an toàn; downcasting cần ép kiểu tường minh, chỉ hợp lệ nếu object thực sự thuộc kiểu đích. `instanceof` có thể kiểm tra trước khi ép, nhưng không nên dùng để quay lại chuỗi if/else theo loại.

**Bài tập tự chọn:** Tạo `Shape` với `Circle` và `Rectangle`, viết overload tính diện tích từ các bộ tham số khác nhau; dự đoán khi nào chọn overload và khi nào gọi override. Thử upcast rồi kiểm tra một downcast hợp lệ/không hợp lệ. Bài này chỉ giúp đào sâu OOP, không thêm chức năng cốt lõi của ứng dụng.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| Constructor lớp con báo lỗi | Chưa gọi constructor cha phù hợp | Gọi super(...) đầu constructor |
| Override không chạy | Sai tên/tham số method | Dùng @Override để phát hiện |
| Field private lớp cha không truy cập được | private chỉ dùng trong class khai báo | Dùng getter hoặc method nghiệp vụ |
| Vẫn có if/else cho từng subtype | Dùng type để rẽ nhánh thay vì hành vi chung | Gọi method override qua Product |

## Tổng Kết Và Mốc Project

- Encapsulation bảo vệ invariant; kế thừa chia sẻ/đặc tả hành vi có quan hệ IS-A; overriding và đa hình chọn hành vi theo object thực tế.
- Order chứa CartItem là ví dụ ngắn về HAS-A; Product và các lớp con minh họa kế thừa/đa hình.
- Cần một hợp đồng cho thanh toán/giao hàng mà không sửa code cũ: nội dung buổi 5.

### Câu Hỏi Ôn Tập

1. Lớp con kế thừa gì từ lớp cha?
2. super trong constructor dùng để làm gì?
3. @Override giúp phát hiện lỗi nào?
4. Đa hình thể hiện ra sao trong List<Product>?
5. Vì sao không nên sửa field giá từ mọi nơi?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 04](../BTVN/buoi-04-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Inheritance](https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html)
- [Oracle — Overriding and Hiding Methods](https://docs.oracle.com/javase/tutorial/java/IandI/override.html)
- [Oracle — Polymorphism](https://docs.oracle.com/javase/tutorial/java/IandI/polymorphism.html)
