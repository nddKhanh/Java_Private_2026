> **Prompt cho agent:** Chỉ xử lý 4 vấn đề trong tài liệu theo thứ tự ưu tiên; đọc đúng hàm/file được chỉ ra, sửa tối thiểu, thêm test hồi quy sát từng lỗi rồi chạy test liên quan. Không quét lại toàn repo, không refactor hoặc mở rộng phạm vi nếu không cần. Kết thúc chỉ báo ngắn: file đã sửa, test đã chạy/kết quả, việc chưa xác minh; không viết giải thích lan man.

# Ant Colony Wars — Các lỗi còn lại sau rà soát source

**Phạm vi:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game/`.

**Mốc đã rà soát:** commit `a3bc54321b6cb5ae18893679e32e90a9214c2e5b` (14/11/2026). Kết luận dựa trên đọc source và test hiện có; **chưa chạy độc lập Maven test, frontend build hoặc thao tác IntelliJ GUI thực tế trong phiên rà soát**.

## P0 — Lỗi logic cần sửa

### 1. ATTACK không bắn xuyên đồng minh khi đồng minh đứng trước địch

- **Nguyên nhân:** `GameEngine.firstAttackTarget()` chọn đối tượng gần nhất trong hướng/tầm bắn, gồm cả đồng minh; `applyAttack()` sau đó trả `FRIENDLY_TARGET`. Vì thế lệnh bị từ chối *trước khi tạo đạn*, dù `applyTick()` đã cho phép đạn xuyên đồng minh.
- **Yêu cầu:** Khi chọn mục tiêu, bỏ qua đơn vị/HQ/công trình **cùng phe**; chọn mục tiêu **địch** đầu tiên trong hướng và tầm. Đạn vẫn bay theo Bresenham cố định, xuyên qua đồng minh không gây damage/không bị chặn và dừng khi va chạm địch.
- **Kiểm thử:** Quân ta → đồng minh → địch cùng hàng: `ATTACK` được chấp nhận, đạn xuyên đồng minh và gây damage cho địch. Chỉ có đồng minh trong tầm, không có địch: không bắn trúng đồng minh. Kiểm thử cả đội khác nhưng cùng `factionId`.
- **File:** `game-core/src/main/java/com/antcolonywars/core/GameEngine.java` (`firstAttackTarget`, `applyAttack`); test ở `GameEngineAttackTest.java`.

### 2. Spawn thiếu sourceId có thể lách giới hạn 10 lệnh/công trình

- **Nguyên nhân:** `MatchCoordinator.firstLimitViolation()` đếm `SPAWN` theo `sourceId` nếu có, nhưng khi thiếu `sourceId` lại đếm theo `unitId` của quân mới. Trong khi đó, `GameEngine.productionSource()` vẫn cho phép tự tìm HQ/BARRACKS khi không có nguồn.
- **Yêu cầu:** Hạn mức tối đa **10 hành động mỗi đối tượng trong một batch**, gồm công trình sản xuất; không dùng ID quân mới để đếm spawn. Ưu tiên bắt buộc `sourceId` trên server đối với `SPAWN` nếu hợp đồng SDK mới đã yêu cầu chọn công trình. Nếu cần tương thích API cũ, phải phân giải nguồn sản xuất thực tế một cách nhất quán cho cả **kiểm tra hạn mức** và **thực thi**.
- **Kiểm thử:** Gửi 11 `SPAWN` với `unitId` khác nhau và không có `sourceId`: không được vượt giới hạn (hoặc bị từ chối vì thiếu nguồn). Hai công trình có hạn mức độc lập. Kiểm thử Atomic/Partial tại lệnh vượt ngưỡng, đồng thời bảo đảm các lỗi tài nguyên/vị trí không che mất mục đích test.
- **File:** `game-server/src/main/java/com/antcolonywars/server/MatchCoordinator.java` (`firstLimitViolation`), `game-core/src/main/java/com/antcolonywars/core/GameEngine.java` (`productionSource`); test tại `MatchCoordinatorTest.java` / WebSocket integration.

## P1 — An toàn luồng Run

### 3. Callback bất đồng bộ của lần Run cũ có thể ảnh hưởng lần Run mới

- **Hiện trạng/rủi ro:** `CodeStudioApplication.acceptSubmission()` có thay thế `activeSubmission` và dừng process cũ, nhưng callback bất đồng bộ từ `pairWithServer()` → `finishPairing()` và `compileSource()` → `finishCompilation()` chưa được ràng buộc rõ với từng lần Run. Nếu gửi lần 2 khi lần 1 đang pairing/biên dịch, kết quả cũ có nguy cơ áp lên phiên mới.
- **Yêu cầu:** Gắn `submissionId`/generation cho mỗi lần Run; trước khi callback cập nhật UI, cấu hình trận, kết quả biên dịch hoặc khởi chạy bot phải kiểm tra đó vẫn là phiên hiện hành. Callback quá hạn bị bỏ qua/giải phóng tài nguyên. Không cho batch của phiên cũ gửi nhầm trận/token sau khi bị thay thế.
- **Kiểm thử:** Run A rồi Run B rất nhanh khi A đang pairing; lặp lại khi A đang compile/chạy. Chỉ B được sử dụng source, key, match/token hiện tại và gửi batch; A nhận trạng thái bị thay thế. Kiểm thử bằng future có thể chủ động điều khiển thứ tự hoàn thành.
- **File:** `game-desktop/src/main/java/com/antcolonywars/desktop/CodeStudioApplication.java` (`acceptSubmission`, `pairWithServer`, `finishPairing`, `compileSource`, `finishCompilation`, `runBot`); Desktop integration tests.
- **Mức độ chắc chắn:** **Nguy cơ suy ra từ source**, chưa tái hiện bằng runtime.

## P2 — Tính nhất quán thông báo lỗi

### 4. Atomic có thể báo lỗi vượt hạn mức thay vì lỗi đầu tiên

- **Nguyên nhân:** `MatchCoordinator.applyBatch()` chạy `firstLimitViolation()` cho **toàn batch** trước khi `GameSession.applyBatch()` kiểm tra luật game. Nếu lệnh 1 sai luật nhưng lệnh 11 vượt hạn mức, Atomic có thể trả `BATCH_LIMIT_EXCEEDED` ở lệnh 11 thay vì lỗi sớm hơn.
- **Yêu cầu:** Kiểm tra luật chơi và hạn mức **theo đúng thứ tự lệnh**; trả `failedIndex`, `error`, ID chủ thể/hành động tương ứng với **lỗi đầu tiên**. Atomic vẫn rollback toàn batch; Partial giữ các lệnh hợp lệ trước lỗi đầu tiên.
- **Kiểm thử:** Batch gồm lệnh 1 `MOVE` vượt biên, sau đó đủ `WAIT` để lệnh 11 vượt hạn mức: cả Atomic và Partial phải báo lỗi lệnh 1, `appliedCount=0`. Batch không lỗi sớm, chỉ lỗi giới hạn tại lệnh 11: Atomic rollback 0; Partial giữ 10 lệnh đầu (nếu các lệnh đó hợp lệ).
- **File:** `game-server/src/main/java/com/antcolonywars/server/MatchCoordinator.java` (`applyBatch`, `firstLimitViolation`), `game-core/src/main/java/com/antcolonywars/core/GameSession.java`; test tại `MatchCoordinatorTest.java`.

## Kiểm chứng kết thúc

1. Test hồi quy cho cả 4 mục, ưu tiên test core/server/desktop liên quan trước.
2. Chạy Maven reactor tests và `npm run build` trong `game-web`; ghi rõ lệnh + kết quả thực tế.
3. Thử **IntelliJ Run → Desktop → Server → console IntelliJ** bằng GUI thật, gồm Atomic/Partial và bấm Run liên tiếp. Nếu chưa chạy được, ghi rõ **chưa xác minh**, không đánh dấu pass.

**Không sửa lại nếu không có hồi quy:** Partial lỗi chuyển đổi command, đạn xuyên đồng minh ở bước *va chạm*, `spawn` có `sourceId` tính theo công trình, lỗi có ID chủ thể, hình dạng riêng/nhãn ID của unit và công trình, `bot.stay()`, hủy batch dở khi Java exception, cooldown tower và đường đạn Bresenham.
