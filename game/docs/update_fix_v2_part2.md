> **Prompt cho agent:** Chỉ xử lý đúng các mục chưa đạt trong tài liệu; đọc đúng file/hàm liên quan trước khi sửa, ưu tiên tìm kiếm có mục tiêu thay vì quét toàn repo; sửa tối thiểu, tái sử dụng code hiện có, chạy test trực tiếp liên quan rồi báo ngắn gọn file đã sửa, kết quả và vấn đề còn lại; không phân tích dài, không mở rộng phạm vi hoặc chạy lệnh lặp nếu không có lỗi mới.

# Ant Colony Wars — Các vấn đề còn cần sửa

**Phạm vi:** `nddKhanh/Java_Private_2026`, nhánh `develop`, thư mục `game`. Tổng hợp từ lần rà soát source gần nhất và các quyết định mới. Chỉ liệt kê vấn đề còn tồn tại; không lặp lại các chức năng đã sửa.

## P0 — Đúng luật xử lý batch

### 1. Partial khi một lệnh không chuyển đổi được
- **Hiện trạng:** `GameWebSocketHandler.handleBatch()` chuyển đổi toàn bộ lệnh trước khi gọi `applyBatch()`. Nếu một lệnh không hợp lệ ngay ở bước chuyển đổi, server trả `INVALID_COMMAND` cho cả batch, không thực hiện các lệnh trước đó dù chọn Partial.
- **Yêu cầu:** Với batch có cấu trúc hợp lệ, xử lý lệnh theo thứ tự. **Atomic**: một lệnh lỗi thì không áp dụng hành động nào. **Partial**: áp dụng các lệnh hợp lệ trước lỗi đầu tiên, dừng tại lỗi, trả `failedIndex`, `appliedCount`, mã lỗi và chủ thể gây lỗi. Phân biệt lỗi cấu trúc request khiến không thể đọc batch với lỗi của một hành động cụ thể.
- **Kiểm chứng:** Batch 4 lệnh, lệnh thứ 3 có hướng sai: Atomic áp dụng 0; Partial áp dụng đúng 2; lệnh thứ 4 không chạy. Kiểm tra cả lỗi chuyển đổi dữ liệu và lỗi luật chơi.

## P1 — Luật chiến đấu, giới hạn lệnh và lỗi

### 2. Đạn xuyên qua đồng minh, không gây sát thương
- **Hiện trạng:** `GameEngine.applyTick()` xử lý va chạm với đơn vị/công trình/HQ trên đường đạn mà chưa loại trừ đối tượng cùng phe.
- **Yêu cầu:** Đạn **không gây sát thương và không bị chặn** bởi bất kỳ đơn vị, HQ hoặc công trình đồng minh nào (tính theo quan hệ phe/đồng minh hiện có). Đạn tiếp tục trên quỹ đạo Bresenham đã cố định; gặp đối tượng địch thì gây sát thương và dừng theo luật va chạm hiện hành. Không thay đổi cơ chế đạn có thể trượt mục tiêu.
- **Kiểm chứng:** Đạn xuyên qua lính, tường, tháp, HQ đồng minh mà không giảm HP; vẫn trúng mục tiêu địch phía sau. Kiểm tra đồng minh khác đội nhưng cùng phe, nếu game hỗ trợ liên minh.

### 3. Công trình spawn cũng chịu giới hạn request
- **Quy tắc đã chốt:** Tối đa **10 hành động trên mỗi đối tượng được điều khiển trong một batch**, gồm cả đơn vị và công trình. `bot.select(id)` và `bot.state()` không tính. `bot.spawn(idQuan, type, dir)` được tính cho **công trình đã chọn**; không tính cho quân mới sinh.
- **Yêu cầu:** Server đếm theo ID chủ thể thực hiện lệnh, chống vượt hạn mức khi đổi qua lại giữa các đối tượng. Không áp đặt lại giới hạn theo giây/tick. Atomic/Partial xử lý `BATCH_LIMIT_EXCEEDED` đúng theo quy tắc ở mục 1.
- **Kiểm chứng:** Một Barracks spawn 11 lần trong cùng batch → lệnh thứ 11 vượt hạn mức; đổi sang Barracks khác có bộ đếm riêng. Kết hợp lệnh lính và công trình không cộng nhầm. Trường hợp tài nguyên/ô trống không đủ phải được thiết lập test phù hợp để kiểm tra riêng hạn mức.

### 4. Thông báo lỗi phải chỉ rõ chủ thể
- **Hiện trạng:** Một số lỗi như `OUT_OF_BOUNDS` chỉ có thông báo chung, chưa biết quân/công trình nào gây ra.
- **Yêu cầu:** Lỗi hành động từ server trả về console IntelliJ, ngắn gọn bằng **tiếng Việt**, tối thiểu có **số thứ tự lệnh, ID chủ thể, hành động, nguyên nhân**; kèm hướng/vị trí hoặc ID liên quan khi xác định được. Với spawn, chủ thể là **ID công trình đã chọn**. Lỗi Java/biên dịch kèm file và dòng nếu xác định được. Không đưa gợi ý sửa lỗi.
- **Ví dụ:** `Lệnh 4 — worker-01 — di chuyển trái: vượt biên bản đồ.`; `Lệnh 11 — barracks-01 — spawn: vượt giới hạn 10 hành động/batch.`
- **Kiểm chứng:** Lỗi di chuyển, spawn, build, attack, vượt hạn mức và Partial đều hiện đúng chủ thể trong console IDE; không chỉ hiển thị mã lỗi hoặc thông báo chung.

## P1 — Giao diện trận đấu

### 5. Phân biệt hình dạng các loại đơn vị và công trình
- **Hiện trạng cần cải thiện:** Các đối tượng đang khó phân biệt vì biểu tượng/hình dạng quá giống nhau.
- **Yêu cầu:** Mỗi loại **WORKER, MELEE, RANGED, SIEGE** có hình dạng hoặc silhouette riêng, nhìn vào nhận biết được loại mà không cần bấm chọn. **HQ, BARRACKS, TOWER, WALL** cũng có hình dạng riêng. Giữ màu/đánh dấu phe để phân biệt đội; không chỉ dùng màu để phân biệt loại. Có thể dùng sprite/icon đơn giản, ưu tiên dễ đọc trên bản đồ nhỏ.
- **ID hiển thị:** Trên hoặc sát biểu tượng **mọi đơn vị và công trình** luôn có nhãn ID thực tế của đối tượng (không thay bằng tên loại). Nhãn cần đủ tương phản, không che hoàn toàn biểu tượng; nếu ID dài thì có cơ chế hiển thị đầy đủ khi hover/chọn, nhưng vẫn nhận diện được đối tượng trên bản đồ.
- **Kiểm chứng:** Đặt đủ 4 loại đơn vị và 4 loại công trình cạnh nhau; nhận biết được từng loại và ID, kể cả khi thuộc cùng phe. Kiểm tra nhãn khi đối tượng di chuyển, bị tiêu diệt, sinh mới và khi bản đồ đông.

## P2 — Kiểm thử tích hợp bắt buộc

### 6. E2E IntelliJ → Desktop → Server → console IntelliJ
- **Yêu cầu:** Mở project mẫu, điền `MATCH_KEY`, bấm **Run** một lần trong IntelliJ: source tự gửi sang Desktop, biên dịch/chạy, gửi batch lên server, kết quả/lỗi trả về **console IntelliJ**; không cần bấm Run thêm ở Desktop hay mở web.
- **Kịch bản kiểm thử:** Batch hợp lệ; Atomic lỗi giữa batch; Partial lỗi giữa batch (bao gồm lỗi chuyển đổi lệnh); chương trình Java ném exception trước khi hoàn tất batch (không gửi lệnh dở); lỗi spawn từ công trình; đạn xuyên đồng minh; cooldown tháp 1 phát/giây; lỗi hiển thị ID chủ thể.
- **Lưu ý:** Cần thực sự chạy test và một trận mẫu; chỉ đọc source chưa đủ kết luận E2E đã hoạt động.

## Tiêu chí nghiệm thu

- Partial/Atomic nhất quán cả khi lệnh lỗi ở bước chuyển đổi và khi vi phạm luật chơi.
- Đạn không gây sát thương hoặc bị chặn bởi đồng minh; vẫn va chạm với địch.
- Mỗi đơn vị **và công trình** tối đa 10 hành động/batch; `select`/`state` không tính.
- Mọi lỗi hành động chỉ rõ **ID chủ thể** trên console IntelliJ.
- Các loại đơn vị/công trình có hình dạng khác nhau và hiển thị ID trên bản đồ.
- Chạy thành công quy trình một nút Run từ IntelliJ đến server và nhận kết quả về IDE.

**Không nằm trong phạm vi sửa lần này:** các tính năng đã xác nhận có trong source như `bot.stay()`, API `select` → `spawn`, cooldown tháp, lưu quỹ đạo Bresenham cố định, và hủy batch dở khi Java gặp exception — trừ khi test thực tế phát hiện hồi quy.
