> **Prompt cho agent (tiết kiệm token):** Đọc một lần danh sách này, xử lý theo ưu tiên P0→P1→P2; chỉ mở các hàm/file được dẫn, không quét lại toàn repo hoặc refactor ngoài phạm vi. Với mỗi mục: viết test tái hiện lỗi trước, sửa tối thiểu, chạy test liên quan. Gom kiểm tra Maven/frontend vào cuối; báo cáo duy nhất bảng `mục | đã sửa | test thực chạy | còn rủi ro`. Không coi `PROGRESS.md` là bằng chứng test do chính agent vừa chạy; không tự mở rộng tính năng.

# Ant Colony Wars — Rà soát tổng thể, danh sách vấn đề tập trung

**Repo:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game/`.

**Mốc:** [`9c49556ba099ee043a349b8d9e2d3ae6b7b8f3c2`](https://github.com/nddKhanh/Java_Private_2026/commit/9c49556ba099ee043a349b8d9e2d3ae6b7b8f3c2), 14/11/2026.

**Phương pháp/giới hạn:** Lấy cây GitHub đầy đủ (168 tệp trong `game/`, gồm 114 tệp main/frontend và 16 tệp test mã nguồn + một placeholder), đọc sâu các đường đi Core, API, lobby/auth, persistence, WebSocket, Client SDK, Desktop/IntelliJ, Phaser và tài liệu/test liên quan. Đây là **rà soát tĩnh có mục tiêu, chưa chạy Maven, npm, trình duyệt hay LAN thật**. Phân loại: **Xác nhận từ code** = đường chạy hiện tại rõ ràng; **Rủi ro cần test** = giả thuyết có cơ sở nhưng chưa tái hiện.

## A. P0 — Phải xử lý trước khi cho nhiều đội thi đấu thật

### P0-01. Lộ quyền điều khiển của tất cả đội trong một trận — XÁC NHẬN TỪ CODE

- **Nguyên nhân:** `LobbyService.TEAM_TOKENS` dùng giá trị cố định (`BLUE`, `RED`...); `LobbyService.viewFor()` trả token **mọi đội** cho bất kỳ thành viên phòng; `LobbyService.pair()` cũng trả **mọi token đội** thông qua `GET /api/desktop/pairing/{key}` không xác thực tài khoản; `TokenHandshakeInterceptor` chấp nhận token đội để cấp quyền WebSocket mà không kiểm tra tài khoản có thuộc đội đó hay không. Pairing key chỉ gồm 6 ký tự thuộc `2..9` (8^6 = 262.144 khả năng), không thấy chống thử liên tục.
- **Hậu quả:** Biết một token/mã pairing (hoặc chỉ là thành viên của phòng) có thể dùng token đội khác và điều khiển quân đối thủ. Thêm `TEAM_ID` trong IntelliJ chỉ chọn đúng token, **không** chứng minh quyền sở hữu đội.
- **Sửa:** Ràng buộc quyền điều khiển WebSocket với phiên tài khoản + membership trong đúng trận, hoặc cấp bí mật mạnh riêng từng đội và **không bao giờ trả token đội khác** từ lobby/pairing. Mã pairing chỉ nên dùng khám phá phòng/kết nối, không thay thế quyền điều khiển. Tránh token có thể dự đoán, thêm cơ chế giới hạn thử mã phù hợp.
- **Test:** Tài khoản A thuộc TEAM_1 dùng token TEAM_2 để gửi MOVE ⇒ bị chặn; A lấy response của lobby/pairing ⇒ không thể thu thập token đội khác; tài khoản B hợp lệ TEAM_2 vẫn gửi MOVE được; thử restart và reconnect.
- **File:** `game-server/.../LobbyService.java`, `DesktopPairingController.java`, `TokenHandshakeInterceptor.java`, `LobbyController.java`, `game-desktop/.../CodeStudioApplication.java`, test `LobbyFlowIntegrationTest.java`.

### P0-02. Trận tạo từ map script không chia đúng hai phe — XÁC NHẬN TỪ CODE

- **Nguyên nhân:** `MapScriptParser.parse()` tạo mỗi team với `factionId = teamId(index)`. Vì thế map 10 đội tạo **10 phe**, trong khi yêu cầu trò chơi là **10 đội thuộc 2 phe** và `GameEngine.sameFaction()`/điều kiện thắng dựa vào `factionId`.
- **Hậu quả:** Đội đáng lẽ là đồng minh trở thành địch; tự bắn lẫn nhau, tower chọn sai mục tiêu và điều kiện thắng không còn là hai phe.
- **Sửa:** Cho phép map script khai báo phe của từng đội (hoặc một quy tắc phân phe rõ ràng, được thống nhất); validate **đúng 2 phe** nếu đây là luật bắt buộc. Các team cùng phe dùng chung `factionId`. Không hard-code phân phe mơ hồ.
- **Test:** Tạo map 4 hoặc 10 đội, kiểm tra có đúng 2 `factionId`; đồng đội cùng phe không bị đạn/tower đánh; kết thúc trận khi mọi HQ của **phe đối phương** bị loại, không phải từng đội riêng.
- **File:** `game-server/.../MapScriptParser.java`, `game-core/.../GameEngine.java`, test `LobbyFlowIntegrationTest.java`/test parser mới.

## B. P1 — Đúng đắn vận hành/ổn định khi chơi thật

### P1-01. Viewer tự kết nối lại sau khi người dùng chủ động rời trận — XÁC NHẬN TỪ CODE

- **Nguyên nhân:** `GameSocket.disconnect()` gọi `socket.close()` và xóa timer, nhưng listener `close` trong `connect()` **luôn** chạy `scheduleReconnect()`, không có cờ dừng chủ động. `main.ts` gọi `disconnect()` khi về sảnh/chuyển trận.
- **Hậu quả:** Socket cũ sống lại sau ~2 giây; có thể phát snapshot từ trận cũ vào UI của trận mới, tạo kết nối ngầm hoặc hiển thị state sai.
- **Sửa:** Dùng `shouldReconnect`/`disposed`, tắt trước khi close, kiểm tra ở callback `close`, timer và `receive`. Ngắt hẳn handler của socket cũ.
- **Test:** Vào trận A → về sảnh hoặc sang B → sau 3–5 giây không còn socket A, không nhận state A, chỉ một socket còn hoạt động. Thử mạng chập chờn (reconnect vẫn hoạt động nếu chưa chủ động rời).
- **File:** `game-web/src/GameSocket.ts`, `game-web/src/main.ts`.

### P1-02. Không có giới hạn tổng kích thước batch/điểm nghẽn WebSocket toàn server — XÁC NHẬN CẤU TRÚC; CẦN LOAD TEST

- **Nguyên nhân:** `GameWebSocketHandler.handleBatch()` chuyển **toàn bộ** `request.commands()` sang `List<ActionCommand>` trước khi engine dừng ở lỗi; không giới hạn số phần tử/bytes ở tầng ứng dụng. `MatchCoordinator.commandQueueCapacity` có cấu hình/getter nhưng không được áp dụng vào đường `handleBatch`. `handleTextMessage()` là `synchronized` trên một handler cho **mọi trận**, broadcast cũng dùng đồng bộ và gửi từng socket.
- **Hậu quả:** Request lớn hoặc viewer chậm có thể giữ khóa toàn cục, tăng latency cho trận khác. Ngược lại, kích thước thông điệp tối đa WebSocket/container chưa được kiểm chứng có đáp ứng batch hợp lệ với hàng chục/quân hàng trăm hành động hay không.
- **Sửa:** Định nghĩa hạn mức **tổng batch theo quy mô game**, giới hạn bytes/command count **trước khi chuyển toàn bộ**; cấu hình giới hạn WebSocket đủ cho batch hợp lệ; tách tuần tự **theo trận** thay vì đồng bộ tất cả trận; giới hạn/đệm gửi tới viewer chậm. Không tái áp giới hạn 10 lệnh/giây: yêu cầu hiện tại là 10 hành động **mỗi đối tượng mỗi batch**.
- **Test:** Batch lớn hợp lệ cho nhiều đơn vị chạy được; batch quá lớn bị từ chối nhanh; hai match gửi đồng thời không chặn lẫn nhau; một viewer chậm không làm tăng bất thường thời gian xử lý match khác.
- **File:** `game-server/.../GameWebSocketHandler.java`, `MatchCoordinator.java`, `GameWebSocketConfiguration.java`, cấu hình server và integration/load tests.

### P1-03. Snapshot/log lệnh phình theo thời gian và bị ghi lặp theo số event — XÁC NHẬN TỪ CODE

- **Nguyên nhân:** Mỗi `GameEngine.accept()` sao chép `processedCommandIds` rồi thêm ID mới; ID tồn tại mãi trong `MatchState`, bao gồm tick. `MatchCoordinator.persist()` gọi `persistence.save(result.state(), event)` **cho từng event** trong batch; `JdbcMatchPersistence.save()` serialize và UPDATE **toàn bộ snapshot cuối batch** mỗi lần. Với N lệnh, chi phí tăng theo cả số lệnh lịch sử và số event trong batch.
- **Hậu quả:** Mô phỏng dài với tower/tick và nhiều bot dễ tăng bộ nhớ, kích thước JSON, lưu H2 và độ trễ; các lần ghi trùng snapshot không thêm giá trị.
- **Sửa:** Tách metadata chống duplicate khỏi snapshot công khai (hoặc dùng cấu trúc lưu trữ/retention xác định), persist **một snapshot cuối + toàn bộ event trong một transaction**; giữ sequence/restart/replay chính xác.
- **Test:** 1.000–10.000 lệnh/tick có tải đo kích thước snapshot và latency; batch 10 lệnh chỉ ghi snapshot một lần nhưng 10 event; restart/reconnect không mất revision, sequence, idempotency.
- **File:** `game-core/.../GameEngine.java`, `MatchState.java`; `game-server/.../MatchCoordinator.java`, `JdbcMatchPersistence.java`.

### P1-04. Trạng thái RAM có thể lệch database nếu persist lỗi — RỦI RO CÓ CĂN CỨ, CẦN TEST LỖI

- **Nguyên nhân:** `GameSession.applyBatch()` và `advanceTick()` cập nhật state trong RAM **trước khi** `MatchCoordinator` lưu qua `MatchPersistence`. Mỗi `JdbcMatchPersistence.save()` có transaction riêng, không thấy transaction tổng bao bọc cả batch/in-memory commit.
- **Hậu quả:** Nếu lưu DB thất bại sau vài event, RAM đã có lệnh nhưng snapshot/event log trên đĩa có thể cũ/không đầy đủ. Khi khởi động lại sẽ phục hồi khác với trận vừa thấy trên UI.
- **Sửa:** Thiết kế bước tính toán trên working state, persist atomic snapshot+events rồi mới publish/commit trạng thái RAM; quản lý lỗi DB rõ ràng. Không làm cả hệ thống event-sourcing nếu MVP chưa cần.
- **Test:** Inject `MatchPersistence` ném exception ở event thứ 2; sau retry/restart state và history phải nhất quán; Atomic/Partial vẫn giữ nguyên ngữ nghĩa.
- **File:** `game-core/.../GameSession.java`, `game-server/.../MatchCoordinator.java`, `JdbcMatchPersistence.java`.

### P1-05. Người chơi đã vào đội không thể nhập mã để vào lại trận đang diễn ra — XÁC NHẬN TỪ CODE

- **Nguyên nhân:** Web `game-web/src/main.ts` dùng `findLobby()` gọi `POST /api/lobbies/join` với `matchCode` và `teamToken`. Nhưng `LobbyService.join()` kiểm tra `status != WAITING` và trả `MATCH_ALREADY_STARTED` **trước khi** tra `game_lobby_member` để xác định tài khoản đã thuộc trận/đội hay chưa.
- **Hậu quả:** Học viên đã tham gia hợp lệ nhưng đóng tab, về sảnh, tải lại trang hoặc mất mạng sẽ không thể dùng mã trận + token đội để trở lại phòng/trận đang chạy. **Đây không phải là vấn đề của IntelliJ `MATCH_KEY` + `TEAM_ID`**, vì luồng pairing riêng vẫn có thể chạy nếu trận còn tồn tại.
- **Sửa:** Trong `LobbyService.join()`, sau khi xác thực tài khoản và trận, **kiểm tra membership hiện có trước**. Nếu tài khoản đã thuộc *đúng đội* của trận, cho phép lấy lại thông tin phòng và mở viewer/snapshot của trận `RUNNING` mà **không tạo thêm slot, không đổi đội, không reset state**. Nếu chưa là thành viên, tiếp tục từ chối gia nhập trận đã bắt đầu. Với tài khoản đã thuộc đội A nhưng nhập token đội B, từ chối đổi đội sau khi bắt đầu. UI phải có đường quay lại trận bằng mã (hoặc mục "Trận của tôi") khi còn đăng nhập. Phân biệt rời màn hình/mất mạng với chủ động rời đội.
- **Test:** A join TEAM_1 khi `WAITING` → chủ phòng start → A rời viewer/reload → A nhập lại mã trận + token TEAM_1 và xem snapshot mới nhất, không tăng số thành viên, quân/tài nguyên/revision giữ nguyên; B chưa join không được vào sau start; A không thể chuyển TEAM_2; reconnect sau mất WebSocket không sinh trận mới; nếu trận `FINISHED`, hiển thị trạng thái/kết quả thay vì báo nhầm `MATCH_ALREADY_STARTED`.
- **File:** `game-server/src/main/java/com/antcolonywars/server/LobbyService.java` (`join`, `viewFor`), `LobbyController.java`; `game-web/src/main.ts` (`findLobby`, `showWaitingRoom`, `startMatch`); test `LobbyFlowIntegrationTest.java` và thử thực tế trên trình duyệt.

## C. P2 — Trải nghiệm/quan sát/độ sạch tài liệu

### P2-01. Bảng lệnh gần nhất bỏ mất các lệnh trước trong cùng batch — XÁC NHẬN TỪ CODE

- **Nguyên nhân:** `GameWebSocketHandler.handleBatch()` chỉ `broadcastState(result.state(), result.events().getLast())`; `game-web/src/main.ts` chỉ thêm `message.event` vào danh sách `COMMAND GẦN NHẤT`.
- **Hậu quả:** Batch 5 hành động hợp lệ chỉ có lệnh cuối hiển thị trong log, dù revision tăng 5 và database có đủ 5 event.
- **Sửa:** Đưa danh sách events trong `STATE_UPDATED` cho batch (hoặc phát event riêng theo sequence), đảm bảo viewer giữ đủ 5 lệnh mới nhất; không cần gửi toàn snapshot 5 lần.
- **Test:** Batch 3 WAIT/MOVE hợp lệ ⇒ cả 3 hiện trong log đúng thứ tự; Partial 2 thành công + 1 lỗi ⇒ log có đúng 2; reconnect không trùng.
- **File:** `game-server/.../GameWebSocketHandler.java`, `StateUpdatedMessage.java`, `game-web/src/main.ts`, `types.ts`.

### P2-02. Java client có thể chờ hết timeout khi WebSocket đóng giữa lúc đợi state — XÁC NHẬN ĐƯỜNG CHẠY, CHƯA TÁI HIỆN

- **Nguyên nhân:** `GameClient.onClose()` đặt `closed = true` và `failPending()` nhưng không gán `connectionFailure`/`notifyAll()` cho `stateMonitor`. `waitForRevision()` chỉ thức dậy khi có `notifyAll()` hoặc khi quá timeout, và chỉ kiểm `connectionFailure`.
- **Hậu quả:** Nếu socket đóng sau `COMMAND_RESULT` nhưng trước `STATE_UPDATED`, client có thể báo TIMEOUT muộn thay vì lỗi mất kết nối ngay.
- **Sửa:** Khi `onClose()`, chuyển sang failure state và wake up các waiter, không chỉ pending command futures. Viết test ép server đóng socket đúng thời điểm.
- **File:** `game-client-java/.../GameClient.java`.

### P2-03. Tài liệu còn mô tả luật đã bỏ — XÁC NHẬN TỪ CODE/DOCS

- `game/docs/GUIDE.md` còn checklist "10 hành động trong một giây theo đội" và `game/plan.md` còn viết tên API `idle`; thực tế là **10 hành động trên mỗi đơn vị/công trình trong một batch** và API `stay`.
- **Sửa:** Cập nhật đúng những dòng lỗi thời; không viết lại tài liệu đầy đủ.
- **Test:** Soát đúng tên hàm và quota ở README/GUIDE/plan với SDK hiện tại.

## D. Rủi ro triển khai — Chỉ ưu tiên nếu chạy trên máy chung/public (không đồng nhất với lỗi luật MVP)

1. **Java code học viên không được sandbox:** `BotCompiler` biên dịch mã Java tự do, `BotProcessRunner` chỉ giới hạn heap, thời gian và output; tiến trình vẫn có quyền đọc/ghi file và gọi mạng theo tài khoản OS. Nếu Desktop chạy source không tin cậy trên máy giáo viên/máy chung, cần cách ly bằng tài khoản OS/container/VM và quyền mạng/tệp. Nếu mỗi học viên chỉ chạy code của chính mình trên máy riêng thì có thể chấp nhận rủi ro MVP có ghi rõ.
2. **Xác thực chưa đủ cho public Internet:** `AccountService.validatePassword()` chấp nhận mật khẩu dài 1 byte; không thấy throttling đăng nhập, WebSocket dùng `ws://` mặc định, CORS/API/WebSocket origin mở rộng. Với LAN thực hành kiểm soát được thì là hardening, **không được trộn với P0 token đội**.

## Những chức năng không đưa vào sửa lại

- Atomic/Partial tuần tự, exception Java hủy batch dở; spawn đúng HQ/BARRACKS và tính hạn mức công trình.
- Đạn Bresenham cố định, xuyên đồng minh, tháp cooldown 1 giây; API `bot.stay()`.
- `TEAM_ID` trong IntelliJ, cảnh báo overload spawn cũ, nhãn ID nằm trên hình; phân biệt WORKER/MELEE/RANGED/SIEGE và HQ/BARRACKS/TOWER/WALL.

## Kiểm chứng tập trung, một lần

- Chạy unit/integration tests Maven Core/Server/Client/Desktop và `npm test`, `npm run build`; **ghi kết quả thực tế**, không suy luận từ `PROGRESS.md`.
- Bổ sung test tối thiểu cho P0-01, P0-02, P1-01, **P1-05 (vào lại trận sau khi bắt đầu)**, P2-01, P2-02. Bổ sung lỗi DB và load test cho P1-02/03/04.
- Chạy một kịch bản 4–10 đội, 2 phe và hai máy học viên cùng trận, IntelliJ Run → Desktop → Server → Phaser; thử đổi trận/rời trang rồi nhập mã vào lại đúng đội, batch nhiều lệnh, mất mạng, restart giữa trận, viewer chậm.
- Với mục chỉ là rủi ro, báo `chưa tái hiện` nếu chưa chạy test. Không khẳng định repo “hết mọi lỗi” chỉ từ rà soát tĩnh.
