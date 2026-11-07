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

### Cửa sổ 3 — điều khiển quân bằng kịch bản mẫu

```powershell
.\scripts\run-demo.ps1 BlueBot
```

Để kiểm tra lại từ bản đồ ban đầu, hãy khởi động lại máy chủ trước mỗi kịch bản.
Token mẫu của đội xanh là `blue-token`.
Token mẫu của đội đỏ là `red-token`.

## 4. Các khả năng người chơi có thể sử dụng

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

## 5. Cách điều khiển theo từng hành động

Người chơi viết chương trình Java để thực hiện các khả năng dưới đây.
Mỗi hành động thành công làm trạng thái trận đấu thay đổi và được hiển thị trên bản đồ.

### Kết nối vào đội

Chọn địa chỉ máy chủ và mã truy cập của đội muốn điều khiển.

```java
GameClient client = GameClient.connect(
        "ws://localhost:8080/game",
        "blue-token"
);
```

### Chọn quân để điều khiển

Khi không ghi mã quân, hành động dùng quân đang hoạt động đầu tiên của đội.
Khi muốn điều khiển một quân cụ thể, truyền mã quân vào phiên bản hành động có `unitId`.

```java
client.move("worker-blue-2", Direction.RIGHT);
client.harvest("worker-blue-2");
```

### Di chuyển

Mỗi lần gọi chỉ di chuyển một ô.

```java
client.move(Direction.UP);
client.move(Direction.RIGHT);
client.move(Direction.DOWN);
client.move(Direction.LEFT);
```

### Chờ lượt

Chờ lượt giữ nguyên vị trí nhưng vẫn được ghi nhận là một hành động.

```java
client.waitTurn();
client.waitTurn("worker-blue-2");
```

### Tấn công

Truyền mã quân hoặc nhà chính muốn tấn công.

```java
client.attack("worker-red");
client.attack("worker-blue-2", "hq-red");
```

### Tạo quân

Quân mới xuất hiện ở ô kề nhà chính của đội.

```java
client.spawn("worker-blue-2", "Builder", UnitType.WORKER, Direction.DOWN);
client.spawn("ranged-blue-1", "Ranger", UnitType.RANGED, Direction.RIGHT);
```

### Khai thác

Quân phải đứng trong khoảng cách cho phép của điểm tài nguyên.

```java
client.harvest();
client.harvest("worker-blue-2");
```

### Xây dựng

Quân phải đứng cạnh ô muốn xây và đội phải đủ tài nguyên.

```java
client.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);
client.build("worker-blue-2", "wall-blue", BuildingType.WALL, Direction.DOWN);
```

Các hướng hiện có là `UP`, `RIGHT`, `DOWN` và `LEFT`.
Các loại quân hiện có là `WORKER`, `MELEE`, `RANGED` và `SIEGE`.
Các loại công trình hiện có là `BARRACKS`, `TOWER` và `WALL`.

## 6. Kịch bản mẫu để người chơi thử

```powershell
.\scripts\run-demo.ps1 BlueBot
.\scripts\run-demo.ps1 RedBot
.\scripts\run-demo.ps1 WinningScenario
.\scripts\run-demo.ps1 ReinforcementBot
.\scripts\run-demo.ps1 ResourceBuilderBot
```

- [x] `BlueBot`: đưa quân xanh di chuyển trên bản đồ.
- [x] `RedBot`: điều khiển đội đỏ bằng token riêng.
- [x] `WinningScenario`: đưa BLUE tấn công và phá nhà chính RED.
- [x] `ReinforcementBot`: tạo quân mới, chọn quân đó và điều khiển riêng.
- [x] `ResourceBuilderBot`: tới điểm tài nguyên, khai thác và xây BARRACKS.

## 7. Desktop Code Studio

```powershell
.\scripts\start-desktop.ps1
```

- [ ] Nhập máy chủ `ws://localhost:8080/game`.
- [ ] Nhập token `blue-token` hoặc `red-token`.
- [ ] Nhập tên lớp bot và chương trình Java.
- [ ] Nhấn `Compile` hoặc `Ctrl+B` để biên dịch.
- [ ] Nhấn `Run` hoặc `Ctrl+R` để bắt đầu điều khiển trận.
- [ ] Nhấn `Stop` hoặc `Ctrl+K` để dừng chương trình.

## 8. Những khả năng chưa có

- [ ] Công trình tự sản xuất quân hoặc tự phòng thủ.
- [ ] Footprint 2×2.
- [ ] Projectile.
- [ ] Nhịp mô phỏng cố định.
- [ ] Database, tài khoản, lobby, matchmaking và khôi phục sau khi khởi động lại.
- [ ] Nhiều trận cùng lúc và kiểm tra tải quy mô lớn.
- [ ] Kiểm tra hình ảnh trên nhiều trình duyệt hoặc thiết bị.

## 9. Mẫu ghi nhận một lần kiểm thử

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

## 10. Kiểm tra tự động trước khi kiểm tra bằng tay

```powershell
.\scripts\test-all.ps1
```

Không kết luận rằng hình ảnh, thao tác thực tế hoặc khả năng chịu tải đã đạt chỉ dựa trên kiểm tra tự động.
Mỗi phase mới phải cập nhật phần khả năng người chơi, cách điều khiển, kịch bản và mẫu ghi nhận trong tài liệu này.
