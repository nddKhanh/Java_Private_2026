# Changelog

Ghi các thay đổi đã hoàn thành theo kết quả cuối cùng.
Mỗi mục chỉ nên có một câu ngắn và bỏ qua nhóm không có thay đổi.

## 2026-11-08 — Tài liệu cấp gốc

### Added

- Thêm README cấp gốc mô tả dự án, cách chạy, bản đồ tài liệu và quy tắc đồng bộ README với GUIDE, docs README và CHANGELOG.

### Changed

- Gộp phần điều khiển, kịch bản mẫu và Desktop Code Studio trong GUIDE ngay sau phần khởi chạy, dùng một chương trình mẫu và bảng hành động ngắn gọn.
- Đưa bước mở Desktop Code Studio vào luồng bắt đầu chơi, kèm hướng dẫn nhập chương trình, biên dịch, chạy và dừng trong GUIDE cùng hai README.
- Viết lại `GUIDE.md` theo khả năng người chơi cảm nhận được, hành động tương tác và kịch bản sử dụng thay vì chi tiết hệ thống.

## Template

```markdown
## YYYY-MM-DD

### Added

- Thêm SPAWN cạnh HQ, quota unit theo đội, HP spawn cấu hình được và error code rõ cho R1.
- Thêm ID toàn trận, tên hiển thị và `GameBot.selectUnit` để điều khiển nhiều đơn vị độc lập.
- Thêm `ReinforcementBot` cùng hiển thị nhiều unit trên Phaser.
- [Kết quả mới đã có].

### Changed

- Nâng protocol schema lên version 2 cho snapshot có tên unit và action SPAWN.
- Unit hết HP được loại khỏi danh sách active để giải phóng ô và quota nhưng ID vẫn được giữ để cấm tái sử dụng.
- Nâng kế hoạch lên v0.7 và roadmap lên v0.5 với R1 đã kiểm chứng.
- [Kết quả đã được điều chỉnh].

### Fixed

- [Vấn đề đã được xử lý].

### Verified

- Xác minh 35 Maven test, frontend production build và npm audit 0 vulnerability cho R1.
- [Bằng chứng hoặc phạm vi đã kiểm tra].
```

## 2026-11-07 — R2

### Added

- Thêm resource node, stockpile theo team và các command `HARVEST`/`BUILD` cho BARRACKS, TOWER, WALL.
- Thêm `ResourceBuilderBot` và API Java SDK tương ứng.

### Changed

- Nâng protocol schema lên version 3 để truyền tài nguyên và công trình trong snapshot.
- Phaser hiển thị resource node, số lượng tài nguyên của team và building trên map.

### Verified

- Xác minh 39 Maven test, frontend production build và npm audit 0 vulnerability cho R2.

## 2026-11-07 — R3

### Added

- Thêm UnitType WORKER, MELEE, RANGED và SIEGE cùng combat profile khác nhau.

### Changed

- Nâng protocol schema lên version 4 cho spawn unit type và hiển thị type trên Phaser.

### Verified

- Xác minh 40 Maven test, frontend production build và npm audit 0 vulnerability cho R3.

## 2026-11-08 — R4

### Added

- Thêm rate limit theo team, queue tuần tự có capacity và các mã lỗi `RATE_LIMITED`/`COMMAND_QUEUE_FULL`.

### Changed

- Bổ sung cấu hình `actions-per-second` và `command-queue-capacity` vào game profile.

### Verified

- Xác minh 41 Maven test, frontend production build và npm audit 0 vulnerability cho R4.

## 2026-11-07 — MVP và R1

### Added

- Thêm JavaFX Desktop Code Studio với editor, compiler diagnostics, Compile, Run, Stop và console.
- Thêm hợp đồng `BotProgram`/`GameBot` và child JVM runner có timeout, heap cùng output cap.
- Thêm app-image `jpackage`, runtime chứa `jdk.compiler` và script chạy/đóng gói desktop.
- Thêm phím tắt `Ctrl+B`, `Ctrl+R` và `Ctrl+K` cho Compile, Run và Stop trong Desktop Code Studio.
- Thêm test compiler, runner, invalid token, timeout, Stop và luồng source Java → server revision 3.
- Thêm Spring Boot WebSocket server với token profile, schema version, ordering và state broadcast.
- Thêm Java client SDK, BlueBot, RedBot và kịch bản thắng trọn trận.
- Thêm Phaser viewer động với HP, winner, revision, connection status và command log.
- Thêm script chạy server, viewer, demo và toàn bộ test từ source sạch.
- Thêm regression Java SDK → WebSocket → Game Core kết thúc trận ở revision 15.
- Triển khai Game Core bất biến với setup động và luật MOVE, WAIT, ATTACK, GET_STATE.
- Thêm kiểm tra bounds, occupancy, ownership, attack range, HP, chiến thắng và idempotency.
- Thêm snapshot JSON, state hash, command log và replay xác định cho Phase 01.
- Thêm 11 test cho luật gameplay, cấu hình, snapshot và replay.
- Dựng Maven multi-module, Maven bootstrap, Spring Boot shell và Phaser shell cho Phase 00.
- Thêm test JUnit tối thiểu cho `game-core`.
- Xác định Java 21 client chạy local là cách điều khiển bot trong MVP.
- Bổ sung giao thức WebSocket tối thiểu giữa Java client, Spring Boot và Phaser.
- Bổ sung tiêu chí nghiệm thu trực tiếp cho một trận hoàn chỉnh.

### Changed

- Nâng kế hoạch lên v0.6 và roadmap lên v0.4 với D00–D05 đã kiểm chứng.
- Ẩn team token khỏi source, command line và thông báo lỗi kết nối.
- Đồng bộ kế hoạch hậu MVP với Desktop Code Studio, Java compiler local, child JVM runner và roadmap D00–D05.
- Hoàn thiện README cho quy trình dựng, chạy, kiểm thử và giới hạn của MVP.
- Nâng Vite từ 6.1.0 lên 6.4.5 để xử lý các advisory của dev server.
- Rút MVP còn một map 8×8, hai đội, hai HQ và hai bot seed sẵn.
- Giới hạn MVP ở MOVE, WAIT, ATTACK, bounds, occupancy, HP và chiến thắng.
- Rút roadmap còn sáu phase với tổng ước lượng 10–16 ngày công.
- Chuyển spawn, tài nguyên, xây dựng, database và editor Java sang sau MVP.
- Giữ server-authoritative làm kiến trúc chính và thêm các điểm nối dynamic cho setup, snapshot, command và event.
- Đưa `MatchSetup`, `RuleSet`, schema version, sequence, replay và serialization vào điều kiện chuẩn bị chuyển đổi.
- Điều chỉnh ước lượng MVP lên 12–18 ngày công để bao gồm các điểm nối dynamic và replay tối thiểu.

### Verified

- Xác minh 30 Maven test, frontend production build, npm audit 0 vulnerability và packaged compiler smoke test.
- Mở thành công app-image Windows với editor, cấu hình, Compile, Run, Stop và console.
- Người dùng xác nhận trực tiếp luồng Compile → Run và Phaser cập nhật đúng vị trí cùng revision.
- Xác minh Phase 02–05, gồm WebSocket, Java SDK, UI do người dùng kiểm thử và kịch bản thắng end-to-end.
- `scripts/test-all.ps1` pass từ source sạch với 20 test, frontend production build và npm audit 0 vulnerability.
- `mvnw.cmd test` pass toàn bộ Maven reactor; `game-core` có 11 test, 0 failure và 0 error.
- `mvnw.cmd test` pass cả bốn module bằng JDK 21.
- `game-web/npm run build` pass với TypeScript, Vite và Phaser.
- Spring Boot shell khởi động và `/health` trả HTTP 200 với phase `00`.

- `plan.md` và `roadmap.md` đã thống nhất phạm vi, trình tự và thời gian MVP.
- Tài liệu không còn phụ thuộc Python, polling hoặc hệ lệnh số cũ.
- Migration plan được giữ ở trạng thái proposal và không thay thế roadmap chính.
