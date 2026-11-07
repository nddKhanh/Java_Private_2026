# Tiến độ dự án

Chỉ ghi kết quả đã đạt được và bằng chứng kiểm tra được.
Không dùng kế hoạch dự kiến như một kết quả đã hoàn thành.

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
- **Đã đạt:** Nhiều client cùng team dùng chung cửa sổ quota; `RATE_LIMITED` và `COMMAND_QUEUE_FULL` không làm tăng revision.
- **Bằng chứng:** 41 Maven test pass; frontend production build pass; npm audit 0 vulnerability.
- **Tiếp theo:** Hoàn thiện footprint, projectile và fixed simulation tick trước khi mở rộng tài khoản/dữ liệu bền vững.
- **Chưa kiểm chứng:** Chưa chạy load test nhiều kết nối hoặc kiểm thử trực quan trên trình duyệt.
