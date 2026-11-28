# Ant Colony Wars

MVP game chiến thuật 2D trong đó chương trình Java gửi command và Phaser hiển thị state do server xác nhận theo thời gian thực.

**Trạng thái:** Phase 00–05, Desktop D00–D05 và R1–R6 `VERIFIED`; R8.1–R8.4 đã triển khai trong web viewer.

README cấp gốc [`../README.md`](../README.md) là điểm vào chính của dự án.
Khi thay đổi README cấp gốc về cách dùng, chức năng, trạng thái hoặc quy trình, phải cập nhật đồng thời `GUIDE.md`, `README.md` này và `CHANGELOG.md`.
`GUIDE.md` chỉ liệt kê khả năng người chơi có thể cảm nhận và cách tương tác hiện tại, không liệt kê chi tiết triển khai hệ thống.

## Kiến trúc

```text
Java bot → batch → GameClient → WebSocket /game → hàng chờ của từng bot → nhịp mô phỏng → Game Core → STATE_UPDATED → Phaser
```

Server là nguồn sự thật duy nhất cho movement, damage, HP và winner.
Phaser chỉ render snapshot và không tự tính gameplay.
Bot có thể spawn `WORKER`, `MELEE`, `RANGED`, `SIEGE`; lệnh attack nhận mã quân và hướng, rồi server chọn mục tiêu đầu tiên trong tầm trên đường bắn.
HQ chỉ sản xuất WORKER; BARRACKS sản xuất MELEE, RANGED và SIEGE.
Projectile giữ tọa độ đích lúc khai hỏa, đi từng ô ngang hoặc dọc theo đường thẳng, bị công trình chặn và không đuổi theo mục tiêu.
TOWER tự bắn mục tiêu địch gần nhất trong phạm vi 5 ô.
Web viewer có đăng ký/đăng nhập, tạo/tìm trận, phòng chờ theo đội, cùng bảng trạng thái revision, tick, lực lượng, hàng chờ lệnh, nhật ký lệnh và kết quả trận.
Bản đồ dùng model SVG và font Be Vietnam Pro lưu trong `game-web`, hiện tọa độ, thanh máu theo `maxHp` server gửi, đèn trạng thái bot, và phóng to/kéo được.
Khi chạy `start-web.ps1`, viewer mở trên mọi địa chỉ mạng để các máy cùng Wi-Fi truy cập bằng IPv4 của máy chủ.

Desktop Code Studio dùng JavaFX để viết, biên dịch và chạy source Java local trong JVM con, sau đó tái sử dụng `GameClient` để điều khiển server.

```text
JavaFX editor → Java Compiler API → child JVM → GameBot/GameClient → WebSocket → server → Phaser
```

Người học cũng có thể mở `student-bot-template` trong IntelliJ, viết `Main.main(Bot)` rồi chạy `Submit`.
`Submit` đọc pairing key cùng tài khoản từ `.env` và gửi source qua HTTP loopback tới Desktop để tự pair, nhận token điều khiển của tài khoản, compile và chạy.
Game server chỉ nhận batch command, không nhận source hoặc class Java.

Mỗi lần Run gom các hành động thành một batch rồi nối vào cuối hàng chờ của từng bot; server xác nhận ngay khi đã xếp lệnh.
Mỗi unit, HQ và công trình có hàng chờ lệnh riêng trên server; mỗi nhịp 250 ms mỗi bot chạy đúng một lệnh.
Lệnh lỗi khi tới lượt bỏ các lệnh còn lại của đúng bot đó trong đúng lần gửi đó; lệnh của lần gửi khác và của bot khác vẫn chạy.
Lệnh không đọc được bị từ chối ngay khi gửi, cùng các lệnh sau nó của cùng bot.
Lệnh SPAWN hoặc lệnh của quân chưa tồn tại chờ tới khi lệnh xây hoặc lệnh tạo quân trước nó trong cùng lần gửi chạy xong.
Mỗi bot giữ tối đa 40 lệnh chờ và mỗi lần gửi tối đa 256 lệnh, cấu hình trong `game-rules.yml`; lần gửi làm hàng chờ của một bot vượt giới hạn thì phần của bot đó bị từ chối.
Khi trận kết thúc, mọi hàng chờ được xóa.

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

Đăng ký bằng username/password trên web.
Sau khi đăng nhập, chọn **Tạo trận** để nhập JSON map script hoặc **Tìm trận** để nhập mã trận cùng token đội.
Phòng chờ hiển thị mã trận, pairing key, token màu của đội mình, slot tài khoản và nút bắt đầu dành cho chủ phòng.
Token màu chỉ dùng để mời người vào đội, không dùng để điều khiển quân.

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

1. Với phòng tạo từ web, nhập pairing key sáu ký tự đang hiển thị trong phòng chờ cùng username và mật khẩu của tài khoản đã vào đội, rồi nhấn **Pair**.
2. Chờ ứng dụng tự điền Server, Match code và Token điều khiển của tài khoản.
3. Nếu không dùng pairing, vẫn có thể nhập thủ công WebSocket URL, mã phòng và session token của thành viên trận.
4. Giữ Bot class là `Main` và source mẫu mặc định.
5. Nhấn **Compile**; trạng thái phải chuyển thành Compiled.
6. Nhấn **Run**; console báo số lệnh đã xếp vào hàng chờ và các lệnh bị từ chối ngay khi gửi.
7. Quan sát Phaser cập nhật theo từng nhịp; bấm vào bot để xem hàng chờ của nó.

Có thể dùng `Ctrl+B` để Compile, `Ctrl+R` để Run và `Ctrl+K` để Stop.

Để dùng IntelliJ thay cho editor tích hợp:

1. Mở `student-bot-template` bằng IntelliJ với JDK 21.
2. Sao chép `.env.example` thành `.env`, rồi điền `MATCH_KEY`, `USERNAME` và `PASSWORD`.
3. Sửa `src/main/java/Main.java`.
4. Chạy `Submit.main()` trong khi Desktop đang mở và xem toàn bộ kết quả trong console IntelliJ.

API ngắn gồm `select`, `go`, `hit`, `mine`, `build`, `spawn`, `stay` và `state`.
`spawn(id, type, dir)` dùng HQ hoặc BARRACKS đã chọn làm nguồn sản xuất.
API đầy đủ phải truyền `sourceId`; overload cũ không có nguồn sẽ báo lỗi tại client trước khi gửi request.
Desktop chỉ gửi batch sau khi chương trình kết thúc bình thường và trả log biên dịch, Java hoặc kết quả xếp hàng chờ về console IntelliJ.
Lệnh lỗi khi tới lượt chạy hiện trong nhật ký lệnh trên viewer.

Nếu sửa source sau khi compile, ứng dụng bắt buộc compile lại trước khi Run.
Nút Stop kết thúc ngay JVM con đang chạy và bỏ batch chưa gửi.
Lệnh đã vào hàng chờ vẫn chạy sau khi dừng chương trình; dùng nút xóa hàng chờ trên viewer để hủy.

## Account và lobby team

REST API hỗ trợ đăng ký qua `POST /api/accounts`, đăng nhập qua `POST /api/sessions`, xem account qua `GET /api/lobby/me` và join team qua `PUT /api/lobby/membership`.
Web viewer gọi trực tiếp các API này; display name mặc định bằng username nên form đăng ký chỉ cần username/password.
Lobby động dùng `POST /api/lobbies`, `POST /api/lobbies/join`, `GET /api/lobbies/{matchCode}` và `POST /api/lobbies/{matchCode}/start`.
Desktop pairing dùng `GET /api/desktop/pairing/{key}` để tìm server và nhận WebSocket URL, match code cùng trạng thái.
Sau đó Desktop đăng nhập và gọi `POST /api/desktop/pairing/{key}/control` để nhận token điều khiển cho đội của tài khoản; tài khoản chưa vào trận nhận lỗi `NOT_IN_MATCH`.
Token điều khiển chỉ dùng được trong trận đó và được thay mới ở mỗi lần Pair.
Mật khẩu được lưu bằng BCrypt; session token trả về cho client chỉ được lưu dưới dạng SHA-256 trong database.
Sau khi join team, dùng session token ở trường Token của Desktop Code Studio hoặc tham số thứ hai của `GameClient.connect`/`GameBots.connect`.
Tối đa 5 account có thể join cùng team và dùng chung unit cùng state server-authoritative; account thứ 6 nhận lỗi `TEAM_FULL`.
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
Map, match list, team, faction, màu, token demo, unit, HQ, resource node, building, tick interval và session lifetime đều có thể đổi mà không sửa Phaser renderer.
Thông số cân bằng và giới hạn hàng chờ nằm riêng trong `game-server/src/main/resources/game-rules.yml`, mỗi biến có chú thích.
Đổi giá trị thì tăng `version` trong file đó; trận đã lưu theo phiên bản cũ sẽ được tạo lại từ đầu.

Profile mặc định dùng bản đồ 50×50, mỗi đội khởi đầu 100 gỗ và 100 thịt, và nhịp mô phỏng 250 ms (`limits.tick-millis`).
Danh sách `headquarters` để trống thì HQ tự đặt: phe đầu ở mép trái, phe sau ở mép phải, đội đầu tiên của mỗi phe ở hai góc đối nhau và các đội còn lại cách đều dọc mép.
Khối `starter-pack` khai báo quân, mỏ và công trình mà mỗi đội nhận quanh HQ.
Mỏ và công trình đặt theo độ lệch so với HQ, tự đảo chiều ở nửa xa của bản đồ để hướng vào trong.
Quân không khai `dx`/`dy` xuất hiện ở một ô trống ngẫu nhiên sát HQ; cùng một trận luôn cho cùng vị trí.

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
- Gameplay gồm MOVE, WAIT, ATTACK, SPAWN, HARVEST, BUILD và GET_STATE; mỗi bot chạy một lệnh trong hàng chờ của mình mỗi nhịp.
- Hàng chờ và lệnh bị từ chối của một đội chỉ gửi tới thành viên đội đó; mọi thành viên cùng đội được xóa hàng chờ, đồng minh khác đội thì không.
- Hàng chờ chỉ nằm trong bộ nhớ server; khởi động lại server thì các lệnh đang chờ mất.
- SIEGE chiếm footprint 2×2; ATTACK tạo projectile và server cập nhật quỹ đạo, damage cùng winner theo nhịp mô phỏng.
- Spawn tạo unit ở ô kề nguồn sản xuất, trừ thịt theo loại quân và trừ thêm gỗ với SIEGE; HQ chỉ tạo WORKER và BARRACKS chỉ tạo quân chiến đấu.
- Resource node, harvest và building đã có; công trình chặn/nhận damage, bị phá hủy ở HP 0 và mỗi TOWER tự bắn tối đa một phát mỗi giây trong tầm 5.
- Projectile bỏ qua đối tượng cùng phe và viewer dùng model riêng kèm ID thực tế cho mọi unit, HQ và building.
- Unit, HQ và building mang `maxHp` lấy từ `game-rules.yml`; giao thức WebSocket ở schema 9, snapshot lưu theo định dạng cũ bị bỏ khi server khởi động.
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
- `student-bot-template`: project IntelliJ tối thiểu có `.env`, `Main.main(Bot)` và lớp gửi source tới Desktop.
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
