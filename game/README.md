# Ant Colony Wars

Ant Colony Wars là game chiến thuật 2D trong đó chương trình Java gửi command, server xử lý luật và Phaser hiển thị state được xác nhận qua WebSocket.

## Trạng thái hiện tại

- MVP Phase 00–05, Desktop D00–D05 và gameplay R1–R6 đã được ghi nhận `VERIFIED`.
- Chức năng hiện có gồm MOVE, WAIT, ATTACK, SPAWN, HARVEST, BUILD và GET_STATE.
- SPAWN hỗ trợ WORKER, MELEE, RANGED và SIEGE; ATTACK nhận mã quân cùng hướng và quét mục tiêu đầu tiên đến hết tầm đánh.
- Server là nguồn sự thật duy nhất cho vị trí, HP, damage, resource, building, revision và winner.
- P0 batch giới hạn 10 hành động cho mỗi bot/lần Run, hỗ trợ Atomic hoặc Partial và không giới hạn theo giây.
- R4.1 thêm footprint 2×2 cho SIEGE, projectile và nhịp mô phỏng mặc định 250 ms.
- R5 thêm account, đăng nhập, lobby team, session token và H2/Flyway để phục hồi state cùng lịch sử trận sau khi khởi động lại.
- R6 hoàn thiện 10 đội/2 phe, hai match độc lập, tối đa 5 account cùng đội và reconnect theo snapshot mới nhất.
- R8.1–R8.3 thêm màn hình chọn trận, mã truy cập, bảng trạng thái trận và phản hồi thắng/thua cho Phaser viewer.
- R8.4 thêm đăng ký/đăng nhập trên web, tạo trận từ JSON map script, tìm trận bằng mã + token đội và phòng chờ theo slot.
- R8 LAN cho phép một máy phát server/viewer để nhiều máy cùng Wi-Fi truy cập trực tiếp, không qua dịch vụ trung gian.
- Desktop pairing cho phép nhập một key sáu ký tự để tự tìm server trong LAN, điền match code và chọn token đội theo màu.
- P1 cho phép viết `Main.main(Bot)` trong project IntelliJ mẫu rồi chạy `Submit` để Desktop tự pair, compile và chạy source local.

## Kiến trúc

```text
Java bot/GameClient → WebSocket /game → Spring Boot → Game Core → STATE_UPDATED → Phaser
```

Desktop Code Studio chạy source Java local trong JVM con và tái sử dụng Java SDK.

```text
JavaFX editor → compiler → child JVM → GameClient → server → Phaser
```

```text
IntelliJ template → HTTP loopback → Desktop compiler → child JVM → GameClient → server
```

## Yêu cầu

- JDK 21.
- Node.js 20 trở lên.
- PowerShell 5.1 hoặc PowerShell 7.

## Chạy nhanh

Mở ba cửa sổ PowerShell tại thư mục gốc, chạy mỗi lệnh dưới đây trong một cửa sổ riêng:

```powershell
.\scripts\start-server.ps1
.\scripts\start-web.ps1
.\scripts\start-desktop.ps1
```

Máy chủ mở viewer tại `http://127.0.0.1:5173/`; các máy cùng Wi-Fi mở `http://IP_MAY_CHU:5173/`.
Ví dụ: `http://192.168.1.23:5173/`.
Token demo mặc định là `blue-token` và `red-token`.
Profile mặc định có 10 đội thuộc 2 phe, tối đa 50 đơn vị mỗi đội và tối đa 5 account cùng điều khiển một đội.
Hai match `match-default` và `match-training` được tạo từ cùng profile; Java client chọn match bằng overload `GameClient.connect(url, token, matchId)`.

Trên web, đăng ký bằng username/password hoặc đăng nhập tài khoản đã có.
Sau đó chọn **Tạo trận** để dùng map script JSON, hoặc **Tìm trận** để nhập mã trận và token đội.
Phòng chờ hiển thị pairing key riêng của trận cùng token màu của từng đội.

Trong Desktop Code Studio, nhập pairing key từ phòng chờ rồi nhấn **Pair**.
Ứng dụng tự tìm server cổng `8080` trong LAN, điền Server và Match code, sau đó cho chọn Team token từ danh sách màu.
Các trường Server, Match code và Team token vẫn cho phép nhập thủ công khi cần.
Nhấn `Compile` (`Ctrl+B`) rồi `Run` (`Ctrl+R`) để chạy chương trình mẫu; nhấn `Stop` (`Ctrl+K`) để dừng.
Quan sát kết quả trên trang bản đồ.

Để viết trong IntelliJ, mở `student-bot-template`, điền `MATCH_KEY` trong `.env`, sửa `Main.java` rồi chạy `Submit.main()` khi Desktop đang mở.
API ngắn dùng `select`, `go`, `hit`, `mine`, `build`, `spawn`, `idle` và `state`; `idle` thay cho `wait` vì `Object.wait()` là `final` trong Java.

Nếu chỉ muốn chạy kịch bản có sẵn, dùng lệnh sau thay cho Desktop Code Studio:

```powershell
.\scripts\run-demo.ps1 BlueBot
```

Chạy toàn bộ regression, frontend build và audit:

```powershell
.\scripts\test-all.ps1
```

## Tài liệu dự án

| Tài liệu | Mục đích |
|---|---|
| [`docs/README.md`](docs/README.md) | Hướng dẫn chạy các thành phần và giới hạn MVP. |
| [`docs/GUIDE.md`](docs/GUIDE.md) | Checklist chức năng, cách điều khiển và template test lặp lại. |
| [`docs/roadmap.md`](docs/roadmap.md) | Roadmap phase và tiêu chí nghiệm thu. |
| [`docs/PROGRESS.md`](docs/PROGRESS.md) | Tiến độ, bằng chứng và giới hạn kiểm chứng. |
| [`docs/CHANGELOG.md`](docs/CHANGELOG.md) | Lịch sử thay đổi theo phase. |
| [`plan.md`](plan.md) | Kế hoạch sản phẩm, quyết định kiến trúc và hướng phát triển. |

`docs/GUIDE.md` hướng dẫn tuần tự từ khởi chạy, viết và chạy Java, thử từng hành động đến đối chiếu chức năng và ghi kết quả kiểm thử.

## Quy tắc đồng bộ tài liệu

README cấp gốc là điểm vào chính của dự án.
Khi cập nhật README cấp gốc về cách dùng, chức năng, trạng thái hoặc quy trình, bắt buộc cập nhật đồng thời [`docs/GUIDE.md`](docs/GUIDE.md), [`docs/README.md`](docs/README.md) và [`docs/CHANGELOG.md`](docs/CHANGELOG.md).
Nếu thay đổi làm lệch phase, tiêu chí nghiệm thu hoặc giới hạn kiểm chứng, cập nhật thêm [`docs/roadmap.md`](docs/roadmap.md), [`docs/PROGRESS.md`](docs/PROGRESS.md) và [`plan.md`](plan.md).
Mỗi thay đổi tài liệu phải giữ link, command, số test và trạng thái chức năng nhất quán giữa các file liên quan.
Sau khi cập nhật, chạy `git diff --check` và ghi commit bằng tiếng Anh có trailer `Co-Authored-By`.

## Giới hạn hiện tại

- H2 dạng file chỉ phù hợp demo local; chưa có backup, HA hoặc database production.
- Có thể tạo nhiều trận riêng bằng mã; chưa có danh sách phòng công khai, matchmaking tự động hoặc production deployment.
- Chưa có xác minh email, quên mật khẩu, thu hồi mọi session hoặc giới hạn số account mỗi team.
- Chưa có load test quy mô lớn hoặc kiểm thử trực quan projectile trên nhiều trình duyệt.
- Building mới có placement/HP/cost; chưa có production hoặc defense behavior.
- Desktop Code Studio chỉ nên chạy source do người dùng tin cậy.
