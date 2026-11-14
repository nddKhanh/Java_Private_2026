# Project Java mẫu cho học viên

1. Mở riêng thư mục này bằng IntelliJ IDEA và chờ Maven tải dependency.
2. Điền key trận đấu vào `MATCH_KEY` trong `.env`.
3. Viết chiến thuật tại `Main.main(Bot bot)`.
4. Mở Desktop Code Studio; không cần bấm Compile hoặc Run tại Desktop.
5. Chạy `Submit.main()` trong IntelliJ để gửi source và key tới Desktop rồi xem kết quả ngay trong console IntelliJ.

Desktop tự pair trong LAN, biên dịch source, tạo batch rồi gửi command đến server.
Source Java chỉ được biên dịch trên Desktop và không được gửi đến server game.
Batch chỉ được gửi khi `Main.main(Bot bot)` kết thúc bình thường; exception giữa chương trình sẽ bỏ các lệnh đang gom.

API học viên gồm `select`, `go`, `hit`, `mine`, `build`, `spawn`, `stay` và `state`.
Muốn tạo quân, gọi `select` với ID của HQ hoặc BARRACKS rồi gọi `spawn(id, type, dir)`.
