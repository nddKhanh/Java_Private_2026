# Ant Colony Wars — Hướng dẫn sử dụng và template kiểm thử

Tài liệu này là checklist chuẩn để chạy game và ghi nhận kết quả kiểm thử sau mỗi phase.
Đánh dấu `[x]` khi chức năng đã test pass trong môi trường hiện tại.
Đánh dấu `[~]` khi chức năng có một phần nhưng còn giới hạn đã ghi rõ.
Đánh dấu `[ ]` khi chức năng chưa được triển khai hoặc chưa có bằng chứng kiểm thử.

## 1. Trạng thái baseline

- Ngày baseline: 2026-11-08.
- Phase đã ghi nhận: R1, R2, R3 và R4 `VERIFIED`.
- Commit baseline: `2415c44`.
- Server: Spring Boot WebSocket, server-authoritative.
- Client: Java SDK 21, JavaFX Desktop Code Studio và Phaser viewer.
- Protocol hiện tại: schema 4.

## 2. Chuẩn bị môi trường

- [ ] JDK 21 đã cài và có trong `PATH`.
- [ ] Node.js 20 trở lên đã cài và có trong `PATH`.
- [ ] PowerShell 5.1 hoặc PowerShell 7 đang chạy tại thư mục gốc project.
- [ ] Dependency Maven/npm đã tải thành công.

## 3. Khởi động và sử dụng nhanh

Mở ba terminal tại thư mục gốc project.

Terminal 1 — server:

```powershell
.\scripts\start-server.ps1
```

Terminal 2 — Phaser viewer:

```powershell
.\scripts\start-web.ps1
```

Mở `http://127.0.0.1:5173/` và chờ trạng thái `Đang trực tuyến`.

Terminal 3 — chạy bot:

```powershell
.\scripts\run-demo.ps1 BlueBot
```

Server mặc định dùng token `blue-token` cho BLUE và `red-token` cho RED.
Khởi động lại server giữa các kịch bản để trở về state seed ban đầu.

## 4. Checklist chức năng

### Kết nối và quan sát

- [x] Kết nối WebSocket bằng token hợp lệ.
- [x] Từ chối token không hợp lệ.
- [x] Nhận snapshot ban đầu qua `STATE_UPDATED`.
- [x] Đọc state mới qua `GET_STATE`.
- [x] Phaser cập nhật state và revision mà không refresh.
- [x] Hai viewer nhận cùng revision sau một command hợp lệ.

### Điều khiển đơn vị

- [x] `MOVE` theo bốn hướng `UP`, `RIGHT`, `DOWN`, `LEFT`.
- [x] `WAIT` làm tăng revision nhưng không đổi vị trí.
- [x] `ATTACK` unit hoặc headquarters trong tầm.
- [x] Từ chối bounds, occupancy, target không tồn tại, friendly target và out-of-range.
- [x] Unit hết HP bị loại khỏi active snapshot và ID không được tái sử dụng.
- [x] Chọn unit theo ID bằng `selectUnit`.

### Spawn và loại quân

- [x] `SPAWN` unit cạnh headquarters.
- [x] Spawn `WORKER`, `MELEE`, `RANGED` và `SIEGE` qua schema 4.
- [x] Từ chối unit type không hỗ trợ.
- [x] Giới hạn số unit theo team.
- [x] `RANGED` có tầm đánh xa hơn profile cơ bản.
- [x] `SIEGE` gây damage cao hơn profile cơ bản.

### Tài nguyên và xây dựng

- [x] `HARVEST` tại resource node trong range.
- [x] Resource node giảm amount và team tăng resources.
- [x] Từ chối harvest ngoài range hoặc resource đã cạn.
- [x] `BUILD` BARRACKS, TOWER và WALL trên ô kề hợp lệ.
- [x] Trừ đúng cost và từ chối khi thiếu resources.
- [x] Từ chối building trùng ID, ô bị chiếm, ngoài bounds hoặc không kề actor.
- [~] Building được hiển thị trong snapshot và Phaser nhưng chưa có production/defense behavior.

### Quota và an toàn command

- [x] Command được xử lý tuần tự và revision/sequence tăng đúng thứ tự.
- [x] Command ID trùng bị từ chối và không áp dụng hai lần.
- [x] Rate limit mặc định 10 action/giây theo team.
- [x] Nhiều client cùng team dùng chung rate limit.
- [x] Queue capacity mặc định 32 command.
- [x] SDK tự chờ khi nhận `RATE_LIMITED`.

### Desktop Code Studio

- [x] Compile source Java và hiển thị diagnostics.
- [x] Run source trong JVM con.
- [x] Stop JVM con đang chạy.
- [x] Timeout, heap limit và output cap.
- [~] App-image đã có package smoke test trên máy phát triển; chưa xác nhận trên máy Windows thứ hai.

### Chưa có hoặc chưa đủ bằng chứng

- [ ] Database, account, lobby, matchmaking và restart recovery.
- [ ] Nhiều match đồng thời và production deployment.
- [ ] Load test nhiều kết nối hoặc nhiều client cùng team ở quy mô lớn.
- [ ] Footprint 2×2, projectile và fixed simulation tick.
- [ ] Visual QA R2–R4 trên nhiều trình duyệt hoặc thiết bị.
- [ ] Building production, defense hoặc unit production.

## 5. Cách điều khiển hiện tại

### Java SDK

```java
try (GameClient client = GameClient.connect("ws://localhost:8080/game", "blue-token")) {
    client.move(Direction.RIGHT);
    client.waitTurn();
    client.attack("hq-red");
    client.spawn("worker-blue-2", "Builder", UnitType.WORKER, Direction.DOWN);
    client.harvest();
    client.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);
}
```

Các action không chỉ định `unitId` dùng unit active đầu tiên của team.
Dùng overload có `unitId` để điều khiển unit cụ thể.

```java
client.move("worker-blue-2", Direction.RIGHT);
client.harvest("worker-blue-2");
client.build("worker-blue-2", "wall-blue", BuildingType.WALL, Direction.DOWN);
```

`Direction` gồm `UP`, `RIGHT`, `DOWN`, `LEFT`.
`UnitType` gồm `WORKER`, `MELEE`, `RANGED`, `SIEGE`.
`BuildingType` gồm `BARRACKS`, `TOWER`, `WALL`.

### Desktop Code Studio

```powershell
.\scripts\start-desktop.ps1
```

- [ ] Nhập server `ws://localhost:8080/game`.
- [ ] Nhập token `blue-token` hoặc `red-token`.
- [ ] Chọn bot class và source Java.
- [ ] Nhấn `Compile` hoặc `Ctrl+B`.
- [ ] Nhấn `Run` hoặc `Ctrl+R`.
- [ ] Nhấn `Stop` hoặc `Ctrl+K` khi cần dừng JVM con.

### Bot mẫu

```powershell
.\scripts\run-demo.ps1 BlueBot
.\scripts\run-demo.ps1 RedBot
.\scripts\run-demo.ps1 WinningScenario
.\scripts\run-demo.ps1 ReinforcementBot
.\scripts\run-demo.ps1 ResourceBuilderBot
```

## 6. Template test lặp lại

### Thông tin lần test

- Ngày/giờ:
- Người test:
- Commit/branch:
- OS và JDK:
- Browser:
- Profile hoặc token:
- Kết quả tổng thể: `[ ] PASS` `[ ] PASS CÓ GIỚI HẠN` `[ ] FAIL`

### Kịch bản bắt buộc

- [ ] `T01` — Khởi động server và viewer, nhận snapshot revision 0.
- [ ] `T02` — MOVE hợp lệ, viewer nhận vị trí mới và revision tăng 1.
- [ ] `T03` — WAIT, revision tăng và vị trí không đổi.
- [ ] `T04` — ATTACK hợp lệ, HP target giảm đúng profile.
- [ ] `T05` — SPAWN, chọn unit mới và điều khiển bằng ID.
- [ ] `T06` — HARVEST, kiểm tra resource node và team resources.
- [ ] `T07` — BUILD, kiểm tra cost, position, type và building HP.
- [ ] `T08` — Command lỗi, xác nhận state/revision không đổi.
- [ ] `T09` — Hai client cùng team, xác nhận dùng chung quota.
- [ ] `T10` — Chạy `WinningScenario`, xác nhận winner BLUE.
- [ ] `T11` — Chạy `ResourceBuilderBot`, xác nhận harvest rồi build.
- [ ] `T12` — Compile/Run/Stop trong Desktop Code Studio.

### Ghi nhận từng case

| Case | Kết quả | Revision trước/sau | Error nếu có | Evidence |
|---|---|---:|---|---|
| T01 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T02 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T03 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T04 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T05 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T06 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T07 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T08 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T09 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T10 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T11 | `[ ] PASS` `[ ] FAIL` |  |  |  |
| T12 | `[ ] PASS` `[ ] FAIL` |  |  |  |

## 7. Regression command

Chạy bộ kiểm tra tự động trước khi ghi nhận kết quả manual:

```powershell
.\scripts\test-all.ps1
```

Không đánh dấu runtime, visual hoặc load test là pass chỉ dựa trên Maven test.
Mỗi phase mới cần cập nhật checklist, command điều khiển, expected result và số commit baseline.
