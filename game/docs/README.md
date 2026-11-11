# Ant Colony Wars

MVP game chiến thuật 2D trong đó chương trình Java gửi command và Phaser hiển thị state do server xác nhận theo thời gian thực.

**Trạng thái:** Phase 00–05, Desktop D00–D05 và R1–R6 `VERIFIED`; R8.1–R8.4 đã triển khai trong web viewer.

README cấp gốc [`../README.md`](../README.md) là điểm vào chính của dự án.
Khi thay đổi README cấp gốc về cách dùng, chức năng, trạng thái hoặc quy trình, phải cập nhật đồng thời `GUIDE.md`, `README.md` này và `CHANGELOG.md`.
`GUIDE.md` chỉ liệt kê khả năng người chơi có thể cảm nhận và cách tương tác hiện tại, không liệt kê chi tiết triển khai hệ thống.

## Kiến trúc

```text
Java bot → GameClient → WebSocket /game → Spring Boot → Game Core → STATE_UPDATED → Phaser
```

Server là nguồn sự thật duy nhất cho movement, damage, HP và winner.
Phaser chỉ render snapshot và không tự tính gameplay.
Bot có thể spawn `WORKER`, `MELEE`, `RANGED`, `SIEGE`; lệnh attack nhận mã quân và hướng, rồi server chọn mục tiêu đầu tiên trong tầm trên đường bắn.
Web viewer có đăng ký/đăng nhập, tạo/tìm trận, phòng chờ theo đội, cùng bảng trạng thái revision, tick, lực lượng và kết quả trận.
Khi chạy `start-web.ps1`, viewer mở trên mọi địa chỉ mạng để các máy cùng Wi-Fi truy cập bằng IPv4 của máy chủ.

Desktop Code Studio dùng JavaFX để viết, biên dịch và chạy source Java local trong JVM con, sau đó tái sử dụng `GameClient` để điều khiển server.

```text
JavaFX editor → Java Compiler API → child JVM → GameBot/GameClient → WebSocket → server → Phaser
```

## Yêu cầu

- JDK 21.
- Node.js 20 hoặc mới hơn.
- PowerShell 5.1 hoặc PowerShell 7.
- Internet ở lần tải Maven và npm dependency đầu tiên.

## Chạy MVP

Mở PowerShell tại thư mục gốc của project.

Terminal thứ nhất chạy server:

```powershell
.\scripts\start-server.ps1
```

Server mặc định lưu account, lobby, snapshot và event history vào `game-server/data/ant-colony-wars.mv.db`.
Muốn chạy một trận tạm thời từ state seed mà không đọc hoặc sửa database đã lưu, dùng `.\scripts\start-server.ps1 -Fresh`.

Terminal thứ hai chạy Phaser viewer:

```powershell
.\scripts\start-web.ps1
```

Máy chủ mở `http://127.0.0.1:5173/`; các máy cùng Wi-Fi mở `http://IP_MAY_CHU:5173/` và chờ trạng thái `Đang trực tuyến`.
Ví dụ: `http://192.168.1.23:5173/`.

Đăng ký bằng username/password trên web. Sau khi đăng nhập, chọn **Tạo trận** để nhập JSON map script hoặc **Tìm trận** để nhập mã trận cùng token đội. Phòng chờ hiển thị mã trận, token từng đội, slot tài khoản và nút bắt đầu dành cho chủ phòng.

Terminal thứ ba chạy một bot mẫu:

```powershell
.\scripts\run-demo.ps1 BlueBot
.\scripts\run-demo.ps1 RedBot
```

Viewer phải cập nhật vị trí, revision và command log mà không cần refresh.

## Chạy Desktop Code Studio

Desktop Code Studio là cửa sổ để nhập, biên dịch và chạy chương trình Java điều khiển quân.
Trang bản đồ chỉ hiển thị trận đấu, không có ô nhập chương trình Java.
Khởi động server và Phaser viewer như phần trên.
Sau đó mở terminal thứ ba:

```powershell
.\scripts\start-desktop.ps1
```

Trong ứng dụng:

1. Trên cùng máy chạy server, giữ Server là `ws://localhost:8080/game`; nếu Desktop Code Studio chạy trên máy khác, dùng `ws://IP_MAY_CHU:8080/game`. Với phòng tạo từ web, điền mã phòng vào `Match code` và token đội vào `Team token`.
2. Nhập Token là `blue-token`.
3. Giữ Bot class là `MyBot` và source mẫu mặc định.
4. Nhấn **Compile**; trạng thái phải chuyển thành `Compiled`.
5. Nhấn **Run**; console phải hiện thông báo spawn, harvest và build thành công.
6. Quan sát Phaser: `worker-blue-2` xuất hiện, đi tới resource node, stockpile đổi và barracks mới được hiển thị.

Có thể dùng `Ctrl+B` để Compile, `Ctrl+R` để Run và `Ctrl+K` để Stop.

Nếu sửa source sau khi compile, ứng dụng bắt buộc compile lại trước khi Run.
Nút Stop kết thúc ngay JVM con đang chạy.
Các hành động đã thực hiện trong trận vẫn được giữ sau khi dừng chương trình.

## Account và lobby team

REST API hỗ trợ đăng ký qua `POST /api/accounts`, đăng nhập qua `POST /api/sessions`, xem account qua `GET /api/lobby/me` và join team qua `PUT /api/lobby/membership`.
Web viewer gọi trực tiếp các API này; display name mặc định bằng username nên form đăng ký chỉ cần username/password.
Lobby động dùng `POST /api/lobbies`, `POST /api/lobbies/join`, `GET /api/lobbies/{matchCode}` và `POST /api/lobbies/{matchCode}/start`.
Mật khẩu được lưu bằng BCrypt; session token trả về cho client chỉ được lưu dưới dạng SHA-256 trong database.
Sau khi join team, dùng session token ở trường Token của Desktop Code Studio hoặc tham số thứ hai của `GameClient.connect`/`GameBots.connect`.
Tối đa 5 account có thể join cùng team và dùng chung unit, quota cùng state server-authoritative; account thứ 6 nhận lỗi `TEAM_FULL`.
Các demo token `blue-token` và `red-token` vẫn hoạt động cho kịch bản local cũ.

Lịch sử event của trận mặc định đọc tại `GET /api/matches/match-default/history`.
Danh sách match và tick latency đọc tại `GET /api/matches`; match training dùng WebSocket `ws://localhost:8080/game?matchId=match-training` hoặc thay `localhost` bằng IPv4 máy chủ khi chạy qua LAN.

## Đóng gói desktop

```powershell
.\scripts\package-desktop.ps1
```

App-image được tạo tại `game-desktop/target/package/AntColonyCodeStudio/`.
Gói này chứa Java runtime, `jdk.compiler` và `java.exe` riêng cho child JVM.

## Kịch bản chiến thắng

Khởi động lại server để trận trở về state seed rồi chạy:

```powershell
.\scripts\run-demo.ps1 WinningScenario
```

Bot BLUE đi qua bản đồ, hạ đủ năm HQ của phe `SWARM` và kết thúc trận với winner `BLUE`.

Khởi động lại server rồi chạy demo spawn và chọn đơn vị:

```powershell
.\scripts\run-demo.ps1 ReinforcementBot
```

Bot tạo `worker-blue-2`, chọn unit này và di chuyển ba ô mà không đổi vị trí `worker-blue`.

Chạy bot khai thác và xây dựng:

```powershell
.\scripts\run-demo.ps1 ResourceBuilderBot
```

## Chạy toàn bộ kiểm tra

```powershell
.\scripts\test-all.ps1
```

Script chạy Maven reactor tests, cài frontend theo lockfile và build TypeScript/Vite.

Hướng dẫn tại [`GUIDE.md`](GUIDE.md) đi tuần tự từ khởi chạy các cửa sổ PowerShell đến viết và chạy Java, thử hành động, đối chiếu chức năng và ghi kết quả kiểm thử.

## Cấu hình trận

Profile mặc định nằm tại `game-server/src/main/resources/application.yml`.
Map, rules, match list, team, faction, màu, token demo, unit, HQ, resource node, building, quota action, tick interval, session lifetime và HP spawn đều có thể đổi mà không sửa handler WebSocket hoặc Phaser renderer.

Profile mặc định giới hạn 10 action mỗi giây cho mỗi team và queue tuần tự tối đa 32 command.

Token mặc định chỉ dùng cho demo local:

- BLUE: `blue-token`.
- CYAN: `cyan-token`.
- GREEN: `green-token`.
- TEAL: `teal-token`.
- NAVY: `navy-token`.
- RED: `red-token`.
- ORANGE: `orange-token`.
- YELLOW: `yellow-token`.
- MAGENTA: `magenta-token`.
- MAROON: `maroon-token`.

Không dùng các token này cho môi trường public hoặc production.

## Hành vi và giới hạn hiện tại

- Server mặc định phục hồi state từ H2 sau khi restart; chế độ `-Fresh` mới chủ ý giữ state trong RAM.
- Có 10 team trong 2 faction; mỗi team có một unit seed, một HQ và có thể spawn tối đa 50 unit theo profile mặc định.
- Gameplay gồm MOVE, WAIT, ATTACK, SPAWN, HARVEST, BUILD và GET_STATE; server có rate limit theo team và queue tuần tự.
- SIEGE chiếm footprint 2×2; ATTACK tạo projectile và server cập nhật quỹ đạo, damage cùng winner theo nhịp mô phỏng.
- Spawn tạo unit ở ô kề HQ; chi phí spawn chưa được áp dụng.
- Resource node, harvest và building đã có; building production/defense chưa có.
- Account, lobby membership, session, snapshot và event history được lưu bằng H2/Flyway; file database mặc định nằm trong `game-server/data/`.
- Hai match được route độc lập theo `matchId`; reconnect nhận snapshot authoritative mới nhất.
- Chưa có danh sách phòng công khai, matchmaking tự động, email verification, quên mật khẩu, backup hoặc database production.
- Desktop Code Studio chỉ biên dịch và chạy source trên máy local; source/class không được gửi lên server.
- JVM con có heap 64 MB, timeout cấu hình được và output giới hạn 64 KiB mỗi stream.
- Các giới hạn process không phải sandbox an toàn cho code không tin cậy.
- Chỉ chạy source do chính người dùng tin cậy; code từ bên ngoài cần cô lập bằng hệ điều hành hoặc container.
- Server-authoritative là kiến trúc chính.
- Client simulation trong `ANT_COLONY_WARS_CLIENT_SIMULATION_MIGRATION_PLAN.md` chỉ là phương án dự phòng sau MVP.

## Cấu trúc

- `game-core`: state, rules, snapshot và replay thuần Java.
- `game-server`: Spring Boot WebSocket, profile và xử lý command tuần tự.
- `game-client-java`: SDK Java 21 có timeout và error code.
- `game-demo-bots`: BlueBot, RedBot và WinningScenario.
- `game-runner`: entry point tối thiểu chạy `BotProgram` trong JVM con.
- `game-desktop`: JavaFX editor, compiler diagnostics và process control.
- `game-web`: Phaser viewer đọc snapshot WebSocket.
- `scripts`: lệnh chạy server, viewer, desktop, demo, đóng gói và toàn bộ test.

## Tài liệu

- `../plan.md`: phạm vi và thiết kế sản phẩm.
- `roadmap.md`: phase và điều kiện nghiệm thu.
- `CHANGELOG.md`: thay đổi đã hoàn thành.
- `PROGRESS.md`: tiến độ và bằng chứng.
