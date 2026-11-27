# Project Java mẫu cho học viên

1. Mở riêng thư mục này bằng IntelliJ IDEA và chờ Maven tải dependency.
2. Sao chép `.env.example` thành `.env`, rồi điền key trận đấu vào `MATCH_KEY`, tài khoản đã vào đội trên web vào `USERNAME` và `PASSWORD`.
   `.env` nằm trong `.gitignore` nên mật khẩu không bị commit.
3. Viết chiến thuật tại `Main.main(Bot bot)`.
4. Mở Desktop Code Studio; không cần bấm Compile hoặc Run tại Desktop.
5. Chạy `Submit.main()` trong IntelliJ để gửi source, key và tài khoản tới Desktop rồi xem kết quả ngay trong console IntelliJ.

Desktop tự pair trong LAN, đăng nhập để nhận token điều khiển của đội mình, biên dịch source, rồi xếp các lệnh vào hàng chờ của từng bot trên server.
Mỗi bot chạy một lệnh mỗi nhịp 250 ms; theo dõi tiến trình và lệnh bị từ chối trên bản đồ.
Source Java chỉ được biên dịch trên Desktop và không được gửi đến server game.
Batch chỉ được gửi khi `Main.main(Bot bot)` kết thúc bình thường; exception giữa chương trình sẽ bỏ các lệnh đang gom.

API học viên gồm `select`, `go`, `hit`, `mine`, `build`, `spawn`, `stay` và `state`.
Muốn tạo quân, gọi `select` với ID của HQ hoặc BARRACKS rồi gọi `spawn(id, type, dir)`.
API đầy đủ phải truyền `sourceId`; overload `spawn` cũ không có ID công trình đã deprecated và sẽ báo lỗi trước khi gửi mạng.
