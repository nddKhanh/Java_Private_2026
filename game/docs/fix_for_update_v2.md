# Ant Colony Wars — Yêu cầu sửa đổi sau rà soát

**Phạm vi:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game`. Ưu tiên MVP; không mở rộng IDE riêng, plugin IntelliJ, bảo mật thi đấu hoặc bot offline.

## P0 — Đúng luật và không gửi batch lỗi

1. **Spawn theo công trình được chọn:** `bot.select(idCongTrinh)` → `bot.spawn(idQuan, type, dir)`. HQ chỉ tạo `WORKER`; chỉ `BARRACKS` tạo `MELEE`, `RANGED`, `SIEGE`. `TOWER`/`WALL` không được sản xuất quân. `select()` không tính vào giới hạn lệnh. Không dùng tham số `source` trong API học viên.
2. **Giới hạn batch:** Tối đa 10 hành động **mỗi bot trong một lần Run/Submit**; không giới hạn theo giây/tick. Các hành động được kiểm tra tuần tự theo trạng thái sau mỗi lệnh. Atomic: lỗi thì hủy toàn batch; Partial: dừng ở lỗi đầu tiên và giữ các lệnh hợp lệ trước đó.
3. **Batch chưa hoàn chỉnh:** Nếu mã Java gặp exception trong lúc tạo batch thì **không gửi bất kỳ lệnh nào**. Chỉ gửi batch sau khi `Main.main(Bot bot)` kết thúc bình thường; không tự gửi batch còn dở trong `close()` khi xảy ra lỗi.
4. **Va chạm và đường đạn:** Đường bay Bresenham được xác định cố định từ vị trí bắn đến tọa độ mục tiêu lúc khai hỏa; không tự đuổi theo mục tiêu. Đạn có thể trượt, bị công trình/HQ chặn, hoặc va chạm với đơn vị trên đường bay. Công trình nhận sát thương, HP bằng 0 thì biến mất.

## P1 — API và thao tác từ IntelliJ

5. **API học viên:** `bot.select(id)`, `bot.go(dir)`, `bot.hit(dir)`, `bot.mine()`, `bot.build(id, type, dir)`, `bot.spawn(id, type, dir)`, `bot.stay()`, `bot.state()`. Hướng 1/2/3/4 = lên/phải/xuống/trái. `bot.state()` trả snapshot đầu batch, không cập nhật theo từng hành động và không tính hạn mức.
6. **Một nút Run trong IntelliJ:** Project mẫu đọc key trận từ `.env`; bấm Run trên IDE sẽ tự gửi source sang Desktop, Desktop tự biên dịch/chạy, gửi batch đến server và trả kết quả. Không yêu cầu người học bấm thêm Run trên Desktop hay mở web.
7. **Log ngay trong IntelliJ:** Console IDE hiển thị trạng thái gửi/chạy và lỗi biên dịch, lỗi Java, lỗi hành động do server trả về. Thông báo tiếng Việt, ngắn gọn, chỉ rõ lệnh/bot và nguyên nhân; kèm file/dòng nếu xác định được. **Không gợi ý cách sửa.** Desktop có thể giữ log phụ, nhưng không phải nơi bắt buộc học viên xem.

## P2 — Cân bằng chiến đấu và kiểm thử

8. **Tháp canh:** Tự bắn mục tiêu địch trong phạm vi 5 ô; **mỗi tháp tối đa 1 phát/giây**, không bắn 1 phát mỗi tick.
9. **Kiểm thử bắt buộc:** HQ/BARRACKS/TOWER/WALL spawn; giới hạn 10 hành động/bot; Atomic/Partial; exception giữa batch không làm thay đổi trận; đường đạn và vật cản; cooldown tháp; một lần Run IntelliJ gửi lệnh và nhận log lỗi từ server.

**Tiêu chí hoàn thành:** Học viên chỉ cần mở project mẫu trong IntelliJ, điền key, viết `Main.main(Bot bot)` và bấm Run. Lệnh hợp lệ được thực thi đúng luật; mọi lỗi được trả về console IDE bằng tiếng Việt; chương trình Java thất bại không gửi batch dở.
