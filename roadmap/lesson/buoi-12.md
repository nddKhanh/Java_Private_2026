# Buổi 12: Tổng Kết Khóa Học và Khởi Động Bài Tập Lớn

---

## Mục tiêu buổi học

- Kết nối các mảng đã học thành quy trình làm một ứng dụng đơn giản từ đầu.
- Hiểu yêu cầu, phạm vi, tiêu chí chấm và quy trình demo bài tập lớn.
- Chọn đề tài, chia nhóm/vai trò, tạo repository và đăng ký kế hoạch.
- Xác định chức năng, database và cách chứng minh project chạy được.

> **Cầu nối:** Sau khi đã tiếp cận các chủ đề Java và cách phối hợp bằng Git, một yêu cầu lớn cần được chia thành dữ liệu, chức năng, phần việc và tiêu chí nghiệm thu. Buổi này tập trung vào cách lập kế hoạch và trình bày một ứng dụng nhóm; project tích lũy được dùng trong BTVN, không làm trục cho phần lý thuyết.

## I. Hệ Thống Hóa Kiến Thức Theo Nhóm Chủ Đề

Đây là buổi tổng kết và lập kế hoạch theo lộ trình; phần chọn đề tài phục vụ bài tập lớn, không thay thế hệ thống hóa kiến thức. Khi lập kế hoạch, cần xác định vấn đề, người dùng, dữ liệu, chức năng, phạm vi và cách chứng minh kết quả.

Các chủ đề đã học gồm cú pháp Java, điều khiển luồng, OOP, xử lý lỗi, lưu trữ file, SQL/JDBC, phân tách trách nhiệm, giao diện và phối hợp bằng Git. Mỗi chủ đề giải quyết một dạng vấn đề lập trình riêng và có thể được vận dụng tùy theo yêu cầu đề tài.

| Nhóm kiến thức | Câu hỏi cần tự trả lời |
|---|---|
| Java cơ bản | Dữ liệu có kiểu gì, luồng chạy ra sao, method nhận/trả gì? |
| OOP | Trạng thái thuộc object nào, quan hệ là HAS-A hay IS-A, hành vi nào có thể thay thế? |
| Lỗi và lưu trữ | Lỗi nào có thể phục hồi, dữ liệu lưu ở đâu và đọc lại thế nào? |
| SQL/JDBC | Bảng liên hệ qua khóa nào, truy vấn trả về dữ liệu gì? |
| Thiết kế và giao diện | Trách nhiệm chia ra sao, thay đổi được review và trình bày thế nào? |

~~~text
Java cơ bản · OOP · lỗi/lưu trữ · SQL/JDBC · thiết kế · giao diện/Git
~~~

## II. Chọn Đề Tài Và Chốt Phạm Vi

Gợi ý: quản lý bán hàng, thư viện, khóa học, quán cà phê hoặc đơn hàng thương mại điện tử. Đây là đề tài để vận dụng kiến thức; nhóm chọn domain riêng, không cần sao chép ứng dụng ví dụ trong giáo án. Chọn phạm vi vừa đủ để nhóm hoàn thành CRUD nghiệp vụ, có quan hệ dữ liệu phù hợp và demo được luồng chính.

Mỗi nhóm ghi rõ:

- Người dùng chính và vấn đề ứng dụng giải quyết.
- Danh sách chức năng cốt lõi; phân biệt tính năng cần có và ý tưởng để sau.
- Các bảng database dự kiến, khóa chính/ngoại và quan hệ.
- Thành viên, vai trò, người review và quy ước Git.
- Cách chạy ứng dụng, dữ liệu demo và kịch bản trình bày.

## III. Tiêu Chí Chấm Và Demo

Thống nhất tiêu chí theo lộ trình:

- OOP: class có trách nhiệm rõ, dùng encapsulation và quan hệ phù hợp.
- CRUD JDBC: truy vấn có tham số, dữ liệu lưu được, liên kết bảng hợp lệ.
- Git/GitHub: commit có ý nghĩa, branch/PR và đóng góp thành viên nhìn được.
- Giao diện console hoặc Swing đơn giản đáp ứng luồng cốt lõi.
- Báo cáo/demo: trình bày vấn đề, chức năng, database, phân chia công việc và giới hạn.

Demo nên chạy một luồng đầu-cuối bằng dữ liệu mẫu, ví dụ tìm đầu sách → lập phiếu mượn → xem chi tiết phiếu. Chuẩn bị sẵn cách reset dữ liệu demo và phương án nếu máy không có kết nối database.

Khi lập kế hoạch, nhóm biến yêu cầu thành đầu việc kiểm chứng được: chức năng nào thao tác dữ liệu, bảng nào liên quan, lớp nào chịu trách nhiệm, kịch bản nào chứng minh luồng chạy. Đây là cách vận dụng mô hình dữ liệu, JDBC, phân tầng và Git; buổi tổng kết không yêu cầu học thêm framework mới.

Ví dụ với chức năng “lập phiếu mượn”: chốt quy tắc (bạn đọc và sách phải tồn tại, số lượng dương, sách còn có thể mượn); xác định dữ liệu cần lưu (`loans`, `loan_items`, thời điểm mượn); chia trách nhiệm nhận thông tin, áp dụng quy tắc và truy cập database; cuối cùng viết kịch bản demo cùng kết quả mong đợi. Đây là ví dụ phân tích yêu cầu, không phải cấu trúc bắt buộc cho mọi đề tài.

Không phải đề tài nào cũng cần cùng một thiết kế. Nếu ứng dụng chỉ quản lý danh bạ, có thể không cần quan hệ đơn hàng nhiều bảng; nhóm chọn mô hình dựa trên dữ liệu và thao tác thật của đề tài, rồi giải thích lý do. Tiêu chí là các lớp/bảng phản ánh bài toán và có thể chứng minh bằng một luồng chạy được, không phải đưa mọi khái niệm đã học vào project.

## IV. Tổ Chức Công Việc Nhóm

1. Tạo repository và branch chính theo hướng dẫn mentor.
2. Đăng ký đề tài, chức năng cốt lõi và sơ đồ database dự kiến.
3. Chia feature thành phần nhỏ có thể review độc lập.
4. Chốt lịch kiểm tra tiến độ và quy ước merge.
5. Nộp bản đăng ký để mentor phản hồi trước khi mở rộng phạm vi.

## Lỗi Thường Gặp

| Rủi ro | Cách phòng tránh |
|---|---|
| Đề tài quá rộng | Chốt ít chức năng lõi, để ý tưởng khác ở backlog |
| Không ai sở hữu database | Giao người phụ trách schema và review cùng nhóm |
| Dồn tích hợp đến cuối | Tích hợp sớm theo feature nhỏ |
| Chỉ chuẩn bị slide, không chạy app | Demo luồng thật trên dữ liệu mẫu |
| Một người làm toàn bộ | Chia task và dùng lịch sử PR để theo dõi đóng góp |

## Tổng Kết Khóa Học

Học viên đã tiếp cận các nhóm kiến thức Java, OOP, lưu trữ, database, thiết kế, giao diện và làm việc nhóm. Bài tập lớn yêu cầu chọn một domain, xác định phạm vi khả thi, chia việc và chứng minh chức năng bằng một luồng chạy được.

### Câu Hỏi Chốt Kế Hoạch

1. Ứng dụng giải quyết vấn đề nào và ai sử dụng?
2. Ba chức năng cốt lõi nào phải chạy được khi demo?
3. Dữ liệu chính gồm những bảng nào và quan hệ ra sao?
4. Nhóm làm việc và review code theo quy ước nào?
5. Tiêu chí nào chứng minh project hoàn thành?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 12](../BTVN/buoi-12-btvn.md).
## Tài Liệu Tham Khảo

- [GitHub Docs — About Repositories](https://docs.github.com/en/repositories/creating-and-managing-repositories/about-repositories)
- [MySQL Reference Manual](https://dev.mysql.com/doc/refman/8.4/en/)
