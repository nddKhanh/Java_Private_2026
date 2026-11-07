# Ant Colony Wars — Phác thảo kiến trúc Client-side Simulation & kế hoạch chuyển đổi

**Phiên bản:** 0.1 (định hướng sau MVP)
**Trạng thái:** Đề xuất; **không thay thế** roadmap MVP hiện tại.
**Mục tiêu:** Giảm tải server bằng cách đưa thực thi Java, game logic và render sang máy người chơi. Server chủ yếu quản lý phòng, sắp thứ tự sự kiện và chuyển tiếp dữ liệu.

## 1. Bối cảnh và quyết định

### Hiện tại (MVP / kiến trúc cũ)
- Client nhập **một mã lệnh số mỗi lần gửi**; server xử lý hành động, giữ game state, rồi trả kết quả.
- Game chạy trên bản đồ ma trận vuông; đơn vị di chuyển bốn hướng, có tạo/chọn/xóa bot, khai thác, xây dựng, xuất quân và chiến đấu.
- MVP ưu tiên chạy được; chưa cần vòng lặp Java, va chạm, vật cản, quy tắc tốc độ khác nhau hoặc cân bằng.
- Thiết kế hoàn chỉnh vẫn giữ các ý tưởng mở rộng: lập trình Java, lợi thế điều khiển nhiều đơn vị bằng `for`, 10 đội chia hai phe, nhiều tài khoản cùng điều khiển một đội.

### Đích đến (sau MVP)
- **Ứng dụng client:** chạy chương trình Java cục bộ, phân tích lệnh thành sự kiện chuẩn hóa, áp dụng cùng bộ luật game, lưu trạng thái trận đấu tại máy và render 2D.
- **Relay server:** quản lý tài khoản/phòng/quyền gửi lệnh, đóng dấu thứ tự sự kiện và tick hiệu lực, chuyển tiếp sự kiện, hỗ trợ đồng bộ lại. **Không tính di chuyển, sát thương, tài nguyên hay va chạm.**
- **Network event-driven:** client **chỉ gửi khi có lệnh**. Không gửi 24 gói rỗng mỗi giây. Server không cần phát tick rỗng liên tục.
- **Local simulation:** các client tự chạy nhịp mô phỏng mục tiêu **24 tick/giây** và thống nhất theo mốc tick chung do server xác lập.
- **Lệnh:** truyền `command` thay vì truyền trạng thái đã tính (`x`, `hp`), để các client cùng áp dụng luật và cho ra kết quả giống nhau.

> Lưu ý: 24 Hz là nhịp mô phỏng, không bảo đảm đồng hồ các máy đồng bộ tuyệt đối. Không nhận được gói tin **không chứng minh** không có sự kiện đang trên đường truyền. Bản phát hành cần xử lý gói đến muộn hoặc phát hiện lệch trạng thái.

## 2. Luồng xử lý mục tiêu

1. Người chơi chạy chương trình Java bằng ứng dụng client. Client thu lệnh (ví dụ từ `System.out.println(7)`), chuyển thành mã lệnh có cấu trúc.
2. Client gửi **batch lệnh khi có dữ liệu**, kèm ID trận, ID đội, ID đơn vị, số thứ tự cục bộ và mã lệnh.
3. Relay xác thực phiên/kết nối, giới hạn kích thước và tốc độ gửi; sắp thứ tự chính thức, cấp `sequence` và `effectiveTick` tương lai, phát tới các client trong trận.
4. Mỗi client đưa sự kiện vào hàng đợi theo tick; khi đến tick hiệu lực, áp dụng sự kiện bằng **cùng phiên bản Java Game Core**.
5. Client tự render trạng thái. Khi không có sự kiện mới, vẫn thực hiện những cập nhật nội bộ phụ thuộc tick (nếu có), nhưng không cần truyền gói gameplay rỗng.
6. Định kỳ hoặc khi cần, client trao đổi `stateHash` để phát hiện khác biệt. Khi lệch, dùng snapshot đáng tin cậy + nhật ký sự kiện để khôi phục.

### Định dạng thông điệp minh họa

**Client → Relay:**
```json
{
  "matchId": "m1",
  "clientSequence": 17,
  "commands": [
    {"unitName": "ant01", "code": 7}
  ]
}
```

**Relay → Các client:**
```json
{
  "matchId": "m1",
  "sequence": 1052,
  "effectiveTick": 124,
  "teamId": "teamA",
  "unitName": "ant01",
  "code": 7
}
```

**Lưu ý:** Schema chỉ để mô tả ý tưởng, chưa phải API contract cuối cùng. Server chỉ tin `teamId` suy ra từ phiên được xác thực; không tin trường đội mà client tự khai báo.

## 3. Quy tắc đồng bộ tối thiểu

| Chủ đề | Quy tắc đề xuất |
|---|---|
| Tick | 24 tick/s; bắt đầu từ mốc tick chung; hiệu chỉnh sai lệch đồng hồ định kỳ bằng control message thưa |
| Sự kiện | Mọi sự kiện có thứ tự chính thức `sequence` và `effectiveTick` |
| Lệnh trễ | Nếu đã lỡ tick hiệu lực thì không tự ý áp dụng ở tick hiện tại; thực hiện replay/rollback hoặc đồng bộ lại |
| Thứ tự | Client áp dụng cùng thứ tự lệnh; quy tắc giải quyết hành động cùng tick phải cố định |
| Ngẫu nhiên | Dùng seed chung và bộ sinh số ngẫu nhiên xác định, nếu gameplay có ngẫu nhiên |
| Tính nhất quán | Game Core thuần, cùng phiên bản, ưu tiên phép tính số nguyên và logic deterministic |
| Mất kết nối | Khôi phục từ snapshot + event log; cần xác định nguồn snapshot đáng tin cậy |
| Giới hạn lệnh | Mục tiêu tối đa 10 hành động/giây/bot; giới hạn mạng có thể ở relay, kiểm tra luật bot nằm ở client nếu relay không giữ game state |
| Chống gian lận | Relay thuần **không xác minh được** kết quả tính toán của client; cần cơ chế đối chiếu hoặc trọng tài nếu thi đấu cạnh tranh nghiêm túc |

### Xung đột cần giải quyết trong thiết kế

- **Tự áp dụng lệnh ngay hay chờ server chốt?** Để tránh mỗi client có một kết quả chính thức khác nhau, lệnh phải được thực thi chính thức theo `effectiveTick`; client có thể hiển thị dự đoán riêng nếu cần phản hồi nhanh.
- **Server không giữ game state:** không thể kiểm tra chắc chắn bot có tồn tại, thuộc đội hay còn sống. Có thể xác thực tài khoản/phòng và chống spam mạng, nhưng không chứng minh tính hợp lệ của mọi hành động.
- **Phân biệt `for` với nhiều `println`:** không thể xác minh qua stdout hoặc lệnh đã chuẩn hóa; client tự báo không đủ tin cậy. Quy tắc thưởng riêng cho `for` chỉ có thể bảo đảm bằng môi trường thực thi/kiểm chứng đáng tin cậy hoặc thay đổi luật chơi.
- **Client không online:** thông điệp và kết quả phải có cơ chế khôi phục; một client bỏ lỡ sự kiện không thể mặc định là trạng thái không thay đổi.

## 4. Phần giữ lại và phần phải thay đổi

| Thành phần | Khả năng tái sử dụng | Ghi chú |
|---|---|---|
| Enum mã lệnh và parser | **Cao** | Định nghĩa schema/protocol chung |
| Model bản đồ, bot, công trình, tài nguyên | **Cao** | Tách khỏi database, network, UI |
| Rule engine di chuyển, khai thác, chiến đấu | **Cao nếu viết Java thuần** | Hàm đầu vào là state + command, đầu ra là state + events |
| Giao diện Phaser / React | **Cao** | Đổi nguồn cập nhật từ server state sang local state |
| Tài khoản, phòng, chia đội | **Khá cao** | Relay vẫn phải quản lý |
| WebSocket server | **Một phần** | Đổi từ broadcast state sang phát event đã sắp thứ tự |
| Lưu state server | **Thay đổi lớn** | Thành event log, checkpoint/snapshot tùy chính sách |
| Xử lý đồng thời, xung đột | **Phải thiết kế lại** | Đồng bộ tick, ordering, rollback/replay |
| Java Runner | **Thành phần mới** | Chạy cục bộ, giới hạn tài nguyên và output; cảnh báo về code không tin cậy |
| Kiểm tra chống gian lận | **Chưa được giải quyết** | Cần tách hạng mục riêng nếu thi đấu nghiêm túc |

## 5. Chuẩn bị từ bây giờ để giảm chi phí chuyển đổi

1. **Game Core Java thuần:** tách model và rules thành module độc lập, không import Spring Boot, WebSocket, JPA hoặc Phaser.
2. **Hàm xử lý deterministic:** ví dụ `apply(GameState state, Command command) -> Result`; tránh dùng `System.currentTimeMillis()`, đồng hồ hệ thống hoặc nguồn random không có seed trong luật game.
3. **Command protocol ổn định:** có version; định nghĩa rõ `CREATE`, `SELECT`, `DELETE`, `MOVE`, `WAIT`, `ATTACK`, `GATHER`, `BUILD`, `TRAIN` và lỗi tương ứng.
4. **Game state có serialize được:** lưu và tải snapshot; state không chứa tài nguyên runtime không thể tái tạo.
5. **Event log ngay từ MVP:** lưu command, thứ tự, kết quả, timestamp (và tick nếu có) để phục vụ replay sau này.
6. **Renderer chỉ nhận state/events:** Phaser không tự quyết định kết quả hợp lệ của lệnh.
7. **Test tái hiện:** một bộ command phải tạo cùng kết quả trên hai lần chạy Game Core.
8. **Không phụ thuộc client `SELECT` làm nguồn chân lý chung:** lựa chọn bot có thể là trạng thái riêng của từng tài khoản; mỗi command gửi kèm bot đích đã xác định.

## 6. Roadmap chuyển đổi sau khi MVP cũ hoàn thành

### M1 — Tách Game Core thành thư viện dùng chung
- Di chuyển toàn bộ luật game khỏi controller/service giao tiếp mạng.
- Đầu vào: state + command; đầu ra: state mới + sự kiện kết quả.
- **Nghiệm thu:** chạy ≥100 lệnh kiểm thử offline và kết quả giống server MVP.

### M2 — Chạy mô phỏng cục bộ trên một client
- Nhúng Game Core vào app Java desktop hoặc chạy Java engine cục bộ cạnh frontend Phaser.
- Tự điều khiển bản đồ không cần server.
- **Nghiệm thu:** cùng command log, client và server MVP xuất cùng state hash.

### M3 — Relay sự kiện giữa hai client
- Relay chỉ sắp thứ tự và phát event; hai client tự tính.
- Ban đầu chọn lệnh ngắn, một trận, không va chạm phức tạp.
- **Nghiệm thu:** 1.000 sự kiện liên tiếp, hai client cho cùng state hash; không mất/lặp lệnh.

### M4 — Tick 24 Hz và xử lý sự kiện đến muộn
- Tick chung, `sequence`, `effectiveTick`; kiểm thử độ trễ giả lập, đảo thứ tự và mất kết nối.
- **Nghiệm thu:** client sau khi replay/sync lại phải khớp state hash; không tự áp dụng nhầm lệnh ở tick khác.

### M5 — Java Runner và command batch cục bộ
- Chạy code Java người chơi ở client; parser đọc stdout, giới hạn số dòng/batch/output.
- **Nghiệm thu:** chương trình hợp lệ tạo lệnh đúng; chương trình lỗi/vòng lặp vô hạn không treo ứng dụng chính.

### M6 — Phòng nhiều người, chia đội, kết nối lại
- Quyền đội, xung đột nhiều tài khoản, log lệnh, reconnect và checkpoint.
- **Nghiệm thu:** hai tài khoản cùng điều khiển một bot vẫn nhận cùng thứ tự lệnh; client nối lại khôi phục đúng state.

### M7 — Kiểm thử tải và xác minh tính tin cậy
- Benchmark 10 đội, số bot mục tiêu, Java Runner trên máy thực tế, mạng giả lập có độ trễ.
- Tùy mức thi đấu: thêm máy trọng tài hoặc replay log trên môi trường đáng tin cậy.
- **Nghiệm thu:** không lệch state trong bài test tải đã quy định, latency và băng thông đạt mục tiêu đo thực tế.

## 7. Ước lượng công sức chuyển đổi (tham khảo, không phải cam kết)

Giả sử **một lập trình viên quen Java, React/Phaser và WebSocket**, chức năng gameplay đã hoạt động ổn định:

| Điều kiện của hệ thống cũ | Mức độ chuyển đổi | Dự toán rất sơ bộ |
|---|---|---|
| Logic Java thuần, protocol rõ, có replay test | Trung bình | Khoảng 2–4 tuần cho bản thử hai client; thêm thời gian hoàn thiện production |
| Logic lẫn sâu Spring Boot, không có snapshot/test | Cao | Khoảng 1–2 tháng hoặc hơn, phụ thuộc quy mô code |
| Yêu cầu đồng bộ ổn định khi mạng trễ, reconnect và chống gian lận | Cao hơn đáng kể | Có thể cần thêm nhiều tuần để thử nghiệm, sửa lỗi và harden |

Các khoảng trên mang tính định hướng, phụ thuộc phạm vi, năng lực đội phát triển và yêu cầu chất lượng. Phần tốn thời gian không phải chuyển Java sang client, mà là **đảm bảo nhiều máy mô phỏng giống nhau trong điều kiện mạng không lý tưởng**.

## 8. Khuyến nghị chốt

- **Không đổi kiến trúc MVP ngay.** Chỉ thêm yêu cầu chuẩn bị chuyển đổi ở phần 5.
- **Đường đi ít rủi ro:** hoàn thành MVP server-authoritative → tách Game Core → chạy offline client → hai client dùng relay → thêm sync/reconnect → Java Runner.
- **Chỉ truyền action/event, không tin kết quả trạng thái client tự khai.** Server không chạy gameplay vẫn có thể đóng vai trò sắp thứ tự sự kiện.
- **Chấp nhận giới hạn tin cậy:** không có trọng tài đáng tin cậy thì không thể bảo đảm tuyệt đối công bằng hoặc chống gian lận trong trận thi đấu.

---

*Tài liệu này là một hướng phát triển kiến trúc sau MVP, không thay đổi luật chơi và danh sách phase đã chốt trước đó.*
