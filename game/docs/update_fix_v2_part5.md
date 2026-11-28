> **Prompt cho agent (tiết kiệm token):** Đọc một lần danh sách này, xử lý theo ưu tiên P0→P1→P2; chỉ mở các hàm/file được dẫn, không quét lại toàn repo hoặc refactor ngoài phạm vi. Với mỗi mục: viết test tái hiện lỗi trước, sửa tối thiểu, chạy test liên quan. Gom kiểm tra Maven/frontend vào cuối; báo cáo duy nhất bảng `mục | đã sửa | test thực chạy | còn rủi ro`. Không coi `PROGRESS.md` là bằng chứng test do chính agent vừa chạy; không tự mở rộng tính năng.

# Ant Colony Wars — Vấn đề còn mở

**Repo:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game/`.

Danh sách chỉ giữ việc chưa xong; mục nào xử lý xong thì xóa khỏi file.

## A. P1 — Đúng đắn vận hành/ổn định khi chơi thật

### P1-02. Khóa WebSocket toàn server — XÁC NHẬN TỪ CODE; CHỈ CẦN KHI ĐÔNG NGƯỜI CHƠI

- **Hiện trạng:** `handleTextMessage()` vẫn `synchronized` trên một handler chung cho mọi trận; broadcast gửi tuần tự tới từng socket.
- **Hậu quả:** Một trận bận hoặc viewer chậm làm tăng độ trễ của trận khác.
  Với khoảng 30 người chơi, chưa cần xử lý.
- **Sửa:** Tuần tự hóa theo trận thay vì khóa toàn server; giới hạn hoặc đệm gửi tới viewer chậm.
  Không tái áp giới hạn theo giây.
- **Test:** Hai trận gửi đồng thời không chặn nhau; load test nhiều kết nối.
- **File:** `game-server/.../GameWebSocketHandler.java`, `MatchCoordinator.java`.

## A2. P2 — Đồ họa và trải nghiệm (B-06 đã chốt; đợt 1 xong ngày 2026-11-28)

### B-06. Kế hoạch đồ họa

- **Nguyên tắc:** học ý tưởng từ [tram-cuoi-cung-hoc-vien](https://github.com/tung51652-alt/tram-cuoi-cung-hoc-vien), không sao chép mã hay tài sản đồ họa; mọi hình do dự án tự vẽ.
  Không sửa server; viewer chỉ dùng dữ liệu `STATE_UPDATED` hiện có (state, queues, events, rejections).
- **Không làm:** tạm dừng, xem chậm/nhanh, phím tốc độ xem; trận đấu online nên viewer luôn hiện trạng thái trực tiếp.

#### Đợt 2. Nhật ký lệnh có màu

- **Web:**
  - Dòng nhật ký hiện tiếng Việt, dùng chung `ACTION_LABELS` với hàng chờ, kèm biểu tượng và màu theo loại lệnh; cam là bị từ chối.
  - Giữ khoảng 50 dòng có cuộn.
  - Lọc "Chỉ đội tôi" và "Chỉ lỗi".
  - Bấm vào dòng thì chọn bot đó trên bản đồ, chỉ với bot đội mình.
- **Desktop:**
  - Console đổi từ `TextArea` sang danh sách dòng có màu: đỏ là lỗi biên dịch hoặc exception, cam là bị từ chối, xanh là thành công, xám là thông tin.
  - Lỗi biên dịch hiện số dòng kèm một câu giải thích dễ hiểu.
- **Test:**
  - Web: test phân loại dòng nhật ký.
  - Desktop: test phân loại mức thông báo, không cần mở GUI.

#### Đợt 3. Chuyển động trên bản đồ

- `BattleScene` giữ đối tượng theo id thay vì `children.removeAll()` mỗi trạng thái; chỉ tạo mới, cập nhật hoặc xóa phần thay đổi.
- Quân trượt sang ô mới trong 250 ms, đúng nhịp mô phỏng.
- Đạn bay theo đường đi.
- Nháy đỏ khi trúng đòn.
- Hiệu ứng vỡ khi quân hoặc công trình bị phá, rồi mới xóa.
- Khi tab bị ẩn rồi quay lại: bỏ hiệu ứng tồn đọng, nhảy thẳng tới trạng thái mới nhất.
- **Test:**
  - Test hàm so sánh trạng thái cũ và mới để biết đối tượng nào tạo, cập nhật hoặc xóa.
  - Soát bằng mắt trận 10 đội trên 50×50.

#### Đợt 4. Hướng dẫn có hình, làm sau đợt 1–3

- **Ảnh model:** bảng model có ảnh từng loại quân, công trình, mỏ và đạn, kèm HP, sát thương, tầm và giá đọc từ `game-rules.yml` để không lệch số.
- **Bản đồ mẫu:**
  - Ảnh bản đồ 12×12 và 50×50, đánh số các vùng: HQ, mỏ, bãi trống, đường tiến quân.
  - Ảnh minh họa hệ tọa độ: gốc (0,0), hướng UP/RIGHT/DOWN/LEFT, đường đạn.
- **Ảnh giao diện:** sảnh, phòng chờ, màn trận và Desktop, có ô số đỏ đánh dấu từng khu vực, chú thích ngay dưới ảnh.
- **Trang trí docs:**
  - Banner đầu `GUIDE.md` vẽ từ bộ model.
  - Biểu tượng nhỏ cho từng mục.
  - Hộp "Mẹo" và "Lưu ý".
  - Chú giải màu nhật ký.
  - Mọi thứ hiển thị đúng trên GitHub.
- **Cách tạo ảnh:**
  - Script trong `game/scripts/` mở trang trưng bày model và bản đồ mẫu bằng Chrome headless rồi xuất PNG vào `game/docs/images/`, để chạy lại khi đồ họa đổi.
  - Ảnh Desktop chụp tay.

## B. Quyết định còn mở — cần chủ dự án chốt trước khi làm

### B-01. Cân bằng chiến đấu

- **Giá trị còn là số thử nghiệm:** mọi thông số đã nằm trong `game-server/src/main/resources/game-rules.yml`, nhưng HQ chỉ có 3 HP, mọi quân 3 HP, mọi công trình 5 HP; SIEGE chỉ hơn 1 sát thương, RANGED chỉ hơn 1 tầm.
  Cần chốt bảng HP, sát thương, tầm cho từng loại quân, HP từng loại công trình và HP của HQ.

### B-02. Cân bằng kinh tế

- **Giá quá rẻ so với vốn đầu:** 100 gỗ và 100 thịt khởi đầu trong khi quân tốn 1–3 thịt, công trình tốn 1–3 gỗ, nên khai thác gần như không cần.
  Cần chốt lại vốn khởi đầu hoặc bảng giá.
- **Khai thác:** 3 đơn vị mỗi lệnh, phải đứng đúng trên mỏ, không giới hạn số lần mỗi giây.
  Cần chốt lượng khai thác và có cần hồi chiêu khai thác không.
- **Giới hạn quân:** 50 quân × 10 đội trên bản đồ 50×50 dễ kín chỗ.
  Cần chốt giới hạn quân mỗi đội cho bản đồ này.

### B-07. Viết code trong trình soạn thảo bất kỳ

- **Hiện trạng:** người chơi phải dùng ô soạn thảo của Desktop Code Studio, hoặc project IntelliJ mẫu chạy `Submit.main()`.
- **Tham khảo:** ở repo trên, người chơi sửa một file bằng trình soạn thảo bất kỳ rồi lưu; game có nút "Chạy lại code" (F5) luôn đọc bản đã lưu mới nhất, không cần bước gửi code.
- **Mục tiêu:** một file mẫu mà người chơi mở bằng IDE hay trình soạn thảo nào cũng chạy được.
- **Hướng có thể chọn:**
  - Desktop theo dõi một file người chơi chọn và nút Run luôn biên dịch bản đã lưu, giống repo tham khảo.
  - File mẫu tự chứa một lớp nhỏ chỉ dùng thư viện chuẩn của JDK 21 để gửi lệnh tới Desktop qua loopback, nên chạy được bằng `java Main.java` hay nút Run của IDE bất kỳ mà không cần cài thêm thư viện.
- **Cần chốt:** chọn hướng gửi code, file mẫu đặt ở đâu và có giữ project IntelliJ mẫu hiện tại không.

## C. Rủi ro triển khai — Chỉ ưu tiên nếu chạy trên máy chung/public

1. **Java code học viên không được sandbox:** `BotCompiler` biên dịch mã Java tự do, `BotProcessRunner` chỉ giới hạn heap, thời gian và output; tiến trình vẫn có quyền đọc/ghi file và gọi mạng theo tài khoản OS. Nếu Desktop chạy source không tin cậy trên máy giáo viên/máy chung, cần cách ly bằng tài khoản OS/container/VM và quyền mạng/tệp. Nếu mỗi học viên chỉ chạy code của chính mình trên máy riêng thì có thể chấp nhận rủi ro MVP có ghi rõ.
2. **Xác thực chưa đủ cho public Internet:** `AccountService.validatePassword()` chấp nhận mật khẩu dài 1 byte; không thấy throttling đăng nhập, WebSocket dùng `ws://` mặc định, CORS/API/WebSocket origin mở rộng. Với LAN thực hành kiểm soát được thì đây là hardening.

## Những chức năng không đưa vào sửa lại

- Hàng chờ lệnh theo bot, exception Java hủy batch dở; spawn đúng HQ/BARRACKS.
- Đạn Bresenham cố định, xuyên đồng minh, tháp cooldown 1 giây; API `bot.stay()`.
- Cảnh báo overload spawn cũ, nhãn ID nằm trên hình; phân biệt WORKER/MELEE/RANGED/SIEGE và HQ/BARRACKS/TOWER/WALL.
- Tạm dừng và đổi tốc độ xem trận: trận đấu online, viewer luôn hiện trực tiếp.

## Kiểm chứng tập trung, một lần

- Chạy unit/integration tests Maven Core/Server/Client/Desktop và `npm test`, `npm run build`; **ghi kết quả thực tế**, không suy luận từ `PROGRESS.md`.
- Bổ sung test và load test cho P1-02 khi cần mở rộng.
- Chạy một kịch bản 4–10 đội, 2 phe và hai máy học viên cùng trận, IntelliJ Run → Desktop → Server → Phaser; batch nhiều lệnh, mất mạng, restart giữa trận, viewer chậm.
- Với mục chỉ là rủi ro, báo `chưa tái hiện` nếu chưa chạy test. Không khẳng định repo “hết mọi lỗi” chỉ từ rà soát tĩnh.
