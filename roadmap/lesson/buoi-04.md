# Buổi 4: Encapsulation, Kế Thừa Và Đa Hình

---

> **Hạn chế từ buổi 3:** class đã gom được dữ liệu nhưng setter còn có thể làm trạng thái sai; khi xuất hiện nhiều loại object, code dễ lặp field/method và dùng nhiều nhánh theo loại.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Bảo vệ trạng thái object bằng field private và method có quy tắc.
- Dùng `extends`, `super` và `@Override` để tạo Book từ Product.
- Dùng một `List<Product>` chứa nhiều loại sản phẩm và gọi đa hình.
- Tạo Order có Customer, nhiều CartItem và trạng thái chuyển có kiểm soát.
- Phân biệt IS-A với HAS-A qua các mô hình object đơn giản.

> **Kết quả cuối buổi:** Product thường và Book cùng hiển thị qua `getDescription()`; một Order mẫu chỉ chuyển từ PENDING sang CONFIRMED hoặc CANCELLED.

> **Môi trường:** JDK 17, tiếp tục model và dữ liệu của buổi 3.

## I. Encapsulation Không Chỉ Là Viết Getter/Setter

Nếu mọi field private đều có setter không điều kiện, code bên ngoài vẫn có thể làm Product mang giá âm hoặc stock âm. Encapsulation có nghĩa là object tự bảo vệ trạng thái hợp lệ.

```java
public void setPrice(double price) {
    if (price <= 0) {
        throw new IllegalArgumentException("Giá phải lớn hơn 0");
    }
    this.price = price;
}

public void decreaseStock(int quantity) {
    if (quantity <= 0 || quantity > stock) {
        throw new IllegalArgumentException("Số lượng không hợp lệ");
    }
    stock -= quantity;
}
```

`id` nhận ở constructor và không có setter vì mã định danh không nên đổi sau khi object đã tham gia các quan hệ khác. Với `stock`, method mang tên nghiệp vụ như `decreaseStock` diễn đạt tốt hơn `setStock` trong luồng bán hàng.

### Thảo luận

Giáo viên đưa hai cách gọi `product.setStock(product.getStock() - quantity)` và `product.decreaseStock(quantity)`. Học viên chỉ ra cách thứ hai tập trung được kiểm tra nào và giảm lặp ra sao.

## II. Khi Nào Book Là Một Product?

Book có toàn bộ đặc điểm của Product và bổ sung author. Đây là quan hệ IS-A: Book **là một** Product.

Trước hết, Product cung cấp một hành vi có thể thay đổi theo loại:

```java
public String getDescription() {
    return name;
}
```

Tạo lớp con:

```java
public class Book extends Product {
    private String author;

    public Book(String id, String name, double price, int stock, String author) {
        super(id, name, price, stock);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String getDescription() {
        return getName() + " — tác giả: " + author;
    }
}
```

`super(...)` gọi constructor Product để phần trạng thái kế thừa được khởi tạo đúng. Field của Product vẫn private; Book dùng getter thay vì phá encapsulation.

`@Override` giúp compiler kiểm tra ta thực sự đang ghi đè đúng chữ ký. Nếu viết nhầm `getDescriptions()`, compiler sẽ báo ngay thay vì tạo một method mới ngoài ý muốn.

## III. Đa Hình Loại Bỏ Rẽ Nhánh Theo Loại

```java
List<Product> catalog = new ArrayList<>();
catalog.add(new Product("SP01", "Sổ tay", 25000, 50));
catalog.add(new Product("SP02", "Bút", 10000, 100));
catalog.add(new Book("SP03", "Java nhập môn", 90000, 8, "An Nguyen"));

for (Product product : catalog) {
    System.out.println(product.getDescription());
}
```

Biến trong vòng lặp có kiểu Product, nhưng JVM gọi implementation theo object thật. Kết quả của SP03 đến từ Book. Nơi hiển thị không cần `if (product instanceof Book)` chỉ để chọn cách mô tả.

Đa hình hữu ích khi các object chia sẻ cùng hợp đồng nhưng có cách thực hiện khác nhau. Không nên tạo kế thừa chỉ để tái sử dụng vài dòng code nếu quan hệ “là một” không đúng.

## IV. Order Dùng HAS-A

Order không phải Customer và cũng không phải CartItem. Order **có một** Customer và **có nhiều** CartItem.

```java
public class Order {
    private String id;
    private Customer customer;
    private List<CartItem> items = new ArrayList<>();
    private String status = "PENDING";

    public Order(String id, Customer customer) {
        this.id = id;
        this.customer = customer;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getStatus() { return status; }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getTotal();
        }
        return total;
    }

    public void confirm() {
        if (status.equals("PENDING")) {
            status = "CONFIRMED";
        }
    }

    public void cancel() {
        if (status.equals("PENDING")) {
            status = "CANCELLED";
        }
    }
}
```

Không cung cấp `setStatus(String)` tự do. Nếu có, code bên ngoài có thể gán `DONE`, `ABC` hoặc đưa đơn đã xác nhận về PENDING. `confirm()` và `cancel()` biểu đạt đúng hai chuyển trạng thái được phép. Buổi 8 sẽ thay các chuỗi này bằng enum để compiler kiểm soát tốt hơn.

## V. Demo Xuyên Suốt

```java
Customer customer = new Customer("KH01", "Nguyễn An", "0900000000");
Product notebook = new Product("SP01", "Sổ tay", 25000, 50);
Product pen = new Product("SP02", "Bút", 10000, 100);

Order order = new Order("DH01", customer);
order.addItem(new CartItem(notebook, 2));
order.addItem(new CartItem(pen, 1));

System.out.println(order.getStatus()); // PENDING
System.out.println(order.getTotal());  // 60000
order.confirm();
order.cancel();
System.out.println(order.getStatus()); // vẫn CONFIRMED
```

Yêu cầu học viên vẽ object graph trước khi chạy:

```text
Order DH01 ── Customer KH01
    ├── CartItem ── Product SP01
    └── CartItem ── Product SP02
```

Sơ đồ giúp thấy rõ HAS-A và tránh tạo các field trùng lặp như `customerName` trong Order hoặc `productName` trong CartItem ở giai đoạn này.

## VI. Thực Hành Có Hướng Dẫn

1. Thêm validation cho price và stock của Product, chạy một ca hợp lệ và một ca sai.
2. Thêm `getDescription()` vào Product mà chưa tạo Book; xác nhận output cũ không đổi.
3. Tạo Book SP03 và đưa vào `List<Product>`.
4. Tạo Order DH01 bằng dữ liệu mẫu của buổi học và kiểm tra tổng.
5. Thử chuỗi chuyển trạng thái: confirm → cancel và cancel → confirm trên hai order khác nhau.
6. Viết một class demo tạo và sử dụng các object; không đặt logic kiểm tra vào trong model.

Mỗi nhóm giải thích bằng lời một quan hệ IS-A và hai quan hệ HAS-A trước khi kết thúc thực hành.

## Lỗi thường gặp

| Lỗi | Vì sao xảy ra | Cách sửa |
|---|---|---|
| Book không khởi tạo được Product | Quên `super(...)` | Gọi constructor cha ở câu lệnh đầu tiên |
| Override không chạy | Sai chữ ký method | Dùng `@Override` và kiểm tra tham số/kiểu trả về |
| Danh sách chỉ nhận Book | Khai báo `List<Book>` | Dùng `List<Product>` khi cần đa hình |
| Có thể gán status tùy ý | Dùng setter chung | Chỉ cung cấp method chuyển trạng thái hợp lệ |
| Lớp con truy cập field private | Nhầm kế thừa với quyền truy cập | Dùng protected/public method phù hợp, ưu tiên getter |

## Tổng kết buổi 4

Encapsulation bảo vệ quy tắc của object. Kế thừa diễn đạt quan hệ IS-A và đa hình cho phép code gọi hành vi chung mà không phụ thuộc loại cụ thể. Composition/HAS-A ghép Customer, Order, CartItem và Product thành mô hình đúng với bài toán cửa hàng.

## Bài tập nhỏ trên lớp — Danh sách nhân viên đa hình

Tạo mô hình:

- `Employee` có `id`, `name`, `baseSalary`; các field private và `getMonthlyIncome()` trả `baseSalary`.
- `Developer extends Employee`, bổ sung `overtimeHours`; override thu nhập bằng `baseSalary + overtimeHours * 200000`.
- `SalesEmployee extends Employee`, bổ sung `sales`; override thu nhập bằng `baseSalary + sales * 0.05`.
- Đưa ba object khác loại vào `List<Employee>` và gọi cùng `getMonthlyIncome()` trong một vòng lặp.

**Nghiệm thu:** code hiển thị thu nhập mà không dùng `instanceof` hoặc `if` để phân loại nhân viên. Thời gian làm khoảng 20 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 04](../BTVN/buoi-04-btvn.md). Giữ nguyên model để buổi 5 bổ sung interface và quy tắc equality.
