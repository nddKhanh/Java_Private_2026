# Buổi 12: Tổng Kết Và Khởi Động Capstone

---

> **Hạn chế từ buổi 11:** học viên đã có các kỹ thuật riêng lẻ nhưng chưa tự chốt phạm vi, mô hình dữ liệu, transaction, phân công và tiêu chí demo cho một sản phẩm nhóm.

## Mục tiêu buổi học

Sau buổi học, mỗi nhóm có thể:

- Nối kiến thức 11 buổi thành một luồng ứng dụng hoàn chỉnh.
- Chọn đề tài và thu hẹp phạm vi thành sản phẩm demo được.
- Thiết kế entity, ERD, use case nhiều bảng và ranh giới tầng.
- Lập kế hoạch transaction, test case, Git/PR và demo.
- Bắt đầu sprint đầu tiên với backlog và tiêu chí nghiệm thu rõ.

> **Kết quả cuối buổi:** mỗi nhóm có đề cương một trang, ERD, kiến trúc, luồng transaction, backlog và bộ dữ liệu demo cho đề tài tự chọn.

## I. Nhìn Lại Khóa Học Theo Một Luồng Dữ Liệu

Thay vì ôn từng khái niệm rời, theo dõi một request từ người dùng tới database:

```text
Người dùng nhập dữ liệu
    → View đọc và hiển thị
    → Controller điều phối
    → Service kiểm tra nghiệp vụ
    → Model biểu diễn trạng thái/hành vi
    → Repository interface mô tả nhu cầu dữ liệu
    → JDBC implementation chạy SQL
    → MySQL bảo vệ khóa, quan hệ và tính bền vững
```

Các kiến thức nền nằm trong luồng đó:

- Biến, điều kiện, vòng lặp, method xử lý dữ liệu.
- OOP/encapsulation tổ chức model.
- Kế thừa, đa hình, interface tạo khả năng thay thế.
- Exception xử lý đường thất bại.
- Collection giữ tập object trong bộ nhớ.
- JDBC/SQL lưu và truy vấn dữ liệu.
- MVC/SOLID đặt trách nhiệm và dependency.
- Git/PR cho phép nhiều người cộng tác.

Mỗi nhóm chọn một chức năng đã làm và kể lại toàn bộ đường đi này. Nếu một bước không chỉ ra được class chịu trách nhiệm, đó là điểm cần sửa trước capstone.

## II. Chọn Đề Tài Từ Vấn Đề, Không Từ Danh Sách Công Nghệ

Một đề tài tốt có người dùng, dữ liệu và một giao dịch cốt lõi. Ví dụ:

| Đề tài | Entity gợi ý | Luồng nhiều bảng |
|---|---|---|
| Cửa hàng | Product, Customer, Order, OrderItem | Tạo đơn và giảm tồn kho |
| Thư viện | Book, Reader, Loan, LoanItem | Tạo phiếu mượn và đổi trạng thái sách |
| Đặt lịch | Customer, Staff, Appointment | Đặt lịch và khóa khung giờ |
| Khóa học | Student, Course, Enrollment | Đăng ký học và cập nhật số chỗ |

Không chọn đề tài chỉ vì “có thể dùng Swing”. Giao diện là cách truy cập; nghiệp vụ và dữ liệu mới quyết định cấu trúc ứng dụng.

### Bài tập tại lớp: câu tuyên bố phạm vi

Nhóm hoàn thành mẫu:

```text
Ứng dụng giúp [người dùng] thực hiện [ba chức năng chính].
Luồng quan trọng nhất là [tên luồng], ghi vào [các bảng].
Sản phẩm demo bằng [console hoặc Swing].
Các chức năng chưa làm trong phiên bản đầu: [...].
```

Nếu câu trả lời vượt một trang hoặc có hơn năm chức năng chính, phạm vi đang quá rộng.

## III. Từ Use Case Sang ERD Và Object

Với mỗi use case, nhóm xác định:

1. Input từ người dùng.
2. Quy tắc nghiệp vụ.
3. Entity tham gia.
4. Bảng bị đọc/ghi.
5. Output và đường lỗi.

Ví dụ một ứng dụng bán hàng:

```text
Input: customerId, [(productId, quantity)], createdAt
Rule: customer/product tồn tại, quantity > 0 và không vượt stock
Write: orders, order_items, products.stock
Output: generated orderId và tổng
Failure: bất kỳ bước nào lỗi thì rollback toàn bộ
```

Sau đó mới vẽ ERD và class. Không vẽ bảng chỉ từ danh từ; phải đối chiếu với use case để biết cột lịch sử, khóa ngoại và ràng buộc.

## IV. Transaction Là Yêu Cầu Cốt Lõi

Capstone phải có ít nhất một luồng ghi nhiều bảng. Luồng đó chạy trên một Connection và một transaction:

```java
connection.setAutoCommit(false);
try {
    // bước 1
    // bước 2
    // bước 3
    connection.commit();
} catch (Exception e) {
    connection.rollback();
    throw e;
} finally {
    connection.setAutoCommit(true);
}
```

Nhóm phải xác định ai sở hữu transaction. Với kiến trúc hiện tại, một coordinator repository hoặc transaction-aware service có thể điều phối, nhưng các repository tham gia phải dùng cùng Connection. Không mở một Connection mới ở mỗi bước rồi gọi rollback trên Connection khác.

Test bắt buộc: cố ý làm bước giữa thất bại và chạy SELECT chứng minh không còn row/stock thay đổi dở dang.

## V. Thiết Kế Kiến Trúc Trước Khi Chia Việc

Mỗi nhóm vẽ sơ đồ:

```text
[ConsoleView hoặc SwingView]
             ↓
         Controller
             ↓
           Service
             ↓
      Repository interfaces
             ↓
       JDBC implementations
             ↓
            MySQL
```

Sau đó ghi class vào từng hộp. Nếu một class nằm ở hai hộp, trách nhiệm chưa rõ. Nếu View gọi thẳng JdbcRepository, sơ đồ và code không khớp.

Chia việc theo vertical slice nhỏ sẽ giảm conflict hơn chia “một người làm toàn model, một người làm toàn SQL”. Ví dụ:

- Feature product/customer CRUD.
- Feature tạo giao dịch nhiều bảng.
- Feature xem chi tiết và báo cáo.
- Feature giao diện/tài liệu tích hợp.

Mỗi slice vẫn cần thống nhất contract trước khi các thành viên làm song song.

## VI. Backlog Có Tiêu Chí Nghiệm Thu

Một task tốt không chỉ ghi “Làm Order”. Ví dụ:

```text
Task: Tạo Order và giảm stock trong transaction.
Input mẫu: KH01, SP01 × 2, SP02 × 1, thời điểm cố định do nhóm chọn.
Kết quả: Order có generated id, tổng khớp phép tính tay và stock giảm đúng.
Ca lỗi: P99 ở item thứ hai; không có Order/OrderItem và stock không đổi.
Done: code, SQL, test output, README và PR đã review.
```

Định nghĩa Done chung:

- Build/run trên JDK 17.
- Test case chuẩn đạt.
- Không lộ credential.
- SQL dùng tham số và tài nguyên được đóng.
- PR được review và comment đã xử lý.
- README cập nhật nếu cách chạy thay đổi.

## VII. Lập Kịch Bản Demo Trước Khi Code Xong

Demo tốt có thể lặp lại từ dữ liệu sạch:

1. Chạy schema và seed.
2. Khởi động ứng dụng.
3. Xem dữ liệu seed.
4. Thực hiện CRUD.
5. Chạy giao dịch nhiều bảng.
6. Xem chi tiết và báo cáo.
7. Chạy ca rollback.
8. Khởi động lại để chứng minh dữ liệu còn.

Nhóm ghi input và output mong đợi cho từng bước. Tránh dùng thời gian hệ thống hoặc dữ liệu ngẫu nhiên nếu khiến báo cáo không thể đối chiếu.

## VIII. Review Đề Cương Theo Ba Vòng

### Vòng 1 — Phạm vi

- Ba chức năng chính có demo được không?
- Có phần nào chỉ “cho đẹp” nhưng chưa phục vụ use case?
- Có chức năng nào nên chuyển thành nâng cao?

### Vòng 2 — Dữ liệu và transaction

- Khóa chính/ngoại có rõ không?
- Có snapshot dữ liệu lịch sử cần thiết không?
- Transaction gồm chính xác các bước nào?
- Ca rollback chứng minh bằng query nào?

### Vòng 3 — Kiến trúc và cộng tác

- Dependency có đi đúng tầng không?
- Task/branch có đủ nhỏ để review không?
- README và seed do ai chịu trách nhiệm?
- Ai review chéo feature nào?

Sau mỗi vòng, nhóm sửa trực tiếp đề cương thay vì chỉ ghi nhận góp ý.

## IX. Mốc Cuối Buổi

Mỗi nhóm trình bày trong 5 phút:

- Vấn đề và người dùng.
- ERD.
- Luồng nghiệp vụ nhiều bảng và transaction.
- Kiến trúc/package.
- Ba test case chính, gồm một rollback.
- Backlog và phân công.

Mentor chỉ duyệt khi phạm vi, dữ liệu đầu vào và kết quả nghiệm thu đủ cụ thể để một nhóm khác đọc cũng hiểu.

## Lỗi thường gặp

| Lỗi | Cách điều chỉnh |
|---|---|
| Chọn quá nhiều chức năng | Chốt một luồng lõi, một CRUD, một báo cáo trước |
| ERD có nhiều bảng nhưng không có use case | Xóa bảng không tham gia demo |
| Transaction chỉ ghi trong slide | Viết ca lỗi và query chứng minh rollback |
| Chia việc theo tầng gây chờ nhau | Chia vertical slice, thống nhất interface trước |
| Demo phụ thuộc dữ liệu máy cá nhân | Có schema/seed/reset và README |
| Bắt mọi nhóm làm cùng một đề tài | Cho nhóm chọn miền và chốt cùng một bộ tiêu chí kỹ thuật |

## Tổng kết khóa học

Mục tiêu không phải nhớ mọi API mà là biết đưa một yêu cầu đi từ input đến model, nghiệp vụ, dữ liệu và output với trách nhiệm rõ. Capstone là nơi chứng minh chuỗi đó hoạt động, có thể kiểm tra, có rollback khi lỗi và có lịch sử cộng tác qua Git.

## Bài tập nhỏ trên lớp — Đề cương quản lý bãi xe

Trong nhóm nhỏ, lập đề cương một trang cho bài toán quản lý bãi xe:

- Xác định người dùng, ba chức năng chính và ba entity tối thiểu.
- Vẽ ERD có ít nhất một quan hệ một-nhiều.
- Chọn một luồng ghi nhiều bảng cần transaction và mô tả ca rollback.
- Viết ba test case có input/kết quả mong đợi.
- Chia luồng thành ba task/feature branch có thể review độc lập.

**Nghiệm thu:** nhóm khác đọc đề cương có thể chỉ ra dữ liệu được ghi ở đâu, khi nào rollback và cách demo từ database sạch. Thời gian làm khoảng 20 phút.

## Bài tập sau buổi 12

Thực hiện [đặc tả capstone](../BTVN/buoi-12-btvn.md). Nộp đề cương/ERD trước khi mở rộng phạm vi code.
