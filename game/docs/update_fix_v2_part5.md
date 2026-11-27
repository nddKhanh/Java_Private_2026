> **Prompt cho agent (tiết kiệm token):** Đọc một lần danh sách này, xử lý theo ưu tiên P0→P1→P2; chỉ mở các hàm/file được dẫn, không quét lại toàn repo hoặc refactor ngoài phạm vi. Với mỗi mục: viết test tái hiện lỗi trước, sửa tối thiểu, chạy test liên quan. Gom kiểm tra Maven/frontend vào cuối; báo cáo duy nhất bảng `mục | đã sửa | test thực chạy | còn rủi ro`. Không coi `PROGRESS.md` là bằng chứng test do chính agent vừa chạy; không tự mở rộng tính năng.

# Ant Colony Wars — Vấn đề còn mở

**Repo:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game/`.

Danh sách chỉ giữ việc chưa xong; mục nào xử lý xong thì xóa khỏi file.

## A. P1 — Đúng đắn vận hành/ổn định khi chơi thật

### P1-02. Giới hạn batch và khóa WebSocket toàn server — XÁC NHẬN TỪ CODE; CẦN LOAD TEST

- **Hiện trạng:** `GameWebSocketHandler.handleBatch()` từ chối batch quá `256` command, con số viết cứng.
  Một batch hợp lệ có thể lớn hơn: 50 unit × 10 hành động là 500 command, sẽ bị trả `INVALID_BATCH`.
  `MatchCoordinator.commandQueueCapacity` vẫn không được dùng.
  `handleTextMessage()` vẫn `synchronized` trên một handler chung cho mọi trận; broadcast cũng gửi tuần tự tới từng socket.
- **Hậu quả:** Batch hợp lệ của đội đông quân bị từ chối; một trận bận hoặc viewer chậm làm tăng độ trễ của trận khác.
- **Sửa:** Tính giới hạn tổng batch theo profile (số unit và công trình tối đa × 10) và kiểm tra trước khi chuyển đổi command.
  Cấu hình giới hạn bytes WebSocket đủ cho batch hợp lệ.
  Tuần tự hóa theo trận thay vì khóa toàn server; giới hạn hoặc đệm gửi tới viewer chậm.
  Không tái áp giới hạn theo giây.
- **Test:** Batch 500 command hợp lệ chạy được; batch vượt giới hạn bị từ chối nhanh; hai trận gửi đồng thời không chặn nhau.
- **File:** `game-server/.../GameWebSocketHandler.java`, `MatchCoordinator.java`, `GameWebSocketConfiguration.java`.

## C. Rủi ro triển khai — Chỉ ưu tiên nếu chạy trên máy chung/public

1. **Java code học viên không được sandbox:** `BotCompiler` biên dịch mã Java tự do, `BotProcessRunner` chỉ giới hạn heap, thời gian và output; tiến trình vẫn có quyền đọc/ghi file và gọi mạng theo tài khoản OS. Nếu Desktop chạy source không tin cậy trên máy giáo viên/máy chung, cần cách ly bằng tài khoản OS/container/VM và quyền mạng/tệp. Nếu mỗi học viên chỉ chạy code của chính mình trên máy riêng thì có thể chấp nhận rủi ro MVP có ghi rõ.
2. **Xác thực chưa đủ cho public Internet:** `AccountService.validatePassword()` chấp nhận mật khẩu dài 1 byte; không thấy throttling đăng nhập, WebSocket dùng `ws://` mặc định, CORS/API/WebSocket origin mở rộng. Với LAN thực hành kiểm soát được thì đây là hardening.

## Những chức năng không đưa vào sửa lại

- Atomic/Partial tuần tự, exception Java hủy batch dở; spawn đúng HQ/BARRACKS và tính hạn mức công trình.
- Đạn Bresenham cố định, xuyên đồng minh, tháp cooldown 1 giây; API `bot.stay()`.
- Cảnh báo overload spawn cũ, nhãn ID nằm trên hình; phân biệt WORKER/MELEE/RANGED/SIEGE và HQ/BARRACKS/TOWER/WALL.

## Kiểm chứng tập trung, một lần

- Chạy unit/integration tests Maven Core/Server/Client/Desktop và `npm test`, `npm run build`; **ghi kết quả thực tế**, không suy luận từ `PROGRESS.md`.
- Bổ sung test và load test cho P1-02.
- Chạy một kịch bản 4–10 đội, 2 phe và hai máy học viên cùng trận, IntelliJ Run → Desktop → Server → Phaser; batch nhiều lệnh, mất mạng, restart giữa trận, viewer chậm.
- Với mục chỉ là rủi ro, báo `chưa tái hiện` nếu chưa chạy test. Không khẳng định repo “hết mọi lỗi” chỉ từ rà soát tĩnh.
