# ANT COLONY WARS — Kế hoạch sản phẩm và MVP (v1.0)

> **Trạng thái:** MVP Phase 00–05, Desktop Code Studio D00–D05, gameplay R1–R6 đã được kiểm chứng.
> **Kiến trúc chính:** Server-authoritative; server giữ luật và state chính, client gửi command và render snapshot/event.
> **Kiến trúc dự phòng:** `docs/ANT_COLONY_WARS_CLIENT_SIMULATION_MIGRATION_PLAN.md` chỉ là đề xuất sau MVP, chưa thay thế roadmap hiện tại.
> **Mục tiêu dài hạn:** Game chiến thuật online 2D trên lưới ô vuông, trong đó người chơi điều khiển đơn vị bằng chương trình Java.
> **Mục tiêu MVP:** Chứng minh một chương trình Java cơ bản có thể gửi hành động tới server, làm thay đổi trạng thái trận đấu và được Phaser hiển thị ngay qua WebSocket.
> **Mục tiêu hậu MVP gần nhất:** Hoàn thiện trải nghiệm và phát hành trên nền R6.

## 1. Quyết định kỹ thuật

| Thành phần | Công nghệ | Phạm vi |
|---|---|---|
| Game engine và server | Java 21 + Spring Boot | Game Core thuần; state trong RAM theo `MatchSetup` |
| Chương trình điều khiển | Java 21 | Một Java client nhỏ chạy local, gọi API mức cao |
| Giao diện 2D | TypeScript + Phaser | Hiển thị map, đơn vị, HQ, HP, log và kết quả |
| Giao tiếp online | WebSocket + JSON | Java client gửi lệnh; server phát kết quả và state |
| Lưu trữ | H2 + Flyway | Account, session, lobby membership, match snapshot và event history |
| Xác thực | BCrypt + session token | REST đăng ký/đăng nhập/join team; demo token giữ cho local flow |
| Desktop Code Studio | Java 21 + JavaFX + Java Compiler API | Editor, biên dịch local, child JVM runner và pairing LAN bằng key |

MVP không nhận source code Java rồi biên dịch trên server.
Người chơi viết và chạy chương trình Java trên máy local bằng một thư viện client được cung cấp sẵn.
Cách này cho phép dùng biến, `if`, `for`, hàm và class Java thật mà không cần compiler service hoặc sandbox trong MVP.
Sau MVP, Desktop Code Studio cung cấp trải nghiệm này trong một ứng dụng desktop mà không thay đổi quyền quyết định gameplay của server.

## 2. Định nghĩa MVP

MVP chỉ gồm một lát cắt dọc có thể chơi và kiểm thử trực tiếp:

- Một profile mặc định có map 8×8; kích thước map đọc từ cấu hình.
- Profile mặc định có hai đội `BLUE` và `RED`; đội, màu, token và vị trí không nằm trong luật engine.
- Mỗi đội có một HQ và một đơn vị `WORKER` được seed từ `MatchSetup`.
- Token demo được đọc từ cấu hình hoặc biến môi trường.
- Đơn vị đi bốn hướng, đứng im và tấn công.
- Server chặn đi ra ngoài map.
- Một ô chỉ chứa tối đa một đơn vị hoặc một HQ.
- Đơn vị chỉ tấn công mục tiêu địch ở ô kề theo khoảng cách Manhattan bằng 1.
- Đòn đánh trừ HP ngay lập tức; không có projectile.
- Phá HQ đối phương kết thúc trận.
- Phaser nhận state qua WebSocket và hiển thị thay đổi mà không refresh.
- Toàn bộ trạng thái nằm trong RAM và được tạo lại khi server khởi động.

### 2.1. Không thuộc MVP

- Tạo, xóa hoặc sản xuất thêm đơn vị.
- Tài nguyên, khai thác và xây công trình.
- Bốn loại quân và footprint 2×2.
- 10 đội, 5 tài khoản mỗi đội hoặc lobby.
- Database, đăng ký, đăng nhập và phục hồi sau khi restart.
- Đạn bay, animation chiến đấu, fog of war hoặc tìm đường.
- Rate limit 10 hành động mỗi giây và hàng đợi dài.
- Editor Java trên trình duyệt.
- Upload, biên dịch hoặc chạy source code không tin cậy trên server.
- Matchmaking, xếp hạng, replay, pixel art hoàn chỉnh hoặc cân bằng game.

Ranh giới này là bắt buộc.
Một tính năng ngoài danh sách MVP chỉ được triển khai sau khi kịch bản nghiệm thu cuối đã chạy thành công.
Desktop Code Studio thuộc roadmap hậu MVP và không làm thay đổi tiêu chí hoàn thành của MVP đã kiểm chứng.

### 2.2. Ranh giới dynamic của MVP

MVP vẫn chỉ chạy một match và một profile mặc định, nhưng không khóa các giá trị sau trong source code:

- Kích thước map và danh sách tile.
- Danh sách đội, màu hiển thị, token demo và HQ.
- Danh sách unit seed, loại unit, vị trí và HP ban đầu.
- Damage, attack range, occupancy và giới hạn bounds.
- Protocol version, rules version và tên profile.

`MatchSetup` là đầu vào của Game Core.
Server chọn profile mặc định từ file cấu hình, sau đó tạo `MatchState` từ `MatchSetup`.
Thêm profile hoặc đổi sang nguồn setup khác không được yêu cầu sửa luật MOVE/WAIT/ATTACK.

## 3. Cách người chơi điều khiển bằng Java

Module `game-client-java` cung cấp API nhỏ và dễ học:

```java
import com.antcolony.client.Direction;
import com.antcolony.client.GameClient;

public class BlueBot {
    public static void main(String[] args) throws Exception {
        try (GameClient bot = GameClient.connect(
                System.getenv("GAME_WS_URL"),
                System.getenv("GAME_TEAM_TOKEN"))) {
            for (int i = 0; i < 3; i++) {
                bot.move(Direction.RIGHT);
            }

            bot.move(Direction.DOWN);
            bot.attack(System.getenv("GAME_TARGET_ID"));
        }
    }
}
```

API MVP chỉ cần các phương thức sau.
URL, token và unit ID phải lấy từ cấu hình hoặc tham số chạy, không ghi cứng trong thư viện.

```java
GameClient.connect(String url, String teamToken)
client.move(Direction direction)
client.waitTurn()
client.attack(String targetId)
client.getLatestState()
client.close()
```

Mỗi lời gọi hành động gửi đúng một command và chờ `COMMAND_RESULT` tương ứng.
Vòng `for` tự nhiên tạo nhiều command có thứ tự mà không cần phân tích source code.
Server áp dụng cùng luật cho mọi command, bất kể command được viết trực tiếp hay sinh ra từ vòng lặp.

### 3.1. Desktop Code Studio sau MVP

Desktop Code Studio là ứng dụng JavaFX để người học viết source Java, biên dịch local, chạy bot và xem log mà không phải tự cấu hình một project Maven riêng.
Ứng dụng không dịch cú pháp Java thành MOVE/ATTACK bằng regex hoặc AST.
Source được biên dịch và thực thi như Java thật, sau đó gọi `game-client-java` để gửi command tới server.

```text
JavaFX editor
    → Java Compiler API
    → class files trong thư mục tạm
    → child JVM runner
    → game-client-java
    → WebSocket server
    → Game Core và STATE_UPDATED
    → Phaser viewer
```

Code người dùng triển khai một hợp đồng nhỏ để desktop kiểm soát vòng đời chạy:

```java
public final class MyBot implements BotProgram {
    @Override
    public void run(GameBot bot) throws Exception {
        for (int i = 0; i < 3; i++) {
            bot.move(Direction.RIGHT);
        }
    }
}
```

`game-desktop` chịu trách nhiệm UI, cấu hình kết nối, biên dịch và điều phối process.
`game-runner` là entry point tối thiểu chạy class đã biên dịch trong một JVM con và kết nối bằng `game-client-java`.
URL server, team token và unit ID do desktop truyền vào runner; người dùng không cần ghi credential trong source.
Với lobby động, server cấp một pairing key để Desktop tự tìm endpoint LAN, điền match code và nạp danh sách token màu.
Source code và class file chỉ tồn tại local, không được upload lên game server.

Project `student-bot-template` cho phép người học viết `public static void main(Bot bot)` trong IntelliJ.
Lớp `Submit` đọc pairing key và chế độ batch từ `.env`, sau đó gửi source qua HTTP loopback tới Desktop để tự pair, compile và chạy.
API nhập môn gồm `select`, `go`, `hit`, `mine`, `build`, `spawn`, `idle` và `state`; tên `idle` tránh xung đột với `Object.wait()` dạng `final`.

JVM con phải có classpath giới hạn, working directory tạm, giới hạn heap, timeout, giới hạn output và thao tác dừng rõ ràng.
Các giới hạn này bảo vệ độ ổn định của ứng dụng nhưng không phải sandbox an toàn cho code không tin cậy.
Phiên bản đầu chỉ chạy source do chính người dùng tin cậy; nếu nhận code từ người khác thì phải bổ sung cô lập ở mức hệ điều hành hoặc container.

UI tối thiểu gồm editor, cấu hình server/team, nút Compile, Run, Stop và console.
Lỗi compiler phải hiển thị message, dòng và cột để người học sửa trực tiếp.
Phaser tiếp tục chạy ở trình duyệt trong phiên bản đầu; việc nhúng viewer vào desktop chỉ xem xét sau khi luồng compile/run ổn định.

Bản phát hành desktop dùng `jpackage`.
Runtime đóng gói phải có module `jdk.compiler`; nếu chưa đóng gói được compiler thì tài liệu phải yêu cầu rõ JDK 21 thay vì chỉ JRE.

## 4. Mô hình trạng thái tối thiểu

```text
MatchState
├── matchId
├── setupId
├── rulesVersion
├── status: RUNNING | FINISHED
├── winnerTeamId
├── revision
├── eventSequence
├── map: width, height
├── teams[2]
├── units[2]
└── headquarters[2]
```

Mỗi `Unit` cần `id`, `teamId`, `x`, `y`, `hp` và `facing`.
Mỗi `Headquarters` cần `id`, `teamId`, `x`, `y` và `hp`.
ID là duy nhất toàn trận và được dùng trong giao thức.
Tên hiển thị không được dùng làm khóa định danh.

Giá trị HP và damage chỉ là hằng số thử nghiệm.
Đề xuất ban đầu là đơn vị có 3 HP, HQ có 3 HP và mỗi đòn đánh gây 1 damage.

Các giá trị mặc định này thuộc `RuleSet`/`MatchSetup`, không phải literal rải trong handler hoặc renderer.

## 4.1. Hợp đồng Game Core

Game Core phải chạy được không cần Spring Boot, WebSocket, database hoặc Phaser:

```text
apply(GameState state, GameCommand command, RuleSet rules)
    -> ApplyResult(state, events, errors)
```

Hàm xử lý không đọc đồng hồ hệ thống, không gọi network và không dùng random không có seed.
Metadata giao thức như `commandId`, `sequence` và `effectiveTick` tách khỏi luật gameplay.
MVP chỉ dùng `revision`/`sequence`; `effectiveTick` có thể bổ sung sau mà không đổi chữ ký luật.

## 5. Luật xử lý command

Các command của MVP:

| Command | Dữ liệu | Kết quả |
|---|---|---|
| `MOVE` | `unitId`, `direction` | Đi một ô nếu hợp lệ |
| `WAIT` | `unitId` | Không đổi vị trí nhưng tạo event thành công |
| `ATTACK` | `unitId`, `targetId` | Trừ HP mục tiêu địch ở ô kề |
| `GET_STATE` | Không có | Trả snapshot hiện tại |

`GET_STATE` là thao tác chỉ đọc và luôn trả snapshot hiện tại, kể cả khi trận đã kết thúc.

Server phải kiểm tra action command theo thứ tự:

1. Trận còn ở trạng thái `RUNNING`.
2. Token hợp lệ và sở hữu đơn vị gửi lệnh.
3. `commandId` chưa được xử lý trước đó.
4. Command và tham số hợp lệ.
5. Vị trí đích nằm trong map và không bị chiếm.
6. Mục tiêu tấn công thuộc đội địch, còn sống và ở trong tầm.

Action command thất bại không được thay đổi state hoặc tăng `revision`.
Action command được chấp nhận chỉ áp dụng một lần và tăng `revision` đúng một đơn vị, kể cả `WAIT`.
Các command của cùng một match được server xử lý tuần tự.

## 6. Giao thức WebSocket tối thiểu

Client gửi:

```json
{
  "type": "COMMAND",
  "schemaVersion": 4,
  "matchId": "match-default",
  "commandId": "blue-001",
  "action": "MOVE",
  "unitId": "worker-blue",
  "direction": "RIGHT"
}
```

Server trả cho client gửi lệnh:

```json
{
  "type": "COMMAND_RESULT",
  "schemaVersion": 4,
  "commandId": "blue-001",
  "ok": true,
  "revision": 1,
  "sequence": 1
}
```

Server phát cho các client đang theo dõi trận:

```json
{
  "type": "STATE_UPDATED",
  "schemaVersion": 4,
  "matchId": "match-default",
  "revision": 1,
  "sequence": 1,
  "state": {}
}
```

MVP phát toàn bộ snapshot sau mỗi command thành công để giảm logic đồng bộ.
Snapshot phải serialize được và event phải có thứ tự để sau này hỗ trợ replay hoặc client simulation.
Chỉ chuyển sang delta event khi state lớn hơn và có benchmark chứng minh cần thiết.

## 7. Giao diện Phaser

Giao diện MVP chỉ cần:

- Lưới theo kích thước trong snapshot, mặc định 8×8.
- Hai HQ bằng hình chữ nhật khác màu.
- Hai đơn vị bằng hình tròn hoặc sprite placeholder.
- HP của hai HQ và hai đơn vị.
- Trạng thái trận và đội thắng.
- `revision` hiện tại.
- Log năm command gần nhất.
- Trạng thái kết nối WebSocket.

Phaser không tự tính kết quả di chuyển hoặc chiến đấu.
Phaser chỉ render state do server gửi.
Không cần editor, nút điều khiển, animation hoặc asset pixel art trong MVP.

## 8. Kiểm thử trực tiếp

### 8.1. Test tự động

- Unit test cho MOVE bốn hướng, WAIT, bounds và ô bị chiếm.
- Unit test ownership, attack range, damage, phá HQ và `MATCH_FINISHED`.
- Unit test command trùng `commandId` không chạy hai lần.
- Test tạo hai `MatchSetup` khác nhau mà không sửa Game Core.
- Test serialize/deserialize snapshot và replay cùng command log cho cùng state.
- Integration test mở WebSocket, gửi command và nhận `COMMAND_RESULT` cùng `STATE_UPDATED`.
- Test Java client gửi chuỗi command theo đúng thứ tự.

### 8.2. Kịch bản demo bắt buộc

1. Khởi động backend và frontend từ hướng dẫn sạch.
2. Mở trang Phaser và thấy map seed ban đầu.
3. Chạy `BlueBot.java` từ IDE hoặc Maven.
4. Quan sát đơn vị BLUE di chuyển ngay trên Phaser.
5. Chạy `RedBot.java` để chứng minh hai token điều khiển hai đội khác nhau.
6. Chạy kịch bản đưa một đơn vị tới HQ địch và tấn công ba lần.
7. Quan sát Phaser hiển thị `FINISHED` và đúng đội thắng.
8. Chạy lại test tự động và nhận kết quả pass.

## 9. Tiêu chí hoàn thành MVP

MVP hoàn thành khi tất cả điều kiện sau đạt:

- Một lệnh khởi động được backend và một lệnh khởi động được frontend.
- Có thể đổi map/team/unit seed bằng profile cấu hình mà không sửa luật engine.
- Chương trình Java cơ bản dùng được biến, `if`, `for` và hàm để gửi command.
- Java client không cần sửa mã nguồn backend hoặc frontend.
- Server là nguồn sự thật duy nhất.
- Hai token không điều khiển được đơn vị của nhau.
- MOVE, WAIT, ATTACK, bounds, occupancy và chiến thắng hoạt động đúng.
- Phaser cập nhật qua WebSocket mà không refresh.
- Kịch bản demo hoàn thành một trận từ đầu đến cuối.
- Bộ test tự động của MVP pass.
- README ghi đúng lệnh chạy và giới hạn của MVP.

## 10. Thời gian mục tiêu

| Khối | Thời gian dự kiến |
|---|---:|
| Khung dự án và pure game engine | 3–4 ngày công |
| Spring Boot WebSocket và state trong RAM | 2–3 ngày công |
| Java client SDK và chương trình mẫu | 2–3 ngày công |
| Phaser viewer | 2–3 ngày công |
| Tích hợp, test và README | 2–4 ngày công |
| **Tổng MVP** | **12–18 ngày công** |

Một người đã quen công nghệ có thể hoàn thành trong khoảng 2–3 tuần toàn thời gian.
Một người vừa học vừa làm 15–20 giờ mỗi tuần nên dự kiến 4–7 tuần.
Ước lượng không bao gồm sản xuất pixel art, triển khai production hoặc chạy source Java không tin cậy trên server.

## 11. Hướng phát triển sau MVP

### Lobby web và trận động

- Người chơi đăng ký/đăng nhập bằng username/password trước khi tạo hoặc tìm trận.
- Trận tùy chỉnh được mô tả bằng JSON map script với kích thước vuông, tài nguyên, HQ, lính, số đội và capacity tài khoản/đội.
- Server sinh mã trận, pairing key và token màu cho từng đội, giữ phòng chờ trong H2 và chỉ đăng ký match vào simulation khi chủ phòng bắt đầu.
- Tham gia trận dùng mã trận + token đội; kiểm tra capacity được thực hiện transactionally trên server.

1. R1 đã bổ sung spawn WORKER cạnh HQ, ID toàn trận, tên hiển thị, chọn đơn vị và giới hạn theo đội.
2. Đơn vị bị hạ được xóa khỏi snapshot để giải phóng ô và quota.
3. R2 đã bổ sung resource node, stockpile, khai thác và xây BARRACKS/TOWER/WALL qua schema 3.
4. R3 đã bổ sung các loại MELEE/RANGED/SIEGE và combat profile cơ bản.
5. P0 batch đã thay rate limit theo team bằng tối đa 10 hành động/bot/lần Run với chế độ Atomic hoặc Partial.
6. R4.1 đã bổ sung footprint 2×2 cho SIEGE, projectile và simulation tick cấu hình được.
7. R5 đã bổ sung account, lobby membership, session token, H2/Flyway và restart recovery.
8. R6 đã thêm 10 đội chia 2 phe, hai match, tối đa 50 đơn vị mỗi đội, 5 account/team và load gate tick/reconnect.
9. Chỉ thiết kế editor web nếu Desktop Code Studio không đáp ứng; compiler service và sandbox phải tách khỏi game server.
10. Nếu cần client simulation, thực hiện theo tài liệu migration dự phòng sau khi snapshot, command log và deterministic core đã được kiểm chứng.
11. Bổ sung pixel art, animation, tutorial, replay và cân bằng.

Tinh thần của MVP là chứng minh chu trình **Java program → WebSocket → server xử lý → state đổi → Phaser hiển thị → trận kết thúc** với số thành phần ít nhất có thể.
