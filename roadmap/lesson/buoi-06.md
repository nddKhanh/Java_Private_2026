# Buổi 6: Exception, try-with-resources và Lưu Dữ Liệu Ra File

---

## Mục tiêu buổi học

- Giải thích vì sao lỗi input/file không nên làm chương trình dừng đột ngột.
- Dùng try/catch/finally và throw ở mức cơ bản.
- Đóng tài nguyên bằng try-with-resources.
- Đọc/ghi file bằng Files, Path để giữ lại dữ liệu sau khi chương trình kết thúc.

> **Cầu nối:** Các object và phép xử lý trước đó thường giả định dữ liệu đầu vào hợp lệ. Khi giả định không đúng hoặc thao tác bên ngoài thất bại, exception giúp kiểm soát luồng và đưa ra cách phục hồi phù hợp. Buổi này xét riêng lỗi, tài nguyên và lưu trữ file.

## I. Phân Loại Lỗi Qua Ví Dụ

Lỗi phát sinh khi chạy có thể khiến thao tác hiện tại không hoàn tất. Ví dụ nhập chữ vào chỗ cần số có thể gây `InputMismatchException`; chuyển chuỗi không hợp lệ sang số gây `NumberFormatException`; thao tác file có thể gây `IOException`. Exception là object mang thông tin về tình huống bất thường và luồng xử lý sẽ tìm `catch` phù hợp trên call stack. Nếu không có handler phù hợp, chương trình kết thúc luồng hiện tại và in stack trace.

~~~java
try {
    int quantity = Integer.parseInt(input);
    System.out.println("Số lượng: " + quantity);
} catch (NumberFormatException e) {
    System.out.println("Số lượng phải là số nguyên.");
}
~~~

`Integer.parseInt(input)` ném `NumberFormatException` nếu chuỗi không biểu diễn số nguyên. Nếu đọc trực tiếp bằng `Scanner.nextInt()`, token không hợp lệ gây `InputMismatchException`; token đó vẫn còn trong scanner nên cần đọc/bỏ nó hoặc chuyển sang đọc cả dòng rồi parse. Hai cách đọc vì vậy có thể phát sinh loại lỗi khác nhau dù người dùng nhập cùng một chữ.

Không bắt mọi Exception để che lỗi lập trình. Thông báo cho người dùng nên dễ hiểu; log kỹ thuật có thể giữ nguyên nhân để tìm lỗi.

~~~text
method A -> method B -> lỗi phát sinh
              ^             |
              |             v
          catch phù hợp <- exception truyền ngược
              |
      xử lý/khôi phục hoặc báo lỗi lên trên
~~~

## II. try/catch/finally Và throw

- `try` khoanh vùng đoạn có thể phát sinh exception; Java tìm `catch` đầu tiên có kiểu phù hợp. Nên bắt lỗi đủ cụ thể để có thể xử lý, tránh `catch (Exception)` chung chung làm mất tín hiệu lỗi.
- `finally` chạy khi luồng rời khỏi cấu trúc try/catch bình thường hoặc do exception, thường dùng để dọn dẹp thủ công. Bài nhập môn ưu tiên try-with-resources cho tài nguyên đóng được.
- `throw` ném một exception object tại vị trí cụ thể. Nó khác `throws` trong khai báo method; lộ trình buổi này yêu cầu `throw` ở mức cơ bản.
- try-with-resources tự đóng resource implement `AutoCloseable` khi ra khỏi khối, kể cả khi có exception. Nhiều resource được đóng theo thứ tự ngược với khai báo.

Chỉ `catch` ở nơi có thể làm việc hữu ích sau lỗi: hỏi lại input, chọn giá trị thay thế, hoặc báo thao tác lưu thất bại. Nếu method hiện tại không biết cách phục hồi, có thể để exception đi lên caller; `throws IOException` khai báo khả năng đó trong chữ ký method, còn `throw new ...` thực sự ném một lỗi.

~~~java
static int parsePositive(String input) {
    int value = Integer.parseInt(input);
    if (value <= 0) {
        throw new IllegalArgumentException("Giá trị phải lớn hơn 0");
    }
    return value;
}
~~~

`NumberFormatException` từ `parseInt` cũng có thể truyền ra nếu chuỗi không biểu diễn số nguyên. `IllegalArgumentException` ở đây do chương trình chủ động ném khi số có dạng hợp lệ nhưng vi phạm điều kiện miền giá trị. Nơi gọi có thể bắt loại lỗi phù hợp để báo rõ nguyên nhân.

`finally` chạy dù khối `try` thành công hay phát sinh exception, nên trước đây thường được dùng để đóng tài nguyên thủ công. Với resource implement `AutoCloseable`, ưu tiên try-with-resources vì nó tự đóng và giảm nguy cơ quên dọn dẹp.

~~~java
try {
    System.out.println(Integer.parseInt("42"));
} catch (NumberFormatException e) {
    System.out.println("Chuỗi không phải số nguyên");
} finally {
    System.out.println("Kết thúc thao tác parse");
}
~~~

`finally` chạy sau nhánh `try` hoặc `catch`; với input `42`, output gồm `42` rồi `Kết thúc thao tác parse`. Nếu đổi input thành `abc`, `catch` in thông báo lỗi rồi `finally` vẫn chạy.

~~~java
try (BufferedWriter writer = Files.newBufferedWriter(path)) {
    writer.write("B01,Dế Mèn phiêu lưu ký,224");
} catch (IOException e) {
    System.out.println("Không ghi được dữ liệu.");
}
~~~

Nếu muốn báo lại lỗi cho tầng gọi, method có thể ném exception thay vì tự in thông báo rồi tiếp tục như thể thành công. Chọn một nơi chịu trách nhiệm chuyển lỗi kỹ thuật thành thông báo dễ hiểu; không nuốt exception mà không ghi nhận nguyên nhân.

## III. Files, Path Và Định Dạng Lưu

`Path` biểu diễn vị trí file một cách độc lập với thao tác đọc/ghi; `Files` cung cấp các method làm việc với đường dẫn đó. Path tương đối được tính từ working directory của tiến trình, không nhất thiết là thư mục chứa source code. Khi lưu văn bản cần chọn charset nhất quán, ví dụ UTF-8, để ký tự được giải mã đúng.

~~~java
Path path = Paths.get("notes.txt");
Files.write(path, lines, StandardCharsets.UTF_8);
List<String> saved = Files.readAllLines(path, StandardCharsets.UTF_8);
~~~

Để đoạn trên dùng được, cần import `java.nio.file.Path`, `java.nio.file.Paths`, `java.nio.file.Files`, `java.nio.charset.StandardCharsets` và `java.util.List`; `lines` là một `List<String>` đã tạo trước đó. `Files.write` ghi các phần tử thành các dòng văn bản; `readAllLines` trả danh sách dòng để chương trình xử lý tiếp.

`Files.write` với cấu hình mặc định tạo file nếu cần và thay nội dung file hiện có; nếu muốn nối thêm phải chỉ rõ open option tương ứng. `readAllLines` nạp toàn bộ dòng vào bộ nhớ nên phù hợp file nhỏ; không nên mặc định cách này phù hợp file rất lớn.

CSV đơn giản thường biểu diễn mỗi bản ghi trên một dòng và tách trường bằng dấu phẩy. Nếu giá trị bên trong có dấu phân cách, dấu nháy hoặc xuống dòng thì cần quy tắc escape/quote; không thể luôn tách bằng `split(",")` an toàn. Buổi nhập môn có thể giới hạn dữ liệu mẫu không chứa dấu phẩy, nhưng phải nói rõ đó là giới hạn ví dụ chứ không phải CSV tổng quát.

Đọc lại dữ liệu là bài toán đối xứng với ghi: parse dữ liệu, xác thực trường, xử lý dòng hỏng và quyết định có bỏ qua dòng hay dừng. Mục tiêu là không để một bản ghi lỗi làm mất khả năng xử lý toàn bộ file mà không có thông báo. Với định dạng do chính ứng dụng tạo và file nhỏ, có thể đọc toàn bộ; với file lớn, đọc từng dòng/luồng để tránh giữ tất cả trong bộ nhớ. CSV chuẩn có quy tắc escape riêng, nên ví dụ tự tách bằng dấu phân cách chỉ hợp lệ khi ràng buộc dữ liệu loại trừ dấu đó.

## IV. Thực Hành Khái Niệm

1. Bọc việc parse một chuỗi thành số bằng `try/catch`; cho phép người dùng sửa input sai.
2. Viết một method kiểm tra điều kiện dữ liệu và dùng `throw` để báo input không hợp lệ.
3. Ghi nhiều dòng văn bản UTF-8 vào file rồi đọc lại; so sánh nội dung trước/sau.
4. Thử file chưa tồn tại, file rỗng, dòng hỏng và đường dẫn không hợp lệ; phân biệt exception nào cần được xử lý.
5. Vẽ luồng dữ liệu: object/dữ liệu trong chương trình → định dạng text → file → parse → dữ liệu dùng lại.

~~~text
Dữ liệu trong chương trình -> encode/serialize thành dòng -> file
File -> đọc dòng -> parse + validate -> dữ liệu trong chương trình
~~~

## Phần Mở Rộng Và Nâng Cao — Tìm Hiểu Thêm

- **Regex (mở rộng):** biểu thức chính quy mô tả mẫu chuỗi; có thể dùng `String.matches()` cho kiểm tra toàn chuỗi như mã đơn hoặc định dạng cơ bản. Regex xác nhận hình dạng chuỗi, không chứng minh email/số điện thoại thật sự tồn tại. Tránh dạy pattern phức tạp trong buổi nhập môn.
- **Custom exception (nâng cao):** tạo lớp exception riêng khi muốn gọi tên một lỗi nghiệp vụ như số trang không hợp lệ. Bài tập: tạo `InvalidPageCountException` và ném khi số trang nhỏ hơn 1; bắt lỗi tại nơi gọi để hiển thị thông báo. Đây là bài riêng, không bắt buộc để đọc/ghi file.
- **Thread (nâng cao, nếu còn thời gian):** một tiến trình có thể có nhiều luồng thực thi; hai luồng chạm cùng trạng thái có thể tạo race condition. Chỉ demo khái niệm qua bộ đếm dùng chung và yêu cầu dự đoán kết quả; không yêu cầu viết đa luồng để hoàn thành ứng dụng.

Các nội dung trên là phần khám phá tùy chọn. Bài thực hành cốt lõi vẫn là xử lý lỗi, quản lý resource và đọc/ghi dữ liệu file.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Chương trình crash khi nhập chữ | Chuyển số không được bắt | Bắt NumberFormatException và hỏi lại |
| File không đóng | Resource mở nhưng không đóng | Dùng try-with-resources |
| Không tìm thấy file | Path tương đối từ working directory khác | In/kiểm tra thư mục chạy và Path |
| Dữ liệu bị tách sai | CSV đơn giản gặp dấu phẩy trong tên | Giữ giới hạn mẫu hoặc dùng format đúng ở phần sau |

## Tổng Kết Buổi 6

- Exception chuyển luồng tới handler phù hợp; try-with-resources quản lý vòng đời tài nguyên.
- Files/Path tách vị trí file khỏi thao tác đọc/ghi; định dạng text cần quy tắc parse/validate rõ.
- File văn bản phù hợp dữ liệu nhỏ, tuần tự; cần parse và validate khi đọc lại. Truy vấn có cấu trúc là một trong những lý do dùng database.

### Câu Hỏi Ôn Tập

1. try, catch, finally khác vai trò thế nào?
2. Vì sao try-with-resources phù hợp với file?
3. Files và Path dùng để làm gì?
4. Khi nào nên ném lỗi thay vì in thông báo trong setter?
5. Vì sao file CSV mẫu cần nêu giới hạn dấu phẩy?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 06](../BTVN/buoi-06-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Exceptions](https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html)
- [Oracle — File I/O](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html)
- [Java SE API — Files](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html)
