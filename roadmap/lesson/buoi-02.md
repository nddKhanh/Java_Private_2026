# Buổi 2: Điều Kiện, Vòng Lặp, Method, String và ArrayList

---

## Mục tiêu buổi học

- Giải thích cách điều kiện boolean điều khiển nhánh; chọn if/else hoặc switch phù hợp.
- Chọn và sử dụng for, while, do-while, for-each; hiểu thứ tự chạy, điểm dừng và lỗi biên thường gặp.
- Tách thao tác thành method có hoặc không có tham số/giá trị trả về.
- Truyền dữ liệu cơ bản và dùng final/static final cho hằng số.
- Giải thích String bất biến và chọn đúng cách so sánh nội dung.
- Dùng List/ArrayList để thêm, xóa, sửa, duyệt, tìm kiếm; phân biệt danh sách động với mảng có kích thước cố định.

> **Nối tiếp:** Buổi 1 làm quen với lệnh chạy tuần tự, biến và phép tính. Buổi này học cách chương trình rẽ nhánh, lặp lại thao tác, chia xử lý thành method và biểu diễn văn bản/danh sách. Các khái niệm được luyện bằng ví dụ độc lập; menu giỏ hàng là phần bài tập tích lũy về nhà.

## I. Điều Khiển Luồng Giải Quyết Việc Gì?

Chương trình tuần tự thực hiện lần lượt từng lệnh. Nhưng trong nhiều bài toán, hành động tiếp theo phụ thuộc dữ liệu: điểm có đạt không, số nhập có hợp lệ không, hoặc người dùng chọn chức năng nào. Điều kiện cho phép chọn nhánh; vòng lặp cho phép lặp lại một nhóm lệnh mà không sao chép chúng nhiều lần. Menu là một ví dụ quen thuộc, nhưng hai cấu trúc này dùng được trong mọi loại chương trình.

~~~java
int choice;
do {
    System.out.println("1. Thêm sản phẩm");
    System.out.println("2. Xem giỏ hàng");
    System.out.println("0. Thoát");
    choice = scanner.nextInt();

    switch (choice) {
        case 1:
            System.out.println("Thêm sản phẩm");
            break;
        case 2:
            System.out.println("Xem giỏ hàng");
            break;
        case 0:
            System.out.println("Kết thúc");
            break;
        default:
            System.out.println("Lựa chọn không hợp lệ");
    }
} while (choice != 0);
~~~

## II. Điều Kiện Và Vòng Lặp

### Điều Kiện Là Biểu Thức Boolean

Điều kiện trong Java phải cho ra một giá trị `boolean`: `true` hoặc `false`. Các phép so sánh như `>`, `<=`, `==` tạo ra boolean; `&&`, `||`, `!` ghép hoặc đảo các điều kiện boolean. Khác với một số ngôn ngữ, Java không tự hiểu số `0` là false hay số khác `0` là true.

~~~java
int score = 72;
boolean passed = score >= 50;

if (passed) {
    System.out.println("Đạt");
} else {
    System.out.println("Chưa đạt");
}
~~~

`if` kiểm tra điều kiện tại thời điểm chương trình chạy. Nếu kết quả là true, chạy khối `if`; nếu false, bỏ qua khối đó và xét `else` nếu có. Các nhánh `else if` được kiểm tra từ trên xuống; nhánh đầu tiên đúng sẽ chạy, những nhánh còn lại bị bỏ qua. Vì vậy cần sắp xếp điều kiện có thứ tự và không để một nhánh rộng che mất nhánh cụ thể hơn.

~~~java
if (score >= 80) {
    System.out.println("Giỏi");
} else if (score >= 50) {
    System.out.println("Đạt");
} else {
    System.out.println("Chưa đạt");
}
~~~

Trong ví dụ, kiểm tra `score >= 50` trước `score >= 80` sẽ khiến điểm từ 80 trở lên rơi vào nhánh Đạt và không bao giờ tới nhánh Giỏi. Đây là lỗi thứ tự điều kiện thường gặp. Với khoảng giá trị, ghi rõ cả hai biên, ví dụ `score >= 50 && score <= 79`; đừng nhầm `&&` (cả hai vế đúng) với `||` (ít nhất một vế đúng).

`&&` và `||` có cơ chế **short-circuit**: Java chỉ đánh giá vế phải khi cần thiết. Có thể dùng thứ tự này để kiểm tra điều kiện an toàn trước khi truy cập dữ liệu:

~~~java
if (scores.size() > 0 && scores.get(0) >= 50) {
    System.out.println("Phần tử đầu tiên đạt");
}
~~~

Nếu danh sách rỗng, vế đầu là false nên Java không gọi `scores.get(0)`. Đặt biểu thức có thể lỗi ở vế phải của `&&` là một kỹ thuật hữu ích; với `||`, khi vế đầu đã true thì vế phải không được đánh giá. Dùng ngoặc đơn khi ghép nhiều điều kiện để làm rõ ý định.

### Chọn `if/else` Hay `switch`?

| Cấu trúc | Dùng khi | Ví dụ dạng điều kiện |
|---|---|---|
| `if/else` | Kiểm tra khoảng, điều kiện phức hợp hoặc nhiều biến | `score >= 50`, `age >= 18 && hasCard` |
| `switch` | So khớp một biểu thức với các giá trị rời nhau | `choice` bằng 1, 2, 3 hoặc giá trị mặc định |

`switch` giúp các lựa chọn rời nhau dễ đọc hơn một chuỗi `else if`. Với cú pháp switch dạng truyền thống đang dùng trong lộ trình, `break` kết thúc case hiện tại; nếu quên, luồng có thể chạy tiếp sang case kế tiếp (fall-through). `default` xử lý giá trị không khớp case nào. Không dùng switch để kiểm tra khoảng như “điểm từ 50 đến 79”; trường hợp đó phù hợp với `if/else`.

~~~java
switch (choice) {
    case 1:
        System.out.println("Bắt đầu");
        break;
    case 2:
        System.out.println("Xem hướng dẫn");
        break;
    default:
        System.out.println("Lựa chọn không hợp lệ");
}
~~~

### Vòng Lặp Là Gì?

Vòng lặp chạy lại một khối lệnh khi điều kiện còn đúng. Mỗi vòng chạy thân vòng lặp một lần; giá trị dùng trong điều kiện cần thay đổi theo cách nào đó để cuối cùng vòng lặp dừng. Nếu điều kiện luôn đúng và không có lối thoát, chương trình bị lặp vô hạn.

| Loại vòng lặp | Kiểm tra điều kiện | Dùng khi |
|---|---|---|
| `for` | Trước mỗi lượt; khởi tạo, điều kiện và cập nhật đặt cùng dòng | Biết số lượt hoặc cần biến đếm/index |
| `while` | Trước mỗi lượt | Chưa biết số lượt; lặp đến khi một trạng thái thay đổi |
| `do-while` | Sau thân vòng lặp | Thân cần chạy ít nhất một lần, ví dụ hỏi nhập lại hoặc hiện menu |
| for-each | Tự lấy từng phần tử cho đến hết | Duyệt toàn bộ mảng/danh sách khi không cần index |

### Luồng Chạy Của Từng Vòng

Trong `for`, Java chạy phần khởi tạo một lần, kiểm tra điều kiện, chạy thân vòng, chạy phần cập nhật rồi quay lại kiểm tra điều kiện. Nếu điều kiện sai ngay lần đầu, thân vòng không chạy lần nào.

~~~java
for (int i = 0; i < 3; i++) {
    System.out.println(i);
}
~~~

Thứ tự in là `0`, `1`, `2`: bắt đầu tại 0, mỗi lượt tăng 1, dừng khi `i < 3` sai. Với mảng hoặc danh sách có `n` phần tử, index hợp lệ từ `0` đến `n - 1`; vì vậy điều kiện phổ biến là `i < n`, không phải `i <= n`. Đây là cách tránh lỗi lệch một đơn vị (off-by-one).

`while` kiểm tra điều kiện trước thân vòng lặp, nên có thể không chạy lần nào:

~~~java
int number = 1;
while (number <= 3) {
    System.out.println(number);
    number++;
}
~~~

`do-while` chạy thân trước, sau đó mới kiểm tra điều kiện; dấu chấm phẩy sau `while (...)` là bắt buộc:

~~~java
int answer;
do {
    System.out.print("Nhập số dương: ");
    answer = scanner.nextInt();
} while (answer <= 0);
~~~

for-each lấy từng phần tử theo thứ tự, không cần tự quản lý index:

~~~java
for (int score : scores) {
    System.out.println(score);
}
~~~

Đọc code vòng lặp bằng ba câu hỏi: **biến/trạng thái bắt đầu là gì?**, **điều kiện nào giữ vòng chạy?**, **điều gì thay đổi sau mỗi lượt?** Nếu không tìm được chỗ làm điều kiện thay đổi, kiểm tra khả năng lặp vô hạn. Khi cần index, dừng ở một vị trí cụ thể hoặc cập nhật phần tử, dùng `for` thường; khi chỉ đọc từng phần tử, for-each thường dễ hiểu hơn.

### `break` Và `continue`

- `break` thoát ngay khỏi vòng lặp gần nhất. Dùng khi đã tìm thấy kết quả hoặc gặp điều kiện kết thúc rõ ràng.
- `continue` bỏ phần còn lại của thân vòng trong lượt hiện tại rồi chuyển sang lượt tiếp theo. Với `for`, phần cập nhật vẫn chạy trước khi kiểm tra điều kiện lượt kế; với `while`, chương trình quay về kiểm tra điều kiện.
- Trong vòng lặp lồng nhau, hai lệnh này chỉ tác động tới vòng gần nhất. Dùng quá nhiều `break`/`continue` có thể làm khó lần theo luồng; nếu logic rối, tách thành method hoặc viết điều kiện rõ hơn.

~~~java
for (int number = 1; number <= 5; number++) {
    if (number == 3) {
        continue; // bỏ qua phần in ở lượt số 3
    }
    System.out.println(number);
}
~~~

Mẹo với `Scanner`: nếu gọi `nextInt()` rồi sau đó gọi `nextLine()`, `nextInt()` chỉ lấy token số và để lại ký tự xuống dòng. `nextLine()` tiếp theo có thể đọc phần còn lại của dòng đó ngay lập tức. Có thể đọc toàn bộ bằng `nextLine()` rồi chuyển đổi, hoặc gọi thêm `nextLine()` để bỏ phần xuống dòng còn lại trước khi đọc dòng văn bản kế tiếp. Cách đọc cả dòng rồi chuyển đổi thường giúp xử lý input sai nhất quán hơn.

> **Hình minh họa cần bổ sung:** Lưu đồ rẽ nhánh `if/else`, đường đi qua `switch`, và vòng `for`/`while`/`do-while` đánh dấu vị trí kiểm tra điều kiện.

## III. Method, Tham Số Và Giá Trị Trả Về

Method đặt tên cho một thao tác để có thể gọi lại, kiểm tra và ghép với thao tác khác. Tham số là dữ liệu method cần nhận; kiểu trả về cho biết method gửi lại kết quả gì. `void` nghĩa là method không trả kết quả. Method được gọi trực tiếp từ `main` trong bài này khai báo `static`; đây là quy tắc cần dùng, chưa phải phần học sâu về static cấp class.

~~~java
static boolean isPassing(int score) {
    return score >= 50;
}
~~~

`score` nhận một số nguyên khi gọi `isPassing(72)`, còn `return` gửi lại giá trị `true` cho nơi gọi. Với tham số kiểu nguyên thủy, method nhận bản sao của giá trị; với object như danh sách, method nhận bản sao của tham chiếu nên vẫn có thể gọi `add()` hoặc `remove()` trên cùng object. Gán tham số sang một object khác không đổi biến tham chiếu bên ngoài. Đây là mức hiểu cơ bản cần cho các ví dụ trong buổi.

## IV. String — Bản Chất Và Cách Xử Lý Văn Bản

`String` là kiểu biểu diễn một chuỗi ký tự. Ta thường dùng nó để lưu tên, mã, nội dung nhập từ bàn phím và thông báo. String là **immutable** (bất biến): sau khi một object String được tạo, nội dung của object đó không bị sửa tại chỗ. Những method xử lý chuỗi trả về một String mới; biến cũ vẫn tham chiếu tới giá trị ban đầu nếu ta không gán lại.

~~~java
String name = "  Ao thun  ";
String cleanedName = name.trim();

System.out.println(name);        // vẫn là "  Ao thun  "
System.out.println(cleanedName); // "Ao thun"
~~~

Điều này quan trọng vì lời gọi `trim()` không tự thay đổi `name`. Nếu muốn giữ kết quả, phải nhận giá trị trả về như trên. Tương tự, phép nối chuỗi bằng `+` tạo ra kết quả mới:

~~~java
String firstName = "Nguyen";
String fullName = firstName + " Van An";
~~~

### So Sánh Hai String

Biến kiểu tham chiếu lưu thông tin để truy cập object. Vì vậy `==` kiểm tra hai biến có tham chiếu cùng một object hay không; nó không phải cách kiểm tra hai đoạn văn bản có cùng nội dung. Dùng `equals()` để so sánh nội dung:

~~~java
String inputName = new String("Ao thun");
String savedName = new String("Ao thun");

System.out.println(inputName == savedName);      // false: hai object khác nhau
System.out.println(inputName.equals(savedName)); // true: nội dung giống nhau
~~~

Trong ứng dụng thật, không nên tạo String bằng `new` như ví dụ trên; cách viết này chỉ làm rõ khác biệt giữa so sánh tham chiếu và so sánh giá trị. Khi so sánh không phân biệt chữ hoa/thường, dùng `equalsIgnoreCase()`. Để kiểm tra input rỗng, dùng `isEmpty()`; nếu cần loại cả khoảng trắng đầu/cuối, xử lý chuỗi trước rồi kiểm tra.

### Một Số Thao Tác Thường Dùng

| Nhu cầu | Method / cách viết | Ý nghĩa |
|---|---|---|
| Ghép thông tin | `"Tên: " + name` | Tạo nội dung mới từ các giá trị |
| Đếm độ dài | `name.length()` | Trả về số đơn vị ký tự trong String |
| Lấy ký tự tại vị trí | `name.charAt(index)` | Index bắt đầu từ 0; index không hợp lệ gây lỗi |
| Tìm đoạn văn bản | `name.contains("thun")` | Kiểm tra chuỗi con có xuất hiện không |
| Tìm vị trí | `name.indexOf("thun")` | Trả về index đầu tiên hoặc `-1` nếu không thấy |
| Lấy một phần | `name.substring(start, end)` | Lấy từ `start`, không bao gồm `end` |
| Bỏ khoảng trắng ASCII biên | `name.trim()` | Trả về chuỗi mới đã bỏ ký tự có mã không lớn hơn U+0020 ở hai đầu |
| So sánh nội dung | `name.equals(other)` | Phân biệt hoa/thường |

Index của String bắt đầu từ 0 giống index của mảng. `length()` đếm đơn vị mã UTF-16; với ký tự tiếng Anh và phần lớn ví dụ nhập môn, có thể hiểu gần như số ký tự, nhưng một số ký tự Unicode cần hai đơn vị. Vì thế vị trí cuối là `length() - 1`, còn `charAt(length())` là sai index. Một chuỗi rỗng có `length() == 0`; cần cẩn thận không gọi `charAt(0)` trong trường hợp này. `trim()` cũng chỉ bỏ một nhóm ký tự khoảng trắng ASCII ở hai đầu, không phải mọi ký tự được Unicode xem là khoảng trắng.

String hữu ích khi cần lưu, so sánh, tìm kiếm hoặc hiển thị văn bản. Khi chuẩn hóa input, cần chọn thao tác phù hợp với quy tắc dữ liệu; có thể bỏ khoảng trắng ASCII biên rồi dùng `equalsIgnoreCase()` để so sánh không phân biệt hoa/thường. Không nên dùng `==` để so sánh nội dung.

## V. List Và ArrayList — Danh Sách Có Thứ Tự

`List<E>` là **interface** mô tả một danh sách có thứ tự, truy cập được bằng index và cho phép lưu các phần tử trùng nhau. `E` là kiểu phần tử. `ArrayList<E>` là một class triển khai hợp đồng `List<E>` bằng một cấu trúc mảng có thể tự tăng sức chứa. Vì vậy ta thường khai báo theo hợp đồng, khởi tạo bằng cách triển khai:

~~~java
import java.util.ArrayList;
import java.util.List;

List<String> names = new ArrayList<>();
names.add("Áo thun");
names.add("Quần jeans");

System.out.println(names.get(0)); // Áo thun
~~~

Khai báo biến là `List<String>` cho biết ta cần các hành vi của một danh sách; `new ArrayList<>()` chọn cách triển khai cụ thể. Ở buổi này, học viên cần biết cách **dùng** generic có sẵn như `List<String>` hoặc `ArrayList<Integer>`, chưa cần tự viết class generic.

### Các Thao Tác Và Ý Nghĩa

~~~java
List<String> names = new ArrayList<>();
names.add("Áo thun");       // thêm cuối danh sách
names.add("Quần jeans");
names.set(1, "Quần kaki");  // thay phần tử tại index 1
String first = names.get(0); // đọc phần tử tại index 0
int count = names.size();    // số phần tử hiện có
boolean found = names.contains("Áo thun");
names.remove(0);             // xóa phần tử tại index 0
~~~

`size()` là số phần tử đang có, không phải số chỗ chứa tối đa. `get()` và `set()` yêu cầu index nằm trong khoảng từ `0` đến `size() - 1`. Khi `remove()` nhận một số nguyên, nó xóa phần tử ở index đó; để xóa theo nội dung, có thể truyền object cần xóa. Khi danh sách có thể trống, kiểm tra `isEmpty()` hoặc `size()` trước khi lấy phần tử.

`ArrayList` hợp với dữ liệu cần giữ thứ tự, truy cập theo vị trí, thêm cuối và duyệt nhiều lần. `contains()` và tìm kiếm theo điều kiện thường phải kiểm tra lần lượt các phần tử, nên thời gian tăng theo số lượng phần tử. Chèn hoặc xóa ở giữa cũng cần dời các phần tử phía sau để giữ thứ tự. Không cần ghi nhớ cách tăng sức chứa bên trong; điểm cần hiểu là số phần tử có thể thay đổi trong khi index vẫn theo thứ tự.

### Generic Và Kiểu Số Nguyên Thủy

`ArrayList` lưu object, vì thế kiểu phần tử phải là kiểu tham chiếu như `String`, `Double`, `Integer`. Không thể viết `ArrayList<int>`; dùng `ArrayList<Integer>`. Java tự chuyển đổi qua lại giữa `int` và `Integer` trong những trường hợp phù hợp (autoboxing/unboxing), nhưng học viên nên nhận biết rằng wrapper như `Integer` có thể là `null`, còn `int` thì không.

~~~java
List<Integer> quantities = new ArrayList<>();
quantities.add(2);             // int được chuyển thành Integer
int quantity = quantities.get(0); // Integer được chuyển lại thành int
~~~

## VI. Mảng Và ArrayList Khác Nhau Thế Nào?

Mảng (`array`) và `ArrayList` đều lưu nhiều phần tử cùng kiểu và cho truy cập theo index. Khác biệt chính là kích thước của mảng được chốt khi tạo, còn số phần tử của `ArrayList` có thể tăng/giảm trong quá trình chạy.

| Tiêu chí | Mảng | `ArrayList<E>` |
|---|---|---|
| Kích thước | Cố định sau khi tạo; muốn đổi kích thước phải tạo mảng khác và tự sao chép | Số phần tử có thể thay đổi bằng `add()`/`remove()`; class tự quản lý sức chứa |
| Lấy phần tử | `array[index]` | `list.get(index)` |
| Số phần tử | `array.length` | `list.size()` |
| Thêm/xóa | Không có thao tác thêm/xóa; người viết phải quản lý vị trí và sao chép | Có sẵn `add`, `remove`, `set` |
| Kiểu dữ liệu | Có thể lưu primitive như `int` hoặc object như `String`; phần tử có thể được gán lại | Lưu object; primitive dùng wrapper như `Integer` |
| Khi phù hợp | Số lượng đã biết/cố định hoặc cần biểu diễn trực tiếp cấu trúc mảng | Số lượng thay đổi, cần thao tác danh sách thuận tiện |

~~~java
int[] scores = new int[3]; // độ dài luôn là 3
scores[0] = 8;
System.out.println(scores.length);

List<Integer> scoreList = new ArrayList<>();
scoreList.add(8);
scoreList.add(9); // danh sách tăng số phần tử
System.out.println(scoreList.size());
~~~

Hai cấu trúc cùng hỗ trợ truy cập theo index trong thời gian hằng số. Với `ArrayList`, thêm cuối thường có chi phí trung bình thấp (amortized); đôi lúc cấu trúc phải cấp sức chứa mới và chép phần tử. Tìm kiếm bằng `contains()` hoặc duyệt tuần tự là tuyến tính; chèn/xóa ở giữa cũng tuyến tính vì phải dịch chuyển phần tử. Đây là cách mô tả độ phức tạp thông dụng; lộ trình không yêu cầu học cấu trúc cài đặt hay tự xây dynamic array.

Chọn mảng khi kích thước đã cố định và muốn lưu primitive trực tiếp, hoặc khi bài học đang tập trung vào index. Chọn `ArrayList` khi danh sách thay đổi số lượng và cần các thao tác thêm/xóa/sửa thường xuyên. Không có cấu trúc nào luôn tốt hơn; hãy căn cứ vào kích thước dữ liệu và thao tác cần thực hiện.

> **Hình minh họa cần bổ sung:** Minh họa mảng có độ dài cố định và một `ArrayList` có số phần tử/capacity tách biệt; đánh dấu các bước phần tử được dời khi xóa ở giữa.

## VII. Hằng Số Và Truyền Tham Số

`final` ngăn biến được gán lại sau lần gán đầu tiên. `static final` thường dùng cho hằng số chung ở cấp class, ví dụ mức VAT. Với biến tham chiếu `final`, không thể trỏ biến đó sang object khác, nhưng điều đó không tự làm cho object bất biến.

~~~java
static final double VAT_RATE = 0.1;
final int MAX_ITEMS = 20;
~~~

Truyền primitive vào method tạo một bản sao giá trị để method nhận. Với object, giá trị tham chiếu được truyền vào method; method có thể thay đổi trạng thái object thông qua tham chiếu đó, nhưng gán lại biến tham số không làm biến bên ngoài trỏ sang object khác. Đây chỉ là mức giải thích cần dùng cho việc truyền danh sách vào method, chưa đi sâu về bộ nhớ.

## VIII. Ví Dụ Tổng Hợp: Method Nhận Một Danh Sách

Ví dụ sau dùng điểm kiểm tra để kết hợp method, `List<Integer>` và vòng lặp. Method không phụ thuộc vào menu hay một ứng dụng cụ thể; cùng cách tổ chức có thể tái sử dụng trong nhiều bài toán khác.

~~~java
static double calculateAverage(List<Integer> scores) {
    if (scores.isEmpty()) {
        return 0;
    }

    int total = 0;
    for (int score : scores) {
        total += score;
    }
    return (double) total / scores.size();
}
~~~

`for-each` phù hợp vì chỉ cần đọc lần lượt từng điểm, không cần index. Ép `total` sang `double` trước khi chia giúp phép chia giữ phần thập phân. Trường hợp danh sách rỗng được xử lý trước để không chia cho 0. Khi cần ghép các phần tử theo cùng index hoặc sửa một vị trí cụ thể, vòng `for` thông thường sẽ phù hợp hơn.

## Phần Nâng Cao — Debug Bằng IDE

Debugger cho phép tạm dừng chương trình tại breakpoint để quan sát trạng thái thật, thay vì chỉ đoán từ kết quả cuối. Đặt breakpoint ở đầu vòng lặp hoặc trước nhánh cần kiểm tra; chạy Debug; dùng Step Over để chạy lệnh hiện tại, Step Into để đi vào method, Resume để chạy tiếp; xem giá trị biến ở Variables/Watches. Console vẫn hữu ích để đọc input/output, còn debugger giúp xác định chính xác biến đổi xảy ra ở bước nào.

**Bài tập tự chọn:** Đặt breakpoint trong vòng lặp tính tổng, theo dõi index/giá trị/tổng qua từng lượt; thử một input gây nhánh sai và quan sát điều kiện. Đây là kỹ năng gỡ lỗi tùy chọn, không phải yêu cầu để hoàn thành bài menu.

### Chuỗi Thực Hành Về Kiến Thức

1. Viết điều kiện phân loại điểm thành Giỏi/Đạt/Chưa đạt; thử các giá trị ngay dưới, đúng tại và ngay trên từng mốc.
2. Viết cùng một lựa chọn bằng `if/else` và `switch`; nêu cách xử lý một giá trị không hợp lệ.
3. Dùng `for`, `while`, `do-while` in các số 1 đến 10; giải thích thời điểm kiểm tra điều kiện và vòng nào chạy ít nhất một lượt.
4. Viết vòng lặp tính tổng các số từ 1 đến `n`; thử `n = 0`, `n = 1` và một số lớn hơn để tìm lỗi lệch biên.
5. Duyệt danh sách và bỏ qua số âm bằng `continue`; tìm số đầu tiên chia hết cho 7 rồi dừng bằng `break`.
6. Dự đoán khi nào vế phải được chạy với `&&` và `||`; áp dụng short-circuit để không truy cập phần tử đầu khi danh sách rỗng.
7. Dự đoán kết quả `trim()`, `substring()` và `equals()` trước khi chạy chương trình.
8. Viết đoạn kiểm tra tên nhập vào có khớp tên đã lưu hay không, không phân biệt hoa/thường.
9. Tạo `List<Integer>`, thêm, sửa, xóa, tìm và duyệt các số; giải thích `size()` và index sau mỗi thao tác.
10. Viết cùng một thao tác đọc phần tử cho mảng và `ArrayList`; giải thích điểm khác nhau giữa `length` và `size()`.
11. Tính điểm trung bình từ danh sách bằng for-each; xử lý danh sách rỗng và giải thích vì sao không cần index.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| Menu chỉ chạy một lần | Chưa đặt menu trong vòng lặp | Dùng do-while đến lựa chọn thoát |
| Nhánh luôn sai hoặc sai thứ tự | Điều kiện biên/logic chưa đúng hoặc nhánh rộng đứng trước nhánh cụ thể | Thử giá trị biên, đọc điều kiện từ trái sang phải và sắp nhánh cụ thể trước |
| Vòng lặp chạy mãi | Trạng thái trong điều kiện không được cập nhật hoặc điều kiện dừng sai | Theo dõi giá trị đầu, điều kiện tiếp tục và bước cập nhật |
| Bỏ sót phần tử cuối | Dùng `i <= size()` khiến index vượt giới hạn, hoặc dừng quá sớm | Với index từ 0, thường lặp khi `i < size()` |
| `do-while` báo lỗi cú pháp | Thiếu dấu `;` sau `while (condition)` | Viết `} while (condition);` |
| `switch` chạy xuyên qua case | Quên `break` trong cú pháp switch truyền thống | Thêm `break` nếu không chủ ý chạy tiếp |
| Dòng nhập kế tiếp bị rỗng | `nextInt()` để lại ký tự xuống dòng trước `nextLine()` | Đọc và bỏ phần còn lại của dòng hoặc đọc toàn bộ bằng `nextLine()` rồi chuyển đổi |
| Danh sách lệch nhau | Cập nhật một list nhưng quên list khác | Mọi thao tác phải cập nhật cùng index |
| So sánh tên bằng == | `==` kiểm tra hai biến có cùng tham chiếu object | Dùng `equals()` hoặc `equalsIgnoreCase()` để so sánh nội dung |
| Nghĩ rằng `trim()` sửa String cũ | String bất biến; method trả về kết quả mới | Gán kết quả vào biến mới hoặc biến hiện có |
| Nhầm `length` và `size()` | Mảng có field `length`; List có method `size()` | Dùng đúng cú pháp theo cấu trúc dữ liệu |
| Index out of bounds | Sai index hoặc list rỗng | Kiểm tra `size()` trước khi `get()`/`remove()` |
| Viết `ArrayList<int>` | Generic nhận kiểu tham chiếu, không nhận primitive | Dùng `ArrayList<Integer>` |

## Tổng Kết Kiến Thức

- Điều kiện chọn nhánh; vòng lặp lặp lại xử lý; method đóng gói thao tác và nhận/trả dữ liệu.
- String bất biến; dùng `equals()` để so sánh nội dung, và ghi nhận kết quả mới từ các method xử lý chuỗi.
- `List` là hợp đồng danh sách có thứ tự; `ArrayList` là một triển khai có thể đổi số phần tử.
- Mảng có độ dài cố định và có thể lưu primitive; ArrayList linh hoạt hơn nhưng lưu kiểu tham chiếu và có chi phí khi tìm/chèn/xóa.
- Buổi 3 tiếp tục học class/object để gom các dữ liệu liên quan; phần danh sách song song chỉ là ví dụ cho bài tập tích lũy.

### Câu Hỏi Ôn Tập

1. Vì sao điều kiện Java phải là boolean? `&&` và `||` khác nhau thế nào?
2. Short-circuit là gì? Vì sao kiểm tra `size() > 0` trước `get(0)` có thể tránh lỗi?
3. Khi nào dùng `if/else`, khi nào dùng `switch`? Lỗi gì có thể xảy ra nếu quên `break`?
4. `for`, `while`, `do-while` kiểm tra điều kiện ở thời điểm nào? Loại nào bảo đảm thân chạy ít nhất một lần?
5. Vì sao lặp qua index thường dùng `i < size()` thay vì `i <= size()`? Nêu một ví dụ lỗi lệch biên.
6. Điều gì có thể làm vòng lặp vô hạn? Khi `continue` nằm trong `for`, phần cập nhật có chạy không?
7. Method có tham số và giá trị trả về giúp gì?
8. String bất biến có nghĩa gì? Gọi `trim()` mà không gán kết quả thì biến ban đầu ra sao?
9. `==` và `equals()` khác nhau thế nào khi so sánh String?
10. `List` và `ArrayList` khác nhau về vai trò ra sao?
11. So sánh mảng với ArrayList về kích thước, cú pháp lấy phần tử, kiểu dữ liệu và thao tác thêm/xóa.
12. Vì sao `ArrayList<int>` không hợp lệ? `ArrayList<Integer>` hoạt động thế nào khi thêm một giá trị int?
13. Vì sao truy cập giữa list rỗng gây lỗi? `size()` khác sức chứa thế nào?

## Bài Tập Về Nhà

Xem yêu cầu và phần bài tập tự chọn trong file riêng: [Bài tập về nhà Buổi 2](../BTVN/buoi-02-btvn.md).

## Tài Liệu Tham Khảo

- [Oracle — Control Flow Statements](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/flow.html)
- [Oracle — String](https://docs.oracle.com/javase/tutorial/java/data/strings.html)
- [Oracle — The List Interface](https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html)
- [Java API — String](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/String.html)
- [Java API — List](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/List.html)
- [Java API — ArrayList](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ArrayList.html)
- [Oracle — Arrays](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html)

