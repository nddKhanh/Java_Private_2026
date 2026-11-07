# Ant Colony Wars

MVP game chiến thuật 2D trong đó chương trình Java gửi command và Phaser hiển thị state do server xác nhận theo thời gian thực.

**Trạng thái:** Phase 00–05, Desktop D00–D05 và R1–R4 `VERIFIED`.

README cấp gốc [`../README.md`](../README.md) là điểm vào chính của dự án.
Khi thay đổi README cấp gốc về cách dùng, chức năng, trạng thái hoặc quy trình, phải cập nhật đồng thời `GUIDE.md`, `README.md` này và `CHANGELOG.md`.

## Kiến trúc

```text
Java bot → GameClient → WebSocket /game → Spring Boot → Game Core → STATE_UPDATED → Phaser
```

Server là nguồn sự thật duy nhất cho movement, damage, HP và winner.
Phaser chỉ render snapshot và không tự tính gameplay.

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

Terminal thứ hai chạy Phaser viewer:

```powershell
.\scripts\start-web.ps1
```

Mở `http://127.0.0.1:5173/` và chờ trạng thái `Đang trực tuyến`.

Terminal thứ ba chạy một bot mẫu:

```powershell
.\scripts\run-demo.ps1 BlueBot
.\scripts\run-demo.ps1 RedBot
```

Viewer phải cập nhật vị trí, revision và command log mà không cần refresh.

## Chạy Desktop Code Studio

Khởi động server và Phaser viewer như phần trên.
Sau đó mở terminal thứ ba:

```powershell
.\scripts\start-desktop.ps1
```

Trong ứng dụng:

1. Giữ Server là `ws://localhost:8080/game`.
2. Nhập Token là `blue-token`.
3. Giữ Bot class là `MyBot` và source mẫu mặc định.
4. Nhấn **Compile**; trạng thái phải chuyển thành `Compiled`.
5. Nhấn **Run**; console phải hiện thông báo spawn, harvest và build thành công.
6. Quan sát Phaser: `worker-blue-2` xuất hiện, đi tới resource node, stockpile đổi và barracks mới được hiển thị.

Có thể dùng `Ctrl+B` để Compile, `Ctrl+R` để Run và `Ctrl+K` để Stop.

Nếu sửa source sau khi compile, ứng dụng bắt buộc compile lại trước khi Run.
Nút Stop kết thúc ngay JVM con đang chạy.

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

Bot BLUE đi tới cạnh HQ RED, tấn công ba lần và kết thúc trận với winner `BLUE` ở revision `15`.

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

Script chạy 41 Maven test, cài frontend theo lockfile và build TypeScript/Vite.

Template hướng dẫn sử dụng và kiểm thử chuẩn nằm tại [`GUIDE.md`](GUIDE.md).

## Cấu hình trận

Profile mặc định nằm tại `game-server/src/main/resources/application.yml`.
Map, rules, team, màu, token demo, unit, HQ, resource node, building, quota action và HP spawn đều có thể đổi mà không sửa handler WebSocket hoặc Phaser renderer.

Profile mặc định giới hạn 10 action mỗi giây cho mỗi team và queue tuần tự tối đa 32 command.

Token mặc định chỉ dùng cho demo local:

- BLUE: `blue-token`.
- RED: `red-token`.

Không dùng các token này cho môi trường public hoặc production.

## Hành vi và giới hạn MVP

- State chỉ nằm trong RAM và chủ ý được tạo lại khi server restart.
- Mỗi team có một unit seed sẵn, một HQ và có thể spawn tối đa năm unit theo profile mặc định.
- Gameplay gồm MOVE, WAIT, ATTACK, SPAWN, HARVEST, BUILD và GET_STATE; server có rate limit theo team và queue tuần tự.
- Spawn tạo unit ở ô kề HQ; chi phí spawn chưa được áp dụng.
- Resource node, harvest và building đã có; building production/defense chưa có.
- Không có database, lobby, đăng nhập hoặc matchmaking.
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
