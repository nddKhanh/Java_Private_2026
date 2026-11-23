# Buổi 6: Exception Và Xử Lý Lỗi Trong Java

---

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Giải thích **exception** là gì, phân biệt lỗi biên dịch với lỗi xảy ra khi chương trình chạy.
- Phân biệt **checked exception** và **unchecked exception** ở mức cơ bản.
- Sử dụng `try`, `catch`, `finally`, `throw` và `throws` đúng mục đích.
- Hiểu vì sao cần xử lý lỗi ở đúng vị trí thay vì bắt mọi lỗi rồi bỏ qua.
- Sử dụng `try-with-resources` để tài nguyên được đóng tự động.
- Đọc/ghi file văn bản bằng một công thức có sẵn, không yêu cầu tự xây dựng hệ thống lưu trữ.
- Nhận biết Regex, Thread, Custom Exception và I/O ở mức giới thiệu mở rộng.

> **Kết quả cuối buổi:** học viên biết nhận diện, phát sinh và xử lý exception; đọc/ghi được một file text đơn giản; biết kiểm tra dữ liệu đầu vào và thông báo rõ nguyên nhân khi thao tác thất bại.

> **Môi trường:** JDK 17; đã học class, method, kế thừa và interface ở các buổi trước.

## I. Exception: Khi Chương Trình Không Thể Tiếp Tục Bình Thường

Chương trình không phải lúc nào cũng nhận được dữ liệu đúng hoặc hoạt động trong môi trường như mong đợi.

Ví dụ:

- Người dùng nhập `abc` khi chương trình cần một số nguyên.
- Chương trình truy cập phần tử nằm ngoài phạm vi mảng.
- Chương trình muốn đọc một file không tồn tại.
- Người dùng đăng ký tham gia sự kiện dù chưa đáp ứng độ tuổi tối thiểu.

Cần phân biệt hai trường hợp:

| Trường hợp | Ví dụ | Khi nào phát hiện? |
|---|---|---|
| Lỗi biên dịch | Gọi `student.getScores()` nhưng class chỉ có `getScore()` | Trước khi chạy chương trình |
| Exception khi chạy | `Integer.parseInt("abc")` | Trong lúc chương trình đang chạy |

**Exception** là một **object** biểu diễn tình huống bất thường khi chương trình chạy, không phải một câu lệnh đặc biệt hay đồng nghĩa với việc JVM lập tức dừng. Các loại exception trong Java kế thừa từ `Throwable`; thường chứa thông điệp lỗi, nguyên nhân và thông tin vị trí phát sinh lỗi (*stack trace*).

Exception có thể được **Java tự ném** (ví dụ `Integer.parseInt("abc")`) hoặc **lập trình viên chủ động ném** bằng `throw`. Khi được ném, Java dừng phần code còn lại trong luồng thực thi hiện tại và tìm `catch` phù hợp, bắt đầu từ method đang chạy rồi đến các method gọi bên ngoài. Nếu không tìm thấy, **luồng đang chạy kết thúc**; không phải lúc nào toàn bộ JVM cũng dừng vì có thể còn các thread khác.

Ví dụ:

```java
int quantity = Integer.parseInt("abc");
System.out.println("Đã đọc xong số lượng");
```

`Integer.parseInt("abc")` ném `NumberFormatException`. Nếu exception không được xử lý, dòng `println` bên dưới sẽ không chạy.

### 1. Hai nhóm exception cần biết

| Nhóm | Ví dụ | Quy tắc cơ bản |
|---|---|---|
| Checked exception | `IOException` khi đọc/ghi file | Compiler yêu cầu **bắt** bằng `catch` hoặc **khai báo** bằng `throws` |
| Unchecked exception | `NumberFormatException`, `NullPointerException`, `IllegalArgumentException` | Compiler không bắt buộc `catch` hoặc `throws` |

`RuntimeException` và các class con thuộc nhóm unchecked exception. `IOException` thuộc nhóm checked exception.

Không cần học thuộc toàn bộ cây kế thừa exception. Ở buổi này, quan trọng nhất là nhận biết kiểu lỗi thường gặp và hiểu vì sao cách xử lý khác nhau.

### Kiểm tra nhanh

Vì sao `Integer.parseInt("abc")` có thể biên dịch thành công nhưng vẫn làm chương trình dừng khi chạy? `IOException` khác gì về yêu cầu của compiler?

## II. `try`, `catch`, `finally`: Cấu Trúc Và Luồng Hoạt Động

### 1. Cú pháp cơ bản

```java
try {
    // Code có thể phát sinh exception
} catch (ExceptionType e) {
    // Xử lý khi gặp đúng loại exception
} finally {
    // Công việc dọn dẹp cần thực hiện
}
```

Ý nghĩa từng phần:

- `try`: đặt đoạn code có thể phát sinh exception.
- `catch`: nhận và xử lý exception tương ứng; biến `e` chứa thông tin lỗi.
- `finally`: dùng cho việc dọn dẹp; thông thường được thực hiện dù `try` thành công hay phát sinh lỗi.

`try` phải đi với ít nhất một `catch` hoặc `finally`. Không bắt buộc phải viết cả ba thành phần cùng lúc.

### 2. Ví dụ: nhập số lượng sản phẩm

```java
String input = "abc";

try {
    int quantity = Integer.parseInt(input);
    System.out.println("Số lượng: " + quantity);
} catch (NumberFormatException e) {
    System.out.println("Số lượng phải là số nguyên.");
} finally {
    System.out.println("Kết thúc lượt nhập.");
}
```

Kết quả:

```text
Số lượng phải là số nguyên.
Kết thúc lượt nhập.
```

Nếu `input = "3"`, chương trình in số lượng rồi vẫn in dòng trong `finally`.

| Tình huống | `try` | `catch` | `finally` |
|---|---|---|---|
| Không có exception | Chạy hết | Bỏ qua | Chạy |
| Có exception khớp với `catch` | Dừng tại dòng gây lỗi | Chạy | Chạy |
| Có exception không được bắt | Dừng tại dòng gây lỗi | Không xử lý được | Thông thường vẫn chạy trước khi lỗi lan ra ngoài |

`finally` **không phải** nơi in thông báo thành công: có lỗi vẫn có thể chạy `finally`.

### 3. Nhiều `catch` và phạm vi xử lý

```java
try {
    int[] quantities = {2, 4};
    int index = Integer.parseInt("5");
    System.out.println(quantities[index]);
} catch (NumberFormatException e) {
    System.out.println("Vị trí phải là số nguyên.");
} catch (ArrayIndexOutOfBoundsException e) {
    System.out.println("Vị trí nằm ngoài danh sách.");
}
```

Có thể dùng nhiều `catch` để phân biệt nguyên nhân. Khi các loại exception có quan hệ cha–con, phải đặt loại **cụ thể trước**, loại tổng quát sau.

### 4. Vì sao không nên `try/catch` cả chương trình?

Bản thân `try/catch` thường có chi phí rất nhỏ khi không xảy ra exception; chi phí đáng kể hơn nằm ở việc **ném exception**, đặc biệt nếu ném liên tục. Vấn đề chính của việc bắt mọi lỗi là **logic xử lý**, không phải tốc độ.

```java
try {
    loadData();
    processData();  // Nếu ném exception...
    saveResult();   // ...dòng này sẽ bị bỏ qua
} catch (Exception e) {
    System.out.println("Có lỗi: " + e.getMessage());
}
```

Nếu `processData()` thất bại, `catch` có thể nhận lỗi nhưng **không làm `processData()` chạy lại** và cũng không khiến `saveResult()` tự động chạy. Bắt được lỗi không đồng nghĩa với khôi phục thành công.

Nên đặt `try/catch` **quanh thao tác có thể thất bại và có cách xử lý rõ ràng**, ví dụ nhập sai thì báo người dùng nhập lại. Có thể dùng `catch (Exception e)` ở ranh giới của một tác vụ lớn để ghi log và báo thất bại, nhưng không được coi đó là cách bảo đảm chương trình luôn chạy đúng. `Exception` cũng không bắt mọi `Error` của JVM.

### 5. Vì sao cần `finally` nếu có thể viết code bên dưới?

Nếu có exception **không được bắt**, chương trình sẽ rời khỏi method đang chạy; lệnh nằm sau khối `try` có thể **không được thực hiện**. `finally` được thực hiện khi rời khối `try` theo cơ chế thông thường, kể cả lúc exception đang lan ra ngoài hoặc khi `try` có `return`.

```java
try {
    int x = 10 / 0;
} finally {
    System.out.println("Dọn dẹp"); // Vẫn chạy
}
System.out.println("Tiếp tục");     // Không chạy
```

`finally` dành cho việc dọn dẹp hoặc hoàn tất bắt buộc, **không dùng để báo thành công**. Nó không được đảm bảo trong mọi sự cố cực đoan như JVM bị tắt đột ngột; với tài nguyên hỗ trợ `AutoCloseable`, ưu tiên `try-with-resources`.

### Kiểm tra nhanh

Nếu đặt `System.out.println("Thao tác thành công")` trong `finally`, chương trình có thể báo sai trong tình huống nào?

## III. `throw` Và `throws`: Phát Hiện Lỗi Và Chuyển Lỗi Cho Nơi Gọi

`try/catch` dùng để **xử lý** exception. `throw` dùng để **ném** một exception; `throws` dùng trong khai báo method để **cho biết method có thể ném exception**.

### 1. `throw`: chủ động báo dữ liệu không hợp lệ

```java
static void validateQuantity(int quantity) {
    if (quantity <= 0) {
        throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
    }
}
```

Đoạn code này không tự sửa số lượng sai. Nó từ chối dữ liệu và báo rõ nguyên nhân để nơi gọi quyết định cách xử lý.

### 2. `throws`: khai báo exception từ method

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

static String loadText(Path path) throws IOException {
    return Files.readString(path);
}
```

`IOException` là checked exception, nên method phải xử lý hoặc khai báo `throws IOException`.

Code gọi có thể xử lý:

```java
try {
    String content = loadText(Path.of("notes.txt"));
    System.out.println(content);
} catch (IOException e) {
    System.out.println("Không đọc được file: " + e.getMessage());
}
```

| Từ khóa | Vị trí | Vai trò |
|---|---|---|
| `throw` | Trong thân method | Ném một exception cụ thể |
| `throws` | Sau danh sách tham số của method | Khai báo loại exception có thể được ném ra ngoài |
| `catch` | Sau `try` | Xử lý exception phù hợp |

`throws` **không tạo ra exception và không tự xử lý lỗi**. Nó chỉ khai báo khả năng method có thể ném exception. Đối với checked exception, compiler yêu cầu method phải `catch` hoặc khai báo `throws`. Đối với unchecked exception, có thể dùng `throws` để mô tả nhưng không bắt buộc.

Ví dụ, `throw new IllegalArgumentException("Sai dữ liệu")` thực sự tạo và ném lỗi; còn `void load() throws IOException` chỉ nói rằng lời gọi `load()` có thể thất bại với `IOException`. Nơi gọi có thể `catch` loại lỗi đó hoặc tiếp tục chuyển lên trên.

## IV. Vận Dụng Exception: Đăng Ký Tham Gia Sự Kiện

Giả sử một sự kiện yêu cầu người tham gia **từ 18 tuổi trở lên**. Chương trình cần phát hiện dữ liệu không hợp lệ và đưa ra thông báo phù hợp, thay vì tiếp tục đăng ký như bình thường.

### 1. Ném exception khi dữ liệu không hợp lệ

```java
public class RegistrationService {
    public static void register(String name, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (age < 18) {
            throw new IllegalArgumentException("Người tham gia phải từ 18 tuổi");
        }
        System.out.println("Đăng ký thành công: " + name);
    }
}
```

`register(...)` kiểm tra điều kiện và dùng `throw` để từ chối dữ liệu sai. Khi exception xuất hiện, các lệnh phía sau trong method không tiếp tục chạy như bình thường.

### 2. Bắt exception ở nơi gọi method

```java
public class Main {
    public static void main(String[] args) {
        try {
            RegistrationService.register("An", 16);
            System.out.println("Đã hoàn tất đăng ký");
        } catch (IllegalArgumentException e) {
            System.out.println("Đăng ký thất bại: " + e.getMessage());
        }
    }
}
```

Kết quả:

```text
Đăng ký thất bại: Người tham gia phải từ 18 tuổi
```

Nếu đổi thành `register("An", 20)`, method chạy thành công và khối `catch` được bỏ qua.

**Nguyên tắc:** nơi phát hiện dữ liệu sai có thể `throw` exception; nơi gọi method có thể dùng `catch` phù hợp để xử lý và thông báo. Không cần tự `catch` lỗi ngay tại dòng vừa ném ra nếu nơi đó chưa có cách xử lý phù hợp.

### Kiểm tra nhanh

Nếu `RegistrationService.register("", 20)` được gọi, thông báo nào sẽ xuất hiện? Vì sao chương trình không in `Đã hoàn tất đăng ký`?

## V. `try-with-resources`: Đóng Tài Nguyên Tự Động

Khi mở file, chương trình sử dụng tài nguyên cần được đóng sau khi hoàn tất. Nếu quên đóng, có thể gây lãng phí tài nguyên hoặc ảnh hưởng thao tác với file.

Thay vì tự gọi `close()` trong `finally`, Java cho phép viết:

```java
try (BufferedReader reader = Files.newBufferedReader(
        Path.of("notes.txt"), StandardCharsets.UTF_8)) {
    System.out.println(reader.readLine());
} catch (IOException e) {
    System.out.println("Không đọc được file: " + e.getMessage());
}
```

Trong ví dụ này, cần import `java.io.BufferedReader`, `java.io.IOException`, `java.nio.charset.StandardCharsets`, `java.nio.file.Files` và `java.nio.file.Path`.

Ý nghĩa:

- Tài nguyên được khai báo trong cặp ngoặc `try (...)`.
- Java tự gọi `close()` khi rời khỏi khối, kể cả khi có exception.
- Chỉ dùng cách này với tài nguyên hỗ trợ `AutoCloseable`, chẳng hạn `BufferedReader` hoặc `BufferedWriter`.

### So sánh với `try/catch/finally`

| Cách viết | Mục đích | Tự đóng tài nguyên? |
|---|---|---|
| `try/catch` | Chạy thao tác và xử lý exception | Không |
| `try/catch/finally` | Xử lý exception và tự viết thao tác dọn dẹp trong `finally` | Chỉ khi lập trình viên viết lệnh `close()` |
| `try-with-resources` | Chạy thao tác và tự đóng tài nguyên khai báo trong `try (...)` | Có, nếu tài nguyên implements `AutoCloseable` |

`try-with-resources` **không thay thế `catch`**: vẫn có thể thêm `catch` để xử lý lỗi đọc/ghi. Nó chủ yếu thay việc **tự viết code đóng tài nguyên trong `finally`**, kể cả khi đang có exception. Các công việc dọn dẹp khác ngoài đóng tài nguyên vẫn có thể cần `finally`.

## VI. Đọc/Ghi File Văn Bản

Buổi này chỉ cần làm quen với cách ghi và đọc file text đơn giản: thay đường dẫn, nội dung cần ghi và xử lý `IOException` theo mẫu.

### 1. Ghi file

```java
Path path = Path.of("notes.txt");

try (BufferedWriter writer = Files.newBufferedWriter(
        path, StandardCharsets.UTF_8)) {
    writer.write("Buổi học Java");
    writer.newLine();
    writer.write("Chủ đề: Exception và I/O");
} catch (IOException e) {
    System.out.println("Ghi file thất bại: " + e.getMessage());
}
```

`Files.newBufferedWriter(...)` theo cách viết trên tạo file nếu chưa có và **ghi đè nội dung cũ** nếu file đã tồn tại. `writer.newLine()` dùng để xuống dòng.

### 2. Đọc file

```java
Path path = Path.of("notes.txt");

try (BufferedReader reader = Files.newBufferedReader(
        path, StandardCharsets.UTF_8)) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
} catch (IOException e) {
    System.out.println("Đọc file thất bại: " + e.getMessage());
}
```

`readLine()` trả `null` khi đọc hết file. Sử dụng UTF-8 để đọc/ghi tiếng Việt nhất quán.

Các ví dụ dùng `BufferedWriter`, `BufferedReader`, `IOException`, `Path`, `Files` và `StandardCharsets`. IDE có thể hỗ trợ thêm các lệnh `import` tương ứng.

### 3. Những điểm cần nhớ

- Đặt đường dẫn vào `Path.of(...)`; khi thử bài khác, chỉ cần đổi tên file và nội dung.
- Dùng `try-with-resources` để tự đóng file sau khi đọc hoặc ghi.
- Bắt `IOException` để thông báo khi thao tác với file thất bại.
- Với `newBufferedWriter(...)` như trên, nội dung cũ sẽ bị ghi đè; không dùng tùy tiện với file cần giữ dữ liệu.

## VII. Thực Hành Có Hướng Dẫn

1. Chạy `Integer.parseInt("abc")`, quan sát exception trước và sau khi có `catch`.
2. Thay `"abc"` bằng `"5"`, quan sát khi nào `catch` và `finally` chạy.
3. Tạo method `validateQuantity(int quantity)`; thử `0`, `-2`, `3` để phân biệt dữ liệu hợp lệ và không hợp lệ.
4. Tạo method có `throws IOException`; xử lý lỗi tại nơi gọi bằng `try/catch`.
5. Chạy ví dụ đăng ký sự kiện ở mục IV với tên trống, tuổi 16 và tuổi 20.
6. Sao chép ví dụ ghi file `notes.txt`, thay nội dung rồi đọc lại; thử một đường dẫn không hợp lệ để quan sát `IOException`.

Sau mỗi ca, xác định: **lỗi xuất hiện ở đâu, loại exception là gì và nơi nào chịu trách nhiệm xử lý?**

## VIII. Nội dung mở rộng và nâng cao

### 1. Regex (Mở rộng)

*Regular Expression* là mẫu dùng để kiểm tra hoặc tìm kiếm chuỗi. Ví dụ `"[0-9]+"` mô tả chuỗi gồm một hoặc nhiều chữ số:

```java
System.out.println("123".matches("[0-9]+")); // true
System.out.println("12a".matches("[0-9]+")); // false
```

Regex hữu ích để kiểm tra định dạng dữ liệu; không thay thế toàn bộ kiểm tra nghiệp vụ.

### 2. Thread cơ bản (Nâng cao)

Thread là một luồng thực thi trong chương trình. Ví dụ tạo một luồng chạy công việc riêng:

```java
Thread worker = new Thread(() -> System.out.println("Đang xử lý"));
worker.start();
```

Phân biệt `start()` (khởi chạy thread mới) với gọi trực tiếp `run()` (thực thi như method thông thường). Chưa học đồng bộ, chia sẻ dữ liệu hoặc xử lý tranh chấp giữa các thread.

### 3. Custom Exception (Nâng cao)

Có thể tự định nghĩa một loại exception bằng cách kế thừa `Exception` hoặc `RuntimeException`.

```java
class InvalidAgeException extends RuntimeException {
    public InvalidAgeException(String message) {
        super(message);
    }
}

class Registration {
    static void register(int age) {
        if (age < 18) {
            throw new InvalidAgeException("Chưa đủ 18 tuổi");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            Registration.register(16);
        } catch (InvalidAgeException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

### 4. I/O trong Java (Mở rộng)

**I/O (Input/Output)** là khái niệm chung về đưa dữ liệu vào và xuất dữ liệu ra khỏi chương trình: bàn phím, màn hình, file, kết nối mạng... Đọc/ghi file ở phần VI chỉ là một trường hợp của I/O.

Java có nhiều cách làm việc với I/O, chẳng hạn byte stream (`InputStream`/`OutputStream`), character stream (`Reader`/`Writer`) và các tiện ích `Files`/`Path` thuộc NIO. Ở buổi này **không cần học sâu các nhóm API**; chỉ dùng công thức đọc/ghi file văn bản đã giới thiệu.

## Lỗi thường gặp

| Lỗi | Hậu quả | Cách sửa |
|---|---|---|
| Nhầm lỗi biên dịch với exception | Cố dùng `catch` để sửa lỗi cú pháp | Sửa code để biên dịch được trước |
| `catch (Exception)` cho mọi thao tác | Khó biết nguyên nhân, có thể che lỗi lập trình | Bắt đúng loại exception có thể xử lý |
| Dùng `throw` và `throws` như nhau | Nhầm phát sinh lỗi với khai báo lỗi | `throw` trong thân method; `throws` ở chữ ký method |
| In “thành công” trong `finally` | Thất bại vẫn báo thành công | Chỉ in sau thao tác hoàn tất |
| Quên đóng file | Có thể giữ tài nguyên không cần thiết | Dùng `try-with-resources` |
| Ghi file trước khi kiểm tra dữ liệu | File cũ có thể bị ghi đè dù dữ liệu mới không hợp lệ | Kiểm tra dữ liệu trước khi mở file ghi |
| Không dùng UTF-8 nhất quán | Nội dung tiếng Việt có thể bị đọc sai | Chỉ rõ `StandardCharsets.UTF_8` |

## Tổng kết buổi 6

Exception cho phép chương trình biểu diễn và xử lý các tình huống bất thường một cách rõ ràng. `try/catch/finally` giúp kiểm soát luồng xử lý; `throw/throws` giúp phát hiện và chuyển lỗi tới nơi chịu trách nhiệm. Với file, `try-with-resources` giúp đóng tài nguyên tự động.

Trong chương trình thực tế, điều quan trọng là **phát hiện lỗi đúng nơi, xử lý rõ nguyên nhân và không báo thành công khi thao tác thất bại**.

### Câu hỏi chốt buổi

1. Exception khác lỗi biên dịch ở điểm nào?
2. **Exception bản chất là gì?** Khi exception được ném, Java xử lý luồng thực thi ra sao?
3. Checked exception và unchecked exception khác nhau về yêu cầu của compiler ra sao?
4. `try`, `catch`, `finally` có vai trò gì?
5. **Nếu `if/else` cũng có thể kiểm tra và xử lý tình huống lỗi, vì sao Java vẫn cần Exception? Khi nào nên dùng `if/else`, khi nào nên dùng Exception?**
6. **Vì sao không nên bọc toàn bộ chương trình trong một `try/catch (Exception e)` chỉ để tránh chương trình dừng?**
7. **`finally` có tác dụng gì khi ta có thể đặt câu lệnh trực tiếp bên dưới khối `try/catch`?**
8. `throw` khác `throws` như thế nào?
9. Try-with-resources giúp giải quyết vấn đề gì?
10. **`try-with-resources` khác gì `try/catch/finally`? Khi nào vẫn cần `catch`?**

## Bài tập nhỏ trên lớp — Xử lý danh sách điểm

Tạo chương trình nhận danh sách chuỗi `"8.0"`, `"abc"`, `"7.5"`, `"9.0"`:

- Dùng `Double.parseDouble(...)` để chuyển từng chuỗi thành điểm.
- Bắt `NumberFormatException` ở **từng phần tử**; thông báo vị trí sai và tiếp tục xử lý phần tử còn lại.
- In số điểm hợp lệ và điểm trung bình. Nếu không có điểm hợp lệ, in `0`.
- Sau khi hoàn thành phần chính, dùng **công thức đọc file** đã học để thử dữ liệu tương tự trong `scores.txt` (nếu còn thời gian).

**Kết quả mong đợi:** vị trí thứ 2 không hợp lệ; có 3 điểm hợp lệ; trung bình xấp xỉ `8.17`.

Thời gian làm khoảng 15 phút. Trọng tâm là đặt `try/catch` đúng phạm vi, không phải xây dựng bộ đọc file phức tạp.

## Bài tập về nhà

Hoàn thành [BTVN buổi 06](../BTVN/buoi-06-btvn.md).
