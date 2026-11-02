# Buổi 3: OOP Nhập Môn Qua Product, Customer Và CartItem

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Giải thích vì sao ba danh sách song song của buổi 2 dễ lệch dữ liệu.
- Tạo class, object, field, method, constructor, getter/setter và `toString()`.
- Dùng quan hệ HAS-A để một CartItem giữ một Product.
- Refactor giỏ hàng sang `List<CartItem>` mà vẫn giữ bốn chức năng thêm, xóa, in và tính tổng.
- Phân biệt biến tham chiếu với object và quan sát việc hai biến cùng trỏ một object.

> **Kết quả cuối buổi:** chương trình mẫu dùng `Product`, `Customer` và `CartItem` để tổ chức lại dữ liệu của giỏ hàng buổi 2.

> **Môi trường:** JDK 17.

## I. Bắt Đầu Từ Một Lỗi Có Thật

Ở buổi 2, mặt hàng tại vị trí `i` được ghép từ ba list:

```java
names.get(i);
prices.get(i);
quantities.get(i);
```

Nếu xóa `names.remove(i)` nhưng quên xóa hai list còn lại, tên của sản phẩm này sẽ bị ghép với giá của sản phẩm khác. Vấn đề không nằm ở vòng lặp; vấn đề là dữ liệu vốn thuộc cùng một mặt hàng lại bị lưu ở ba nơi.

Ta muốn chương trình diễn đạt trực tiếp ý này:

```text
Một Product có id, name, price và stock.
Một CartItem có một Product và quantity.
```

OOP giúp gom dữ liệu liên quan và hành vi xử lý dữ liệu đó vào object. Đây là cách làm cho mô hình dễ đọc, dễ mở rộng và ít có nguy cơ lệch dữ liệu hơn; OOP không chỉ là đổi cú pháp.

Các thuật ngữ sẽ dùng trong bài:

- **Class (lớp)** là bản thiết kế mô tả một nhóm object.
- **Object (đối tượng)**, còn gọi là **instance (thể hiện)**, là một giá trị cụ thể được tạo từ class.
- **Field (trường dữ liệu)** lưu trạng thái của object.
- **Method (phương thức)** mô tả hành vi mà object hoặc class cung cấp.

### Kiểm tra nhanh

Xóa phần tử ở index 0 chỉ khỏi `names`, dự đoán output và xác định dữ liệu bị ghép sai.

## II. Class Product: Từ Bản Thiết Kế Đến Object

Package gom các class liên quan vào cùng một không gian tên. Trong source root, tạo package `model` và file `Product.java`. Cấu trúc tối thiểu của chương trình như sau:

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Customer.java
    └── CartItem.java
```

Đường dẫn thư mục phải khớp với dòng `package model;` ở đầu ba class model. `HitShop` cần import các class này trước khi sử dụng.

```java
package model;

public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + price + " | tồn: " + stock;
    }
}
```

`Product` là bản thiết kế. Mỗi lần gọi constructor, chương trình tạo một object có trạng thái riêng:

```java
Product notebook = new Product("SP01", "Sổ tay", 25000, 50);
Product pen = new Product("SP02", "Bút", 10000, 100);
```

Constructor có cùng tên với class, không khai báo kiểu trả về và được gọi khi dùng từ khóa `new`. Trong constructor, `this.name` là field của object hiện tại; `name` là tham số được truyền vào. Việc dùng cùng tên giúp thể hiện rõ ý nghĩa “gán dữ liệu đầu vào cho field”, nhưng cần có `this` để phân biệt hai biến.

Constructor trong ví dụ nhận đủ bốn tham số. Một class cũng có thể khai báo constructor không tham số:

```java
public Product() {
}
```

Nếu class không khai báo constructor nào, compiler tự cung cấp một constructor không tham số mặc định. Khi đã tự viết `Product(String id, String name, double price, int stock)`, compiler không tạo thêm `Product()`; muốn hỗ trợ cả hai cách khởi tạo thì phải khai báo cả hai constructor. Trong bài thực hành chính, ta dùng constructor bốn tham số để mọi `Product` có đủ dữ liệu ngay khi được tạo.

Nếu không override, `toString()` kế thừa từ `Object` thường cho kết quả khó đọc như `model.Product@1a2b3c`. Phiên bản override ở trên trả về chuỗi biểu diễn có ý nghĩa khi in object. Không dùng `toString()` làm nơi tính tổng hoặc thay đổi stock; method này chỉ mô tả object.

## III. Customer Và Trách Nhiệm Của Model

Tạo Customer theo cùng cách:

```java
package model;

public class Customer {
    private String id;
    private String name;
    private String phone;

    public Customer(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return id + " | " + name + " | " + phone;
    }
}
```

Getter và setter cho phép code bên ngoài đọc hoặc cập nhật field `private` thông qua các method `public` do class cung cấp. Không có `setId()` vì mã định danh không nên bị đổi tùy ý sau khi object đã được tạo.

Trong chương trình, `Customer` biểu diễn người đang sở hữu giỏ hàng. Khi in giỏ, ta sẽ in `currentCustomer` trước các dòng hàng; ở buổi 4, chính object này sẽ được đưa vào `Order`.

## IV. HAS-A Với CartItem

Không đưa `quantity` vào Product. Stock mô tả tồn kho; quantity mô tả số lượng một khách đang mua. Hai dữ liệu có vòng đời khác nhau.

```java
package model;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal() {
        return product.getPrice() * quantity;
    }
}
```

`CartItem` **HAS-A** `Product`: một object `CartItem` giữ tham chiếu đến một object `Product`. `getTotal()` hỏi `Product` về giá qua getter; nó không truy cập trực tiếp field `private`.

### Thí nghiệm về tham chiếu

```java
Product p = new Product("SP01", "Sổ tay", 25000, 50);
CartItem item = new CartItem(p, 2);

System.out.println(item.getTotal()); // 50000
p.setPrice(30000);
System.out.println(item.getTotal()); // 60000
```

CartItem không giữ một bản sao Product; nó giữ tham chiếu tới cùng object `p`.

## V. Refactor Giỏ Hàng Và Giữ Các Chức Năng Cũ

Trong `HitShop.java`, import các model và kiểu danh sách:

```java
import model.CartItem;
import model.Customer;
import model.Product;

import java.util.ArrayList;
import java.util.List;
```

Thay ba list song song bằng một list và giữ một khách hàng hiện tại:

```java
private static final List<CartItem> cart = new ArrayList<>();
private static final Customer currentCustomer =
        new Customer("C01", "Linh Nguyễn", "0900000001");
```

`final` ở đây ngăn biến `cart` trỏ sang một list khác; nội dung list vẫn có thể được thêm hoặc xóa.

Các method cũ vẫn giữ mục đích:

```java
static void addItem(Product product, int quantity) {
    cart.add(new CartItem(product, quantity));
}

static void removeItem(int index) {
    if (index < 0 || index >= cart.size()) {
        System.out.println("Không tìm thấy mặt hàng.");
        return;
    }
    cart.remove(index);
}

static void printItems() {
    System.out.println("Khách hàng: " + currentCustomer);

    if (cart.isEmpty()) {
        System.out.println("Giỏ hàng đang trống.");
        return;
    }

    for (int i = 0; i < cart.size(); i++) {
        CartItem item = cart.get(i);
        Product product = item.getProduct();
        System.out.printf("%d. %s | %s | %.1f | SL: %d | Thành tiền: %.1f%n",
                i + 1,
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                item.getTotal());
    }
}

static double calculateTotal() {
    double total = 0;
    for (CartItem item : cart) {
        total += item.getTotal();
    }
    return total;
}
```

Phần menu gần như không đổi. Khi người dùng chọn xóa dòng số `n`, chương trình vẫn gọi `removeItem(n - 1)` vì số thứ tự hiển thị bắt đầu từ 1 còn index bắt đầu từ 0. Khi thêm, phần đọc input chọn một `Product` có sẵn rồi gọi `addItem(product, quantity)`. Quá trình refactor thay cách tổ chức dữ liệu bên trong nhưng giữ các chức năng thêm, xóa, xem giỏ và tính tổng của buổi 2.

Trong phạm vi buổi này, mỗi lần thêm tạo một `CartItem` mới; chưa gộp hai dòng cùng sản phẩm. `quantity` phải là số nguyên dương, nhưng chương trình chưa kiểm tra hoặc trừ `stock`. Quy tắc bảo vệ và thay đổi tồn kho được để sang buổi 4.

## VI. Thực Hành Có Hướng Dẫn

1. Tạo hai `Product` `SP01`, `SP02` và một `Customer` `KH01` bằng dữ liệu tự chọn.
2. In ba object để kiểm tra constructor và `toString()`.
3. Thêm SP01 × 2, SP02 × 1 vào `List<CartItem>`.
4. In từng dòng và tự đối chiếu tổng bằng phép tính tay.
5. Đổi quantity SP01 từ 2 thành 3, chạy lại và giải thích vì sao stock chưa đổi.
6. Nối list mới vào menu của buổi 2; giữ phép đổi số thứ tự xóa thành index bằng `displayNumber - 1`.

Sau mỗi bước, chương trình phải chạy được. Không nên đợi đến khi viết xong cả ba class mới chạy chương trình lần đầu, vì khi đó lỗi package, constructor và logic dễ bị trộn lẫn.

## Lỗi thường gặp

| Lỗi | Nguyên nhân | Cách kiểm tra |
|---|---|---|
| Không import được model | Sai package hoặc sai thư mục | Đối chiếu dòng `package model;` với cấu trúc source |
| `getTotal()` không biên dịch | Truy cập `product.price` khi price private | Dùng `product.getPrice()` |
| In object ra chuỗi khó đọc | Chưa override `toString()` | Thêm method và annotation `@Override` |
| Đổi quantity làm đổi stock | Gộp hai trách nhiệm | Chỉ CartItem giữ quantity; Product giữ stock |
| Tổng vẫn sai sau refactor | Menu còn dùng list cũ | Lần theo luồng từ lựa chọn menu tới `List<CartItem>` |
| Xóa sai dòng | Truyền số thứ tự 1-based trực tiếp làm index | Gọi `removeItem(displayNumber - 1)` |

## Tổng kết buổi 3

Class gom trạng thái và hành vi liên quan; object là một thể hiện cụ thể của class. `Product` mô tả hàng hóa, `Customer` mô tả người mua, `CartItem` nối `Product` với số lượng mua. Quan hệ HAS-A làm mô hình biểu đạt đúng bài toán và loại bỏ rủi ro lệch ba danh sách song song. Chương trình vẫn chưa bảo vệ giá và tồn kho trước mọi giá trị sai; phần đóng gói có quy tắc đó sẽ được phát triển ở buổi 4.

### Câu hỏi chốt buổi

1. Vì sao số lượng mua không nên được lưu trong field của `Product`?
2. `this.name` và tham số `name` khác nhau như thế nào?
3. `CartItem` giữ một bản sao của `Product` hay giữ tham chiếu tới đối tượng `Product`?
4. Khi refactor dữ liệu giỏ hàng thành object, những hành vi nào của chương trình buổi 2 được giữ nguyên?

## Bài tập nhỏ trên lớp — Đăng ký khóa học

Tạo hai class độc lập với ví dụ trong bài:

- `Course`: `code`, `name`, `fee`; field private, constructor, getter và `toString()`.
- `Enrollment`: giữ một `Course` và `studentName`; có `getSummary()` trả thông tin đăng ký.
- Trong class demo, tạo hai Course và hai Enrollment rồi lưu vào `List<Enrollment>`.
- In toàn bộ đăng ký và tính tổng học phí bằng getter của Course.

**Nghiệm thu:** học viên phải chỉ ra được quan hệ Enrollment HAS-A Course và không lưu trùng `courseName`/`fee` trong Enrollment. Thời gian làm khoảng 15 phút.

## Nâng Cao — Thành Viên `static` Và Phạm Vi Truy Cập

Phần này là tự chọn. Field thông thường thuộc về từng object; field `static` thuộc về class và được dùng chung cho mọi object. Ví dụ đếm số `Product` đã tạo:

```java
private static int count;

public Product(String id, String name, double price, int stock) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.stock = stock;
    count++;
}

public static int getCount() {
    return count;
}
```

Gọi `Product.getCount()` qua tên class. Không gọi qua một object vì `count` không thuộc riêng object nào.

| Access modifier | Trong cùng class | Cùng package | Class con khác package | Nơi khác |
|---|---:|---:|---:|---:|
| `private` | Có | Không | Không | Không |
| default — không ghi modifier | Có | Có | Không | Không |
| `protected` | Có | Có | Có, qua quan hệ kế thừa | Không |
| `public` | Có | Có | Có | Có |

Một package con vẫn là package khác. Với class con ở package khác, quyền `protected` được dùng trong ngữ cảnh kế thừa, không có nghĩa class con có thể truy cập field đó qua mọi object của class cha. Trong bài bắt buộc, field của model để `private`, còn constructor, getter, setter và các hành vi cần gọi từ `HitShop` để `public`. Cách class con sử dụng `protected` sẽ được đặt trong ngữ cảnh kế thừa ở buổi 4.

## Bài tập về nhà

Hoàn thành [BTVN buổi 03](buoi-03-btvn.md) và giữ lại toàn bộ source để mở rộng ở buổi 4.
