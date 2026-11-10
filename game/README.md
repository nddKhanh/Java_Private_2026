# Ant Colony Wars

Ant Colony Wars là game chiến thuật 2D trong đó chương trình Java gửi command, server xử lý luật và Phaser hiển thị state được xác nhận qua WebSocket.

## Trạng thái hiện tại

- MVP Phase 00–05, Desktop D00–D05 và gameplay R1–R4.1 đã được ghi nhận `VERIFIED`.
- Chức năng hiện có gồm MOVE, WAIT, ATTACK, SPAWN, HARVEST, BUILD và GET_STATE.
- Server là nguồn sự thật duy nhất cho vị trí, HP, damage, resource, building, revision và winner.
- R4 thêm rate limit 10 action/giây theo team và queue tuần tự capacity 32 theo profile mặc định.
- R4.1 thêm footprint 2×2 cho SIEGE, projectile và nhịp mô phỏng mặc định 250 ms.

## Kiến trúc

```text
Java bot/GameClient → WebSocket /game → Spring Boot → Game Core → STATE_UPDATED → Phaser
```

Desktop Code Studio chạy source Java local trong JVM con và tái sử dụng Java SDK.

```text
JavaFX editor → compiler → child JVM → GameClient → server → Phaser
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

Mở `http://127.0.0.1:5173/` để xem Phaser viewer.
Token demo mặc định là `blue-token` và `red-token`.

Trong Desktop Code Studio, giữ lớp `MyBot`, nhập máy chủ `ws://localhost:8080/game` và mã truy cập `blue-token`.
Nhấn `Compile` (`Ctrl+B`) rồi `Run` (`Ctrl+R`) để chạy chương trình mẫu; nhấn `Stop` (`Ctrl+K`) để dừng.
Quan sát kết quả trên trang bản đồ.

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

- State chỉ nằm trong RAM và reset khi server restart.
- Chưa có database, account, lobby, matchmaking hoặc production deployment.
- Chưa có load test quy mô lớn hoặc kiểm thử trực quan projectile trên nhiều trình duyệt.
- Building mới có placement/HP/cost; chưa có production hoặc defense behavior.
- Desktop Code Studio chỉ nên chạy source do người dùng tin cậy.
