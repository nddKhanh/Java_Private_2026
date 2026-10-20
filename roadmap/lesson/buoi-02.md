# Buổi 2: Điều Kiện, Vòng Lặp, Method, String Và ArrayList

---

> **Hạn chế từ buổi 1:** chương trình mới chỉ chạy tuần tự một lần; chưa thể chọn chức năng, lặp thao tác hoặc quản lý nhiều giá trị cùng loại.

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Dùng `if/else` và `switch` để chọn hành động theo dữ liệu.
- Dùng `for`, `while`, `do-while`, `break`, `continue` đúng tình huống.
- Tách thao tác thành method có tham số và giá trị trả về.
- So sánh/xử lý String bằng method phù hợp.
- Dùng `List`/`ArrayList` để thêm, xóa, duyệt và tính tổng.
- Xây dựng một chương trình menu quản lý nhiều dòng dữ liệu.

> **Kết quả cuối buổi:** chương trình mẫu có menu thêm, xóa, xem danh sách và tính tổng từ nhiều dòng dữ liệu.

> **Môi trường:** JDK 17.

## I. Từ Chương Trình Chạy Một Lần Sang Menu

Buổi 1 luôn làm bốn bước rồi kết thúc. Một menu cần hai khả năng mới:

```text
Chọn nhánh theo lựa chọn người dùng → điều kiện/switch
Lặp lại cho đến khi chọn thoát       → vòng lặp
```

Khung đầu tiên chưa cần chức năng thật:

```java
int choice;
do {
    System.out.println("1. Thêm mặt hàng");
    System.out.println("2. Xóa mặt hàng");
    System.out.println("3. Xem giỏ");
    System.out.println("4. Xem tổng tiền");
    System.out.println("0. Thoát");
    choice = scanner.nextInt();

    switch (choice) {
        case 1 -> System.out.println("Đang thêm");
        case 2 -> System.out.println("Đang xóa");
        case 3 -> System.out.println("Đang xem giỏ");
        case 4 -> System.out.println("Đang tính tổng");
        case 0 -> System.out.println("Kết thúc.");
        default -> System.out.println("Lựa chọn không hợp lệ.");
    }
} while (choice != 0);
```

Chạy khung này trước. Khi menu lặp và thoát đúng, mới thay từng dòng placeholder bằng method thật.

## II. Điều Kiện: Một Biểu Thức Boolean Chọn Nhánh

```java
if (price <= 0) {
    System.out.println("Giá phải lớn hơn 0.");
} else {
    System.out.println("Giá hợp lệ.");
}
```

Các toán tử thường dùng:

| Nhóm | Toán tử | Ví dụ |
|---|---|---|
| So sánh | `>`, `<`, `>=`, `<=`, `==`, `!=` | `quantity > 0` |
| Logic | `&&`, `||`, `!` | `price > 0 && quantity > 0` |

`if/else` phù hợp với khoảng giá trị hoặc điều kiện kết hợp. `switch` phù hợp khi một giá trị rời rạc chọn một hành động, như menu 0–4.

```java
boolean validItem = !name.isBlank() && price > 0 && quantity > 0;
if (!validItem) {
    System.out.println("Mặt hàng không hợp lệ.");
}
```

Trong BTVN, học viên phải xử lý lựa chọn menu sai và index xóa sai. Kiểm tra dữ liệu nhập sâu hơn sẽ được củng cố ở buổi exception.

## III. Vòng Lặp Và Điểm Dừng

### `for`: biết số lượt hoặc cần index

```java
for (int i = 0; i < names.size(); i++) {
    System.out.println((i + 1) + ". " + names.get(i));
}
```

Index hợp lệ chạy từ 0 đến `size() - 1`. Số thứ tự cho người dùng bắt đầu từ 1 nên hiển thị `i + 1`.

### `for-each`: chỉ cần đọc từng phần tử

```java
for (String name : names) {
    System.out.println(name);
}
```

For-each dễ đọc nhưng không phù hợp khi cần ghép ba list theo cùng index.

### `while` và `do-while`: lặp theo điều kiện

```java
while (choice != 0) {
    // có thể không chạy lần nào nếu choice đã bằng 0
}
```

```java
do {
    // chạy ít nhất một lần, phù hợp menu
} while (choice != 0);
```

`break` dừng vòng lặp hiện tại; `continue` bỏ phần còn lại của lượt hiện tại. Không dùng chúng để che một điều kiện dừng không rõ ràng.

### Kiểm tra biên

Với list có 2 phần tử, thử `i = 0`, `i = 1`, `i = 2`. Học viên cần dự đoán rằng index 2 không hợp lệ trước khi chạy.

## IV. Method Đặt Tên Cho Một Thao Tác

Nếu toàn bộ logic nằm trong `main`, menu sẽ dài và khó kiểm tra. Tách theo hành vi:

```java
static void printMenu() {
    System.out.println("1. Thêm mặt hàng");
    System.out.println("2. Xóa mặt hàng");
    System.out.println("3. Xem giỏ");
    System.out.println("4. Xem tổng tiền");
    System.out.println("0. Thoát");
}
```

```java
static double calculateLineTotal(double price, int quantity) {
    return price * quantity;
}
```

- `void` dùng khi method thực hiện hành động và không trả kết quả.
- Kiểu trả về như `double` buộc method trả một giá trị tương ứng.
- Tham số là dữ liệu method cần; tránh để mọi method phụ thuộc quá nhiều biến toàn cục.
- Trong phạm vi buổi này, các method được gọi trực tiếp từ `main` nên khai báo `static`. OOP sẽ giải thích method gắn với object từ buổi 3.

## V. String: So Sánh Nội Dung, Không So Sánh Tham Chiếu

```java
String input = scanner.nextLine().trim();

if (input.equals("Áo thun")) {
    System.out.println("Khớp chính xác");
}

if (input.equalsIgnoreCase("áo thun")) {
    System.out.println("Khớp không phân biệt hoa thường");
}
```

Không dùng `==` để so sánh nội dung String. `==` kiểm tra hai biến có trỏ cùng object hay không; `equals()` kiểm tra nội dung.

Các method đủ dùng cho buổi này:

- `isBlank()` kiểm tra chuỗi rỗng hoặc chỉ có khoảng trắng.
- `trim()` bỏ khoảng trắng đầu/cuối.
- `equals()` và `equalsIgnoreCase()` so sánh nội dung.
- `contains()` hỗ trợ tìm kiếm ở phần mở rộng.

Sau `scanner.nextInt()` hoặc `nextDouble()`, gọi thêm `scanner.nextLine()` để tiêu thụ ký tự xuống dòng trước khi đọc tên bằng `nextLine()`.

## VI. ArrayList Và Ba Danh Sách Song Song

Buổi 2 chưa học class tự tạo, nên mỗi dòng hàng tạm lưu bằng ba list:

```java
static final List<String> names = new ArrayList<>();
static final List<Double> prices = new ArrayList<>();
static final List<Integer> quantities = new ArrayList<>();
```

Cùng vị trí `i` ở ba list phải mô tả cùng một mặt hàng. Sau mọi thao tác, ba `size()` phải bằng nhau.

```java
static void addItem(String name, double price, int quantity) {
    names.add(name);
    prices.add(price);
    quantities.add(quantity);
}
```

```java
static void removeItem(int index) {
    if (index < 0 || index >= names.size()) {
        System.out.println("Không tìm thấy mặt hàng.");
        return;
    }

    names.remove(index);
    prices.remove(index);
    quantities.remove(index);
}
```

Nếu quên xóa một list, dữ liệu sẽ lệch. Đây là giới hạn có chủ đích để buổi 3 dẫn sang Product và CartItem.

## VII. In Giỏ Và Tính Tổng

```java
static void printItems() {
    if (names.isEmpty()) {
        System.out.println("Giỏ hàng đang trống.");
        return;
    }

    for (int i = 0; i < names.size(); i++) {
        double lineTotal = prices.get(i) * quantities.get(i);
        System.out.printf("%d. %s | %.1f | SL: %d | Thành tiền: %.1f%n",
                i + 1,
                names.get(i),
                prices.get(i),
                quantities.get(i),
                lineTotal);
    }
}
```

```java
static double calculateTotal() {
    double total = 0;
    for (int i = 0; i < names.size(); i++) {
        total += prices.get(i) * quantities.get(i);
    }
    return total;
}
```

Tổng của giỏ rỗng là 0 vì vòng lặp không chạy. Không cần một nhánh đặc biệt chỉ để tính tổng rỗng.

## VIII. Ghép Vào Menu Theo Lát Cắt

Thứ tự thực hành:

1. Menu lặp và thoát đúng.
2. Thêm `printMenu()`.
3. Thêm hai dòng dữ liệu bằng `addItem()` và in giỏ.
4. Thêm `Sổ tay / 25000 / 3` và `Bút / 10000 / 2`, rồi đối chiếu tổng 95000.
5. Đọc số thứ tự cần xóa, đổi thành index bằng `displayNumber - 1`.
6. Nối phần nhập mặt hàng vào case 1.
7. Thử lựa chọn menu 9 và số thứ tự xóa 99.

Khung gọi:

```java
switch (choice) {
    case 1 -> readAndAddItem(scanner);
    case 2 -> readAndRemoveItem(scanner);
    case 3 -> printItems();
    case 4 -> System.out.println("Tổng tiền: " + calculateTotal());
    case 0 -> System.out.println("Kết thúc.");
    default -> System.out.println("Lựa chọn không hợp lệ.");
}
```

Các method `readAnd...` chỉ đọc input rồi gọi method xử lý. Việc tách này giúp phần thêm/xóa có thể được gọi bằng dữ liệu mẫu mà không phải gõ lại toàn bộ menu.

## IX. Debug Có Mục Tiêu

Đặt breakpoint trong `calculateTotal()` và theo dõi `i`, `price`, `quantity`, `total`. Với hai dòng mẫu, total phải lần lượt là 75000 rồi 95000.

Nếu xóa sai dòng, đặt breakpoint trước `remove` và so sánh số thứ tự người dùng với index thực tế. Debugger được dùng để trả lời một câu hỏi cụ thể, không chỉ chạy chậm toàn bộ chương trình.

## Lỗi thường gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| Menu chỉ chạy một lần | Không đặt trong vòng lặp | Dùng do-while với điều kiện `choice != 0` |
| Tên bị đọc rỗng | Còn ký tự xuống dòng sau `nextInt` | Gọi thêm `nextLine()` |
| So sánh tên sai | Dùng `==` | Dùng `equals`/`equalsIgnoreCase` |
| Xóa sai dòng | Nhầm số thứ tự 1-based với index 0-based | Trừ 1 và kiểm tra biên |
| Ba list khác size | Thêm/xóa không đồng thời | Mọi thao tác phải cập nhật cả ba list |
| Tổng sai | Lặp sai biên hoặc ghép sai index | Theo dõi `i` và line total bằng debugger |

## Tổng kết buổi 2

Điều kiện chọn hành động, vòng lặp giữ menu hoạt động, method tách trách nhiệm nhỏ, String xử lý tên và ArrayList giữ nhiều dòng hàng. Ba list song song giúp hoàn thành bài với kiến thức hiện tại, đồng thời tạo ra vấn đề thật để buổi 3 giải quyết bằng OOP.

### Câu hỏi chốt buổi

1. Vì sao menu hợp với do-while?
2. Khi nào dùng switch thay vì if/else?
3. Vì sao String không so sánh bằng `==`?
4. Số thứ tự dòng 1 tương ứng index nào?
5. Điều gì xảy ra nếu chỉ xóa `names` mà không xóa `prices`?

## Bài tập nhỏ trên lớp — Thống kê điểm

Viết chương trình quản lý một `ArrayList<Double>` điểm số:

- Tách method `addScore`, `printScores`, `calculateAverage` và `countPassed`.
- Dùng vòng lặp nhập đúng 5 điểm; điểm hợp lệ nằm trong đoạn 0–10.
- In danh sách, điểm trung bình và số điểm từ 5 trở lên.
- Nếu một điểm ngoài đoạn 0–10, bỏ qua bằng `continue` và không thêm vào list.

**Nghiệm thu:** với `8, 4, 11, 6, 7`, danh sách giữ bốn điểm hợp lệ, trung bình `6.25` và có ba điểm đạt. Thời gian làm khoảng 15 phút.

## Bài tập về nhà

Hoàn thành [BTVN buổi 02](../BTVN/buoi-02-btvn.md). Không tạo class model ở bài lõi; buổi 3 sẽ học cách chuyển dữ liệu rời thành object.
