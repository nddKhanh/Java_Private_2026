# Ant Colony Wars — Tổng hợp yêu cầu cải tiến

**Phạm vi:** Ưu tiên bản thử nghiệm, đơn giản và đủ để học viên lập trình Java trong IntelliJ IDEA, gửi lệnh qua Desktop và chơi trực tuyến. Chưa mở rộng bảo mật hay cơ chế bot offline.

## 1. Giới hạn lệnh và đồng bộ

- Mỗi lần bấm **Submit** gửi một **batch**; tối đa **10 lệnh hành động/bot/batch**. Nhiều bot trong một batch được tính độc lập.
- `bot.select(id)` chỉ chọn bot, **không** tính vào hạn mức.
- **Không giới hạn lệnh theo giây hoặc theo tick**. Tick chỉ phục vụ cập nhật/đồng bộ trạng thái hệ thống, không quyết định hạn mức lệnh.
- Server kiểm tra và thực thi các lệnh theo thứ tự; lệnh vượt hạn mức được coi là không hợp lệ và xử lý theo chế độ ở mục 5.

## 2. Luật công trình và chiến đấu

- **HQ** chỉ sản xuất `WORKER`; **BARRACKS** sản xuất `MELEE`, `RANGED`, `SIEGE`.
- Công trình chặn đường đạn, nhận sát thương, giảm HP; **HP = 0** thì bị phá hủy và biến mất khỏi bản đồ.
- **TOWER** tự động tấn công kẻ địch trong phạm vi **5 ô**.
- Đạn hướng đến **tọa độ của mục tiêu tại thời điểm khai hỏa**, không đuổi theo mục tiêu; có thể bắn trượt hoặc bị công trình chặn.
- Dùng **Bresenham** để xác định các ô trên đường bay thẳng của đạn; đạn **không tìm đường vòng** qua công trình.

## 3. Lập trình bằng IntelliJ, gửi qua Desktop

- Cung cấp **project Java mẫu** có code base/SDK để học viên dùng trực tiếp trong IntelliJ IDEA, không cần xây IDE riêng hoặc plugin IntelliJ.
- Học viên nhập **key trận đấu** trong `.env`, viết chiến thuật tại phương thức `Main.main(Bot bot)` (**không phải** hàm `public static void main(String[] args)`).
- Class/launcher có sẵn phụ trách gửi **mã nguồn** sang ứng dụng **Desktop**; Desktop biên dịch, chuyển lời gọi SDK thành lệnh game và gửi batch đến **server**.
- Không cần gửi mã Java lên server để biên dịch.

## 4. API điều khiển rút gọn

| API học viên | Ý nghĩa |
|---|---|
| `bot.select(id)` | Chọn bot |
| `bot.go(1/2/3/4)` | Di chuyển **lên / phải / xuống / trái** |
| `bot.hit(1/2/3/4)` | Tấn công theo hướng tương ứng |
| `bot.mine()` | Khai thác tài nguyên |
| `bot.build(id, type, dir)` | Xây công trình |
| `bot.spawn(source, type, id, dir)` | Tạo quân; `source` là **ID của HQ hoặc doanh trại** sản xuất quân |
| `bot.wait()` | Đứng yên |
| `bot.state()` | Đọc **snapshot trận đấu hiện tại**: bản đồ, vị trí/HP đơn vị và công trình, tài nguyên |

API ngắn là lớp gọi thuận tiện; **Desktop chuyển thành command tương ứng**. `bot.state()` chỉ đọc snapshot đã nhận khi lập batch, **không cập nhật sau từng lệnh trong cùng batch** và không tính vào hạn mức 10 lệnh. Có thể giữ API đầy đủ hiện tại để tương thích.

## 5. Kiểm tra lệnh và hiển thị lỗi

- **IntelliJ** hỗ trợ phát hiện lỗi cú pháp/kiểu dữ liệu Java. **Desktop** vẫn biên dịch lại để xác nhận, sau đó kiểm tra tính hợp lệ của hành động theo luật chơi.
- Desktop hiển thị thông báo lỗi **ngắn gọn bằng tiếng Việt**: **lệnh/bot gặp lỗi và nguyên nhân cụ thể**; kèm **file/dòng code** nếu xác định được. **Không đưa gợi ý khắc phục**. Lỗi trả về từ server cũng được hiển thị theo cách này.
- Chuyển sang **hai chế độ bật/tắt trên Desktop**:
  - **Atomic:** Có bất kỳ lệnh không hợp lệ → từ chối cả batch, không thay đổi trạng thái.
  - **Partial:** Thực hiện các lệnh hợp lệ theo thứ tự, **dừng ở lỗi đầu tiên**, giữ kết quả trước lỗi.
- Validate theo **trạng thái sau mỗi lệnh**, không dùng một snapshot cố định cho toàn bộ batch. Server kiểm tra lại trước khi cập nhật trạng thái thật.
- **Phân biệt:** Lỗi biên dịch/exception Java làm chương trình không tạo được batch hợp lệ; chế độ Partial chỉ áp dụng với **lỗi hành động game trong batch đã tạo thành công**.

## 6. Thứ tự ưu tiên

1. **P0:** Batch tối đa 10 lệnh/bot/lần gửi; Atomic/Partial; mã lỗi tiếng Việt.
2. **P1:** Project mẫu IntelliJ → Desktop → Server; API lệnh rút gọn.
3. **P2:** HQ/doanh trại, công trình chịu sát thương, đường đạn cố định, tháp tự động bắn.

**Ngoài phạm vi hiện tại:** tăng cường xác thực dành cho môi trường thi đấu thật, bot chạy offline/vòng đời phức tạp, phát triển trình soạn thảo thay IntelliJ.
