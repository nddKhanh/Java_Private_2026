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

### P1-03. `processedCommandIds` phình theo thời gian — XÁC NHẬN TỪ CODE

- **Hiện trạng:** Snapshot đã được ghi một lần mỗi batch.
  Nhưng mỗi `GameEngine.accept()` vẫn sao chép toàn bộ `processedCommandIds` rồi thêm ID mới, kể cả `system:tick:N` mỗi 250 ms khi có tower bắn hoặc projectile bay.
- **Hậu quả:** Trận dài làm snapshot JSON, bộ nhớ, dung lượng H2 và độ trễ tăng dần.
- **Sửa:** Không lưu ID của tick; chỉ giữ một cửa sổ ID gần nhất (hoặc tách metadata chống trùng khỏi snapshot công khai) mà vẫn giữ idempotency khi reconnect.
- **Test:** Chạy 10.000 tick và đo kích thước snapshot; gửi lại commandId gần đây vẫn bị coi là trùng; restart không mất revision và sequence.
- **File:** `game-core/.../GameEngine.java`, `MatchState.java`.

## B. P2 — Trải nghiệm/quan sát

### P2-01. Broadcast một snapshot đầy đủ cho mỗi event trong batch — XÁC NHẬN TỪ CODE

- **Hiện trạng:** Log lệnh đã đủ các lệnh trong batch, nhưng `GameWebSocketHandler.handleBatch()` gửi một `STATE_UPDATED` kèm toàn bộ snapshot cho từng event.
  Batch 256 command thành 256 snapshot gửi tới mọi viewer.
- **Sửa:** Gửi một `STATE_UPDATED` cho mỗi batch, kèm danh sách `events` theo thứ tự; viewer thêm đủ các event vào log.
- **Test:** Batch 3 lệnh hợp lệ ⇒ viewer nhận một snapshot và 3 event đúng thứ tự; Partial 2 thành công + 1 lỗi ⇒ log có đúng 2; reconnect không trùng.
- **File:** `game-server/.../GameWebSocketHandler.java`, `StateUpdatedMessage.java`, `game-web/src/main.ts`, `types.ts`.

## C. Rủi ro triển khai — Chỉ ưu tiên nếu chạy trên máy chung/public

1. **Java code học viên không được sandbox:** `BotCompiler` biên dịch mã Java tự do, `BotProcessRunner` chỉ giới hạn heap, thời gian và output; tiến trình vẫn có quyền đọc/ghi file và gọi mạng theo tài khoản OS. Nếu Desktop chạy source không tin cậy trên máy giáo viên/máy chung, cần cách ly bằng tài khoản OS/container/VM và quyền mạng/tệp. Nếu mỗi học viên chỉ chạy code của chính mình trên máy riêng thì có thể chấp nhận rủi ro MVP có ghi rõ.
2. **Xác thực chưa đủ cho public Internet:** `AccountService.validatePassword()` chấp nhận mật khẩu dài 1 byte; không thấy throttling đăng nhập, WebSocket dùng `ws://` mặc định, CORS/API/WebSocket origin mở rộng. Với LAN thực hành kiểm soát được thì đây là hardening.

## Những chức năng không đưa vào sửa lại

- Atomic/Partial tuần tự, exception Java hủy batch dở; spawn đúng HQ/BARRACKS và tính hạn mức công trình.
- Đạn Bresenham cố định, xuyên đồng minh, tháp cooldown 1 giây; API `bot.stay()`.
- Cảnh báo overload spawn cũ, nhãn ID nằm trên hình; phân biệt WORKER/MELEE/RANGED/SIEGE và HQ/BARRACKS/TOWER/WALL.

## Kiểm chứng tập trung, một lần

- Chạy unit/integration tests Maven Core/Server/Client/Desktop và `npm test`, `npm run build`; **ghi kết quả thực tế**, không suy luận từ `PROGRESS.md`.
- Bổ sung test cho P1-02, P1-03, P2-01 và load test cho P1-02/P1-03.
- Chạy một kịch bản 4–10 đội, 2 phe và hai máy học viên cùng trận, IntelliJ Run → Desktop → Server → Phaser; batch nhiều lệnh, mất mạng, restart giữa trận, viewer chậm.
- Với mục chỉ là rủi ro, báo `chưa tái hiện` nếu chưa chạy test. Không khẳng định repo “hết mọi lỗi” chỉ từ rà soát tĩnh.
