# Ant Colony Wars — Hướng dẫn chơi và mẫu kiểm thử

Tài liệu này mô tả những gì người chơi có thể nhìn thấy và thực hiện trong các kịch bản hiện tại.
Đây cũng là mẫu checklist dùng lại sau mỗi lần cập nhật game.

Đánh dấu `[x]` khi đã kiểm tra thành công.
Đánh dấu `[~]` khi dùng được nhưng còn giới hạn đã ghi rõ.
Đánh dấu `[ ]` khi chưa dùng được hoặc chưa có bằng chứng kiểm tra.

## 1. Trạng thái hiện tại

- Ngày cập nhật: 2026-11-14.
- Kịch bản đã hoàn thành: R1, R2, R3, R4, R4.1, R5, R6 và R8.1–R8.4.
- Người chơi điều khiển quân của mình bằng chương trình Java hoặc Desktop Code Studio.
- Phaser là màn hình quan sát bản đồ và trạng thái trận đấu.
- Máy chủ giữ kết quả chính thức của mọi hành động.
- Web viewer mở đầu bằng phòng chọn trận, tự đọc danh sách từ `/api/matches` và cho phép nhập mã session token.
- Trong trận, người chơi thấy revision, nhịp mô phỏng, lực lượng, command gần nhất và banner khi trận kết thúc.

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

Lệnh này dùng database H2 dạng file và tiếp tục account, lobby cùng trận đã lưu sau khi khởi động lại.
Để chạy một trận tạm thời từ state seed mà không đụng database đã lưu, thay lệnh trên bằng:

```powershell
.\scripts\start-server.ps1 -Fresh
```

### Cửa sổ 2 — bật màn hình bản đồ

```powershell
.\scripts\start-web.ps1
```

Mở `http://127.0.0.1:5173/` và chờ dòng trạng thái trực tuyến.

Đăng ký hoặc đăng nhập ngay trên web. Sau đó có thể tạo trận mới, tìm trận bằng mã + token đội, hoặc mở một trận đang chạy để quan sát.

### Tạo và tìm trận trên web

1. Chọn **Tạo trận**, giữ hoặc sửa JSON mẫu rồi bấm **Tạo phòng chờ**.
2. Map script dùng `map: "nxn"`, `teams`, `accountsPerTeam` và các mảng `resources`, `headquarters`, `units`.
3. Gửi pairing key sáu ký tự cho người dùng Desktop; gửi mã trận và token màu cho người tham gia bằng web nếu cần.
4. Người chơi khác chọn **Tìm trận**, nhập đúng hai mã; nếu đội đầy, giao diện báo lỗi.
5. Chủ phòng bấm **Bắt đầu trận đấu**. Sau khi bắt đầu, dùng token đội trong Desktop Code Studio và thêm `?matchId=MA_TRAN` vào URL server.

### Chơi cùng Wi-Fi trong mạng LAN

Một máy làm máy chủ và chạy cả server lẫn web viewer.
Tìm địa chỉ IPv4 của máy đó bằng `ipconfig`, ví dụ `192.168.1.23`.
Chạy `.\scripts\start-server.ps1`, sau đó chạy `.\scripts\start-web.ps1`.
Mở cổng TCP `8080` và `5173` trên Windows Firewall nếu máy khác không truy cập được.
Người chơi khác cùng Wi-Fi mở `http://192.168.1.23:5173/` bằng địa chỉ IPv4 vừa tìm.
Viewer tự nối API và WebSocket về đúng máy đang phát trang, nên không cần sửa mã nguồn hay đặt `localhost` thủ công.
LAN nhanh hơn đường Internet vì dữ liệu đi thẳng trong mạng nội bộ.

### Cửa sổ 3 — mở nơi viết, biên dịch và chạy Java

```powershell
.\scripts\start-desktop.ps1
```

Chờ cửa sổ Desktop Code Studio xuất hiện.
Giữ cả ba cửa sổ PowerShell hoạt động trong khi chơi; mỗi cửa sổ chạy một lệnh ở trên.

Trong Desktop Code Studio, nhập pairing key đang hiển thị tại phòng chờ rồi nhấn **Pair**.
Ứng dụng quét các subnet IPv4 local trên cổng `8080`, tự điền Server và Match code, rồi nạp danh sách Team token để chọn.
Nếu Windows Firewall chặn kết nối, mở TCP `8080` trên máy chủ.

### Viết bot trong IntelliJ

1. Mở thư mục `student-bot-template` bằng IntelliJ và chọn JDK 21.
2. Điền pairing key sáu ký tự vào `MATCH_KEY` trong file `.env`.
3. Viết chương trình trong `src/main/java/Main.java` với entry point `public static void main(Bot bot)`.
4. Chạy `Submit.main()` trong IntelliJ khi Desktop Code Studio đang mở.
5. Desktop nhận source qua `127.0.0.1:17321`, tự pair, compile và chạy bot.

Source chỉ đi từ IntelliJ tới Desktop trên chính máy đang chạy và không được upload lên game server.
Có thể đặt `BATCH_MODE=ATOMIC` hoặc `BATCH_MODE=PARTIAL` trong `.env`.

### Tùy chọn — đăng nhập và join team thay cho demo token

Sau khi server đã chạy, mở thêm PowerShell và đăng ký account:

```powershell
$register = @{
    username = "blue_one"
    displayName = "Blue One"
    password = "strong-pass-123"
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/accounts -ContentType application/json -Body $register
```

Đăng nhập để nhận session token:

```powershell
$login = @{ username = "blue_one"; password = "strong-pass-123" } | ConvertTo-Json
$session = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/sessions -ContentType application/json -Body $login
```

Join đội BLUE:

```powershell
$headers = @{ Authorization = "Bearer $($session.token)" }
$membership = @{ teamId = "BLUE" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri http://localhost:8080/api/lobby/membership -Headers $headers -ContentType application/json -Body $membership
```

Dùng `$session.token` ở trường Token của Desktop Code Studio.
Account phải join team trước khi session token được phép điều khiển quân qua WebSocket.
Có thể tạo tối đa năm account và join cùng BLUE; tất cả account dùng chung state của đội.
Account thứ sáu bị từ chối với mã `TEAM_FULL`.

## 4. Viết và chạy chương trình điều khiển

API IntelliJ được khuyến nghị cho người học mới:

```java
import com.antcolonywars.sdk.Bot;

public final class Main {
    public static void main(Bot bot) {
        bot.select("worker-blue");
        bot.go(2);
        bot.idle();
    }
}
```

Các hướng của `go` là `1` lên, `2` phải, `3` xuống và `4` trái.
API ngắn còn có `hit`, `mine`, `build`, `spawn` và `state`.
Java không cho định nghĩa `bot.wait()` vì `Object.wait()` là phương thức `final`, do đó thao tác chờ dùng tên `bot.idle()`.
API `BotProgram`/`GameBot` đầy đủ bên dưới vẫn được giữ để tương thích.

### Bước 1 — chọn đội và nhập chương trình

Với trận tạo từ web, ưu tiên nhập pairing key và nhấn **Pair**.
Chọn một token màu trong ô `Team token`; Server và Match code đã được điền tự động.
Chọn `ATOMIC` để toàn bộ batch chỉ được áp dụng khi mọi hành động hợp lệ, hoặc `PARTIAL` để dừng tại lỗi đầu tiên và giữ các hành động trước đó.
Luồng thủ công vẫn hỗ trợ `ws://192.168.1.23:8080/game`, mã trận và token đội khi không thể quét LAN.
Với `match-default`, để trống Match code và dùng demo token `blue-token` hoặc `red-token`.
Giữ tên lớp là `MyBot`.
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
3. Desktop gom các hành động của lần Run thành một batch rồi gửi đến server.
4. Mở trang bản đồ: quân xanh đi từ `(1, 0)` sang `(2, 0)` rồi đứng chờ.
5. Nhấn `Stop` (`Ctrl+K`) khi cần dừng chương trình đang chạy.

Mỗi bot có tối đa 10 hành động trong một batch; `selectUnit` chỉ đổi bot đang chọn nên không tính hạn mức.
Không có giới hạn lệnh theo giây hoặc simulation tick.
Các lời gọi đọc state trong lúc tạo batch dùng snapshot nhận tại đầu lần Run.

Sau mỗi lần sửa, biên dịch lại trước khi chạy.
Dừng chương trình không hoàn tác hành động đã thực hiện.
Khởi động lại server mặc định sẽ phục hồi trận đã lưu.
Để thử kịch bản từ state seed mà không xóa dữ liệu, dừng máy chủ bằng `Ctrl+C`, chạy `.\scripts\start-server.ps1 -Fresh` rồi chờ bản đồ kết nối lại.

### Bước 3 — thay hành động muốn thử

Thay các dòng bên trong hàm `run` bằng hành động dưới đây.
Mỗi dòng là một ví dụ riêng; cần đưa quân tới vị trí phù hợp trước khi khai thác, xây dựng hoặc tấn công.

| Muốn thực hiện | Lệnh trong hàm `run` | Điều kiện hoặc kết quả |
|---|---|---|
| Chọn quân | `bot.selectUnit("worker-blue");` | Quân thuộc đội đang điều khiển. |
| Đi một ô | `bot.move(Direction.RIGHT);` | Ô đích trống và nằm trong bản đồ. |
| Đứng chờ | `bot.waitTurn();` | Giữ nguyên vị trí. |
| Tấn công | `bot.attack("ranged-blue-1", Direction.RIGHT);` | Quét sang phải đến hết tầm; mục tiêu đầu tiên trên đường bắn nhận projectile. |
| Chờ đạn chạm mục tiêu | `bot.awaitSimulationIdle();` | Chờ toàn bộ projectile hiện tại kết thúc trước khi đọc HP hoặc winner. |
| Tạo quân | `bot.spawn("barracks-blue", "ranged-blue-1", "Linh danh xa", UnitType.RANGED, Direction.DOWN);` | HQ chỉ tạo `WORKER`; BARRACKS tạo `MELEE`, `RANGED`, `SIEGE`; vùng xuất quân phải trống. |
| Khai thác | `bot.harvest();` | Mặc định đứng tại điểm tài nguyên còn trữ lượng. |
| Xây doanh trại | `bot.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);` | Ô bên phải trống; mặc định tốn 3 tài nguyên. |
| Xây tháp | `bot.build("tower-blue", BuildingType.TOWER, Direction.DOWN);` | Ô bên dưới trống; mặc định tốn 2 tài nguyên. |
| Xây tường | `bot.build("wall-blue", BuildingType.WALL, Direction.LEFT);` | Ô bên trái trống; mặc định tốn 1 tài nguyên. |

Đổi `Direction.RIGHT` thành `UP`, `DOWN` hoặc `LEFT` để đi lên, xuống hoặc trái.
Sau khi tạo quân mới, gọi `bot.selectUnit("worker-blue-2");` để điều khiển quân đó.
Để thử đội đỏ, dùng mã truy cập `red-token` và chọn `worker-red`.

**Tạo loại quân khác:** Thêm import sau ở đầu chương trình Desktop Code Studio:

```java
import com.antcolonywars.core.UnitType;
```

Trong hàm `run`, có thể tạo và điều khiển quân đánh xa như sau:

```java
bot.spawn("barracks-blue", "ranged-blue-1", "Linh danh xa", UnitType.RANGED, Direction.DOWN);
bot.selectUnit("ranged-blue-1");
bot.move(Direction.RIGHT);
bot.attack(Direction.RIGHT);
```

Đổi `UnitType.RANGED` thành `MELEE` (cận chiến), `SIEGE` (công thành) hoặc `WORKER` (quân thợ).
Nếu đã gọi `bot.selectUnit("ranged-blue-1")`, có thể viết ngắn `bot.attack(Direction.RIGHT)`.
Nếu không có mục tiêu trên đường bắn trong tầm, server trả `TARGET_NOT_FOUND`; nếu đồng minh đứng trước, server trả `FRIENDLY_TARGET`.
Đạn giữ tọa độ của mục tiêu tại lúc khai hỏa, đi theo đường Bresenham và không đổi hướng khi mục tiêu di chuyển.
Công trình trên đường bay chặn đạn và nhận sát thương; công trình biến mất khi HP về 0.
TOWER tự bắn mục tiêu địch gần nhất trong phạm vi 5 ô theo simulation tick.

### Cách khác — chạy kịch bản có sẵn

Nếu muốn thử nhanh mà không viết Java, mở một cửa sổ PowerShell khác và chạy một kịch bản:

```powershell
.\scripts\run-demo.ps1 ResourceBuilderBot
```

| Tên thay vào lệnh | Điều quan sát được |
|---|---|
| `BlueBot` | Quân xanh di chuyển. |
| `RedBot` | Quân đỏ di chuyển. |
| `WinningScenario` | Đội xanh hạ đủ năm HQ phe SWARM và thắng. |
| `ReinforcementBot` | Tạo quân mới rồi điều khiển quân đó. |
| `ResourceBuilderBot` | Quân tới điểm tài nguyên, khai thác và xây doanh trại. |

Khởi động lại máy chủ trước mỗi kịch bản độc lập.

## 5. Các khả năng người chơi có thể sử dụng

### Quan sát trận đấu

- [x] Mở bản đồ và xem trạng thái kết nối.
- [x] Xem vị trí, tên, loại quân và HP của từng quân.
- [x] Xem nhà chính của 10 đội và điểm sinh lực còn lại.
- [x] Xem điểm tài nguyên cùng số lượng còn lại.
- [x] Xem số tài nguyên hiện có của mỗi đội.
- [x] Xem BARRACKS, TOWER và WALL trên bản đồ.
- [x] Theo dõi số lần thay đổi trạng thái của trận đấu.
- [x] Theo dõi đội thắng khi toàn bộ HQ của phe đối phương bị phá hủy.

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
- [x] Batch giới hạn 10 hành động cho mỗi bot và được server xử lý tuần tự.
- [x] Desktop cho chọn Atomic hoặc Partial trước khi Run.

### Account, lobby và dữ liệu đã lưu

- [x] Đăng ký account và đăng nhập để nhận session token.
- [x] Join BLUE hoặc RED trước khi dùng session token điều khiển quân.
- [x] Năm account cùng team điều khiển chung state; giới hạn được tính theo bot trong từng batch.
- [x] Account thứ sáu bị từ chối khi team đã đủ chỗ.
- [x] Reconnect nhận lại snapshot mới nhất; `matchId` tách biệt giữa các match.
- [x] Snapshot cùng event history được ghi vào H2 qua Flyway migration.
- [x] State đã lưu được nạp lại và tiếp tục revision, processed command cùng simulation state.
- [~] Demo token cũ vẫn dùng được; chưa có giao diện đăng nhập trong Desktop Code Studio.

## 6. Những khả năng chưa có

- [ ] Công trình tự sản xuất quân hoặc tự phòng thủ.
- [x] Hai match chạy đồng thời với route WebSocket riêng và endpoint tick latency.
- [~] Load gate 400 match-tick đã có trong test; chưa phải benchmark production nhiều máy.
- [ ] Matchmaking, xác minh email, quên mật khẩu, quản lý/thu hồi session.
- [ ] Backup, HA và database production.
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
