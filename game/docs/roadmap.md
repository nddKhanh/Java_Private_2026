# ANT COLONY WARS — Roadmap điều khiển bằng Java (v0.6)

> **Nguồn yêu cầu:** `../plan.md` v0.8.
> **Mục tiêu:** Hoàn thành nhanh một lát cắt có thể chạy và kiểm thử trực tiếp từ chương trình Java tới Phaser.
> **Nguyên tắc:** Chỉ chuyển phase khi test của phase hiện tại và toàn bộ test cũ đều pass.
> **Kiến trúc chính:** Server-authoritative; tài liệu client simulation trong `docs/ANT_COLONY_WARS_CLIENT_SIMULATION_MIGRATION_PLAN.md` chỉ là phương án dự phòng.
> **Ưu tiên hậu MVP:** Desktop Code Studio để viết, biên dịch và chạy Java local bằng SDK hiện có.

## 1. Phạm vi MVP và invariants

MVP có đúng các thành phần sau:

- Java 21 pure game engine.
- Spring Boot WebSocket server.
- Java 21 client SDK chạy local.
- TypeScript + Phaser viewer.
- Một profile match mặc định 8×8 trong RAM; kích thước và seed đọc từ cấu hình.
- Hai đội, hai token demo, hai HQ và hai WORKER trong profile mặc định.
- MOVE, WAIT, ATTACK, bounds, occupancy, HP và chiến thắng.
- Test tự động và một kịch bản demo từ đầu đến cuối.

Các phase MVP không được tự thêm database, đăng nhập, tài nguyên, xây dựng, spawn, projectile, rate limit, editor web hoặc chạy source Java trên server.
Việc cấu hình hóa profile, snapshot và command/event schema là bắt buộc để giữ đường chuyển đổi mở, nhưng không kích hoạt client simulation.

## 2. Cấu trúc dự án đề xuất

```text
ant-colony-wars/
├── pom.xml
├── game-core/
├── game-server/
├── game-client-java/
├── game-demo-bots/
├── game-web/
├── scripts/
└── README.md
```

`game-core` không phụ thuộc Spring, WebSocket hoặc Phaser.
`game-server` gọi `game-core` và giữ state trong RAM.
`game-client-java` chỉ chứa model giao thức và API điều khiển.
`game-demo-bots` chứa các chương trình Java mẫu.
`game-web` chỉ render state do server gửi.

Cấu trúc dự kiến bổ sung cho track desktop:

```text
ant-colony-wars/
├── game-desktop/   # JavaFX editor, compiler orchestration và process control
└── game-runner/    # Entry point cho JVM con chạy bot đã biên dịch
```

Hai module này mới ở trạng thái kế hoạch và chưa phải artifact đã triển khai.

Các điểm nối phải giữ ổn định từ MVP:

- `MatchSetup`/`RuleSet` tạo state từ profile, không để handler biết đội seed cụ thể.
- `GameCommand` và `ApplyResult` là hợp đồng của Game Core.
- Snapshot phải serialize được; event có `schemaVersion`, `revision` và `sequence`.
- WebSocket là adapter transport; Game Core không biết client đang dùng WebSocket hay relay.
- Renderer không tự quyết định lệnh hợp lệ hoặc kết quả chiến đấu.

## 3. Quy tắc nghiệm thu chung

Mỗi phase phải có:

1. Artifact chạy được.
2. Test tự động cho logic mới.
3. Một kịch bản kiểm thử thủ công ngắn.
4. Không phá test của phase trước.
5. Không đưa tính năng hậu MVP vào code đang nghiệm thu.

Trạng thái phase là `TODO → DOING → VERIFIED` hoặc `BLOCKED`.
Chỉ phase `VERIFIED` mới cho phép bắt đầu phase tiếp theo.

---

# A. TỪ CON SỐ 0 ĐẾN MVP

## Phase 00 — Khung dự án có thể dựng lại

**Thời gian:** 1 ngày công.

**Trạng thái:** `VERIFIED`.

**Mục tiêu:** Có cấu trúc tối thiểu và lệnh build ổn định trước khi viết gameplay.

**Triển khai:**

- Maven multi-module cho `game-core`, `game-server`, `game-client-java` và `game-demo-bots`.
- Dự án TypeScript + Phaser riêng trong `game-web`.
- Ghim phiên bản Java, Spring Boot, Node và Phaser.
- Thêm lệnh test backend và frontend tối thiểu.

**Nghiệm thu:**

- [x] `mvnw.cmd test` chạy thành công từ thư mục gốc.
- [x] Frontend cài dependency và build được bằng lockfile.
- [x] Không cần sửa source code sau khi clone để chạy build.
- [x] README ghi đúng yêu cầu JDK và Node.

**Bằng chứng:** `mvnw.cmd test` pass cả bốn module bằng JDK 21; `game-web/npm run build` pass; Spring Boot `/health` trả HTTP 200.

## Phase 01 — Pure game engine

**Thời gian:** 2–3 ngày công.

**Trạng thái:** `VERIFIED`.

**Mục tiêu:** Hoàn thành toàn bộ luật MVP mà chưa có mạng và giao diện.

**Triển khai:**

- `MatchState`, `MatchSetup`, `RuleSet`, `Team`, `Unit`, `Headquarters`, `Position` và `Direction`.
- Tạo state từ profile mặc định; không hard-code đội, map hoặc seed trong rule handler.
- `MOVE`, `WAIT`, `ATTACK` và `GET_STATE`.
- Bounds, occupancy, ownership, attack range, HP và kết thúc trận.
- `commandId` chống xử lý trùng; `revision` và `sequence` tăng theo action được chấp nhận.
- Snapshot serialize/deserialize được và command log có thể replay.

**Nghiệm thu:**

- [x] MOVE bốn hướng thay đổi đúng một ô.
- [x] MOVE ra ngoài map trả `OUT_OF_BOUNDS` và state không đổi.
- [x] MOVE vào ô bị chiếm trả `CELL_OCCUPIED` và state không đổi.
- [x] WAIT thành công, không đổi vị trí nhưng tăng `revision` để viewer quan sát được action.
- [x] Đội BLUE không điều khiển được đơn vị RED.
- [x] ATTACK ngoài tầm trả `OUT_OF_RANGE`.
- [x] ATTACK mục tiêu địch ở ô kề trừ đúng HP.
- [x] HQ hết HP làm match `FINISHED` và đặt đúng đội thắng.
- [x] Command sau khi match kết thúc trả `MATCH_FINISHED`.
- [x] GET_STATE vẫn đọc được snapshot sau khi match kết thúc.
- [x] Gửi lại cùng `commandId` không thay đổi state lần thứ hai.
- [x] Chạy lại cùng chuỗi command cho kết quả xác định.
- [x] Hai `MatchSetup` khác nhau tạo được match mà không sửa Game Core.
- [x] Serialize/deserialize snapshot không làm mất dữ liệu gameplay.
- [x] Replay cùng command log tạo cùng state hash.

**Bằng chứng:** 12 test hiện tại của `game-core` pass trong toàn bộ Maven reactor bằng JDK 21.

## Phase 02 — Spring Boot WebSocket server

**Thời gian:** 2–3 ngày công.

**Trạng thái:** `VERIFIED`.

**Mục tiêu:** Hai client có thể gửi command và quan sát cùng state qua WebSocket.

**Triển khai:**

- Một endpoint WebSocket `/game` dùng JSON.
- Token và team mapping đọc từ profile/config, với `BLUE` và `RED` là giá trị mặc định.
- Message `COMMAND`, `GET_STATE`, `COMMAND_RESULT` và `STATE_UPDATED`.
- Một hàng đợi xử lý tuần tự cho match.
- State nằm trong RAM và seed lại khi server khởi động.
- Phát toàn bộ snapshot sau mỗi command thành công.
- Envelope có `schemaVersion`, `matchId`, `revision` và `sequence`.

**Nghiệm thu:**

- [x] Token sai không mở được phiên điều khiển.
- [x] Token BLUE chỉ điều khiển được `worker-blue`.
- [x] Command hợp lệ trả đúng `commandId`, `ok=true` và `revision`.
- [x] Command lỗi trả mã lỗi rõ và không phát state giả.
- [x] Hai WebSocket cùng nhận snapshot có cùng `revision`.
- [x] Hai command gần đồng thời được xử lý theo một thứ tự xác định.
- [x] Integration test kết nối, gửi MOVE và nhận `STATE_UPDATED`.
- [x] Đổi profile token/team mà không sửa WebSocket handler.
- [x] Client cũ nhận lỗi schema rõ ràng khi gửi version không hỗ trợ.

**Bằng chứng:** 5 test của `game-server` pass, gồm kết nối WebSocket thật trên cổng ngẫu nhiên, token, ownership, broadcast, ordering và schema version.

## Phase 03 — Java client SDK và bot mẫu

**Thời gian:** 2–3 ngày công.

**Trạng thái:** `VERIFIED`.

**Mục tiêu:** Người học Java điều khiển được bot bằng code Java cơ bản.

**Triển khai:**

- `GameClient`, `Direction`, `GameState` và exception có mã lỗi.
- Dùng WebSocket client chuẩn của Java 21 nếu đáp ứng đủ nhu cầu.
- Mỗi API action tự tạo `commandId` và chờ kết quả tương ứng.
- `BlueBot.java`, `RedBot.java` và `WinningScenario.java`.
- Ví dụ dùng biến, `if`, `for` và phương thức Java.

**Nghiệm thu:**

- [x] `BlueBot.java` kết nối bằng token BLUE.
- [x] `bot.move(Direction.RIGHT)` làm state server đổi đúng một lần.
- [x] Vòng `for` ba lần tạo đúng ba command tuần tự.
- [x] Lỗi server được chuyển thành exception hoặc result dễ hiểu.
- [x] Timeout không làm chương trình chờ vô hạn.
- [x] `close()` đóng WebSocket sạch.
- [x] Chương trình mẫu chạy từ IDE và từ Maven.

**Bằng chứng:** 4 integration test của `game-client-java` pass và script Maven chạy `WinningScenario` thành công.

## Phase 04 — Phaser viewer

**Thời gian:** 2–3 ngày công.

**Trạng thái:** `VERIFIED`.

**Mục tiêu:** Quan sát trực tiếp chương trình Java đang tác động lên trận đấu.

**Triển khai:**

- Scene duy nhất hiển thị lưới theo kích thước snapshot, mặc định 8×8.
- Placeholder render theo danh sách HQ/unit trong snapshot, không khóa số lượng hoặc màu trong scene.
- Panel HP, trạng thái match, winner, revision và kết nối.
- Log năm command gần nhất.
- Kết nối WebSocket chỉ để nhận snapshot và trạng thái kết nối.

**Nghiệm thu:**

- [x] Mở trang thấy đúng state seed ban đầu.
- [x] Chạy `BlueBot.java` làm bot BLUE di chuyển mà không refresh.
- [x] Chạy `RedBot.java` làm bot RED di chuyển độc lập.
- [x] HP cập nhật ngay sau ATTACK.
- [x] Khi HQ bị phá, giao diện hiển thị đúng winner.
- [x] Mất kết nối hiển thị trạng thái offline thay vì giữ trạng thái giả.
- [x] Phaser không tự tính movement, damage hoặc winner.
- [x] Đổi map size hoặc màu team trong snapshot không cần sửa renderer.

**Bằng chứng:** Người dùng xác nhận toàn bộ kịch bản giao diện thành công; TypeScript và Vite 6.4.5 production build pass.

## Phase 05 — Demo trực tiếp và đóng gói MVP

**Thời gian:** 2–4 ngày công.

**Trạng thái:** `VERIFIED`.

**Mục tiêu:** Một người mới có thể dựng, chạy và kiểm chứng MVP bằng README.

**Triển khai:**

- Script hoặc lệnh ngắn để chạy server và frontend.
- Kịch bản Java đưa bot tới HQ địch và phá HQ.
- Regression test cho toàn bộ command và WebSocket flow.
- Regression test cho snapshot serialization và replay command log.
- README mô tả kiến trúc, lệnh chạy, kịch bản demo và giới hạn.
- Ghi rõ state mất khi restart là hành vi chủ ý của MVP.
- Ghi rõ server-authoritative là kiến trúc chính; client simulation chỉ là phương án dự phòng.

**Nghiệm thu cuối:**

- [x] Dựng project từ trạng thái sạch không cần sửa source.
- [x] Mở Phaser thấy một match 8×8 và state seed.
- [x] Java client BLUE và RED chỉ điều khiển được bot của mình.
- [x] Java code có thể dùng `for` để gửi chuỗi movement.
- [x] WebSocket cập nhật cùng một revision cho các viewer.
- [x] Bounds, occupancy, range, damage và ownership đều có test.
- [x] Kịch bản Java phá được HQ và Phaser hiện đúng winner.
- [x] Command sau `FINISHED` không thay đổi state.
- [x] Replay command log tạo cùng state hash với trận mẫu.
- [x] Thay đổi profile map/team/unit seed không yêu cầu sửa rule engine hoặc Phaser renderer.
- [x] Toàn bộ test backend, client và frontend pass.

**Bằng chứng:** `scripts/test-all.ps1` pass từ bản source sạch với 20 test, frontend build và npm audit 0 vulnerability; ba script chạy server, viewer và kịch bản thắng đều đã chạy thành công.

**Định nghĩa hoàn thành:** pipeline **Java program → WebSocket → Spring Boot → game-core → state mới → Phaser** chạy trọn một trận và có thể lặp lại theo README.

---

# B. THỜI GIAN MVP

| Phase | Nội dung | Ngày công |
|---|---|---:|
| 00 | Khung dự án | 1 |
| 01 | Pure game engine và dynamic setup | 2–3 |
| 02 | WebSocket server | 2–3 |
| 03 | Java client SDK | 2–3 |
| 04 | Phaser viewer | 2–3 |
| 05 | Tích hợp, replay và đóng gói | 3–4 |
|  | **Tổng** | **12–18** |

Nên dự phòng thêm 2 ngày cho lỗi tích hợp nếu người phát triển chưa quen WebSocket hoặc Phaser.
Mục tiêu lịch hợp lý là 2–3 tuần toàn thời gian hoặc 4–7 tuần khi làm 15–20 giờ mỗi tuần.

---

# C. DESKTOP CODE STUDIO — ƯU TIÊN TIẾP THEO

Desktop Code Studio chạy source Java thật trên máy người dùng rồi điều khiển server thông qua `game-client-java`.
Không phân tích cú pháp để tự chuyển source thành command và không gửi source lên game server.

## D00 — Hợp đồng bot và ranh giới tin cậy

**Thời gian:** 0,5–1 ngày công.

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- Định nghĩa `BotProgram` và facade `GameBot` tối thiểu trên API hiện có.
- Chốt input do desktop cấp: URL, token, match và unit; credential không nằm trong source mẫu.
- Ghi rõ source chỉ chạy local và phiên bản đầu chỉ hỗ trợ code do người dùng tin cậy.
- Giữ nguyên protocol và quyền xác nhận gameplay của server.

**Nghiệm thu:**

- [x] Một source mẫu triển khai được hợp đồng mà không cần `main` hoặc tự mở WebSocket.
- [x] Hợp đồng không chứa lớp Spring, Phaser hoặc chi tiết transport.
- [x] Tài liệu phân biệt giới hạn process với sandbox cho code không tin cậy.

**Bằng chứng:** `BotProgram`, `GameBot`, `GameBots` và integration test facade gửi ba MOVE qua server.

## D01 — JavaFX desktop shell

**Thời gian:** 1–2 ngày công.

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- Tạo `game-desktop` với editor, cấu hình server/team, console và trạng thái chạy.
- Cung cấp các thao tác Compile, Run và Stop.
- Lưu cấu hình kết nối ngoài source người dùng.

**Nghiệm thu:**

- [x] Ứng dụng khởi động bằng JDK 21 và hiển thị source mẫu.
- [x] UI không bị treo khi tác vụ compile/run chạy nền.
- [x] Token không bị ghi vào file source hoặc log mặc định.

**Bằng chứng:** App-image JavaFX đã mở thành công và accessibility tree xác nhận đủ editor, cấu hình, Compile, Run, Stop cùng console.

## D02 — Biên dịch Java local và diagnostics

**Thời gian:** 1–2 ngày công.

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- Dùng Java Compiler API để compile source vào thư mục tạm.
- Chỉ cung cấp classpath cần thiết cho hợp đồng bot và client SDK.
- Chuẩn hóa lỗi compiler theo message, dòng và cột để UI hiển thị.

**Nghiệm thu:**

- [x] Source hợp lệ tạo class file trong thư mục tạm.
- [x] Source sai cú pháp hiển thị đúng dòng, cột và không chạy class cũ.
- [x] Mỗi lần compile có workspace riêng và được dọn an toàn.

**Bằng chứng:** `BotCompilerTest` kiểm tra source đúng, source lỗi, diagnostics dòng/cột và output tạm.

## D03 — Child JVM runner

**Thời gian:** 2–3 ngày công.

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- Tạo `game-runner` để nạp `BotProgram` và chạy trong JVM tách khỏi JavaFX.
- Giới hạn classpath, working directory, heap, thời gian chạy và lượng stdout/stderr.
- Stop phải kết thúc process con và giải phóng kết nối cùng file tạm.

**Nghiệm thu:**

- [x] Exception hoặc vòng lặp vô hạn của bot không làm sập hoặc treo desktop.
- [x] Timeout và Stop kết thúc được process con.
- [x] Console nhận stdout/stderr có giới hạn và trạng thái kết thúc rõ.

**Bằng chứng:** Integration test xác nhận timeout, Stop, giới hạn process và desktop test JVM tiếp tục hoạt động.

## D04 — Tích hợp GameClient và luồng điều khiển

**Thời gian:** 1–2 ngày công.

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- Runner tạo `GameBot` từ cấu hình desktop và tái sử dụng `game-client-java`.
- Truyền command result, lỗi kết nối và trạng thái chạy về console desktop.
- Giữ Phaser là viewer ngoài trình duyệt trong phiên bản đầu.

**Nghiệm thu:**

- [x] Source mẫu dùng vòng `for` gửi ba MOVE tuần tự tới server.
- [x] Token sai và server offline cho lỗi dễ hiểu mà desktop vẫn hoạt động.
- [x] Server nhận state/revision giống khi chạy demo bot hiện tại.

**Bằng chứng:** Child JVM đưa `worker-blue` tới x=4 ở revision 3; invalid token trả `CONNECTION_FAILED` mà không lộ credential.

## D05 — Đóng gói và nghiệm thu desktop

**Thời gian:** 2–3 ngày công.

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- Đóng gói ứng dụng bằng `jpackage` và bảo đảm runtime có `jdk.compiler`, hoặc yêu cầu rõ JDK 21.
- Bổ sung hướng dẫn cài, cấu hình, compile, run, stop và mở Phaser viewer.
- Chạy regression MVP để xác nhận desktop không làm đổi protocol hoặc Game Core.

**Nghiệm thu:**

- [x] App-image dùng runtime đóng gói có thể mở mà không dùng JavaFX/JDK bên ngoài.
- [x] Compile error, infinite loop, Stop và invalid token đều có hành vi rõ ràng.
- [x] Source và class file không được gửi hoặc lưu trên game server.
- [x] Toàn bộ 30 test Maven, frontend build và npm audit đều pass.
- [x] Người dùng xác nhận thao tác Compile → Run trên giao diện và Phaser cập nhật đúng.

**Bằng chứng:** `scripts/package-desktop.ps1` tạo app-image, chạy smoke test `jdk.compiler`/child JVM và launcher đã mở được trên Windows.
Người dùng xác nhận trực tiếp Compile → Run, console bot hoàn tất và Phaser cập nhật đúng revision/vị trí.

| Track | Nội dung | Ngày công |
|---|---|---:|
| D00 | Hợp đồng và trust boundary | 0,5–1 |
| D01 | JavaFX shell | 1–2 |
| D02 | Compiler và diagnostics | 1–2 |
| D03 | Child JVM runner | 2–3 |
| D04 | GameClient integration | 1–2 |
| D05 | Packaging và acceptance | 2–3 |
|  | **Tổng Desktop Code Studio** | **7,5–13** |

---

# D. CÁC HƯỚNG MỞ RỘNG SAU DESKTOP

## R1 — Nhiều đơn vị và spawn

**Trạng thái:** `VERIFIED`.

**Triển khai:**

- `SPAWN` tạo WORKER ở ô kề HQ theo hướng do bot chọn.
- Mỗi unit có ID toàn trận và tên hiển thị riêng.
- `GameBot.selectUnit` chọn đơn vị cho MOVE, WAIT và ATTACK tiếp theo.
- Rules cấu hình giới hạn unit mỗi đội và HP unit mới.
- Unit hết HP bị loại khỏi danh sách active để giải phóng ô và quota; ID đã dùng không được tái sử dụng trong trận.
- Phaser hiển thị nhiều unit cùng tên và ID riêng.

**Nghiệm thu:**

- [x] Spawn hợp lệ tạo unit mới và tăng revision đúng một lần.
- [x] ID trùng, ô bị chiếm, ô ngoài map và vượt quota bị từ chối mà state không đổi.
- [x] Bot chọn unit mới bằng ID rồi điều khiển độc lập với unit seed.
- [x] Unit bị hạ được xóa khỏi snapshot.
- [x] Snapshot, replay và regression cũ vẫn pass.

**Bằng chứng:** 35 Maven test pass; frontend production build pass; npm audit 0 vulnerability.

## R2 — Tài nguyên và xây dựng

- WORKER khai thác tài nguyên tại resource node trong tầm cho phép.
- BARRACKS, TOWER và WALL được xây trên ô kề hợp lệ với chi phí cấu hình.
- Team stockpile, resource node và building được giữ trong snapshot server-authoritative.

**Nghiệm thu:**

- [x] `HARVEST` giảm lượng resource node và cộng tài nguyên cho đúng team.
- [x] `BUILD` kiểm tra ownership, khoảng cách, bounds, occupancy và đủ tài nguyên trước khi tạo công trình.
- [x] BARRACKS, TOWER và WALL có chi phí cùng HP cấu hình được.
- [x] Snapshot WebSocket và Phaser hiển thị resource node, stockpile và building.
- [x] Java SDK, bot mẫu và replay/regression giữ nguyên tính xác định của core.

**Bằng chứng:** 39 Maven test pass; frontend production build pass; npm audit 0 vulnerability.

## R3 — Loại quân và chiến đấu

- MELEE, RANGED và SIEGE có thể spawn qua Java SDK/WebSocket.
- RANGED được tăng tầm đánh; SIEGE được tăng damage; WORKER giữ profile cơ bản.
- Phaser hiển thị loại quân và schema 4 bảo vệ contract spawn mới.

**Nghiệm thu:**

- [x] Core validate loại quân và từ chối type không hỗ trợ.
- [x] Combat profile của RANGED/SIEGE được kiểm thử bằng unit test.
- [x] Spawn type được truyền xuyên suốt từ client đến snapshot.

**Phạm vi chuyển tiếp:** Footprint 2×2, projectile và fixed simulation tick được hoàn thiện trong R4.1.

## R4 — Hàng đợi và quota

- Giới hạn hành động theo team, dùng chung cho mọi Java client của team.
- Coordinator xử lý command theo queue tuần tự có capacity cấu hình.
- Mã lỗi `RATE_LIMITED` và `COMMAND_QUEUE_FULL` được trả về mà không đổi state.

**Nghiệm thu:**

- [x] Mặc định giới hạn 10 action mỗi giây theo team.
- [x] Queue capacity mặc định là 32 và không làm thay đổi thứ tự revision.
- [x] Rate window dùng clock server và được kiểm thử với clock deterministic.
- [x] Cấu hình nằm trong profile, không đưa quota vào Game Core.

**Bằng chứng:** 41 Maven test pass; frontend production build pass; npm audit 0 vulnerability.

## R4.1 — Footprint, projectile và simulation tick

- SIEGE chiếm footprint 2×2 trong spawn, movement, occupancy, attack range và render.
- ATTACK tạo projectile; damage và winner chỉ được áp dụng khi projectile chạm mục tiêu.
- Server chạy simulation tick mặc định 250 ms ngoài quota hành động của team và phát state mới qua WebSocket schema 5.
- Java SDK và `GameBot` có thể chờ simulation trở về trạng thái không còn projectile.

**Nghiệm thu:**

- [x] Mọi ô trong footprint 2×2 được kiểm tra bounds và occupancy.
- [x] Projectile cùng tick được đưa vào snapshot, replay và state hash xác định.
- [x] Simulation tick không tiêu thụ rate limit của team.
- [x] Phaser hiển thị footprint SIEGE và projectile từ snapshot server-authoritative.
- [x] Kịch bản Java client chờ projectile kết thúc trước khi đọc winner.

**Bằng chứng:** 42 Maven test pass; frontend production build pass.

**Chưa kiểm chứng:** Chưa kiểm thử trực quan chuyển động projectile trên trình duyệt hoặc app-image đóng gói và chưa chạy load test tick với nhiều kết nối.

## R5 — Tài khoản và dữ liệu bền vững

- Đăng nhập, lobby và quan hệ tài khoản–đội.
- Database, restart recovery và lịch sử trận.
- Nhiều người cùng điều khiển một đội.

## R6 — Quy mô trận đầy đủ

- 10 đội chia 2 phe.
- Tối đa 50 đơn vị mỗi đội.
- Load test, quan sát tick latency và xử lý reconnect.

## R7 — Editor Java trên web, chỉ khi desktop không đáp ứng

- Desktop Code Studio là trải nghiệm mặc định để viết và chạy Java local.
- Chỉ bắt đầu editor web khi có yêu cầu sản phẩm mà desktop không đáp ứng.
- Compiler service phải tách khỏi game server.
- Code không tin cậy chạy trong sandbox cô lập với giới hạn CPU, RAM, thời gian, output, filesystem và network.
- Không dùng editor hoặc sandbox làm blocker cho các phase gameplay.

## R8 — Trải nghiệm và phát hành

- Pixel art, animation, tutorial, replay và log chi tiết.
- Cân bằng HP, damage, chi phí, map và thời lượng trận.
- Matchmaking, xếp hạng và vận hành production nếu sản phẩm cần.

## R9 — Phương án client simulation, chỉ khi được phê duyệt

- Không triển khai trong MVP hoặc các phase gameplay chính.
- Chỉ bắt đầu khi Game Core deterministic, snapshot serialization, command log và replay đã được nghiệm thu.
- Tuân theo `docs/ANT_COLONY_WARS_CLIENT_SIMULATION_MIGRATION_PLAN.md` như một proposal riêng.
- Mỗi mốc chuyển đổi phải có state hash đối chiếu với server-authoritative baseline.
- Nếu yêu cầu chống gian lận hoặc tính đúng tuyệt đối chưa giải quyết được, giữ server-authoritative làm production path.

---

# E. TEST XUYÊN SUỐT

| Nhóm | Test cốt lõi | Bắt đầu từ |
|---|---|---|
| Determinism | Cùng state và command cho cùng kết quả | Phase 01 |
| Config setup | Hai profile tạo match không sửa engine | Phase 01 |
| Snapshot | Serialize/deserialize không mất state | Phase 01 |
| Replay | Command log tạo cùng state hash | Phase 01, 05 |
| Validation | Bounds, occupancy, range và match status | Phase 01 |
| Ownership | Token không điều khiển được bot đội khác | Phase 01–02 |
| Idempotency | `commandId` không áp dụng hai lần | Phase 01–02 |
| Ordering | Command của match được xử lý tuần tự | Phase 02 |
| Protocol | JSON hợp lệ/sai và mã lỗi rõ | Phase 02 |
| Schema version | Envelope version không hỗ trợ bị từ chối rõ | Phase 02 |
| Java client | API, timeout, close và chuỗi `for` | Phase 03 |
| Rendering | Phaser phản ánh state server | Phase 04 |
| End-to-end | Java phá HQ và Phaser hiện winner | Phase 05 |
| Compiler diagnostics | Source đúng/sai, dòng/cột và class output riêng | D02 |
| Runner isolation | Exception, timeout, Stop, heap và output limit | D03 |
| Desktop command flow | Source Java → SDK → server → Phaser | D04 |
| Desktop packaging | Runtime sạch có compiler và chạy được bot mẫu | D05 |

Roadmap này giữ MVP nhỏ đã hoàn chỉnh và ưu tiên trải nghiệm lập trình desktop trước khi mở rộng gameplay.
