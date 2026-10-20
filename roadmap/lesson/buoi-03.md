# Buổi 3: OOP Nhập Môn Qua Product, Customer Và CartItem

---

> **Hạn chế từ buổi 2:** nhiều danh sách song song phải luôn cùng index và cùng kích thước; chỉ cần quên cập nhật một danh sách là dữ liệu bị ghép sai.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Giải thích vì sao ba danh sách song song của buổi 2 dễ lệch dữ liệu.
- Tạo class, object, field, constructor, getter/setter và `toString()`.
- Dùng quan hệ HAS-A để một CartItem giữ một Product.
- Refactor giỏ hàng sang `List<CartItem>` mà vẫn giữ hành vi thêm, xóa, in và tính tổng.
- Phân biệt biến tham chiếu với object và quan sát việc hai biến cùng trỏ một object.

> **Kết quả cuối buổi:** chương trình mẫu dùng Product, Customer và CartItem thay cho ba danh sách song song.

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

OOP giúp gom dữ liệu liên quan và hành vi của nó vào object. Đây là một cách làm mô hình dễ đọc và khó bị lệch, không chỉ là đổi cú pháp.

### Kiểm tra nhanh

Giáo viên cố ý xóa phần tử index 0 chỉ khỏi `names`, sau đó yêu cầu học viên dự đoán output. Học viên cần chỉ ra được dữ liệu nào đã ghép sai trước khi chuyển sang class.

## II. Class Product: Từ Bản Thiết Kế Đến Object

Tạo package `model` và file `Product.java`:

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

Trong constructor, `this.name` là field của object hiện tại; `name` là tham số được truyền vào. Việc dùng cùng tên giúp đọc rõ “gán dữ liệu đầu vào cho field”, nhưng phải có `this` để phân biệt.

`toString()` trả một biểu diễn hữu ích khi in object. Không dùng `toString()` làm nơi tính tổng hoặc thay đổi stock; method này chỉ mô tả object.

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

Ở giai đoạn này, getter/setter giúp code bên ngoài làm việc với field private. Buổi 4 sẽ quay lại câu hỏi quan trọng hơn: setter nào thực sự nên tồn tại và quy tắc nào phải được bảo vệ.

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

CartItem **HAS-A** Product: object này giữ tham chiếu tới object kia. `getTotal()` hỏi Product về giá qua getter; không truy cập field private trực tiếp.

### Thí nghiệm về tham chiếu

```java
Product p = new Product("SP01", "Sổ tay", 25000, 50);
CartItem item = new CartItem(p, 2);

System.out.println(item.getTotal()); // 50000
p.setPrice(30000);
System.out.println(item.getTotal()); // 60000
```

CartItem không giữ một bản sao Product; nó giữ tham chiếu tới cùng object `p`. Đây là điều cần hiểu trước khi sang buổi 9, nơi OrderItem phải chốt `unitPrice` riêng để giá lịch sử không đổi.

## V. Refactor Giỏ Hàng Mà Không Đổi Hành Vi

Thay ba list bằng một list:

```java
private static final List<CartItem> cart = new ArrayList<>();
```

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
    for (int i = 0; i < cart.size(); i++) {
        CartItem item = cart.get(i);
        System.out.printf("%d. %s | SL: %d | Thành tiền: %.1f%n",
                i + 1,
                item.getProduct().getName(),
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

Điểm cần quan sát là phần menu gần như không đổi. Refactor tốt thay cách tổ chức bên trong nhưng giữ kết quả người dùng đang nhận.

## VI. Thực Hành Có Hướng Dẫn

1. Tạo hai Product `SP01`, `SP02` và một Customer `KH01` bằng dữ liệu tự chọn.
2. In ba object để kiểm tra constructor và `toString()`.
3. Thêm SP01 × 2, SP02 × 1 vào `List<CartItem>`.
4. In từng dòng và tự đối chiếu tổng bằng phép tính tay.
5. Đổi quantity SP01 từ 2 thành 3, chạy lại và giải thích vì sao stock chưa đổi.
6. Nối list mới vào menu của buổi 2.

Sau mỗi bước, chương trình phải chạy được. Không viết xong cả ba class mới chạy lần đầu, vì khi đó lỗi package, constructor và logic bị trộn vào nhau.

## Lỗi thường gặp

| Lỗi | Nguyên nhân | Cách kiểm tra |
|---|---|---|
| Không import được model | Sai package hoặc sai thư mục | Đối chiếu dòng `package model;` với cấu trúc source |
| `getTotal()` không biên dịch | Truy cập `product.price` khi price private | Dùng `product.getPrice()` |
| In object ra chuỗi khó đọc | Chưa override `toString()` | Thêm method và annotation `@Override` |
| Đổi quantity làm đổi stock | Gộp hai trách nhiệm | Chỉ CartItem giữ quantity; Product giữ stock |
| Tổng vẫn sai sau refactor | Menu còn dùng list cũ | Lần theo luồng từ lựa chọn menu tới `List<CartItem>` |

## Tổng kết buổi 3

Class gom trạng thái và hành vi liên quan; object là một thể hiện cụ thể của class. Product mô tả hàng hóa, Customer mô tả người mua, CartItem nối Product với số lượng mua. Quan hệ HAS-A làm mô hình biểu đạt đúng bài toán và loại bỏ rủi ro lệch ba danh sách song song.

### Câu hỏi chốt buổi

1. Vì sao quantity không nên là field dùng chung của Product?
2. `this.name` và tham số `name` khác nhau thế nào?
3. CartItem giữ bản sao Product hay tham chiếu tới Product?
4. Refactor thành object đã giữ lại hành vi nào của buổi 2?

## Bài tập nhỏ trên lớp — Đăng ký khóa học

Tạo hai class độc lập với ví dụ trong bài:

- `Course`: `code`, `name`, `fee`; field private, constructor, getter và `toString()`.
- `Enrollment`: giữ một `Course` và `studentName`; có `getSummary()` trả thông tin đăng ký.
- Trong class demo, tạo hai Course và hai Enrollment rồi lưu vào `List<Enrollment>`.
- In toàn bộ đăng ký và tính tổng học phí bằng getter của Course.

**Nghiệm thu:** học viên phải chỉ ra được quan hệ Enrollment HAS-A Course và không lưu trùng `courseName`/`fee` trong Enrollment. Thời gian làm khoảng 15 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 03](../BTVN/buoi-03-btvn.md) và giữ lại toàn bộ source để mở rộng ở buổi 4.
