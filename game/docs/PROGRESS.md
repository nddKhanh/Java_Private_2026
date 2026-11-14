# Tiến độ dự án

Chỉ ghi kết quả đã đạt được và bằng chứng kiểm tra được.
Không dùng kế hoạch dự kiến như một kết quả đã hoàn thành.

## 2026-11-14 — P1 IntelliJ và API bot rút gọn

- **Trạng thái:** `VERIFIED` bằng test tự động.
- **Đã đạt:** Thêm project mẫu IntelliJ với `.env`, `Main.main(Bot)` và lớp `Submit` gửi source tới Desktop qua loopback.
- **Đã đạt:** Desktop tự điền pairing key, chọn Atomic/Partial, biên dịch và chạy source; game server không nhận source hoặc class Java.
- **Đã đạt:** API ngắn gồm `select`, `go`, `hit`, `mine`, `build`, `spawn`, `idle` và `state`; API đầy đủ cũ vẫn hoạt động.
- **Bằng chứng:** `mvnw -q -pl student-bot-template,game-desktop -am test` hoàn tất với exit code 0.
- **Giới hạn:** Java không cho override `Object.wait()` vì phương thức này là `final`, nên thao tác chờ của API ngắn mang tên `idle()`.
- **Chưa kiểm chứng:** Chưa kiểm thử trực quan thao tác Run `Submit` trong IntelliJ với app-image Desktop đã đóng gói.
- **Tiếp theo:** P2 luật sản xuất theo công trình, building combat, tower tự bắn và projectile theo đường cố định.

## 2026-11-14 — P0 batch và kiểm tra lệnh

- **Trạng thái:** VERIFIED.
- **Đã đạt:** Mỗi lần Run trên Desktop tạo một batch; mỗi bot có tối đa 10 hành động và thao tác chọn bot không tính hạn mức.
- **Đã đạt:** Desktop cho chọn Atomic hoặc Partial; server kiểm tra lại tuần tự trên state sau từng lệnh và trả lỗi tiếng Việt tại lệnh đầu tiên không hợp lệ.
- **Đã đạt:** Đã bỏ rate limit theo giây; simulation tick tiếp tục chạy độc lập với batch của người chơi.
- **Bằng chứng:** Toàn bộ Maven test pass, gồm test Game Core, coordinator, WebSocket schema 7, Java client, runner và Desktop.
- **Tiếp theo:** P1 project mẫu IntelliJ, API rút gọn và luồng chuyển source qua Desktop.
- **Chưa kiểm chứng:** Chưa kiểm thử trực quan combobox chế độ batch trong app-image đã đóng gói.

## 2026-11-14 — Desktop pairing trong LAN

- **Trạng thái:** `IMPLEMENTED`.
- **Đã đạt:** Mỗi lobby động có pairing key riêng; endpoint pairing trả LAN WebSocket URL, match code và token đội theo màu.
- **Đã đạt:** Desktop quét LAN từ một key, tự điền cấu hình và cho chọn team token; nhập thủ công vẫn được giữ.
- **Bằng chứng:** Maven reactor test qua `game-desktop` pass; frontend production build pass; Flyway V4 chạy trên H2 test database.
- **Chưa kiểm chứng:** Chưa kiểm thử pairing giữa hai máy vật lý hoặc qua Windows Firewall; quét tự động dùng cổng server mặc định `8080` và subnet IPv4 `/24` hiện hoạt.

## Template cập nhật

```markdown
## YYYY-MM-DD — [Mốc hoặc phase]

- **Trạng thái:** `TODO | DOING | VERIFIED | BLOCKED`
- **Đã đạt:** [Kết quả ngắn gọn].
- **Bằng chứng:** [File, test, ảnh hoặc lệnh kiểm tra].
- **Tiếp theo:** [Một việc gần nhất].
- **Chưa kiểm chứng:** [Giới hạn còn lại, nếu có].
```

## 2026-11-07 — Thiết kế MVP

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Chốt MVP điều khiển bằng Java 21 client, Spring Boot WebSocket và Phaser viewer.
- **Đã đạt:** Chốt một trận 8×8 với hai đội, hai HQ, hai bot và vòng đời chiến thắng tối thiểu.
- **Đã đạt:** Chia quá trình triển khai thành sáu phase với tiêu chí nghiệm thu cụ thể.
- **Bằng chứng:** `plan.md` v0.4 và `roadmap.md` v0.2.
- **Tiếp theo:** Khởi tạo Phase 00 và xác minh build sạch cho backend cùng frontend.
- **Chưa kiểm chứng:** Chưa có mã nguồn, build, test tự động hoặc demo runtime.

## 2026-11-07 — Phase 00

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Dựng Maven multi-module gồm `game-core`, `game-server`, `game-client-java` và `game-demo-bots`.
- **Đã đạt:** Dựng Spring Boot health shell, Phaser scene placeholder và Maven bootstrap.
- **Bằng chứng:** `mvnw.cmd test` pass cả bốn module với JDK 21; `game-web/npm run build` pass; `/health` trả HTTP 200.
- **Tiếp theo:** Bắt đầu Phase 01, triển khai `MatchState` và luật engine thuần Java.
- **Chưa kiểm chứng:** Chưa có gameplay, WebSocket protocol hoặc Java bot điều khiển thật.

## 2026-11-07 — Chuẩn bị khả năng chuyển đổi kiến trúc

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Giữ server-authoritative làm hướng phát triển chính.
- **Đã đạt:** Bổ sung vào kế hoạch các seam cho `MatchSetup`, `RuleSet`, Game Core deterministic, snapshot serialization, command log và schema version.
- **Bằng chứng:** `../plan.md` v0.4 và `roadmap.md` đã cập nhật; migration document vẫn ghi rõ trạng thái proposal.
- **Tiếp theo:** Khi bắt đầu Phase 01, triển khai setup/config và Game Core thuần trước khi viết handler mạng.
- **Chưa kiểm chứng:** Chưa triển khai client simulation, relay-only server, tick 24 Hz hoặc Java runner trên client.

## 2026-11-07 — Phase 01

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Triển khai state bất biến, setup cấu hình được và toàn bộ luật MOVE, WAIT, ATTACK, GET_STATE của MVP.
- **Đã đạt:** Bổ sung bounds, occupancy, ownership, attack range, HP, kết thúc trận, idempotency, revision và event sequence.
- **Đã đạt:** Bổ sung snapshot JSON, state hash, command log và replay xác định.
- **Bằng chứng:** `mvnw.cmd test` pass toàn bộ Maven reactor; riêng `game-core` có 11 test, 0 failure và 0 error.
- **Tiếp theo:** Bắt đầu Phase 02, bọc Game Core bằng Spring Boot WebSocket và quản lý command tuần tự theo match.
- **Chưa kiểm chứng:** Chưa có WebSocket protocol, token mapping, broadcast state hoặc kiểm thử nhiều client.

## 2026-11-07 — Phase 02

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Triển khai `/game` WebSocket, token/team profile, protocol version, command result và state broadcast tuần tự.
- **Bằng chứng:** 5 test `game-server` pass với WebSocket thật trên cổng ngẫu nhiên.
- **Tiếp theo:** Xây Java client SDK và bot mẫu ở Phase 03.
- **Chưa kiểm chứng:** Chưa có Java SDK hoặc giao diện trực quan.

## 2026-11-07 — Phase 03

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Triển khai Java 21 `GameClient`, timeout, error code, close sạch và ba chương trình demo.
- **Bằng chứng:** 4 integration test `game-client-java` pass; Maven chạy được bot mẫu và kịch bản thắng.
- **Tiếp theo:** Dựng Phaser viewer ở Phase 04.
- **Chưa kiểm chứng:** Chưa có renderer trong trình duyệt.

## 2026-11-07 — Phase 04

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Phaser render map động, unit, HQ, HP, winner, revision, trạng thái kết nối và năm command gần nhất.
- **Bằng chứng:** TypeScript/Vite production build pass; người dùng xác nhận các kịch bản UI, bot BLUE/RED, damage, winner và offline đều thành công.
- **Tiếp theo:** Đóng gói lệnh chạy, regression và hướng dẫn MVP ở Phase 05.
- **Chưa kiểm chứng:** Chưa kiểm thử trình duyệt hoặc thiết bị ngoài máy phát triển local.

## 2026-11-07 — Phase 05

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Bổ sung script chạy server, viewer, demo và toàn bộ test cùng README hướng dẫn dựng lại MVP.
- **Đã đạt:** Bổ sung regression thắng trọn trận qua Java SDK/WebSocket và nâng Vite lên bản 6.4.5 đã vá advisory.
- **Bằng chứng:** Bản source sạch chạy `scripts/test-all.ps1` với 20 test pass, frontend build pass và npm audit 0 vulnerability.
- **Bằng chứng:** `WinningScenario` kết thúc trận với winner `BLUE` ở revision 15; script server và viewer đều khởi động thành công.
- **Tiếp theo:** Bắt đầu D00 để chốt hợp đồng bot và ranh giới tin cậy cho Desktop Code Studio.
- **Chưa kiểm chứng:** Chưa triển khai production deployment, database, nhiều match hoặc load test.

## 2026-11-07 — Desktop Code Studio D00–D05

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Triển khai `BotProgram`/`GameBot`, JavaFX editor, compiler diagnostics, child JVM runner, timeout, Stop và output cap.
- **Đã đạt:** Source mẫu dùng vòng `for` gửi ba MOVE qua SDK và làm server đạt revision 3.
- **Đã đạt:** Đóng gói app-image có runtime, `jdk.compiler` và `java.exe` để chạy bot trong process riêng.
- **Đã đạt:** Bổ sung phím tắt `Ctrl+B`, `Ctrl+R` và `Ctrl+K` cho Compile, Run và Stop.
- **Bằng chứng:** 30 Maven test pass; frontend build pass; npm audit 0 vulnerability; package smoke test pass; app-image mở được trên Windows.
- **Bằng chứng:** Người dùng xác nhận trực tiếp Compile → Run, bot hoàn tất và Phaser cập nhật đúng vị trí cùng revision.
- **Tiếp theo:** Bắt đầu R1 — nhiều đơn vị và spawn.
- **Chưa kiểm chứng:** Chưa thử app-image trên máy Windows thứ hai; chưa có production deployment, database hoặc load test.

## 2026-11-07 — R1 nhiều đơn vị và spawn

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Triển khai SPAWN cạnh HQ, ID toàn trận, tên hiển thị, quota theo đội và chọn unit qua Java SDK.
- **Đã đạt:** Unit bị hạ được loại khỏi danh sách active nhưng ID không được tái sử dụng; Phaser hiển thị đồng thời nhiều unit và command log SPAWN.
- **Bằng chứng:** 35 Maven test pass; frontend production build pass; npm audit 0 vulnerability.
- **Tiếp theo:** Bắt đầu R2 với tài nguyên, khai thác và công trình mà không thay đổi tính xác định của Game Core.
- **Chưa kiểm chứng:** Chưa kiểm thử trực quan R1 trên trình duyệt hoặc app-image đóng gói; chưa có chi phí spawn hay production building.

## 2026-11-07 — R2 tài nguyên và xây dựng

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Bổ sung resource node, stockpile theo team, `HARVEST` có range và `BUILD` cho BARRACKS, TOWER, WALL với chi phí/HP cấu hình được.
- **Đã đạt:** Đồng bộ schema 3 qua Game Core, WebSocket, Java SDK và Phaser; viewer hiển thị resource node, tài nguyên và công trình.
- **Đã đạt:** Bổ sung `ResourceBuilderBot` và cập nhật bot mặc định Desktop Code Studio để khai thác rồi xây barracks.
- **Bằng chứng:** 39 Maven test pass; frontend production build pass; npm audit 0 vulnerability.
- **Tiếp theo:** Bắt đầu R3 với loại quân MELEE, RANGED, SIEGE và mở rộng luật chiến đấu.
- **Chưa kiểm chứng:** Chưa kiểm thử trực quan R2 trên trình duyệt hoặc app-image đóng gói; building chưa có production/defense behavior.

## 2026-11-07 — R3 loại quân và combat profile

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Bổ sung WORKER, MELEE, RANGED và SIEGE; spawn type đi qua schema 4 từ Java SDK/WebSocket tới snapshot.
- **Đã đạt:** RANGED có tầm đánh xa hơn và SIEGE gây damage cao hơn; Phaser hiển thị type cạnh unit.
- **Bằng chứng:** 40 Maven test pass; frontend production build pass; npm audit 0 vulnerability.
- **Tiếp theo:** Hoàn thiện footprint, projectile và fixed simulation tick trong phase chiến đấu tiếp theo.
- **Chưa kiểm chứng:** Chưa kiểm thử trực quan R3 trên trình duyệt hoặc app-image đóng gói.

## 2026-11-08 — R4 hàng đợi và quota

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Thêm rate limit 10 action/giây theo team và queue tuần tự có capacity cấu hình mặc định 32.
- **Ghi chú:** Cơ chế rate limit theo giây của mốc này đã được thay bằng batch tối đa 10 hành động/bot ở P0 ngày 2026-11-14.
- **Đã đạt:** Nhiều client cùng team dùng chung cửa sổ quota; `RATE_LIMITED` và `COMMAND_QUEUE_FULL` không làm tăng revision.
- **Bằng chứng:** 41 Maven test pass; frontend production build pass; npm audit 0 vulnerability.
- **Tiếp theo:** Hoàn thiện footprint, projectile và fixed simulation tick trước khi mở rộng tài khoản/dữ liệu bền vững.
- **Chưa kiểm chứng:** Chưa chạy load test nhiều kết nối hoặc kiểm thử trực quan trên trình duyệt.

## 2026-11-10 — R4.1 footprint, projectile và simulation tick

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** SIEGE dùng footprint 2×2 xuyên suốt spawn, movement, occupancy, attack range, snapshot và Phaser.
- **Đã đạt:** ATTACK tạo projectile; server áp dụng damage và winner theo simulation tick mặc định 250 ms rồi phát state qua WebSocket schema 5.
- **Đã đạt:** Tick nằm ngoài quota team; replay, state hash, Java SDK và `GameBot` đồng bộ được trạng thái sau projectile.
- **Bằng chứng:** 42 Maven test pass; frontend production build pass.
- **Tiếp theo:** Bắt đầu R5 với tài khoản, lobby và dữ liệu bền vững.
- **Chưa kiểm chứng:** Chưa kiểm thử trực quan projectile trên trình duyệt/app-image hoặc load test tick với nhiều kết nối.

## 2026-11-11 — R5 account, lobby và dữ liệu bền vững

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Thêm REST đăng ký/đăng nhập/join team, BCrypt password, session token lưu hash và quyền WebSocket theo lobby membership.
- **Đã đạt:** Nhiều account cùng team điều khiển chung state/quota; demo token cũ vẫn tương thích.
- **Đã đạt:** Flyway V1 và H2 lưu account, session, membership, match snapshot cùng event history; GameSession tiếp tục được từ snapshot đã lưu.
- **Đã đạt:** `start-server.ps1 -Fresh` chạy state seed bằng database in-memory mà không xóa file dữ liệu mặc định.
- **Bằng chứng:** 46 Maven test pass; frontend không đổi so với production build R4.1 đã pass.
- **Tiếp theo:** R6 — quy mô trận đầy đủ.
- **Chưa kiểm chứng:** Chưa kiểm thử thủ công restart bằng file H2, penetration test, backup/restore hoặc tải đồng thời nhiều session.

## 2026-11-11 — R6 quy mô trận đầy đủ

- **Trạng thái:** `VERIFIED`
- **Đã đạt:** Cấu hình 10 đội thuộc 2 phe, tối đa 50 đơn vị mỗi đội và luật thắng theo phe.
- **Đã đạt:** Hai match route độc lập theo `matchId`; snapshot, event history, revision và tick state không trộn giữa match.
- **Đã đạt:** Khóa database theo team giới hạn 5 account; năm WebSocket cùng team điều khiển chung state/quota, account thứ sáu nhận `TEAM_FULL`.
- **Đã đạt:** Reconnect nhận snapshot authoritative mới nhất; endpoint `/api/matches` hiển thị revision và tick latency.
- **Đã đạt:** Demo `WinningScenario` hạ đủ HQ phe đối phương thay vì chỉ hạ một HQ.
- **Bằng chứng:** 49 Maven test pass; targeted R6 test chạy 400 match-tick và kiểm tra profile, faction, account cap, multi-match cùng reconnect.
- **Tiếp theo:** R8 — trải nghiệm và phát hành; R7 chỉ mở khi Desktop Code Studio không đáp ứng.
- **Chưa kiểm chứng:** Chưa benchmark production nhiều máy, kiểm thử UI 10 đội, penetration test, backup/restore hoặc database production.

## 2026-11-11 — R8.1–R8.3 trải nghiệm viewer

- **Trạng thái:** `IMPLEMENTED`, chưa có browser visual sign-off.
- **Đã đạt:** Phòng vào trận tải danh sách từ `/api/matches`, chọn `matchId` và truyền session token đúng vào WebSocket.
- **Đã đạt:** Viewer hiển thị trạng thái kết nối, revision, tick, winner, lực lượng, command gần nhất và banner kết thúc trận.
- **Đã đạt:** Bố cục desktop/mobile được gom về một hệ giao diện, có đường quay lại đổi trận và tự nối lại khi mất kết nối.
- **Đã đạt:** Server bind `0.0.0.0`, web viewer bind `0.0.0.0` và tự suy ra hostname để nhiều máy cùng Wi-Fi truy cập trực tiếp.
- **Bằng chứng:** `game-web/npm run build` pass; copy lint gaming không có finding; gate không có FAIL trong phần đã chạy.
- **Giới hạn:** consistency/layout browser chưa chạy vì Node 20 không đáp ứng yêu cầu Node 22 của bộ kiểm tra; chưa có visual sign-off thủ công.
- **Tiếp theo:** R8.4 lobby tài khoản và trận động; sau đó cân bằng, animation/tutorial/replay.

## 2026-11-11 — R8.4 lobby tài khoản và trận động

- **Trạng thái:** `IMPLEMENTED`.
- **Đã đạt:** Đăng ký/đăng nhập web tối giản, session khôi phục từ local storage và đăng xuất.
- **Đã đạt:** Tạo setup từ JSON map script, sinh mã trận/token đội, phòng chờ theo slot và chủ phòng bắt đầu.
- **Đã đạt:** Tìm trận bằng mã + token, khóa capacity trong transaction và trả lỗi `TEAM_FULL`.
- **Đã đạt:** Match động được đưa vào registry, simulation và WebSocket; trạng thái lobby/setup phục hồi từ H2.
- **Bằng chứng:** `LobbyFlowIntegrationTest` pass và `game-web/npm run build` pass.
- **Giới hạn:** Chưa có danh sách phòng công khai, mời bạn bè, matchmaking tự động hoặc quyền quản trị phòng nâng cao.

## 2026-11-11 — spawn đa loại và tấn công theo hướng

- **Trạng thái:** `VERIFIED`.
- **Đã đạt:** `GameBot` spawn trực tiếp bốn `UnitType`; attack nhận mã quân và hướng, chọn thực thể đầu tiên trên đường bắn trong tầm riêng của loại quân.
- **Đã đạt:** Bot mẫu đọc state và dùng lệnh an toàn, không còn đi/thu hoạch theo tọa độ cố định của map mặc định.
- **Bằng chứng:** `GameEngineAttackTest`, `GameEngineSpawnTest`, replay, coordinator, `GameClientIntegrationTest` và `BotCompilerTest` pass.
