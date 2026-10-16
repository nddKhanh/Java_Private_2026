# Buổi 3: OOP Nhập Môn — Class, Object, Constructor, Getter/Setter và HAS-A

---

## Mục tiêu buổi học

- Hiểu vì sao danh sách song song khó quản lý và class/object giải quyết điều đó ra sao.
- Viết class có field, method, constructor, getter/setter và toString().
- Phân biệt class, object và instance; hiểu biến object là tham chiếu.
- Dùng package, this và field private trong model.
- Tạo Book, Reader, Loan; biểu diễn Loan liên kết một Reader với một Book.

> **Cầu nối:** Buổi 2 đã dùng danh sách để lưu nhiều giá trị, nhưng các dữ liệu liên quan vẫn có thể bị tách rời và khó quản lý cùng nhau. Class/object cung cấp cách mô tả một khái niệm bằng trạng thái và hành vi gắn với nhau. Buổi này tập trung vào nền tảng đó; ví dụ được chọn độc lập theo chủ đề.

## I. Vấn Đề Của Danh Sách Song Song

Có thể lưu tên môn, điểm số và số tín chỉ ở ba list riêng. Khi xóa một môn, phải xóa đúng index trong cả ba list; bỏ sót một list làm điểm số bị ghép với sai tên môn. Thêm thuộc tính mới cũng buộc phải đồng bộ nhiều cấu trúc.

~~~java
subjectNames.remove(index);
scores.remove(index);
credits.remove(index);
~~~

Thay vì truyền ba list và index khắp chương trình, ta muốn thao tác trên một object `Course` chứa tên môn, điểm số và số tín chỉ liên quan.

Đây là ví dụ dẫn nhập từ dữ liệu song song. Cùng vấn đề xuất hiện ở nhiều lĩnh vực: thông tin sách đi cùng mã sách, hoặc tọa độ x phải đi cùng tọa độ y. Phần cốt lõi là cách class/object gom các giá trị mô tả cùng một khái niệm.

## II. OOP Và Package

OOP tổ chức chương trình quanh object, mỗi object gắn dữ liệu và hành vi liên quan. Package giúp nhóm class và tránh để toàn bộ source trong một chỗ.

~~~text
src/edu/example/library/
  Main.java
  model/
    Book.java
    Reader.java
    Loan.java
~~~

Khai báo package ở đầu file, ví dụ `package edu.example.library.model;`. Main import model từ package khác. Chữ thường, không dấu, thường dùng tên miền đảo ngược.

## III. Class, Object, Field Và Method

- Class là kiểu do người lập trình định nghĩa: nó khai báo mỗi object có dữ liệu nào (field) và có thể thực hiện hành vi nào (method). Class không phải object; nó là mô tả để Java tạo object.
- `new Book(...)` tạo một object cụ thể trong bộ nhớ. Object đó là một instance của class `Book`; mỗi instance có trạng thái field riêng.
- Method instance được gọi trên một object cụ thể; bên trong method, `this` chỉ object đang nhận lời gọi. Một method có thể dùng/đổi field của object đó, nhưng không nên gộp mọi trách nhiệm chương trình vào một class.
- Biến có kiểu class lưu một tham chiếu để truy cập object, chứ không chứa bản sao độc lập của toàn bộ object. Gán `Book second = first;` sao chép tham chiếu, nên sửa object thông qua `second` cũng quan sát được qua `first`. Muốn tạo object khác phải gọi `new`; phép gán không tự sao chép.
- Field kiểu số/boolean nhận giá trị mặc định khi là field và chưa được gán; field kiểu tham chiếu có thể là `null`. Biến cục bộ trong method không tự được khởi tạo mặc định. Gọi method hoặc đọc field qua `null` gây `NullPointerException`.

~~~java
public class Book {
    private String id;
    private String name;
    private String author;
    private int pages;

    public Book(String id, String name, String author, int pages) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.pages = pages;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public int getPages() { return pages; }

    public void setName(String name) { this.name = name; }
    public void setAuthor(String author) { this.author = author; }

    public void printInfo() {
        System.out.println(id + " - " + name + " - " + author + " - " + pages + " trang");
    }
}
~~~

Không thể gọi instance method `printInfo()` bằng class nếu method không khai báo `static`; cần có object cụ thể như `book.printInfo()`. `static` cấp class là phần nâng cao theo lộ trình, nên buổi này chỉ phân biệt method instance với điểm vào `main` ở mức cần thiết.

~~~text
Class Book (bản mô tả)         Objects (các instance)
  id, name, author, pages    b1 ───> Book{B01, Dế Mèn, ...}
  getName(), printInfo()     b2 ───┘  (b1 và b2 cùng tham chiếu object)
                              b3 ───> Book{B02, ...}
~~~

Mỗi object có trạng thái riêng; chỉ phép gán tham chiếu mới làm hai biến cùng trỏ một object. Gọi `new Book(...)` tạo instance mới.

## IV. Constructor, Getter/Setter Và this

Constructor khởi tạo trạng thái ban đầu khi tạo object. Constructor có tên trùng class, không khai báo kiểu trả về và được gọi cùng `new`. Nếu không khai báo constructor nào, compiler cung cấp constructor mặc định không tham số. Một khi đã khai báo bất kỳ constructor nào, compiler không tự thêm constructor đó nữa; nếu vẫn cần tạo object chưa có dữ liệu, phải tự viết constructor không tham số. Nhiều constructor cùng tên nhưng khác danh sách tham số là constructor overloading.

~~~java
public Book(String id, String name, String author, int pages) {
    this.id = id;
    this.name = name;
    this.author = author;
    this.pages = pages;
}
~~~

Ví dụ constructor không tham số do lập trình viên tự viết:

~~~java
public Book() {
    this("UNKNOWN", "Chưa đặt tên", "Chưa rõ", 0);
}
~~~

Constructor này gọi constructor có tham số để tránh lặp lại logic khởi tạo. Dùng no-arg khi cần tạo object trước rồi điền dữ liệu sau; nếu object phải luôn hợp lệ ngay từ lúc sinh ra, constructor nhận đủ dữ liệu thường an toàn hơn.

`this.name` là field của object hiện tại; `name` là tham số. Getter đọc field; setter cho phép cập nhật có kiểm soát. Bài chính dùng quy ước field private, method cần gọi từ ngoài public. Các mức access modifier khác là phần nâng cao.

Getter và setter là method truy cập/cập nhật trạng thái. Không phải field nào cũng bắt buộc có setter: nếu một giá trị không nên đổi sau khi khởi tạo, có thể chỉ cung cấp getter. Việc đặt field `private` giúp class làm chủ trạng thái; buổi 4 sẽ đào sâu cách giữ các điều kiện hợp lệ của object.

## V. toString() Và HAS-A

`toString()` được kế thừa từ `Object`; phiên bản mặc định thường in tên class cùng biểu diễn mã băm, không phải bản mô tả dành cho người dùng. Override method này để cung cấp biểu diễn dễ đọc khi debug hoặc in object. Không dùng nội dung `toString()` thay cho định danh dữ liệu hoặc định dạng giao diện ổn định.

Quan hệ HAS-A nghĩa một object giữ tham chiếu tới object khác như một field. Nó mô tả quan hệ giữa các thành phần, không phải quan hệ kế thừa. Hãy đặt dữ liệu vào object mà nó mô tả: thời hạn mượn thuộc một lượt `Loan`, không phải thuộc `Book` nói chung.

~~~java
public class Loan {
    private Book book;
    private Reader reader;
    private int days;

    public Loan(Book book, Reader reader, int days) {
        this.book = book;
        this.reader = reader;
        this.days = days;
    }

    public Book getBook() { return book; }
    public Reader getReader() { return reader; }
    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    @Override
    public String toString() {
        return reader.getName() + " mượn " + book.getName() + " trong " + days + " ngày";
    }
}
~~~

`Loan` giữ tham chiếu tới một `Book`, một `Reader` và thời hạn mượn. Ta dùng HAS-A vì một lượt mượn liên kết hai object; không dùng kế thừa vì Loan không phải là một loại Book hay Reader. Dữ liệu `days` thuộc về từng lượt mượn, không thuộc riêng về sách hoặc bạn đọc.

Trong ví dụ `Loan`, `@Override` yêu cầu compiler xác nhận method `toString()` đang ghi đè đúng method có sẵn của `Object`. Nếu viết sai tên/chữ ký, compiler báo lỗi ngay thay vì âm thầm tạo một method khác.

Trong miền thư viện, `Book` mô tả sách, `Reader` mô tả bạn đọc, còn `Loan` giữ quan hệ giữa bạn đọc và sách đang mượn. Mỗi class có thể được lưu trong file riêng; tên file phải trùng với class public.

~~~java
public class Reader {
    private String id;
    private String name;

    public Reader(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
~~~

## VI. Ghép Các Object Thành Một Mô Hình

Đây là ví dụ tổng hợp độc lập về các khái niệm vừa học.

~~~java
import java.util.ArrayList;
import java.util.List;

// Đặt trong main sau khi đã khai báo Book và Loan.
Book book = new Book("B01", "Dế Mèn phiêu lưu ký", "Tô Hoài", 224);
Reader reader = new Reader("R01", "Lan");
Loan loan = new Loan(book, reader, 14);
List<Loan> loans = new ArrayList<>();
loans.add(loan);
System.out.println(loan); // Lan mượn Dế Mèn phiêu lưu ký trong 14 ngày
~~~

Trong ví dụ này `book` tham chiếu tới một `Book`; `loan` là object riêng giữ tham chiếu tới cùng cuốn sách. `loans` lưu các lượt mượn. Khi `System.out.println(loan)` chạy, Java gọi `loan.toString()` nên output được xác định bởi method vừa viết.

Thay các danh sách song song bằng `ArrayList<Loan>`: mỗi phần tử có kiểu rõ ràng và chứa những dữ liệu liên quan của một lượt mượn.

### Bài Luyện Tập Khái Niệm

1. Mô tả bằng class `Book` các field và method phù hợp; sau đó tạo hai object có dữ liệu khác nhau.
2. Giải thích bằng hình vẽ hoặc lời giải thích vì sao `Book second = first;` chưa tạo bản sao object.
3. Viết constructor không tham số và constructor có tham số; nêu constructor nào được compiler tạo tự động trong từng trường hợp.
4. Chọn field nào chỉ cần getter, field nào cho phép setter; giải thích theo trạng thái cần bảo vệ.
5. Override `toString()` cho một class dữ liệu nhỏ và phân biệt mục đích của nó với `equals()` (sẽ học ở buổi 5).

## Phần Nâng Cao — Tìm Hiểu Thêm

- `static` thuộc về class thay vì từng object; các object dùng chung một thành viên static. Có thể dùng bộ đếm để hiểu vòng đời, nhưng cần lưu ý đây là trạng thái dùng chung và mọi object cùng ảnh hưởng giá trị.
- Access modifier: `private` chỉ trong class, không ghi modifier là package-private, `protected` cho package và lớp con theo quy tắc truy cập Java, `public` cho nơi gọi nhìn thấy. Ưu tiên mức truy cập hẹp nhất đáp ứng nhu cầu.

**Bài tập tự chọn:** Tạo bộ đếm static số instance đã tạo, thử truy cập từ object và từ class; đặt các class vào hai package để quan sát phạm vi truy cập. Đây là bài khám phá, không phải yêu cầu bắt buộc của mô hình cơ bản.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| field không truy cập được | field private | Dùng getter/setter |
| field vẫn null/0 | constructor chưa gán field | Dùng this.field = thamSo |
| new Book() không compile | Chỉ có constructor có tham số | Gọi đúng constructor hoặc tự viết no-arg |
| in ra tên class và mã lạ | Chưa override toString() | Viết toString() |

## Tổng Kết Buổi 3

- Class mô tả dữ liệu/hành vi; object là instance, có trạng thái riêng và có thể được tham chiếu từ nhiều biến.
- Constructor khởi tạo trạng thái; getter/setter kiểm soát truy cập; `this` phân biệt field và tham số; `toString()` cung cấp biểu diễn dễ đọc.
- HAS-A mô tả object giữ tham chiếu tới object khác; Loan/Book là ví dụ minh họa.
- Giới hạn tiếp theo: khi cùng một nhóm kiểu có phần chung nhưng hành vi khác nhau, cần cách mô hình hóa biến thể và giữ trạng thái hợp lệ; đó là trọng tâm buổi 4.

### Câu Hỏi Ôn Tập

1. Phân biệt class, object, instance.
2. Vì sao danh sách Loan tránh phải đồng bộ nhiều danh sách song song?
3. this.field khác tham số cùng tên thế nào?
4. Java tự thêm constructor không tham số khi nào?
5. HAS-A biểu diễn điều gì? Vì sao số ngày mượn thuộc Loan thay vì Book?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 03](../BTVN/buoi-03-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Classes and Objects](https://docs.oracle.com/javase/tutorial/java/javaOO/index.html)
- [Oracle — Constructors](https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html)
- [Oracle — Packages](https://docs.oracle.com/javase/tutorial/java/package/index.html)
