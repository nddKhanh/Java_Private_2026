# Buổi 3: OOP Nhập Môn — Class, Object, Constructor, Getter/Setter và HAS-A

---

## Mục tiêu buổi học

- Hiểu vì sao danh sách song song khó quản lý và class/object giải quyết điều đó ra sao.
- Viết class có field, method, constructor, getter/setter và toString().
- Phân biệt class, object và instance; hiểu biến object là tham chiếu.
- Dùng package, this và field private trong model.
- Tạo Product, Customer, CartItem; biểu diễn CartItem có một Product.

> **Nối tiếp kiến thức:** Buổi 2 đã học kiểu tham chiếu và danh sách có thứ tự. Buổi này học mô hình OOP cơ bản: dùng class để mô tả một kiểu dữ liệu có trạng thái và hành vi, tạo object cụ thể từ class, và biểu diễn quan hệ giữa các object. Kế thừa/đa hình để buổi 4; abstract class, interface, equals()/hashCode() buổi 5; exception buổi 6.

## I. Vấn Đề Của Danh Sách Song Song

Buổi 2 dùng ba list: tên, giá và số lượng. Khi xóa một sản phẩm phải xóa đúng index ở cả ba list; bỏ sót một list làm tên hàng đi kèm giá của hàng khác. Thêm một thuộc tính cũng kéo theo sửa nhiều đoạn code.

~~~java
names.remove(index);
prices.remove(index);
quantities.remove(index);
~~~

Thay vì truyền ba list và index khắp chương trình, ta muốn truyền một sản phẩm duy nhất.

Đây là ví dụ dẫn nhập từ dữ liệu song song. Cùng vấn đề xuất hiện ở nhiều lĩnh vực: điểm số phải đi cùng môn học, thông tin sách đi cùng mã sách, hoặc tọa độ x phải đi cùng tọa độ y. Phần cốt lõi của buổi học là cách class/object mô hình hóa các khái niệm này.

## II. OOP Và Package

OOP tổ chức chương trình quanh object, mỗi object gắn dữ liệu và hành vi liên quan. Package giúp nhóm class và tránh để toàn bộ source trong một chỗ.

~~~text
src/com/hit/shop/
  Main.java
  model/
    Product.java
    Customer.java
    CartItem.java
~~~

Khai báo package ở đầu file, ví dụ `package com.hit.shop.model;`. Main import model từ package khác. Chữ thường, không dấu, thường dùng tên miền đảo ngược.

## III. Class, Object, Field Và Method

- Class là kiểu do người lập trình định nghĩa: nó khai báo mỗi object có dữ liệu nào (field) và có thể thực hiện hành vi nào (method). Class không phải object; nó là mô tả để Java tạo object.
- `new Product(...)` tạo một object cụ thể trong bộ nhớ. Object đó là một instance của class `Product`; mỗi instance có trạng thái field riêng.
- Method instance được gọi trên một object cụ thể; bên trong method, `this` chỉ object đang nhận lời gọi. Một method có thể dùng/đổi field của object đó, nhưng không nên gộp mọi trách nhiệm chương trình vào một class.
- Biến có kiểu class lưu một tham chiếu để truy cập object, chứ không chứa bản sao độc lập của toàn bộ object. Gán `Product b = a;` sao chép tham chiếu, nên sửa object thông qua `b` cũng quan sát được qua `a`. Muốn tạo object khác phải gọi `new`; phép gán không tự sao chép.
- Field kiểu số/boolean nhận giá trị mặc định khi là field và chưa được gán; field kiểu tham chiếu có thể là `null`. Biến cục bộ trong method không tự được khởi tạo mặc định. Gọi method hoặc đọc field qua `null` gây `NullPointerException`.

~~~java
public class Product {
    private String name;
    private double price;
    private int stock;

    public void printInfo() {
        System.out.println(name + " - " + price + " - tồn: " + stock);
    }
}
~~~

Không thể gọi instance method `printInfo()` bằng class nếu method không khai báo `static`; cần có object cụ thể như `product.printInfo()`. `static` cấp class là phần nâng cao theo lộ trình, nên buổi này chỉ phân biệt method instance với điểm vào `main` ở mức cần thiết.

> **Hình minh họa cần bổ sung:** Sơ đồ một class `Product` với các field/method và hai object riêng biệt; minh họa hai biến tham chiếu cùng trỏ một object sau phép gán.

## IV. Constructor, Getter/Setter Và this

Constructor khởi tạo trạng thái ban đầu khi tạo object. Constructor có tên trùng class, không khai báo kiểu trả về và được gọi cùng `new`. Nếu chưa khai báo constructor nào, compiler tạo constructor không tham số mặc định; khi đã tự khai báo một constructor, compiler không tự thêm constructor không tham số nữa. Có thể tự viết cả constructor không tham số và constructor có tham số nếu bài toán cần.

~~~java
public Product(String name, double price, int stock) {
    this.name = name;
    this.price = price;
    this.stock = stock;
}
~~~

`this.name` là field của object hiện tại; `name` là tham số. Getter đọc field; setter cho phép cập nhật có kiểm soát. Bài chính dùng quy ước field private, method cần gọi từ ngoài public. Các mức access modifier khác là phần nâng cao.

Getter và setter là method truy cập/cập nhật trạng thái. Không phải field nào cũng bắt buộc có setter: nếu một giá trị không nên đổi sau khi khởi tạo, có thể chỉ cung cấp getter. Việc đặt field `private` giúp class làm chủ trạng thái; buổi 4 sẽ đào sâu cách giữ các điều kiện hợp lệ của object.

## V. toString() Và HAS-A

`toString()` được kế thừa từ `Object`; phiên bản mặc định thường in tên class cùng biểu diễn mã băm, không phải bản mô tả dành cho người dùng. Override method này để cung cấp biểu diễn dễ đọc khi debug hoặc in object. Không dùng nội dung `toString()` thay cho định danh dữ liệu hoặc định dạng giao diện ổn định.

Quan hệ HAS-A nghĩa một object giữ tham chiếu tới object khác như một field. Nó mô tả quan hệ giữa các thành phần, không phải quan hệ kế thừa. Hãy đặt dữ liệu vào object mà nó mô tả: số lượng thuộc một dòng `CartItem`, không phải thuộc `Product` nói chung.

~~~java
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }
}
~~~

Ví dụ override:

~~~java
@Override
public String toString() {
    return name + " - " + price;
}
~~~

Đoạn trên được đặt bên trong class có field `name` và `price`; `@Override` yêu cầu compiler kiểm tra method đang ghi đè đúng chữ ký của method cha.

Trong project, Product giữ mã, tên, giá, tồn kho; Customer giữ thông tin khách; CartItem giữ product và quantity. Chọn mã thủ công ở buổi này, chưa cần ID tự tăng.

## VI. Ghép Thành Giỏ Hàng

Đây là ví dụ ứng dụng ngắn của các khái niệm vừa học; bài học không phụ thuộc vào việc hoàn thiện project.

~~~java
Product shirt = new Product("P01", "Áo thun", 150000, 50);
CartItem item = new CartItem(shirt, 2);
List<CartItem> cart = new ArrayList<>();
cart.add(item);
System.out.println(item); // Áo thun x 2 = 300000.0
~~~

Thay danh sách song song bằng `ArrayList<CartItem>`: xóa một phần tử là xóa một object chứa cả sản phẩm và số lượng.

### Bài Luyện Tập Khái Niệm

1. Mô tả bằng class `Book` các field và method phù hợp; sau đó tạo hai object có dữ liệu khác nhau.
2. Giải thích bằng hình vẽ hoặc lời giải thích vì sao `Book second = first;` chưa tạo bản sao object.
3. Viết constructor không tham số và constructor có tham số; nêu constructor nào được compiler tạo tự động trong từng trường hợp.
4. Chọn field nào chỉ cần getter, field nào cho phép setter; giải thích theo trạng thái cần bảo vệ.
5. Override `toString()` cho một class dữ liệu nhỏ và phân biệt mục đích của nó với `equals()` (sẽ học ở buổi 5).

## Phần Nâng Cao — Tìm Hiểu Thêm

- `static` thuộc về class thay vì từng object; các object dùng chung một thành viên static. Có thể dùng bộ đếm để hiểu vòng đời, nhưng cần lưu ý đây là trạng thái dùng chung và mọi object cùng ảnh hưởng giá trị.
- Access modifier: `private` chỉ trong class, không ghi modifier là package-private, `protected` cho package và lớp con theo quy tắc truy cập Java, `public` cho nơi gọi nhìn thấy. Ưu tiên mức truy cập hẹp nhất đáp ứng nhu cầu.

**Bài tập tự chọn:** Tạo bộ đếm static số instance đã tạo, thử truy cập từ object và từ class; đặt các class vào hai package để quan sát phạm vi truy cập. Đây là bài khám phá, không làm thay đổi yêu cầu project cốt lõi.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| field không truy cập được | field private | Dùng getter/setter |
| field vẫn null/0 | constructor chưa gán field | Dùng this.field = thamSo |
| new Product() không compile | Chỉ có constructor có tham số | Gọi đúng constructor hoặc tự viết no-arg |
| in ra tên class và mã lạ | Chưa override toString() | Viết toString() |

## Tổng Kết Buổi 3

- Class mô tả dữ liệu/hành vi; object là instance, có trạng thái riêng và có thể được tham chiếu từ nhiều biến.
- Constructor khởi tạo trạng thái; getter/setter kiểm soát truy cập; `this` phân biệt field và tham số; `toString()` cung cấp biểu diễn dễ đọc.
- HAS-A mô tả object chứa object khác; Product/CartItem là ví dụ ứng dụng các khái niệm này.
- Giới hạn tiếp theo: nhiều loại sản phẩm làm logic if/else tăng; dữ liệu cần được kiểm soát sâu hơn.

### Câu Hỏi Ôn Tập

1. Phân biệt class, object, instance.
2. Vì sao xóa object trong CartItem list tránh lệch các list?
3. this.field khác tham số cùng tên thế nào?
4. Java tự thêm constructor không tham số khi nào?
5. HAS-A biểu diễn điều gì? Vì sao quantity đặt ở CartItem?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 03](../BTVN/buoi-03-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Classes and Objects](https://docs.oracle.com/javase/tutorial/java/javaOO/index.html)
- [Oracle — Constructors](https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html)
- [Oracle — Packages](https://docs.oracle.com/javase/tutorial/java/package/index.html)
