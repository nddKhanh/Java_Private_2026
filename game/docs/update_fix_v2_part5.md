> **Prompt cho agent (tiết kiệm token):** Đọc một lần danh sách này, xử lý theo ưu tiên P0→P1→P2; chỉ mở các hàm/file được dẫn, không quét lại toàn repo hoặc refactor ngoài phạm vi. Với mỗi mục: viết test tái hiện lỗi trước, sửa tối thiểu, chạy test liên quan. Gom kiểm tra Maven/frontend vào cuối; báo cáo duy nhất bảng `mục | đã sửa | test thực chạy | còn rủi ro`. Không coi `PROGRESS.md` là bằng chứng test do chính agent vừa chạy; không tự mở rộng tính năng.

# Ant Colony Wars — Vấn đề còn mở

**Repo:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game/`.

Danh sách chỉ giữ việc chưa xong; mục nào xử lý xong thì xóa khỏi file.

## A. P1 — Đúng đắn vận hành/ổn định khi chơi thật

### P1-02. Khóa WebSocket toàn server — XÁC NHẬN TỪ CODE; CHỈ CẦN KHI ĐÔNG NGƯỜI CHƠI

- **Hiện trạng:** `handleTextMessage()` vẫn `synchronized` trên một handler chung cho mọi trận; broadcast gửi tuần tự tới từng socket.
  `MatchCoordinator.commandQueueCapacity` vẫn không được dùng.
- **Hậu quả:** Một trận bận hoặc viewer chậm làm tăng độ trễ của trận khác.
  Với khoảng 30 người chơi, chưa cần xử lý.
- **Sửa:** Tuần tự hóa theo trận thay vì khóa toàn server; giới hạn hoặc đệm gửi tới viewer chậm.
  Không tái áp giới hạn theo giây.
- **Test:** Hai trận gửi đồng thời không chặn nhau; load test nhiều kết nối.
- **File:** `game-server/.../GameWebSocketHandler.java`, `MatchCoordinator.java`.

## B. Quyết định còn mở — cần chủ dự án chốt trước khi làm

### B-01. Cân bằng chiến đấu

- **Đánh không có hồi chiêu:** mỗi ATTACK tạo ngay một viên đạn; 10 ATTACK trong một batch gây 10 sát thương cùng lúc.
  Cần chốt thời gian hồi đánh cho từng loại quân (đề xuất 1 giây như TOWER).
- **HQ quá yếu:** HQ có 3 HP (`application.yml`, viết cứng trong `MapScriptParser`), một quân đứng cạnh bắn 3 phát là thắng.
  Cần chốt HP của HQ (đề xuất 20–30) và đưa giá trị vào profile thay vì viết cứng.
- **Tốc độ di chuyển không giới hạn:** mỗi batch cho 10 bước và số batch mỗi giây không giới hạn, trong khi đạn bay 1 ô mỗi 250 ms nên né đạn quá dễ.
  Cần chốt có giới hạn số ô mỗi giây cho từng quân hay không.
- **Chỉ số theo loại quân:** mọi quân có 3 HP; SIEGE chỉ hơn 1 sát thương, RANGED chỉ hơn 1 tầm.
  Cần chốt bảng HP, sát thương, tầm cho WORKER/MELEE/RANGED/SIEGE.
- **Công trình:** mọi loại có 5 HP; TOWER có sát thương 1, tầm 5, hồi 1 giây viết cứng trong `GameEngine`.
  Cần chốt HP từng loại và đưa thông số TOWER vào profile.

### B-02. Cân bằng kinh tế

- **Giá quá rẻ so với vốn đầu:** 100 gỗ và 100 thịt khởi đầu trong khi quân tốn 1–3 thịt, công trình tốn 1–3 gỗ, nên khai thác gần như không cần.
  Cần chốt lại vốn khởi đầu hoặc bảng giá.
- **Khai thác:** 3 đơn vị mỗi lệnh, phải đứng đúng trên mỏ, không giới hạn số lần mỗi giây.
  Cần chốt lượng khai thác và có cần hồi chiêu khai thác không.
- **Giới hạn quân:** 50 quân × 10 đội trên bản đồ 50×50 dễ kín chỗ.
  Cần chốt giới hạn quân mỗi đội cho bản đồ này.

### B-03. Gói khởi tạo đội còn thiếu

- **Chưa có công trình mặc định:** gói hỗ trợ `buildings` nhưng mặc định để trống; cần chốt có BARRACKS, TOWER hay WALL sẵn không.
- **Chưa có quân chiến đấu khởi đầu:** gói chỉ có một WORKER.
- **Không có mỏ chung giữa bản đồ:** `resources` của profile mặc định để trống, các đội không có lý do tranh chấp.
- **Map script không tùy biến được gói:** trận tạo trên web luôn dùng gói của server; cần chốt có cho map script ghi đè gói không.
- **Mọi đội nhận gói giống hệt nhau:** chưa có gói riêng theo đội hoặc theo phe.
- **Lỗi đặt gói khó hiểu:** gói đè lên HQ khác hoặc ra ngoài bản đồ chỉ báo lỗi chung `INVALID_MAP_SCRIPT` hoặc làm server không khởi động.

### B-04. Server gửi thông tin của mọi đội cho mọi người xem

- **Hiện trạng:** mục Lực lượng trên viewer chỉ hiện đội của người xem, nhưng `STATE_UPDATED` vẫn chứa kho gỗ/thịt, HP và quân của tất cả các đội.
- **Hậu quả:** người chơi đọc được kho và lực lượng của đối thủ qua WebSocket hoặc `bot.state()`.
- **Cần chốt:** có lọc snapshot theo đội của từng kết nối không, và đội đối phương còn thấy gì (vị trí quân trên bản đồ, HP, kho tài nguyên).

## C. Rủi ro triển khai — Chỉ ưu tiên nếu chạy trên máy chung/public

1. **Java code học viên không được sandbox:** `BotCompiler` biên dịch mã Java tự do, `BotProcessRunner` chỉ giới hạn heap, thời gian và output; tiến trình vẫn có quyền đọc/ghi file và gọi mạng theo tài khoản OS. Nếu Desktop chạy source không tin cậy trên máy giáo viên/máy chung, cần cách ly bằng tài khoản OS/container/VM và quyền mạng/tệp. Nếu mỗi học viên chỉ chạy code của chính mình trên máy riêng thì có thể chấp nhận rủi ro MVP có ghi rõ.
2. **Xác thực chưa đủ cho public Internet:** `AccountService.validatePassword()` chấp nhận mật khẩu dài 1 byte; không thấy throttling đăng nhập, WebSocket dùng `ws://` mặc định, CORS/API/WebSocket origin mở rộng. Với LAN thực hành kiểm soát được thì đây là hardening.

## Những chức năng không đưa vào sửa lại

- Atomic/Partial tuần tự, exception Java hủy batch dở; spawn đúng HQ/BARRACKS và tính hạn mức công trình.
- Đạn Bresenham cố định, xuyên đồng minh, tháp cooldown 1 giây; API `bot.stay()`.
- Cảnh báo overload spawn cũ, nhãn ID nằm trên hình; phân biệt WORKER/MELEE/RANGED/SIEGE và HQ/BARRACKS/TOWER/WALL.

## Kiểm chứng tập trung, một lần

- Chạy unit/integration tests Maven Core/Server/Client/Desktop và `npm test`, `npm run build`; **ghi kết quả thực tế**, không suy luận từ `PROGRESS.md`.
- Bổ sung test và load test cho P1-02 khi cần mở rộng.
- Chạy một kịch bản 4–10 đội, 2 phe và hai máy học viên cùng trận, IntelliJ Run → Desktop → Server → Phaser; batch nhiều lệnh, mất mạng, restart giữa trận, viewer chậm.
- Với mục chỉ là rủi ro, báo `chưa tái hiện` nếu chưa chạy test. Không khẳng định repo “hết mọi lỗi” chỉ từ rà soát tĩnh.
