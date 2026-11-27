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

- **Giá trị còn là số thử nghiệm:** mọi thông số đã nằm trong `game-server/src/main/resources/game-rules.yml`, nhưng HQ chỉ có 3 HP, mọi quân 3 HP, mọi công trình 5 HP; SIEGE chỉ hơn 1 sát thương, RANGED chỉ hơn 1 tầm.
  Cần chốt bảng HP, sát thương, tầm cho từng loại quân, HP từng loại công trình và HP của HQ.
- **Đánh không hồi chiêu, đi không giới hạn tốc độ:** hết khi làm B-05, vì mỗi bot chỉ chạy một lệnh mỗi nhịp 250 ms.

### B-02. Cân bằng kinh tế

- **Giá quá rẻ so với vốn đầu:** 100 gỗ và 100 thịt khởi đầu trong khi quân tốn 1–3 thịt, công trình tốn 1–3 gỗ, nên khai thác gần như không cần.
  Cần chốt lại vốn khởi đầu hoặc bảng giá.
- **Khai thác:** 3 đơn vị mỗi lệnh, phải đứng đúng trên mỏ, không giới hạn số lần mỗi giây.
  Cần chốt lượng khai thác và có cần hồi chiêu khai thác không.
- **Giới hạn quân:** 50 quân × 10 đội trên bản đồ 50×50 dễ kín chỗ.
  Cần chốt giới hạn quân mỗi đội cho bản đồ này.

### B-05. Hàng chờ lệnh cho từng bot

- **Mục tiêu:** mỗi nhịp 250 ms, mỗi unit hoặc công trình chỉ chạy một lệnh; lần Run chỉ nối thêm lệnh vào cuối hàng chờ của từng bot.
- **Đã chốt:**
  - Bỏ Atomic/Partial; lệnh lỗi lúc tới lượt chạy thì xóa các lệnh còn lại của đúng bot đó trong đúng lần gửi đó, lệnh của lần gửi khác và của bot khác giữ nguyên.
  - Bỏ giới hạn 10 hành động mỗi bot mỗi batch; chống spam bằng giới hạn độ dài hàng chờ mỗi bot và giữ giới hạn 256 lệnh mỗi lần gửi, cả hai đặt trong `game-rules.yml`.
  - Bấm vào unit hoặc công trình của đội mình thì cột phải hiện hàng chờ của nó thay cho Lực lượng, chỉ có nút xóa toàn bộ lệnh.
  - Mọi thành viên cùng đội được xóa hàng chờ; đồng minh khác đội thì không; hàng chờ chỉ gửi cho thành viên cùng đội.
  - Không thêm hồi chiêu riêng cho quân.
  - Người chơi điều khiển trực tiếp qua giao diện: vị trí trên viewer là vị trí thật, hàng chờ trên viewer là hàng chờ thật của server.
    `bot.state()` giữ nguyên là state lúc bắt đầu Run, chưa tính lệnh còn chờ; Run không tự chờ hàng chờ chạy hết.
    Docs ghi rõ điều này.

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
