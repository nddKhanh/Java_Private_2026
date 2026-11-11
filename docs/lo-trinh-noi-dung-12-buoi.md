# Lộ Trình Nội Dung Khóa Java Private HIT 2026

> Nguồn: `Lộ trình lớp JAVA PRIVATE HIT 2026.xlsx` — trang tính `Trang tính1`. Bản gốc gồm 12 buổi. Bản Markdown này đã gộp nội dung và BTVN buổi 10 vào buổi 8; giữ số cũ để đối chiếu, còn 11 buổi nội dung. Không bổ sung buổi kiểm tra. File Excel là nguồn lịch sử, chưa phản ánh điều chỉnh này.

## Mục Tiêu Khóa Học

Cho phép áp dụng kiến thức đã học để tạo nên 1 dự án nhỏ trong suốt quá trình học thay vì giới thiệu kiến thức rời rạc. Sau mỗi buổi , dự án sẽ được cải thiện thêm 1 phần giúp tăng độ trực quan

## Nội Dung Theo Từng Buổi

### Buổi 01

> Giáo án: [lesson/buoi-01.md](../roadmap/lesson/buoi-01.md)
> Bài tập: [BTVN/buoi-01-btvn.md](../roadmap/BTVN/buoi-01-btvn.md)

#### Trọng Tâm Nên Dạy

- Làm quen lớp
- Bài thử đầu vào
- Setup JDK, IntelliJ IDEA
- GitHub cơ bản
- Chương trình Java đầu tiên
- Nhập/xuất dữ liệu
- Kiểu dữ liệu cơ bản
- Biến, hằng số
- Quy tắc đặt tên
- Toán tử

#### Vấn Đề Cần Giải Quyết

Học viên biết cách viết, chạy và nộp một chương trình Java cơ bản.

#### Vai Trò Của Project

Bài nhỏ: tính tổng tiền đơn hàng.

#### Nội Dung Cụ Thể Sẽ Dạy

- Cài JDK, IntelliJ IDEA, kiểm tra môi trường chạy Java (Làm ở nhà)

- Làm quen lớp, giới thiệu mục tiêu khóa học
- Bài thử đầu vào để phân loại học viên
- Cấu trúc chương trình Java đầu tiên: class, main
- Nhập dữ liệu bằng Scanner, xuất dữ liệu bằng System.out.println
- Kiểu dữ liệu cơ bản: int, long, double, char, boolean, String
- Biến, hằng số, quy tắc đặt tên biến/method/class
- Phép toán số học, so sánh, logic
- GitHub cơ bản: tạo repo, clone, commit, push, nộp BTVN

#### Vấn Đề Để Lại Cho Buổi Sau

Chương trình chỉ xử lý một lần, chưa rẽ nhánh theo điều kiện, chưa lặp menu, code dễ bị lặp.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: tính tổng tiền đơn hàng từ giá và số lượng nhập vào, nộp qua GitHub.
- Bài mở rộng: tính thêm VAT và phí vận chuyển.
- Ghi chú: dùng double cho giá có thể sai số khi tính tiền thật; chỉ nhắc để học viên biết, khóa học dùng double cho đơn giản.
- Buổi nhẹ: dành thời gian cho môi trường và GitHub vì đây là phần học viên dễ gặp lỗi nhất.

### Buổi 02

> Giáo án: [lesson/buoi-02.md](../roadmap/lesson/buoi-02.md)
> Bài tập: [BTVN/buoi-02-btvn.md](../roadmap/BTVN/buoi-02-btvn.md)

#### Trọng Tâm Nên Dạy

- Điều kiện
- Vòng lặp
- Hàm/method
- Tham số và giá trị trả về
- Truyền tham số
- final, static final
- String
- ArrayList, List

- Debug bằng IDE hoặc console (Nâng cao)

#### Vấn Đề Cần Giải Quyết

Giải quyết việc chương trình cần chọn chức năng, lặp lại thao tác và tách code lặp thành hàm.

#### Vai Trò Của Project

Menu console để thêm sản phẩm vào giỏ và tính tổng tiền bằng các ArrayList song song: tên, giá, số lượng.

#### Nội Dung Cụ Thể Sẽ Dạy

- Câu lệnh điều kiện: if/else, switch
- Vòng lặp: for, while, do while, break, continue; for-each để duyệt ArrayList
- Viết hàm/method để tách code lặp
- Method có/không tham số, có/không giá trị trả về; method gọi từ main phải là static (giải thích tối thiểu)
- Truyền tham số: kiểu nguyên thủy và kiểu tham chiếu ở mức cơ bản
- final và static final cho hằng số: VAT, phí vận chuyển
- Xử lý String: nối chuỗi, so sánh bằng .equals() khác với ==, một số method thông dụng
- ArrayList/List: thêm, xóa, sửa, duyệt, tìm kiếm; cách dùng ArrayList<String>, ArrayList<Double>

- Debug bằng IDE hoặc console để theo dõi vòng lặp (Nâng cao)

#### Vấn Đề Để Lại Cho Buổi Sau

Khi quản lý nhiều thông tin sản phẩm và giỏ hàng bằng nhiều ArrayList song song, code khó đồng bộ và dễ sai index.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: menu thêm, xóa, xem sản phẩm trong giỏ bằng các ArrayList song song và tính tổng tiền.
- Bài mở rộng: tìm sản phẩm theo tên; tính tổng tiền kèm VAT và phí vận chuyển bằng static final.
- ArrayList<...> chỉ dạy cách dùng; phần tự viết generic là Nâng cao ở buổi 5.

### Buổi 03

> Giáo án: [lesson/buoi-03.md](../roadmap/lesson/buoi-03.md)
> Bài tập: [BTVN/buoi-03-btvn.md](../roadmap/BTVN/buoi-03-btvn.md)

#### Trọng Tâm Nên Dạy

- OOP nhập môn
- Package
- Class, object, instance
- Field, method
- Constructor
- Getter/setter
- this
- toString()
- HAS-A

- static ở cấp class (Nâng cao)
- Access modifier (Nâng cao)

#### Vấn Đề Cần Giải Quyết

Giải quyết vấn đề danh sách song song khó quản lý bằng cách gom dữ liệu và hành vi vào class/object.

#### Vai Trò Của Project

Tạo các class Product, Customer, CartItem; giỏ hàng là ArrayList<CartItem>.

#### Nội Dung Cụ Thể Sẽ Dạy

- Vấn đề của danh sách song song: khó quản lý productNames, prices, quantities
- Khái niệm OOP và lý do cần class/object
- Package và cách tổ chức class
- Class, object, instance
- Field và method
- Constructor không tham số và có tham số
- Getter/setter và kiểm soát truy cập dữ liệu; field khai báo private
- Từ khóa this
- toString(), vì in object ra chỉ thấy mã băm
- Quan hệ HAS-A: CartItem có một Product
- Thực hành: viết class Product, Customer, CartItem và thêm vào giỏ bằng ArrayList<CartItem>

- static ở cấp class: đếm số sản phẩm, ID tự tăng (Nâng cao)
- Access modifier đầy đủ: private, public, protected, default (Nâng cao)

#### Vấn Đề Để Lại Cho Buổi Sau

Khi có nhiều loại sản phẩm, đơn hàng và trạng thái đơn hàng, code bắt đầu nhiều if/else; dữ liệu quan trọng vẫn có nguy cơ bị sửa tùy tiện.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: viết ba class và thêm sản phẩm vào giỏ bằng ArrayList.
- Bài mở rộng: kiểm tra dữ liệu hợp lệ trong setter.
- Bài nâng cao: dùng static làm ID tự tăng cho Product và Customer.
- Chỉ dạy HAS-A ở buổi này; IS-A để sang buổi 4.

### Buổi 04

> Giáo án: [lesson/buoi-04.md](../roadmap/lesson/buoi-04.md)
> Bài tập: [BTVN/buoi-04-btvn.md](../roadmap/BTVN/buoi-04-btvn.md)

#### Trọng Tâm Nên Dạy

- Encapsulation chuyên sâu
- Kiểm soát thay đổi dữ liệu
- Kế thừa
- extends, super
- @Override
- Đa hình

- Overloading, overriding (Nâng cao)
- HAS-A, IS-A, composition (Nâng cao)
- Upcasting, downcasting (Nâng cao)

#### Vấn Đề Cần Giải Quyết

Giải quyết việc code nhiều if/else theo loại sản phẩm, lặp field/method giữa các loại sản phẩm và thiếu kiểm soát dữ liệu quan trọng.

#### Vai Trò Của Project

Tổ chức project rõ hơn: Product làm lớp cha, các lớp con như Book, Electronics, Food; thêm class Order chứa danh sách CartItem.

#### Nội Dung Cụ Thể Sẽ Dạy

- Đào sâu encapsulation: vì sao không public trực tiếp field quan trọng
- Kiểm soát thay đổi giá, số lượng, trạng thái đơn hàng qua method
- Kế thừa với extends, dùng super
- Dùng @Override đúng cách
- Đa hình: biến kiểu lớp cha tham chiếu object lớp con, cùng một lời gọi method nhưng hành vi khác nhau
- Áp dụng vào sản phẩm và đơn hàng: thay if/else theo loại bằng đa hình

- So sánh HAS-A và IS-A, khi nào dùng kế thừa/composition (Nâng cao)
- Upcasting, downcasting, instanceof (Nâng cao)
- So sánh overloading và overriding (Nâng cao)

#### Vấn Đề Để Lại Cho Buổi Sau

Các nhóm class có chung hành vi nhưng cách làm khác nhau như thanh toán, giao hàng vẫn phải dùng if/else; cần một hợp đồng chung để thêm loại mới mà không sửa code cũ.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: thêm một loại sản phẩm mới bằng kế thừa mà không sửa code cũ.
- Ghi chú: abstract class và interface chuyển sang buổi 5 nên buổi này nhẹ hơn.
- Nếu lớp chậm, giữ kế thừa, @Override, đa hình; bỏ các mục Nâng cao.

### Buổi 05

> Giáo án: [lesson/buoi-05.md](../roadmap/lesson/buoi-05.md)
> Bài tập: [BTVN/buoi-05-btvn.md](../roadmap/BTVN/buoi-05-btvn.md)

#### Trọng Tâm Nên Dạy

- Abstract class
- Interface
- equals() và hashCode()

- Lambda cơ bản (Mở rộng)
- Optional (Nâng cao)
- Generics (Nâng cao)

#### Vấn Đề Cần Giải Quyết

Giải quyết việc các nhóm class có chung hành vi nhưng cách làm khác nhau khó mở rộng, và việc so sánh object theo nội dung.

#### Vai Trò Của Project

Thêm interface PaymentMethod, DeliveryMethod và các lớp cài đặt; so sánh Product theo mã bằng equals().

#### Nội Dung Cụ Thể Sẽ Dạy

- Vấn đề từ buổi 4: phương thức thanh toán và giao hàng khác nhau vẫn phải xử lý bằng if/else
- Tính trừu tượng với abstract class: khi nào dùng, method abstract, không tạo được object, chứa phần code chung
- Interface: khai báo hợp đồng, implements, một class implement nhiều interface
- Chọn abstract class hay interface: “là một loại của” so với “có khả năng”
- Áp dụng: PaymentMethod, CashPayment, BankTransferPayment, DeliveryMethod
- equals() và hashCode() ở mức ứng dụng: == và equals() với object, tìm/xóa trong ArrayList, quy tắc override cả hai method
- Kiểm tra OOP 1 tiếng cuối

- Lambda expression cơ bản với forEach, removeIf (Mở rộng)
- Tự viết class/method generic (Nâng cao)
- Optional chỉ giới thiệu sớm; học bắt buộc ở buổi 8 (Nâng cao)

#### Vấn Đề Để Lại Cho Buổi Sau

Chương trình vẫn dễ crash khi nhập sai, dữ liệu mất khi tắt app, chưa kiểm tra email và số điện thoại tốt.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: thêm một phương thức thanh toán mới bằng implements PaymentMethod; override equals() để tìm sản phẩm trong giỏ theo mã.
- Bài mở rộng: dùng lambda để duyệt/lọc danh sách sản phẩm.
- Phạm vi: khóa học không dạy sâu Set, Map, Stream, BigDecimal.

### Buổi 06

> Giáo án: [lesson/buoi-06.md](../roadmap/lesson/buoi-06.md)
> Bài tập: [BTVN/buoi-06-btvn.md](../roadmap/BTVN/buoi-06-btvn.md)

#### Trọng Tâm Nên Dạy

- Exception
- try/catch/finally
- try-with-resources
- Đọc/ghi file

- Regex (Mở rộng)
- Thread cơ bản (Nâng cao)
- Custom exception (Nâng cao)

#### Vấn Đề Cần Giải Quyết

Giải quyết lỗi nhập sai làm chương trình crash, lỗi nghiệp vụ và việc dữ liệu tạm bị mất khi tắt chương trình.

#### Vai Trò Của Project

Kiểm tra dữ liệu nhập và lưu đơn hàng tạm ra file.

#### Nội Dung Cụ Thể Sẽ Dạy

- Vì sao chương trình cần xử lý lỗi thay vì để crash
- try/catch/finally: bắt lỗi nhập sai như NumberFormatException, InputMismatchException; lỗi đọc/ghi file như IOException
- Ném lỗi bằng throw ở mức cơ bản
- try-with-resources
- Đọc và ghi file bằng Files, Path
- Lưu đơn hàng tạm ra file text hoặc CSV

- Regex để kiểm tra số điện thoại, email, mã đơn hàng (Mở rộng)
- Custom exception cho lỗi nghiệp vụ: số lượng không hợp lệ, sản phẩm không tồn tại (Nâng cao)
- Thread cơ bản, demo khái niệm nếu còn thời gian (Nâng cao)

#### Vấn Đề Để Lại Cho Buổi Sau

Lưu file khó truy vấn, khó cập nhật một bản ghi, khó quản lý khi số lượng sản phẩm và đơn hàng tăng.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: kiểm tra dữ liệu nhập và lưu/đọc lại đơn hàng từ file.
- Bài mở rộng: validate email/số điện thoại bằng Regex.
- Bài nâng cao: custom exception cho lỗi nghiệp vụ.
- Nếu còn thời gian, demo race condition để dùng lại khi nói về tồn kho.

### Buổi 07

> Giáo án: [lesson/buoi-07.md](../roadmap/lesson/buoi-07.md)
> Bài tập: [BTVN/buoi-07-btvn.md](../roadmap/BTVN/buoi-07-btvn.md)

#### Trọng Tâm Nên Dạy

- Maven
- pom.xml
- JDBC URL
- Connection
- SQL CRUD
- Statement
- PreparedStatement
- SQL Injection
- ResultSet

#### Vấn Đề Cần Giải Quyết

Giải quyết việc lưu file khó quản lý bằng cách đưa dữ liệu vào MySQL và thao tác qua JDBC.

#### Vai Trò Của Project

CRUD sản phẩm bằng JDBC, chỉ một bảng products; khách hàng và đơn hàng làm ở các buổi sau.

#### Nội Dung Cụ Thể Sẽ Dạy

- Vì sao cần Maven khi dùng thư viện ngoài
- Cấu trúc project Maven và file pom.xml
- Thêm MySQL Connector/J
- Tạo database và table MySQL cơ bản
- Cột giá dùng DECIMAL, ánh xạ sang Java tạm thời ở mức đơn giản
- JDBC URL, username, password
- Kết nối DB bằng Connection
- Ôn SQL CRUD: SELECT, INSERT, UPDATE, DELETE
- Dùng Statement cho câu SQL đơn giản
- Dùng PreparedStatement để truyền tham số, giải quyết SQL Injection
- Đọc dữ liệu bằng ResultSet
- Đóng Connection, PreparedStatement, ResultSet bằng try-with-resources
- Không để mật khẩu DB trong code: đọc từ file cấu hình và thêm file đó vào .gitignore

#### Vấn Đề Để Lại Cho Buổi Sau

Code JDBC bắt đầu lặp nhiều, SQL nằm lẫn với xử lý nghiệp vụ, cần tách code DB ra khỏi logic xử lý đơn hàng.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: hoàn thiện thêm, xem, sửa, xóa sản phẩm bằng JDBC.
- Ghi chú: nên có phần thực hành khoảng 1 giờ ngay trong buổi.

### Buổi 08 — Tách Tầng, MVC Và Refactor (Gộp Buổi 8 Và 10 Cũ)

> Giáo án: [buoi-08.md](../roadmap/lesson/buoi-08.md)
> Bài tập: [buoi-08-btvn.md](../roadmap/BTVN/buoi-08-btvn.md)

#### Trọng Tâm Nên Dạy

- DAO/Repository, Service, repository interface và constructor injection.
- MVC: Model, View, Controller; trách nhiệm và hướng gọi giữa các tầng.
- Optional: isPresent, orElse, orElseThrow; enum cho trạng thái.
- DTO/record ở mức giới thiệu.
- SOLID cơ bản: SRP và DIP; composition root.
- Refactor giữ nguyên hành vi; clean code, đặt tên và giảm lặp.
- Singleton/Factory, checked/unchecked exception (đọc thêm).

#### Vấn Đề Cần Giải Quyết

Code JDBC lẫn nhập/xuất và nghiệp vụ gây khó sửa. Học viên cần hiểu vai trò từng tầng, cách dùng interface và cách đổi cấu trúc mà giữ nguyên kết quả.

#### Vai Trò Của Project

BTVN áp dụng vào Product/Customer: View → Controller → Service → Repository interface → JDBC implementation. Dùng OrderStatus và chạy cùng ProductService với memory repository.

#### Nội Dung Cụ Thể Sẽ Dạy

- Giải thích khái niệm bằng một chức năng tìm liên hệ nhỏ, không xây toàn bộ HitShop trên lớp.
- Repository tách SQL/mapping khỏi nghiệp vụ; Service kiểm tra quy tắc và nhận repository qua constructor.
- Optional diễn đạt không tìm thấy; phân biệt với lỗi kết nối database.
- View nhập/hiển thị; Controller điều phối; Model giữ dữ liệu/hành vi.
- Composition root chọn implementation và lắp dependency.
- SRP theo lý do thay đổi; DIP qua repository interface và ví dụ memory/JDBC.
- enum giới hạn trạng thái; DTO/record mang dữ liệu qua các tầng.
- Refactor từng bước với input/output cố định; một bài thực hành nhỏ trên lớp.
- Singleton/Factory và checked/unchecked exception để đọc thêm, không yêu cầu triển khai trong bài bắt buộc.

#### Vấn Đề Để Lại Cho Buổi Sau

Đã có kiến trúc để mở rộng; chưa học quan hệ bảng, generated key và đơn hàng nhiều dòng. Các nội dung đó tiếp tục ở buổi 9.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN gộp: CRUD Product, Customer, Optional, enum, View/Controller, SRP/DIP, composition root, memory repository và nghiệm thu trước/sau refactor.
- Không làm lại một BTVN refactor riêng ở buổi 10.
- Chưa yêu cầu OrderItem hoặc OrderItemInput trước khi học dữ liệu nhiều bảng; phần áp dụng đó nằm ở buổi 9.

### Buổi 09

> Giáo án: [lesson/buoi-09.md](../roadmap/lesson/buoi-09.md)
> Bài tập: [BTVN/buoi-09-btvn.md](../roadmap/BTVN/buoi-09-btvn.md)

#### Trọng Tâm Nên Dạy

- Quan hệ bảng 1-n
- Khóa chính, khóa ngoại
- JOIN, GROUP BY
- ORDER BY
- getGeneratedKeys
- JDBC nhiều bảng

- LIMIT/OFFSET (Nâng cao)
- Transaction, ACID (Nâng cao)
- Batch insert (Nâng cao)
- RowMapper (Nâng cao)

#### Vấn Đề Cần Giải Quyết

Giải quyết việc dữ liệu nhiều bảng như đơn hàng và chi tiết đơn hàng cần được thiết kế, liên kết và truy vấn đúng bằng JDBC. Hibernate không được dạy trong buổi này.

#### Vai Trò Của Project

Hoàn thiện OrderRepository và OrderService; tạo đơn hàng gồm orders và order_items.

#### Nội Dung Cụ Thể Sẽ Dạy

- Thiết kế bảng orders và order_items: quan hệ 1-n, khóa chính, khóa ngoại, ràng buộc cơ bản
- Cột thời điểm tạo đơn dùng DATETIME, đọc/ghi bằng LocalDateTime
- JOIN để lấy đơn hàng kèm chi tiết và thông tin khách hàng/sản phẩm
- GROUP BY để thống kê doanh thu theo ngày hoặc theo khách hàng
- ORDER BY để sắp xếp danh sách đơn hàng
- Lấy ID đơn vừa tạo bằng getGeneratedKeys để ghi order_items
- Viết JDBC cho dữ liệu nhiều bảng bằng PreparedStatement và ResultSet
- Tổ chức OrderRepository, OrderItemRepository, OrderService theo kiến trúc buổi 8.
- View nhận productId/quantity bằng OrderItemInput; Controller gọi Service để tra Product và chốt giá; không nhận unitPrice từ người dùng.

- LIMIT/OFFSET để phân trang danh sách đơn hàng (Nâng cao)
- Transaction: setAutoCommit(false), commit, rollback, ACID ở mức khái niệm (Nâng cao)
- Batch insert bằng addBatch, executeBatch cho order_items (Nâng cao)
- RowMapper để giảm lặp code đọc ResultSet; ORM/Hibernate chỉ nhắc như hướng học tiếp (Nâng cao)

#### Vấn Đề Để Lại Cho Buổi Sau

Tiếp tục kiến trúc buổi 8 cho đơn nhiều bảng; sau đó chuẩn bị Git làm việc nhóm và giao diện ở buổi 11.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: hoàn thiện chức năng tạo đơn hàng, xem chi tiết đơn hàng và thống kê doanh thu bằng JOIN/GROUP BY.
- Bài nâng cao: phân trang bằng LIMIT/OFFSET; dùng transaction để rollback khi một bước lỗi; batch insert; RowMapper.
- Ghi chú: trọng tâm là JDBC nhiều bảng. Transaction và LIMIT/OFFSET không bắt buộc.

### Buổi 10 — Đã Gộp Vào Buổi 08

Không tổ chức một buổi nội dung hoặc BTVN riêng. MVC, SRP/DIP, composition root và refactor nằm trong buổi 8; áp dụng cho tạo/xem đơn nhiều bảng nằm ở buổi 9. Giữ số các buổi 9, 11, 12 để không làm đứt liên kết và tài liệu cũ; không thêm buổi kiểm tra.

### Buổi 11

> Giáo án: [lesson/buoi-11.md](../roadmap/lesson/buoi-11.md)
> Bài tập: [BTVN/buoi-11-btvn.md](../roadmap/BTVN/buoi-11-btvn.md)

#### Trọng Tâm Nên Dạy

- Git Flow
- Branching
- Pull request
- Code review
- Conflict resolution
- Commit message
- Swing cơ bản

- Event handling (Mở rộng)

#### Vấn Đề Cần Giải Quyết

Giải quyết vấn đề làm việc nhóm trên cùng repo và chuẩn bị giao diện đơn giản cho bài tập lớn.

#### Vai Trò Của Project

Chuẩn bị làm nhóm và làm giao diện Swing cơ bản.

#### Nội Dung Cụ Thể Sẽ Dạy

- Git Flow: main, develop, feature
- Tạo branch cho từng chức năng
- Quy trình làm việc nhóm với commit, push, pull request
- Review code qua pull request
- Demo conflict thật và cách xử lý conflict cơ bản
- Quy ước commit message và đặt tên branch
- Swing cơ bản: dựng form và bảng với JFrame, JPanel, JButton, JTextField, JTable

- Event handling: bắt sự kiện bằng ActionListener, gắn giao diện với service đã viết (Mở rộng)

#### Vấn Đề Để Lại Cho Buổi Sau

Học viên cần chọn đề tài đủ phạm vi, chia việc, tạo repo và thống nhất tiêu chí hoàn thành.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

- BTVN: dựng một form và một bảng cho chức năng quản lý sản phẩm, nộp qua pull request.
- Bài mở rộng: bắt sự kiện nút Thêm/Xóa và hiển thị dữ liệu từ ProductService.
- Ghi chú: trọng tâm là Git làm việc nhóm; giao diện giới hạn ở một form và một bảng. Dùng Swing vì có sẵn trong JDK.

### Buổi 12

> Giáo án: [lesson/buoi-12.md](../roadmap/lesson/buoi-12.md)
> Bài tập: [BTVN/buoi-12-btvn.md](../roadmap/BTVN/buoi-12-btvn.md)

#### Trọng Tâm Nên Dạy

- Tổng kết khóa học
- Yêu cầu bài tập lớn
- Gợi ý đề tài
- Chia nhóm
- Tạo repo GitHub
- Đăng ký đề tài
- Tiêu chí chấm
- Hướng dẫn demo cuối khóa

#### Vấn Đề Cần Giải Quyết

Giải quyết việc chuyển từ học từng phần sang làm bài tập lớn có kế hoạch, repo và tiêu chí rõ ràng.

#### Vai Trò Của Project

Chốt đề tài, repo và tiêu chí chấm.

#### Nội Dung Cụ Thể Sẽ Dạy

- Tổng kết các mảng kiến thức: Java cơ bản, OOP, xử lý lỗi và file, SQL/JDBC, quan hệ bảng, JOIN, GROUP BY, Git/GitHub, Swing cơ bản
- Công bố yêu cầu bài tập lớn
- Gợi ý đề tài: quản lý bán hàng, quản lý thư viện, quản lý khóa học, quản lý quán cà phê, quản lý đơn hàng thương mại điện tử
- Chia nhóm và phân vai
- Mỗi nhóm tạo repo GitHub
- Đăng ký đề tài, chức năng chính, database dự kiến
- Thống nhất tiêu chí chấm: OOP, CRUD JDBC, Git, giao diện console hoặc Swing đơn giản, báo cáo
- Hướng dẫn cách trình bày demo cuối khóa

#### Vấn Đề Để Lại Cho Buổi Sau

Không còn vấn đề để dẫn sang buổi sau; chuyển sang giai đoạn làm bài tập lớn, mentor hỗ trợ theo nhóm.

#### Ghi Chú Và Bài Tập Theo Lộ Trình

_Không có ghi chú riêng trong lộ trình._

## Cách Dùng Tài Liệu Này Để Rà Soát Giáo Án

- Đối chiếu từng mục trong “Nội Dung Cụ Thể Sẽ Dạy” với nội dung lý thuyết của giáo án tương ứng; không chỉ kiểm tra tên chủ đề có xuất hiện.
- Giữ đúng ranh giới giữa phần bắt buộc, phần mở rộng và phần nâng cao theo ghi chú của từng buổi.
- Giữ “Vai Trò Của Project” và bài tập ở vai trò ứng dụng/bài tập theo lộ trình; không để chúng thay thế nội dung giảng giải kiến thức.
- Kiểm tra giáo án đã nêu vấn đề sẽ giải quyết, kiến thức được dạy, nội dung chuyển tiếp và ghi chú quan trọng của hàng lộ trình chưa.
