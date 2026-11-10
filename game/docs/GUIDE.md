# Ant Colony Wars — Hướng dẫn chơi và mẫu kiểm thử

Tài liệu này mô tả những gì người chơi có thể nhìn thấy và thực hiện trong các kịch bản hiện tại.
Đây cũng là mẫu checklist dùng lại sau mỗi lần cập nhật game.

Đánh dấu `[x]` khi đã kiểm tra thành công.
Đánh dấu `[~]` khi dùng được nhưng còn giới hạn đã ghi rõ.
Đánh dấu `[ ]` khi chưa dùng được hoặc chưa có bằng chứng kiểm tra.

## 1. Trạng thái hiện tại

- Ngày cập nhật: 2026-11-08.
- Kịch bản đã hoàn thành: R1, R2, R3 và R4.
- Người chơi điều khiển quân của mình bằng chương trình Java hoặc Desktop Code Studio.
- Phaser là màn hình quan sát bản đồ và trạng thái trận đấu.
- Máy chủ giữ kết quả chính thức của mọi hành động.

Khi README cấp gốc thay đổi về cách dùng, chức năng, trạng thái hoặc quy trình, phải cập nhật lại tài liệu này cùng `docs/README.md` và `docs/CHANGELOG.md`.

## 2. Chuẩn bị

- [ ] Cài JDK 21.
- [ ] Cài Node.js 20 trở lên.
- [ ] Mở PowerShell tại thư mục gốc của dự án.
- [ ] Đã tải xong các thư viện Maven và npm.

## 3. Bắt đầu một trận

Mở ba cửa sổ PowerShell tại thư mục gốc.

### Cửa sổ 1 — bật máy chủ

```powershell
.\scripts\start-server.ps1
```

### Cửa sổ 2 — bật màn hình bản đồ

```powershell
.\scripts\start-web.ps1
```

Mở `http://127.0.0.1:5173/` và chờ dòng trạng thái trực tuyến.

### Cửa sổ 3 — mở nơi viết, biên dịch và chạy Java

```powershell
.\scripts\start-desktop.ps1
```

Chờ cửa sổ Desktop Code Studio xuất hiện.
Giữ cả ba cửa sổ PowerShell hoạt động trong khi chơi; mỗi cửa sổ chạy một lệnh ở trên.

## 4. Viết và chạy chương trình điều khiển

### Bước 1 — chọn đội và nhập chương trình

Trong Desktop Code Studio, nhập `Server` là `ws://localhost:8080/game`, `Token` là `blue-token` và tên lớp là `MyBot`.
Thay nội dung ô soạn thảo bằng chương trình sau để thử trên trận mới:

```java
import com.antcolonywars.bot.BotProgram;
import com.antcolonywars.bot.GameBot;
import com.antcolonywars.client.Direction;
import com.antcolonywars.client.BuildingType;

public final class MyBot implements BotProgram {
    @Override
    public void run(GameBot bot) {
        bot.selectUnit("worker-blue");
        bot.move(Direction.RIGHT);
        bot.waitTurn();
    }
}
```

### Bước 2 — biên dịch, chạy và quan sát

1. Nhấn `Compile` (`Ctrl+B`); nếu có lỗi, sửa theo dòng và cột được thông báo.
2. Nhấn `Run` (`Ctrl+R`) sau khi biên dịch thành công.
3. Mở trang bản đồ: quân xanh đi từ `(1, 0)` sang `(2, 0)` rồi đứng chờ.
4. Nhấn `Stop` (`Ctrl+K`) khi cần dừng chương trình đang chạy.

Sau mỗi lần sửa, biên dịch lại trước khi chạy.
Dừng chương trình không hoàn tác hành động đã thực hiện.
Để thử một kịch bản từ đầu, dừng máy chủ bằng `Ctrl+C` ở cửa sổ 1 rồi chạy lại `.\scripts\start-server.ps1` và chờ bản đồ kết nối lại.

### Bước 3 — thay hành động muốn thử

Thay các dòng bên trong hàm `run` bằng hành động dưới đây.
Mỗi dòng là một ví dụ riêng; cần đưa quân tới vị trí phù hợp trước khi khai thác, xây dựng hoặc tấn công.

| Muốn thực hiện | Lệnh trong hàm `run` | Điều kiện hoặc kết quả |
|---|---|---|
| Chọn quân | `bot.selectUnit("worker-blue");` | Quân thuộc đội đang điều khiển. |
| Đi một ô | `bot.move(Direction.RIGHT);` | Ô đích trống và nằm trong bản đồ. |
| Đứng chờ | `bot.waitTurn();` | Giữ nguyên vị trí. |
| Tấn công | `bot.attack("hq-red");` | Nhà chính đối phương phải trong tầm; lệnh tạo projectile. |
| Chờ đạn chạm mục tiêu | `bot.awaitSimulationIdle();` | Chờ toàn bộ projectile hiện tại kết thúc trước khi đọc HP hoặc winner. |
| Tạo quân thợ | `bot.spawn("worker-blue-2", "Tho 2", Direction.DOWN);` | Ô dưới nhà chính trống, mã quân chưa dùng và đội còn chỗ. |
| Khai thác | `bot.harvest();` | Mặc định đứng tại điểm tài nguyên còn trữ lượng. |
| Xây doanh trại | `bot.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);` | Ô bên phải trống; mặc định tốn 3 tài nguyên. |
| Xây tháp | `bot.build("tower-blue", BuildingType.TOWER, Direction.DOWN);` | Ô bên dưới trống; mặc định tốn 2 tài nguyên. |
| Xây tường | `bot.build("wall-blue", BuildingType.WALL, Direction.LEFT);` | Ô bên trái trống; mặc định tốn 1 tài nguyên. |

Đổi `Direction.RIGHT` thành `UP`, `DOWN` hoặc `LEFT` để đi lên, xuống hoặc trái.
Sau khi tạo quân mới, gọi `bot.selectUnit("worker-blue-2");` để điều khiển quân đó.
Để thử đội đỏ, dùng mã truy cập `red-token` và chọn `worker-red`.

**Tạo loại quân khác:** Các lệnh `bot.spawn` hiện chỉ tạo quân thợ.
Để thử quân cận chiến, đánh xa hoặc công thành trong Desktop Code Studio, thêm các dòng nhập sau ở đầu chương trình:

```java
import com.antcolonywars.client.GameClient;
import com.antcolonywars.core.UnitType;
```

Thay nội dung hàm `run` bằng đoạn dưới đây; trên trận mới, quân đánh xa xuất hiện dưới nhà chính xanh:

```java
try (GameClient client = GameClient.connect("ws://localhost:8080/game", "blue-token")) {
    client.spawn("ranged-blue-1", "Linh danh xa", UnitType.RANGED, Direction.DOWN);
    client.move("ranged-blue-1", Direction.RIGHT);
}
```

Đổi `UnitType.RANGED` thành `MELEE` (cận chiến), `SIEGE` (công thành) hoặc `WORKER` (quân thợ).
Kết nối này dùng mã truy cập ghi trong đoạn chương trình.

### Cách khác — chạy kịch bản có sẵn

Nếu muốn thử nhanh mà không viết Java, mở một cửa sổ PowerShell khác và chạy một kịch bản:

```powershell
.\scripts\run-demo.ps1 ResourceBuilderBot
```

| Tên thay vào lệnh | Điều quan sát được |
|---|---|
| `BlueBot` | Quân xanh di chuyển. |
| `RedBot` | Quân đỏ di chuyển. |
| `WinningScenario` | Đội xanh phá nhà chính đỏ và thắng. |
| `ReinforcementBot` | Tạo quân mới rồi điều khiển quân đó. |
| `ResourceBuilderBot` | Quân tới điểm tài nguyên, khai thác và xây doanh trại. |

Khởi động lại máy chủ trước mỗi kịch bản độc lập.

## 5. Các khả năng người chơi có thể sử dụng

### Quan sát trận đấu

- [x] Mở bản đồ và xem trạng thái kết nối.
- [x] Xem vị trí, tên, loại quân và HP của từng quân.
- [x] Xem nhà chính của hai đội và điểm sinh lực còn lại.
- [x] Xem điểm tài nguyên cùng số lượng còn lại.
- [x] Xem số tài nguyên hiện có của mỗi đội.
- [x] Xem BARRACKS, TOWER và WALL trên bản đồ.
- [x] Theo dõi số lần thay đổi trạng thái của trận đấu.
- [x] Theo dõi đội thắng khi nhà chính bị phá hủy.

### Di chuyển và chờ lượt

- [x] Chọn quân đang điều khiển.
- [x] Di chuyển quân lên một ô.
- [x] Di chuyển quân sang phải một ô.
- [x] Di chuyển quân xuống một ô.
- [x] Di chuyển quân sang trái một ô.
- [x] Chờ một lượt mà không đổi vị trí.
- [x] Bị từ chối khi đi ra ngoài bản đồ.
- [x] Bị từ chối khi đi vào ô đã bị chiếm.

### Tấn công và kết thúc trận

- [x] Tấn công quân đối phương trong tầm.
- [x] Tấn công nhà chính đối phương trong tầm.
- [x] Nhìn thấy projectile di chuyển theo nhịp mô phỏng mặc định 250 ms.
- [x] Làm giảm HP của mục tiêu.
- [x] Hạ quân đối phương khi HP về 0.
- [x] Phá nhà chính và kết thúc trận.
- [x] Bị từ chối khi tấn công đồng đội, mục tiêu không tồn tại hoặc mục tiêu ngoài tầm.
- [x] Quân RANGED có tầm tấn công xa hơn loại cơ bản.
- [x] Quân SIEGE gây sát thương cao hơn loại cơ bản.

### Tạo thêm quân

- [x] Tạo quân mới cạnh nhà chính của đội mình.
- [x] Đặt tên cho quân mới.
- [x] Tạo các loại WORKER, MELEE, RANGED và SIEGE.
- [x] Quân SIEGE chiếm footprint 2×2 khi tạo và di chuyển.
- [x] Chọn quân mới theo mã riêng và điều khiển độc lập.
- [x] Bị từ chối khi vượt giới hạn số quân của đội.
- [x] Bị từ chối khi tạo loại quân không được hỗ trợ.

### Khai thác tài nguyên

- [x] Đưa WORKER tới điểm tài nguyên.
- [x] Khai thác khi quân đứng trong khoảng cách cho phép.
- [x] Nhận tài nguyên vào kho của đội.
- [x] Làm giảm lượng tài nguyên còn lại tại node.
- [x] Bị từ chối khi đứng quá xa node.
- [x] Bị từ chối khi node đã hết tài nguyên.

### Xây dựng

- [x] Xây BARRACKS cạnh quân đang điều khiển.
- [x] Xây TOWER cạnh quân đang điều khiển.
- [x] Xây WALL cạnh quân đang điều khiển.
- [x] Trừ đúng chi phí xây dựng khỏi kho tài nguyên.
- [x] Bị từ chối khi không đủ tài nguyên.
- [x] Bị từ chối khi ô xây ngoài bản đồ, đã bị chiếm hoặc không kề quân.
- [~] Công trình có vị trí, loại và HP, nhưng chưa sản xuất quân hoặc tự phòng thủ.

### Giới hạn khi chơi

- [x] Máy chủ xử lý lần lượt các hành động.
- [x] Mỗi đội có giới hạn 10 hành động trong một giây theo cấu hình mặc định.
- [x] Hai chương trình cùng điều khiển một đội dùng chung giới hạn này.
- [x] Chương trình Java tự chờ rồi gửi lại khi bị giới hạn tốc độ.
- [~] Hàng đợi có giới hạn 32 lệnh theo cấu hình mặc định; chưa có giao diện riêng để xem hàng đợi.

## 6. Những khả năng chưa có

- [ ] Công trình tự sản xuất quân hoặc tự phòng thủ.
- [ ] Database, tài khoản, lobby, matchmaking và khôi phục sau khi khởi động lại.
- [ ] Nhiều trận cùng lúc và kiểm tra tải quy mô lớn.
- [ ] Kiểm tra hình ảnh trên nhiều trình duyệt hoặc thiết bị.

## 7. Mẫu ghi nhận một lần kiểm thử

### Thông tin lần kiểm thử

- Ngày/giờ:
- Người kiểm thử:
- Nhánh và commit:
- Hệ điều hành:
- Trình duyệt:
- Đội và token:
- Kết quả: `[ ] ĐẠT` `[ ] ĐẠT CÓ GIỚI HẠN` `[ ] KHÔNG ĐẠT`

### Danh sách hành động

- [ ] Kết nối vào đội.
- [ ] Di chuyển lên, phải, xuống hoặc trái.
- [ ] Chờ lượt.
- [ ] Tấn công quân hoặc nhà chính.
- [ ] Tạo quân mới.
- [ ] Chọn và điều khiển quân mới.
- [ ] Khai thác điểm tài nguyên.
- [ ] Xây BARRACKS, TOWER hoặc WALL.
- [ ] Thử một hành động không hợp lệ và xác nhận trạng thái không đổi.
- [ ] Chạy kịch bản thắng hoặc kịch bản xây dựng.

### Ghi nhận kết quả

| Hành động | Kết quả | Trạng thái trước/sau | Thông báo lỗi | Bằng chứng |
|---|---|---:|---|---|
| Kết nối | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Di chuyển | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Chờ lượt | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Tấn công | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Tạo quân | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Khai thác | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Xây dựng | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |
| Hành động không hợp lệ | `[ ] ĐẠT` `[ ] KHÔNG ĐẠT` |  |  |  |

## 8. Kiểm tra tự động trước khi kiểm tra bằng tay

```powershell
.\scripts\test-all.ps1
```

Không kết luận rằng hình ảnh, thao tác thực tế hoặc khả năng chịu tải đã đạt chỉ dựa trên kiểm tra tự động.
Mỗi phase mới phải cập nhật phần khả năng người chơi, cách điều khiển, kịch bản và mẫu ghi nhận trong tài liệu này.
