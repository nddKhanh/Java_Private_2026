# Buổi 12: Tổng Kết Khóa Học và Khởi Động Bài Tập Lớn

---

## Mục tiêu buổi học

- Kết nối các mảng đã học thành quy trình làm một ứng dụng đơn giản từ đầu.
- Hiểu yêu cầu, phạm vi, tiêu chí chấm và quy trình demo bài tập lớn.
- Chọn đề tài, chia nhóm/vai trò, tạo repository và đăng ký kế hoạch.
- Xác định chức năng, database và cách chứng minh project chạy được.

> **Nối tiếp:** 11 buổi trước xây dần ứng dụng quản lý bán hàng/đơn hàng: Java cơ bản, OOP, xử lý lỗi/file, SQL/JDBC nhiều bảng, cấu trúc MVC và Swing. Buổi 12 chuyển từ bài tập có hướng dẫn sang bài tập lớn theo nhóm.

## I. Nhìn Lại Ứng Dụng Đã Xây

Đây là buổi tổng kết và lập kế hoạch theo lộ trình; phần chọn project phục vụ bài tập lớn, không thay thế hệ thống hóa kiến thức. Nhìn lại chuỗi khái niệm: dữ liệu/điều khiển luồng → object và quan hệ → xử lý lỗi/lưu trữ → SQL/JDBC → phân tầng → làm việc nhóm.

Ứng dụng xuyên suốt đi từ phép tính giá × số lượng, thành menu giỏ hàng, rồi model Product/Customer/CartItem, Order, payment/delivery, lưu trữ MySQL, repository/service và giao diện demo. Mỗi bước giải quyết giới hạn của bước trước.

| Chặng | Kết quả chính |
|---|---|
| Buổi 1–2 | Chương trình console, menu và giỏ hàng |
| Buổi 3–5 | OOP, nhiều loại sản phẩm, thanh toán/giao hàng |
| Buổi 6–7 | Xử lý lỗi, file, rồi CRUD JDBC |
| Buổi 8–9 | Repository/service và dữ liệu đơn nhiều bảng |
| Buổi 10–11 | MVC, refactor, Git nhóm và Swing cơ bản |

> **Hình minh họa cần bổ sung:** Bản đồ kiến thức từ chương trình Java cơ bản đến ứng dụng có lưu trữ và giao diện.

## II. Chọn Đề Tài Và Chốt Phạm Vi

Gợi ý: quản lý bán hàng, thư viện, khóa học, quán cà phê hoặc đơn hàng thương mại điện tử. Chọn phạm vi vừa đủ để nhóm hoàn thành được CRUD nghiệp vụ, có quan hệ dữ liệu phù hợp và demo được luồng chính.

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

Demo nên chạy một luồng đầu-cuối bằng dữ liệu mẫu, ví dụ tìm sản phẩm → tạo đơn → xem chi tiết đơn. Chuẩn bị sẵn cách reset dữ liệu demo và phương án nếu máy không có kết nối database.

Khi lập kế hoạch, nhóm biến yêu cầu thành đầu việc kiểm chứng được: chức năng nào thao tác dữ liệu, bảng nào liên quan, lớp nào chịu trách nhiệm, kịch bản nào chứng minh luồng chạy. Đây là cách vận dụng mô hình dữ liệu, JDBC, phân tầng và Git; buổi tổng kết không yêu cầu học thêm framework mới.

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

Học viên đã đi từ chương trình Java đầu tiên đến một ứng dụng quản lý đơn hàng đơn giản có model, giao diện, database và quy trình làm việc. Bài tập lớn là cơ hội chọn domain khác và áp dụng lại kiến thức, không phải bắt đầu từ kiến thức rời rạc.

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
