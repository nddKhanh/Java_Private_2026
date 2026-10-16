# Buổi 1: Làm Quen Java — Môi Trường, Chương Trình Đầu Tiên và Nền Tảng Cú Pháp

---

## Mục tiêu buổi học

- Chạy được chương trình Java trong IntelliJ IDEA sau khi cài JDK.
- Giải thích cấu trúc class, method main và luồng biên dịch/chạy Java ở mức nhập môn.
- Nhập dữ liệu bằng Scanner, xuất bằng System.out.println; chọn kiểu dữ liệu, biến, hằng số và toán tử phù hợp.
- Biết tạo repository, commit và push bài tập lên GitHub.

> **Mở đầu khóa học:** Giới thiệu mục tiêu và cách học; làm bài thử đầu vào để nhận biết kiến thức đã có. Trọng tâm buổi này là hiểu và chạy được một chương trình Java tối giản. Ví dụ và bài luyện tập dùng dữ liệu trung tính để học biến, kiểu dữ liệu và toán tử.

## I. Cấu Trúc Chương Trình Java Đầu Tiên

Java bắt đầu thực thi từ method `main`. Class bao quanh các thành phần của chương trình; câu lệnh trong `main` chạy tuần tự từ trên xuống. Bắt đầu bằng một chương trình ngắn:

~~~java
public class Main {
    public static void main(String[] args) {
        System.out.println("Xin chào Java");
    }
}
~~~

`public class Main` khai báo class tên `Main`; vì class là `public`, tên file phải là `Main.java`. `main` là điểm vào chương trình theo đúng chữ ký mà Java nhận diện. `String[] args` là tham số chứa đối số dòng lệnh; buổi này chỉ nhận biết, chưa cần dùng. `System.out.println` in dữ liệu rồi xuống dòng; `System.out.print` in mà không tự xuống dòng.

~~~text
Main.java
└── class Main
    └── main(...)
        ├── câu lệnh 1
        ├── câu lệnh 2
        └── câu lệnh 3
~~~

Java gọi `main` trước; các câu lệnh bên trong chạy theo thứ tự từ trên xuống, trừ khi cấu trúc điều khiển ở buổi sau làm luồng rẽ nhánh/lặp lại.

## II. Java Chạy Như Thế Nào?

- JDK là bộ công cụ dành cho phát triển Java, gồm compiler `javac` và công cụ chạy. IntelliJ IDEA là IDE: môi trường giúp viết, quản lý và chạy source thuận tiện; IDE không thay thế JDK.
- Compiler chuyển source `.java` thành bytecode trong file `.class`. JVM đọc và thực thi bytecode trên hệ điều hành tương ứng; vì vậy cùng bytecode có thể chạy trên nhiều hệ thống có JVM phù hợp.
- Class là đơn vị khai báo kiểu và chứa thành phần chương trình. Điểm vào phổ biến của ứng dụng console là method `main` có chữ ký `public static void main(String[] args)`.
- Tên file chứa class public phải trùng tên class, ví dụ `Main.java`.
- Chọn JDK project trong IntelliJ, tạo Java project, tạo class `Main` rồi chạy bằng nút Run.

Trình tự khái quát là **source code → biên dịch → bytecode → JVM thực thi**. Khi chương trình không chạy, hãy phân biệt lỗi cấu hình môi trường, lỗi biên dịch và lỗi lúc chạy: kiểm tra JDK/SDK trước, sau đó đọc thông báo lỗi đầu tiên và xác định dòng liên quan. Theo lộ trình, cài JDK và IntelliJ, kiểm tra môi trường chạy Java là phần chuẩn bị ở nhà.

~~~text
Main.java --javac biên dịch--> Main.class (bytecode) --JVM thực thi--> kết quả
~~~

## III. Kiểu Dữ Liệu, Biến Và Phép Toán

| Kiểu | Ví dụ dùng trong ứng dụng |
|---|---|
| `int` | số lần thử hoặc số học viên |
| `long` | số nguyên cần phạm vi lớn hơn `int` |
| `double` | nhiệt độ hoặc điểm trung bình có phần thập phân |
| `char` | một ký tự |
| `boolean` | còn hàng hay không |
| `String` | họ tên hoặc một dòng văn bản |

Java phân biệt kiểu nguyên thủy như `int`, `double`, `boolean`, `char` với kiểu tham chiếu như `String`. Biến có khai báo kiểu nên chỉ nhận giá trị phù hợp với kiểu đó; kiểu dữ liệu giúp compiler phát hiện một số lỗi trước khi chạy. `int` phù hợp số nguyên trong phạm vi của kiểu; `long` có phạm vi lớn hơn; `double` biểu diễn số thực gần đúng; `boolean` chỉ nhận `true/false`; `char` lưu một đơn vị mã ký tự. `String` lưu chuỗi văn bản và là kiểu tham chiếu.

Biến là tên gắn với một vùng lưu giá trị trong chương trình. Khai báo như `int quantity = 3;` vừa tạo biến, vừa gán giá trị ban đầu. Biến thường có thể được gán lại; hằng số dùng `final` chỉ được gán một lần. Tên biến/method viết camelCase, tên class viết PascalCase; chọn tên thể hiện ý nghĩa như `quantity` thay vì `x`.

Các phép toán số học gồm `+ - * / %`; chia hai số nguyên cho kết quả nguyên nên phần thập phân bị bỏ. Nếu cần kết quả thực, ít nhất một toán hạng phải là số thực hoặc cần ép kiểu phù hợp. Phép so sánh `> < >= <= == !=` tạo kết quả boolean; logic `&& || !` ghép điều kiện. Khi một biểu thức có nhiều toán tử, thứ tự ưu tiên ảnh hưởng kết quả; dùng ngoặc đơn để làm rõ ý định và tránh đọc nhầm.

~~~java
int count = 5;
int groups = count / 2;          // 2: phép chia số nguyên
double average = (double) count / 2; // 2.5: ép kiểu trước khi chia
boolean inRange = count >= 1 && count <= 10;
~~~

> ⚠️ Giá dùng `double` chỉ để đơn giản hóa bài học. Sai số số thực và cách dùng `BigDecimal` không nằm trong phạm vi khóa học này.

## IV. GitHub Cơ Bản

Repository là nơi lưu source code và lịch sử thay đổi. `clone` tạo bản làm việc trên máy; `commit` ghi nhận một nhóm thay đổi vào lịch sử cục bộ; `push` gửi commit lên remote GitHub. Các thao tác này khác nhau và không tự thay thế nhau. Commit message nên ngắn, nói rõ nội dung, ví dụ `Add first Java program`.

Không đưa mật khẩu, token hay thông tin cá nhân vào repository. Học viên cần kiểm tra repo sau khi push để chắc rằng bài đã được nộp.

~~~text
Working directory --commit--> Local repository --push--> GitHub remote
~~~

## V. Thực Hành Có Hướng Dẫn

1. Tạo Java project, kiểm tra JDK/SDK và chạy chương trình in một dòng chào.
2. Thêm Scanner để nhập một chuỗi và một số; đối chiếu kiểu nhập với method đọc dữ liệu.
3. Tạo các biến số nguyên/số thực, dùng toán tử và in kết quả; thử phép chia nguyên và phép chia có ép kiểu.
4. Tạo repository, clone, commit một thay đổi rồi push; xác nhận commit đã xuất hiện trên GitHub.

### Ví Dụ Nhập Và Tính Toán Hoàn Chỉnh

Ví dụ dưới đây có thể chạy độc lập. `Scanner` đọc dữ liệu từ bàn phím; `nextLine()` đọc cả dòng tên, còn `nextInt()` đọc số nguyên. Hai giá trị đơn giá và số lượng được đưa vào biến có kiểu phù hợp, sau đó chương trình tính thành tiền.

~~~java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Tên học viên: ");
        String name = scanner.nextLine();
        System.out.print("Điểm kiểm tra: ");
        double score = scanner.nextDouble();
        System.out.print("Số lần làm bài: ");
        int attempts = scanner.nextInt();

        System.out.println(name + " đạt " + score + " điểm sau " + attempts + " lần làm.");
    }
}
~~~

Với input `Lan`, `8.5`, `2`, output là `Lan đạt 8.5 điểm sau 2 lần làm.` Ví dụ cố ý chỉ nhận input hợp lệ; cách xử lý input sai và lặp hỏi lại sẽ được học sau.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| IntelliJ không tìm thấy JDK | Chưa cài hoặc chưa chọn JDK | Cài JDK và chọn SDK trong project settings |
| Không chạy được main | Chữ ký method sai hoặc file không đúng class | Dùng đúng chữ ký main; kiểm tra tên file/class |
| Nhập chữ ở nơi cần số | Scanner đang đọc kiểu số | Nhập đúng kiểu theo lời nhắc |
| Push bị từ chối | Chưa pull hoặc remote chưa khớp | Đọc thông báo Git, kiểm tra remote và lịch sử trước khi sửa |

## Tổng Kết Buổi 1

| Hạng mục | Trạng thái |
|---|---|
| Nền tảng | JDK, IDE, compile/run và cấu trúc class/main |
| Ngôn ngữ | Nhập/xuất, kiểu dữ liệu, biến, hằng số, quy tắc tên, toán tử |
| Công cụ cộng tác | Repository, clone, commit, push và nộp bài |
| Nội dung tiếp theo | Điều kiện, vòng lặp và method để điều khiển luồng và tránh lặp code |

### Câu Hỏi Ôn Tập

1. JDK và IntelliJ có vai trò gì?
2. Method main dùng để làm gì?
3. Khi nào dùng int, double, String và boolean?
4. `final` khác biến thường thế nào?
5. Commit và push khác nhau thế nào?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 01](../BTVN/buoi-01-btvn.md).

## Tài Liệu Tham Khảo

- [Oracle — Getting Started with Java](https://docs.oracle.com/javase/tutorial/getStarted/index.html)
- [Oracle — Variables](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/variables.html)
- [GitHub Docs — Hello World](https://docs.github.com/en/get-started/start-your-journey/hello-world)

