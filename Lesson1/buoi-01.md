# Buổi 1: Làm Quen Java — Môi Trường, Chương Trình Đầu Tiên và Nền Tảng Cú Pháp

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Kiểm tra JDK, cấu hình Project SDK trong IntelliJ IDEA và chạy một chương trình Java.
- Giải thích vai trò của class, method `main` và luồng source code → bytecode → JVM.
- Dùng `Scanner` và `System.out` để nhập, xử lý và xuất dữ liệu trong một chương trình tuần tự.
- Chọn kiểu dữ liệu phù hợp, khai báo biến/hằng số, đặt tên và viết các biểu thức cơ bản.
- Tạo repository, commit, push và kiểm tra bài đã xuất hiện trên GitHub.

> **Kết quả cuối buổi:** mỗi học viên chạy được một chương trình nhận tên mặt hàng, đơn giá, số lượng và in thành tiền; sau đó đưa source code lên GitHub.

## Chuẩn bị môi trường trước buổi học

Học viên cài **JDK 17**, IntelliJ IDEA và Git ở nhà. Toàn bộ khóa học thống nhất JDK 17 để ví dụ `record`, API và cấu hình Maven không khác nhau giữa các máy. Đầu buổi, không cài lại ngay từ đầu nếu chưa cần; hãy kiểm tra từng lớp để biết lỗi nằm ở công cụ nào.

~~~bash
java -version
javac -version
git --version
~~~

- `java -version` thành công cho biết máy có công cụ chạy Java.
- `javac -version` thành công cho biết máy có Java compiler.
- `git --version` thành công cho biết máy có thể thực hiện phần nộp bài bằng Git.
- Hai lệnh Java cần hiển thị phiên bản 17. Nếu máy có nhiều JDK, kiểm tra cả `JAVA_HOME` và Project SDK thay vì chỉ nhìn một trong hai nơi.

Trong IntelliJ IDEA, mở **Project Structure** và kiểm tra **Project SDK** đang trỏ tới một JDK hợp lệ. Sau đó tạo Java project, tạo class `Main` và chạy chương trình in một dòng. Chỉ khi bước này thành công mới chuyển sang phần cú pháp.

| Hiện tượng | Vị trí cần kiểm tra đầu tiên |
|---|---|
| Terminal không nhận `java` hoặc `javac` | JDK và biến môi trường |
| Terminal chạy được nhưng IntelliJ không chạy được | Project SDK và cấu hình Run |
| IntelliJ nhận JDK nhưng code báo đỏ | Tên file, cú pháp và thông báo compiler |
| Chương trình chạy nhưng kết quả sai | Giá trị biến và biểu thức tính toán |

## I. Chương Trình Cần Làm Gì?

Trước khi học cú pháp, bắt đầu từ một yêu cầu cụ thể: người dùng nhập tên mặt hàng, đơn giá và số lượng; chương trình tính thành tiền rồi in kết quả. Có thể tách yêu cầu này thành ba bước:

~~~text
Input                         Process                    Output
Tên, đơn giá, số lượng   →   đơn giá × số lượng   →   thông tin và thành tiền
~~~

Mô hình **Input – Process – Output** giúp xác định chương trình cần lưu dữ liệu nào và thực hiện phép tính gì. Trong buổi đầu, các câu lệnh chạy lần lượt từ trên xuống:

1. Nhận dữ liệu.
2. Lưu dữ liệu vào biến.
3. Tính kết quả.
4. In kết quả.

## II. Chương Trình Java Đầu Tiên

Tạo file `Main.java` và viết chương trình sau:

~~~java
public class Main {
    public static void main(String[] args) {
        System.out.println("Xin chào Java");
    }
}
~~~

Đọc chương trình từ ngoài vào trong:

- `public class Main` khai báo class tên `Main`. Vì class này là `public`, file phải có tên `Main.java`.
- Cặp `{ }` xác định phạm vi của class và method. Thiếu hoặc đặt sai dấu ngoặc thường làm compiler báo nhiều lỗi liên tiếp.
- `public static void main(String[] args)` là điểm Java bắt đầu chạy ứng dụng console.
- `System.out.println(...)` in nội dung rồi xuống dòng. `System.out.print(...)` in nhưng giữ con trỏ ở cùng dòng.
- Dấu `;` kết thúc một câu lệnh Java.

Khi nhấn Run, Java không chạy trực tiếp nội dung trong file `.java`. Compiler `javac` chuyển source code thành bytecode trong file `.class`; JVM thực thi bytecode đó.

~~~text
Main.java --javac--> Main.class --JVM--> Kết quả trên console
~~~

JDK cung cấp compiler và công cụ chạy. JVM thực thi bytecode. IntelliJ IDEA giúp viết, tổ chức và chạy project thuận tiện nhưng không thay thế JDK. Phân biệt ba nhóm lỗi giúp sửa nhanh hơn:

- **Lỗi môi trường:** không tìm thấy JDK, SDK hoặc lệnh Java.
- **Lỗi biên dịch:** sai cú pháp, sai tên biến, thiếu dấu hoặc không khớp kiểu dữ liệu.
- **Lỗi khi chạy/kết quả:** chương trình đã biên dịch nhưng input gây lỗi hoặc công thức cho kết quả không đúng.

### Kiểm tra nhanh

Trước khi chạy, hãy dự đoán kết quả của đoạn sau rồi kiểm tra trong IntelliJ:

~~~java
System.out.print("Java ");
System.out.println("cơ bản");
System.out.println("Buổi 1");
~~~

Hai lệnh đầu tạo cùng một dòng vì `print` không tự xuống dòng.

## III. Kiểu Dữ Liệu Và Biến

Chương trình cần giữ lại dữ liệu để sử dụng ở bước sau. Biến là một tên đại diện cho giá trị có kiểu xác định. Ví dụ:

~~~java
String productName = "Áo thun";
double unitPrice = 150000;
int quantity = 2;
~~~

Mỗi câu lệnh gồm kiểu dữ liệu, tên biến và giá trị ban đầu. Sau khi khai báo, Java dùng kiểu dữ liệu để kiểm tra các phép gán và phép toán có phù hợp hay không.

| Kiểu | Dùng cho | Ví dụ |
|---|---|---|
| `int` | Số nguyên trong phạm vi thông dụng | `int quantity = 2;` |
| `long` | Số nguyên cần phạm vi lớn hơn `int` | `long viewCount = 3_000_000_000L;` |
| `double` | Số có phần thập phân hoặc phép tính gần đúng | `double score = 8.5;` |
| `char` | Một ký tự đặt trong nháy đơn | `char size = 'M';` |
| `boolean` | Trạng thái đúng hoặc sai | `boolean available = true;` |
| `String` | Chuỗi văn bản đặt trong nháy kép | `String name = "Áo thun";` |

`int`, `long`, `double`, `char` và `boolean` là các kiểu nguyên thủy. `String` là kiểu tham chiếu dùng để lưu và xử lý chuỗi ký tự.

### Khai báo, gán lại và hằng số

~~~java
int quantity = 2;  // khai báo và gán giá trị ban đầu
quantity = 3;      // gán lại giá trị

final double VAT_RATE = 0.10;
~~~

Biến thông thường có thể nhận giá trị mới phù hợp với kiểu đã khai báo. Biến có `final` chỉ được gán một lần, phù hợp với giá trị không được thay đổi trong quá trình chạy. Cố gán lại `VAT_RATE` sẽ gây lỗi biên dịch.

Giá tiền dùng `double` trong khóa học để giữ bài tập đơn giản. `double` biểu diễn số thực gần đúng nên không phù hợp cho tính tiền chính xác trong hệ thống thực tế; `BigDecimal` nằm ngoài phạm vi khóa học này.

### Quy tắc đặt tên

- Tên biến và method dùng `camelCase`: `unitPrice`, `calculateTotal`.
- Tên class dùng `PascalCase`: `Main`, `OrderCalculator`.
- Hằng số thường dùng chữ hoa và dấu gạch dưới: `VAT_RATE`, `SHIPPING_FEE`.
- Tên không bắt đầu bằng chữ số, không chứa khoảng trắng và không trùng từ khóa Java.
- Ưu tiên tên nói rõ ý nghĩa. `quantity` dễ hiểu hơn `q` hoặc `x`.

### Kiểm tra nhanh

Đoạn nào dưới đây không biên dịch được, và vì sao?

~~~java
int quantity = 2;
quantity = 5;

final double SHIPPING_FEE = 20000;
SHIPPING_FEE = 30000;

int productName = "Áo thun";
~~~

Hai lỗi cần nhận ra là gán lại hằng số và gán một `String` vào biến `int`. Compiler phát hiện các lỗi này trước khi chương trình chạy.

## IV. Toán Tử Và Biểu Thức

Biểu thức kết hợp giá trị, biến và toán tử để tạo ra một kết quả mới. Bài toán của buổi học dùng phép nhân:

~~~java
double subtotal = unitPrice * quantity;
~~~

### Toán tử số học

| Toán tử | Ý nghĩa | Ví dụ |
|---|---|---|
| `+` | Cộng số hoặc nối chuỗi | `price + fee`, `"Tên: " + name` |
| `-` | Trừ | `stock - quantity` |
| `*` | Nhân | `unitPrice * quantity` |
| `/` | Chia | `total / count` |
| `%` | Lấy phần dư | `number % 2` |

Phép chia hai số nguyên cho kết quả nguyên và bỏ phần thập phân:

~~~java
int count = 5;
int groups = count / 2;               // 2
double average = (double) count / 2;  // 2.5
~~~

Ép `count` sang `double` trước khi chia khiến Java thực hiện phép chia số thực. Khi một biểu thức có nhiều toán tử, dùng ngoặc đơn để thể hiện rõ thứ tự mong muốn:

~~~java
final double VAT_RATE = 0.10;
double shippingFee = 20000;
double total = subtotal + (subtotal * VAT_RATE) + shippingFee;
~~~

### Toán tử so sánh và logic

Các toán tử `>`, `<`, `>=`, `<=`, `==`, `!=` tạo ra kết quả `boolean`. `&&` yêu cầu cả hai điều kiện đúng, `||` yêu cầu ít nhất một điều kiện đúng và `!` đảo giá trị boolean.

~~~java
boolean validPrice = unitPrice > 0;
boolean validQuantity = quantity > 0;
boolean validItem = validPrice && validQuantity;
~~~

Đoạn code tính ra giá trị `true` hoặc `false`; bản thân biểu thức boolean không thay đổi luồng chương trình.

## V. Nhập Và Xuất Dữ Liệu Với `Scanner`

Dữ liệu đang được viết cố định trong source code. Để người dùng nhập từ bàn phím, import `Scanner` và tạo một scanner đọc `System.in`:

~~~java
import java.util.Scanner;

Scanner scanner = new Scanner(System.in);
~~~

Mỗi kiểu dữ liệu có cách đọc phù hợp:

| Dữ liệu cần đọc | Lệnh thường dùng |
|---|---|
| Cả một dòng văn bản | `scanner.nextLine()` |
| Số nguyên | `scanner.nextInt()` |
| Số thực | `scanner.nextDouble()` |
| Giá trị boolean | `scanner.nextBoolean()` |

Luôn in lời nhắc trước khi đọc để người dùng biết cần nhập gì:

~~~java
System.out.print("Tên mặt hàng: ");
String productName = scanner.nextLine();

System.out.print("Đơn giá: ");
double unitPrice = scanner.nextDouble();

System.out.print("Số lượng: ");
int quantity = scanner.nextInt();
~~~

Ví dụ đọc chuỗi trước các giá trị số để tránh phải xử lý ký tự xuống dòng còn lại.

## VI. Demo Hoàn Chỉnh: Tính Tiền Một Mặt Hàng

Ghép các phần đã học thành một chương trình hoàn chỉnh:

~~~java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Tên mặt hàng: ");
        String productName = scanner.nextLine();

        System.out.print("Đơn giá: ");
        double unitPrice = scanner.nextDouble();

        System.out.print("Số lượng: ");
        int quantity = scanner.nextInt();

        double subtotal = unitPrice * quantity;

        System.out.println("Tên mặt hàng: " + productName);
        System.out.println("Đơn giá: " + unitPrice);
        System.out.println("Số lượng: " + quantity);
        System.out.println("Thành tiền: " + subtotal);

        scanner.close();
    }
}
~~~

Với dữ liệu `Áo thun`, `150000`, `2`, có thể lần theo trạng thái của chương trình:

| Biến | Giá trị sau khi nhập |
|---|---:|
| `productName` | `"Áo thun"` |
| `unitPrice` | `150000.0` |
| `quantity` | `2` |
| `subtotal` | `300000.0` |

Input được lưu vào biến, biểu thức dùng các biến để tạo kết quả, sau đó output đọc kết quả đó để hiển thị.

### Thực hành có hướng dẫn

1. Chạy chương trình với bộ dữ liệu mẫu và đối chiếu thành tiền.
2. Đổi đơn giá thành `99000` và số lượng thành `3`; dự đoán kết quả trước khi chạy.
3. Thêm biến `final double SHIPPING_FEE = 20000;`, tính và in tổng sau phí vận chuyển.
4. Tạo một biến boolean kiểm tra đơn giá và số lượng đều lớn hơn 0, sau đó in giá trị boolean.
5. Đổi tên biến `subtotal` thành `totalPrice` bằng chức năng Rename của IDE và chạy lại để xác nhận chương trình không đổi hành vi.

## VII. Git Và GitHub Cơ Bản

Source code chỉ nằm trên máy chưa được xem là đã nộp. Git lưu lịch sử thay đổi; GitHub lưu repository từ xa để chia sẻ và kiểm tra bài.

~~~text
GitHub --git clone--> Thư mục làm việc

File thay đổi --git add--> Staging --git commit--> Local repository --git push--> GitHub
~~~

- `clone` tải repository và cấu hình liên kết với remote.
- `status` cho biết file nào mới hoặc đã thay đổi.
- `add` chọn thay đổi đưa vào lần commit tiếp theo.
- `commit` lưu một mốc lịch sử trong repository trên máy.
- `push` gửi các commit chưa có trên GitHub lên remote.

### Quy trình nộp bài

1. Tạo một repository mới trên GitHub và sao chép URL. Với bài đầu tiên, nên tạo repository rỗng để quy trình clone và push dễ theo dõi.
2. Clone repository, sau đó mở đúng thư mục vừa clone bằng IntelliJ IDEA:

~~~bash
git clone <repository-url>
cd <repository-name>
~~~

3. Tạo source code bên trong repository. Dùng `git status` để chắc rằng Git nhìn thấy file:

~~~bash
git status
~~~

4. Đưa thay đổi vào staging, tạo commit rồi push:

~~~bash
git add .
git commit -m "Add lesson 1 homework"
git push -u origin main
~~~

Nếu branch hiện tại không phải `main`, xem tên bằng `git branch --show-current` và push đúng branch đó. Không sao chép token, mật khẩu hoặc thông tin cá nhân vào source code và repository.

5. Mở repository trên GitHub, tải lại trang và kiểm tra ba điều:

- File Java đã xuất hiện.
- Commit mới có đúng message.
- Link repository mở được và trỏ đúng bài cần nộp.

`commit` thành công không có nghĩa bài đã lên GitHub. Học viên chỉ hoàn thành việc nộp khi đã `push` và kiểm tra trên trang repository.

## VIII. Lỗi Thường Gặp Và Cách Đọc Lỗi

| Hiện tượng | Nguyên nhân thường gặp | Cách kiểm tra |
|---|---|---|
| IntelliJ không tìm thấy JDK | Chưa cài hoặc chưa chọn Project SDK | Chạy `java -version`, `javac -version`, sau đó kiểm tra Project SDK |
| Không chạy được `main` | Sai chữ ký method hoặc sai tên file/class | So sánh với mẫu; kiểm tra `Main.java` và `class Main` |
| Báo `cannot find symbol` | Viết sai tên hoặc dùng biến trước khi khai báo | Đọc tên được nhắc trong lỗi và tìm dòng khai báo |
| Báo `incompatible types` | Gán giá trị không phù hợp với kiểu biến | Đối chiếu kiểu ở hai phía của dấu `=` |
| Nhập chữ khi chương trình chờ số | `nextInt()`/`nextDouble()` cần dữ liệu số | Kiểm tra dữ liệu trước khi chuyển đổi |
| Thành tiền sai | Chọn sai biến hoặc sai toán tử | In từng biến và tính tay với cùng input |
| Commit xong nhưng GitHub chưa có bài | Chưa push | Chạy `git status`, push rồi kiểm tra trang GitHub |
| Push không đúng branch | Tên branch khác `main` | Chạy `git branch --show-current` |

Khi compiler báo nhiều lỗi, xử lý lỗi đầu tiên trước rồi biên dịch lại. Một dấu ngoặc hoặc dấu chấm phẩy sai có thể kéo theo nhiều thông báo ở các dòng phía sau.

## Tổng Kết Buổi 1

Một chương trình console cơ bản có thể được đọc theo mạch sau:

~~~text
Yêu cầu
  ↓
Chọn kiểu dữ liệu và biến
  ↓
Nhập dữ liệu bằng Scanner
  ↓
Tính toán bằng biểu thức
  ↓
Xuất kết quả bằng System.out
  ↓
Commit và push source code lên GitHub
~~~

Học viên đạt yêu cầu buổi học khi có thể tự giải thích và thực hiện toàn bộ mạch này, không chỉ chạy được đoạn code sao chép từ ví dụ.

### Câu hỏi ôn tập

1. JDK, JVM và IntelliJ IDEA có vai trò khác nhau như thế nào?
2. Vì sao file chứa `public class Main` phải có tên `Main.java`?
3. `int`, `double`, `boolean` và `String` phù hợp với loại dữ liệu nào?
4. `final` thay đổi cách một biến được sử dụng như thế nào?
5. Vì sao `5 / 2` và `(double) 5 / 2` cho kết quả khác nhau?
6. Biểu thức `unitPrice > 0 && quantity > 0` trả về kiểu dữ liệu nào?
7. `git add`, `git commit` và `git push` khác nhau như thế nào?
8. Làm thế nào để xác nhận bài đã thực sự được nộp lên GitHub?

## Bài tập nhỏ trên lớp — Tính diện tích hình chữ nhật

Viết chương trình `RectangleCalculator`:

- Nhập chiều dài và chiều rộng kiểu `double` bằng `Scanner`.
- Tính chu vi và diện tích bằng hai biểu thức riêng.
- In lại hai giá trị đã nhập, chu vi và diện tích với nhãn rõ ràng.

**Nghiệm thu:** với chiều dài `8.5` và chiều rộng `4`, chương trình in diện tích `34.0` và chu vi `25.0`. Thời gian làm khoảng 10 phút.

## Bài Tập Về Nhà

Hoàn thành [Bài tập về nhà Buổi 01](../BTVN/buoi-01-btvn.md).

## Tài Liệu Tham Khảo

- [Oracle — Getting Started with Java](https://docs.oracle.com/javase/tutorial/getStarted/index.html)
- [Oracle — Variables](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/variables.html)
- [GitHub Docs — Hello World](https://docs.github.com/en/get-started/start-your-journey/hello-world)
