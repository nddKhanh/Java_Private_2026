# ANT COLONY WARS — Lộ trình triển khai theo phase và điều kiện nghiệm thu (v0.1)

> **Tài liệu nguồn:** `Ant_Colony_Wars_MVP_v03_REUPLOAD.md` (game design v0.3, 07/11/2026).  
> **Mục đích:** Đi từ một phép đổi trạng thái rất nhỏ → game điều khiển lệnh số → MVP hai người online → mở dần cơ chế lập trình, phối hợp và luật chiến đấu.  
> **Nguyên tắc:** Không làm trước những thứ không cần để chứng minh phase hiện tại chạy được. Giá trị HP, sát thương, chi phí... chỉ là hằng số thử nghiệm, **không phải quyết định cân bằng**.

## 0. Ranh giới và quy ước chung

- **Đích gameplay:** 10 đội chia 2 phe (5 đội/phe), tối đa 5 tài khoản điều khiển chung/đội; tối đa 50 đơn vị/đội; 4 loại đơn vị (`WORKER`, `MELEE`, `RANGED`, `SIEGE`), các công trình `HQ`, `BARRACKS`, `TOWER`, `WALL`.
- **MVP:** 2 đội đối địch, mỗi đội 1 tài khoản; map vuông 2D; chọn bot; gửi **một lệnh số/một lần**; tạo/xóa bot, 4 hướng, chờ, thợ khai thác/xây, tấn công, phá HQ; 2 trình duyệt đồng bộ trạng thái.
- **Chưa bắt buộc cho MVP:** Python/`print`/`for`; giới hạn 10 hành động/giây/bot; 5 người/đội; 10 đội thực chiến; tầm bắn, bay đạn; giới hạn map, va chạm, một đơn vị/ô; di chuyển tích lũy; chi phí/cân bằng; spawn queue.
- **Giản lược chủ ý của MVP:** mọi đơn vị MOVE 1 ô/lệnh, kể cả khi đổi hướng; có thể đi xuyên nhau, xuyên tường và ra ngoài map; ATTACK giảm HP mục tiêu tức thời dù ở bất kỳ khoảng cách nào, không xét vật cản; tài nguyên là vô hạn.
- **Lệnh số không đổi qua các phase:** `1 <type> <name>` tạo bot; `2 <name>` xóa; `3` lên; `4` xuống; `5` trái; `6` phải; `7` đứng im; `8` khai thác; `9 <target>` tấn công; `10 <type> <x> <y>` xây. `11` xuất quân qua nhà lính và `12` xem trạng thái là bước sau. Bot đích của các mã `3–10` được chọn ở UI và gửi kèm bằng `unit_name` trong request, **không** dùng lựa chọn bot toàn cục ở server.
- **Quy tắc dữ liệu:** (x,y), x tăng sang phải, y tăng xuống dưới; tên bot duy nhất trong **đội**; server kiểm soát trạng thái, trình duyệt chỉ hiển thị.
- **Mỗi phase phải có:** code chạy được, test tự động cho logic vừa thêm, ít nhất 1 bài test thủ công có thể lặp lại, log/mã lỗi rõ, và demo kết quả trước khi chuyển phase.

## 1. Cách tổ chức nghiệm thu

Mỗi phase có 4 tiêu chí độc lập:
1. **Đầu ra:** có artifact chạy được (logic, API hoặc UI tùy phase).
2. **Test pass:** các ca Given/When/Then bên dưới đạt; test của phase trước vẫn pass.
3. **Không thêm phạm vi ngoài:** những gì chưa thuộc phase được đánh dấu TODO/backlog, không cản nghiệm thu.
4. **Có bằng chứng:** log, ảnh chụp hoặc video ngắn/demo script, và bộ test tự động (sau khi có test framework).

Đề xuất thư mục: `docs/roadmap.md`, `docs/rules.md`, `docs/commands.md`, `tests/`, `scripts/demo/`. Trạng thái phase: `TODO → DOING → BLOCKED / VERIFIED`; chỉ `VERIFIED` mới mở phase tiếp theo. Các phase từ R1 trở đi có thể đổi thứ tự nếu không phá phụ thuộc.

---

# A. TỪ CON SỐ 0 ĐẾN MVP

## Phase 00 — Một trạng thái và một hàm di chuyển (không UI, không mạng)

**Mục tiêu:** Chứng minh logic trò chơi có thể là `state + command → new_state`.

**Triển khai:** Một bot mẫu ở `(2,2)`; một hàm nhận mã `3/4/5/6/7`, cập nhật tọa độ/hướng, trả kết quả.

**Nghiệm thu:**
- [ ] Given bot `(2,2)`, when `6`, then bot `(3,2)`.
- [ ] Given bot `(2,2)`, when `3`, then bot `(2,1)`.
- [ ] Given bot `(2,2)`, when `7`, then vị trí giữ nguyên và trả `OK`.
- [ ] Given mã `99`, then trả `UNKNOWN_COMMAND`, state không đổi.
- [ ] Chạy lại chuỗi `6,6,4,7` cho kết quả giống hệt ở mọi lần.

**Chưa làm:** map, tài khoản, frontend, database, AI.

## Phase 01 — Map ma trận và trạng thái game trong bộ nhớ

**Mục tiêu:** Có thế giới 2D tối giản mà không phụ thuộc đồ họa.

**Triển khai:** Map W×H cấu hình; ít nhất ô trống/tài nguyên; lớp `units` tách khỏi lớp `tiles`; render ASCII/JSON để debug. Gắn bot vào state.

**Nghiệm thu:**
- [ ] Tạo được map 8×8 và 20×12 bằng cùng một bộ code.
- [ ] Đặt được ô tài nguyên tại `(3,4)`; bot có thể đồng thời đứng trên ô đó.
- [ ] MOVE cập nhật tọa độ trong state, không chỉ thay đổi ký tự ở frontend.
- [ ] Bot có thể đi ra tọa độ âm/vượt W/H mà engine không crash; log vị trí ngoài map.
- [ ] Hai bot có thể cùng tọa độ trong bản thử nghiệm mà không crash.

## Phase 02 — API gửi đúng một lệnh số

**Mục tiêu:** Client có thể tác động lên state qua HTTP, không cần WebSocket.

**Triển khai:** `POST /matches/{matchId}/commands` với `{team_id, unit_name, command_text}`; tạm xác thực đội bằng dữ liệu thử nghiệm; `GET /matches/{matchId}/state`; parser chỉ cho **1 dòng, 1 lệnh**, có kiểm tra tham số.

**Nghiệm thu:**
- [ ] `command_text="6"` trả `OK` và GET state thấy tọa độ tăng x +1.
- [ ] `"6\n6"` bị từ chối `MULTIPLE_COMMANDS_NOT_ALLOWED`, không di chuyển.
- [ ] `"abc"` hoặc `"13"` trả lỗi xác định, không 500.
- [ ] Sai loại tham số hoặc thiếu tham số trả `INVALID_ARGUMENTS`.
- [ ] Hai request liên tiếp được xử lý theo thứ tự được server chấp nhận; có `event_id`/`revision` tăng dần để đối chiếu.

**Lưu ý:** `team_id` trong request chỉ là scaffolding thử nghiệm; phase tài khoản phải lấy quyền đội từ session/token, không tin client tự khai đội.

## Phase 03 — Màn hình 2D và nút Gửi lệnh

**Mục tiêu:** Demo được việc nhập số → bot di chuyển ngay trên giao diện.

**Triển khai:** Canvas hoặc tile DOM; map bên trái; khung nhập mã lệnh, nút Gửi, trạng thái OK/ERR bên phải; cập nhật bằng GET state sau mỗi lần gửi.

**Nghiệm thu:**
- [ ] Mở web thấy map, bot và tọa độ/hướng.
- [ ] Nhập `6`, nhấn Gửi: sprite đi sang phải 1 ô; `7`: đứng im và log OK.
- [ ] Nhập lệnh sai: hiện lỗi, vị trí không đổi.
- [ ] Không gửi được hai dòng trong một lượt gửi.
- [ ] UI hiển thị bot đã đi ngoài map trong danh sách/log, không che giấu việc engine đã xử lý.

## Phase 04 — Đội/phe và quyền sở hữu tối thiểu

**Mục tiêu:** Mỗi lệnh có chủ sở hữu; sẵn sàng cho hai phe chơi online.

**Triển khai:** Match, team, faction A/B; 2 tài khoản demo (hoặc định danh người chơi đơn giản), mỗi tài khoản thuộc 1 đội trong trận; HQ mỗi đội được đặt trước; lấy quyền từ session/server.

**Nghiệm thu:**
- [ ] Hai tài khoản thuộc hai đội, hai HQ có đúng chủ sở hữu.
- [ ] Tài khoản A gửi lệnh tới bot đội B: `FORBIDDEN_UNIT`.
- [ ] Người chơi không thể sửa `team_id` trong request để điều khiển đội kia.
- [ ] Reload trang không làm mất quan hệ tài khoản–đội trong vòng đời trận.
- [ ] Không khóa logic thành chỉ có đúng hai team trong schema.

## Phase 05 — Tạo, chọn, đặt tên, xóa bot

**Mục tiêu:** Điều khiển nhiều bot riêng biệt bằng tên dễ nhớ.

**Triển khai:** `1 <type> <name>` tạo tại HQ (có thể chồng ô); dropdown chọn bot; `2 <name>` xóa bot; loại `WORKER/MELEE/RANGED/SIEGE` có thể được tạo để thử nghiệm; tối đa 50/đội nếu đã dễ kiểm tra.

**Nghiệm thu:**
- [ ] `1 worker ant01` tạo bot tên `ant01` đúng đội, xuất hiện gần HQ.
- [ ] Chọn `ant01` rồi `6` chỉ di chuyển `ant01`, bot khác đứng yên.
- [ ] Tạo trùng tên trong cùng đội → `DUPLICATE_UNIT_NAME`; đội khác vẫn được đặt cùng tên.
- [ ] `2 ant01` xóa đúng bot; chọn bot đã xóa để MOVE → `UNKNOWN_UNIT`.
- [ ] Khi đã có 50 bot, lần tạo thứ 51 bị từ chối nếu giới hạn đã bật; việc này có thể hoàn thành ở Phase 11 nếu muốn giữ phase nhỏ.

## Phase 06 — Ô tài nguyên và thợ khai thác

**Mục tiêu:** Có vòng lặp gameplay đầu tiên: tìm ô tài nguyên → tới ô → khai thác.

**Triển khai:** FOOD/WOOD/STONE là ô tài nguyên trữ lượng vô hạn. Chỉ thợ đứng **trên** ô mới dùng `8` được; một lệnh hợp lệ cộng lượng tài nguyên thử nghiệm cố định vào ví đội.

**Nghiệm thu:**
- [ ] Thợ trên ô tài nguyên gửi `8` → tài nguyên đội tăng.
- [ ] Thợ không đứng trên ô tài nguyên gửi `8` → `NOT_ON_RESOURCE`.
- [ ] Lính gửi `8` → `WORKER_REQUIRED`.
- [ ] Khai thác nhiều lần không làm cạn ô tài nguyên.
- [ ] Số liệu tài nguyên hiển thị nhất quán sau refresh.

## Phase 07 — Thợ xây công trình bằng một lệnh

**Mục tiêu:** Người chơi có thể xây xong công trình ngay bằng lệnh số.

**Triển khai:** `10 <type> <x> <y>`; chỉ WORKER xây; hỗ trợ BARRACKS, TOWER, WALL; HQ tạo sẵn, không xây lại; lưu building owner/HP/position. Không kiểm tra diện tích, va chạm hoặc giới hạn map.

**Nghiệm thu:**
- [ ] Thợ dùng `10 wall 5 5` → xuất hiện tường tại `(5,5)` ngay.
- [ ] MELEE dùng lệnh xây → `WORKER_REQUIRED`, không có công trình mới.
- [ ] Không cho xây HQ qua lệnh số.
- [ ] Cho phép xây ở ô có đơn vị hoặc ngoài bản đồ mà server không crash (giản lược MVP).
- [ ] Tạo lại state cho client vẫn thấy công trình đã xây.

## Phase 08 — Tấn công, HP, phá HQ và kết thúc trận

**Mục tiêu:** Có vòng đời chiến đấu hoàn chỉnh tối thiểu.

**Triển khai:** `9 <target>`; mục tiêu là bot/công trình đội đối địch còn sống; trừ HP ngay, bỏ tầm đánh và vật cản; HP về 0 → đối tượng bị phá; HQ về 0 → đội bị loại; phe không còn HQ → thua.

**Nghiệm thu:**
- [ ] Đánh mục tiêu địch hợp lệ → HP giảm đúng giá trị test.
- [ ] Đánh mục tiêu không tồn tại hoặc đã chết → `INVALID_TARGET`.
- [ ] Mặc định không tấn công đơn vị cùng phe.
- [ ] Hai tường chắn giữa hai bot **không** ngăn đòn đánh MVP; mục tiêu ở xa vẫn bị đánh.
- [ ] Phá HQ đối phương → trạng thái đội `ELIMINATED`; nếu phe kia không còn HQ → trận `FINISHED` với phe thắng đúng.
- [ ] Sau FINISHED, lệnh thay đổi trạng thái bị từ chối `MATCH_FINISHED`.

## Phase 09 — Hai người online thấy cùng trận

**Mục tiêu:** Hoàn thiện pipeline online thực sự, không chỉ demo trên một tab.

**Triển khai:** Hai client với hai phiên tài khoản khác nhau; đồng bộ state qua polling (đủ cho MVP) hoặc WebSocket nếu đã sẵn; thông báo lỗi và event/revision; không để client tự quyết định kết quả.

**Nghiệm thu:**
- [ ] Hai trình duyệt/phiên khác nhau nhìn thấy cùng map và HQ.
- [ ] A di chuyển bot, B thấy thay đổi mà không refresh thủ công.
- [ ] B khai thác/xây/tấn công, A thấy tài nguyên/công trình/HP cập nhật.
- [ ] Hai người gửi gần như cùng lúc → server xử lý nhất quán, không mất dữ liệu hoặc tạo trạng thái lỗi.
- [ ] Mất kết nối rồi vào lại → đọc được state hiện tại; không reset trận.

## Phase 10 — Giao diện MVP dùng được cho người mới

**Mục tiêu:** Có thể chơi mà không cần Postman hay kiến thức backend.

**Triển khai:** Danh sách bot, chọn bot, thanh tài nguyên, HP đơn vị/HQ, bảng mã 1–10, kết quả lệnh, thông báo thắng thua. Tile/sprite placeholder được chấp nhận.

**Nghiệm thu:**
- [ ] Một người chưa biết API có thể tạo bot, chọn bot, di chuyển, khai thác, xây và tấn công qua UI.
- [ ] Mỗi click Gửi chỉ sinh một lệnh; không có batch, Python hay `for`.
- [ ] Sai cú pháp hiển thị hướng sửa ngắn gọn.
- [ ] Không thể vô tình chọn bot đội địch để điều khiển.
- [ ] Mọi chức năng phase 00–09 vẫn chạy được.

## Phase 11 — Đóng gói và kiểm thử hồi quy MVP

**Mục tiêu:** Chốt bản MVP chạy được với quy trình dựng lại rõ ràng.

**Triển khai:** Script khởi động local/server, seed map và 2 đội, cấu hình W/H, chạy test tự động; quy định trần 50 bot/đội; chặn command ngoài quyền; lưu tạm state trong RAM được phép nếu có thể tái tạo trận demo (lưu DB bền vững thuộc hậu MVP nếu chưa cần).

**Nghiệm thu cuối MVP (bắt buộc tất cả):**
- [ ] Khởi động từ repo mới theo README không cần sửa code nguồn.
- [ ] Hai tài khoản / hai đội / hai phe vào cùng một trận.
- [ ] Tạo/đặt tên/chọn/xóa bot; 4 hướng/WAIT đều chạy bằng **một lệnh số mỗi lần gửi**.
- [ ] Thợ đứng trên tài nguyên khai thác vô hạn; thợ xây BARRACKS/TOWER/WALL một lệnh.
- [ ] Bot tấn công, giảm HP, phá HQ, hệ thống trả phe thắng.
- [ ] Hai trình duyệt thấy state cập nhật và lỗi rõ ràng.
- [ ] 50 bot/đội được hỗ trợ tối thiểu về lưu trữ/chọn/gửi lệnh; không phải bảo đảm 50 bot đều di chuyển cùng lúc.
- [ ] Kiểm thử hồi quy cho mọi mã lệnh MVP; không crash ở các tình huống giản lược (ra ngoài map, chồng ô, đánh xuyên).

**Định nghĩa hoàn thành MVP:** chứng minh chu trình **nhập lệnh số → server xác thực & xử lý → state đổi → hai client cùng nhìn thấy → trận kết thúc**. **Không** dùng các mục sau làm blocker: Python/for, rate limiting 10/s, va chạm, đồ họa đẹp, cân bằng chi phí, 10 đội / 5 người một đội.

---

# B. SAU MVP: HOÀN THIỆN CƠ CHẾ CHƠI THEO TỪNG LÁT CẮT

## Phase R1 — Chặn biên map, phân lớp vật thể, một bot/ô

**Triển khai:** chặn di chuyển ra ngoài map; mỗi ô tối đa một đơn vị; ô tài nguyên có thể có một đơn vị đứng để khai thác; vật cản/công trình chặn đường. Với xe công thành, tạm chưa áp dụng footprint 2×2 cho đến R2.

**Nghiệm thu:**
- [ ] MOVE ra ngoài trả `OUT_OF_BOUNDS` và giữ nguyên vị trí.
- [ ] Hai đơn vị không cùng đứng một ô, nhưng một đơn vị được đứng trên ô tài nguyên.
- [ ] Tường chặn đường, không đi xuyên.
- [ ] Lệnh thất bại không thay đổi trạng thái.

## Phase R2 — Footprint và vị trí xây/xuất quân hợp lệ

**Triển khai:** xe công thành chiếm 2×2; công trình kiểm tra ô trống; thợ xây tại vị trí hợp lệ; nhà xuất quân tạo quân ở ô trống kề; thiếu chỗ → `SPAWN_QUEUED`, giữ lệnh chờ (và giữ slot dân số, chi phí khi có kinh tế).

**Nghiệm thu:**
- [ ] Xe không xuất hiện/đi qua nơi không đủ 2×2 ô.
- [ ] Tường/công trình không chồng lên vật chiếm chỗ.
- [ ] Không có ô spawn trống → lệnh nằm trong queue, chưa sinh đơn vị.
- [ ] Sau khi giải phóng chỗ → đơn vị sinh đúng một lần.
- [ ] Phá công trình/hủy yêu cầu → queue không bị treo hay nhân bản quân.

## Phase R3 — Hướng quay và di chuyển tích lũy theo loại quân

**Triển khai:** giữ `facing` + `move_count`, không tạo trường `move_direction`; MOVE đổi hướng sẽ quay mặt và tính là lệnh tích lũy đầu; MELEE: 2 lệnh/ô, RANGED và WORKER: 1 lệnh/ô, SIEGE: 5 lệnh/ô. WAIT không đổi hướng/vị trí.

**Nghiệm thu:**
- [ ] MELEE gửi 1 MOVE hướng mới: mới quay mặt/chưa đi; MOVE lần 2 cùng hướng: đi 1 ô.
- [ ] RANGED/WORKER gửi 1 MOVE: đi 1 ô.
- [ ] SIEGE gửi 4 MOVE cùng hướng: chưa đi; lần 5: đi 1 ô.
- [ ] Đổi hướng giữa chừng đặt lại tiến độ, không giữ lệnh cũ.
- [ ] WAIT không làm di chuyển, không xóa tiến độ theo quy tắc hiện tại.

## Phase R4 — Tầm tấn công và các loại quân

**Triển khai:** tầm Manhattan: MELEE 1, RANGED 3, SIEGE 7; TOWER 5 (chọn kích hoạt tháp ở bước sau nếu chưa quyết); chặn mục tiêu ngoài tầm; phân loại HP/damage bằng cấu hình tạm.

**Nghiệm thu:**
- [ ] MELEE cách 1 đánh được, cách 2 không được.
- [ ] RANGED cách 3 đánh được, cách 4 không được.
- [ ] SIEGE cách 7 đánh được, cách 8 không được.
- [ ] Mục tiêu ngoài tầm không mất HP.

## Phase R5 — Đạn bay, vật chắn và xuyên vật thể

**Triển khai:** đạn tầm xa tốc độ 10 ô/giây, tính theo đồng hồ mô phỏng server; đạn RANGED gặp vật chắn/đơn vị trên đường thì va chạm và dừng; SIEGE có đạn xuyên; MELEE xử lý tức thời. Xử lý projectile độc lập với FPS client.

**Nghiệm thu:**
- [ ] Đạn di chuyển đúng tốc độ game time (ví dụ 5 ô cần 0,5 giây nếu không va chạm).
- [ ] RANGED có tường chắn thì không gây sát thương xuyên tường.
- [ ] RANGED gặp đơn vị trước mục tiêu → sự kiện va chạm đúng thứ tự.
- [ ] SIEGE xuyên được vật cản theo luật thiết kế.
- [ ] Kết quả giống nhau khi client render 30 FPS và 60 FPS.

## Phase R6 — Xử lý 10 hành động/giây/bot trên server

**Triển khai:** quota tính theo **đơn vị** và **thời gian mô phỏng/server**, không tính theo HTTP request; giới hạn queue; sự kiện bị từ chối có mã `RATE_LIMIT`/`QUEUE_FULL`. Quản lý lệnh không được lách quota hành động qua tài khoản khác.

**Nghiệm thu:**
- [ ] Gửi 11 hành động tới cùng bot trong cửa sổ 1 giây → tối đa 10 được thực thi trong cửa sổ đó.
- [ ] Hai bot khác nhau có quota riêng, không chia chung 10 lệnh.
- [ ] Nhiều nguồn lệnh cùng nhắm một bot vẫn chỉ tối đa 10 hành động/s/bot.
- [ ] WAIT cũng tính một hành động; SELECT trên UI không tiêu quota.
- [ ] Test gửi 100 lệnh đồng thời không làm server crash hoặc phá thứ tự được chấp nhận.

## Phase R7 — Ngôn ngữ lặp và ưu thế của `for`

**Triển khai:** giữ mã lệnh số; hỗ trợ chế độ gửi chuỗi lệnh từ vòng lặp. **Không thể** xác minh lệnh do `for` sinh ra bằng cách đọc `stdout` thuần; chọn **DSL `REPEAT`** trước hoặc runtime có đánh dấu nguồn lệnh tin cậy. Trong chế độ `print` đơn, một bot chỉ nhận 1 hành động/lượt gửi; trong chế độ lặp hợp lệ, cho phép xếp nhiều hành động nhưng vẫn 10 hành động/s/bot. Sandbox có CPU/RAM/timeout/output cap.

**Nghiệm thu:**
- [ ] Gửi nhiều dòng `print` thủ công điều khiển cùng bot → chỉ tối đa 1 hành động được nhận, phần dư trả `DUPLICATE_ACTION_IN_SUBMISSION`.
- [ ] Vòng lặp hợp lệ sinh được nhiều hành động cho cùng bot, có thứ tự và không vượt quota.
- [ ] 10.000 lệnh/infinite loop bị chặn bởi quota và giới hạn thực thi, server vẫn phục vụ trận khác.
- [ ] Lệnh sai trong chuỗi có chính sách xử lý rõ (bỏ lệnh sai, không phá toàn bộ match).
- [ ] Test đối chứng: chuỗi stdout giống nhau nhưng nguồn lệnh khác nhau **không bị đánh đồng**, nếu đó là luật muốn giữ.

## Phase R8 — 10 đội, 2 phe, nhiều người cùng điều khiển

**Triển khai:** 10 đội chia 2 phe, mỗi đội tối đa 5 tài khoản; cùng tài nguyên, bot, trạng thái, queue và quota; sự kiện gắn `actor_account_id` để audit; giải quyết xung đột lệnh theo thứ tự server tiếp nhận, có quy tắc tie-break nhất quán.

**Nghiệm thu:**
- [ ] 10 đội được phân đúng 5 A/5 B; mỗi đội tối đa 50 bot.
- [ ] 5 tài khoản cùng đội đều chọn/điều khiển được bot thuộc đội đó.
- [ ] Tài khoản thứ 6 không thể chiếm slot điều khiển đồng thời.
- [ ] 5 tài khoản cùng spam một bot **không** nhân quota 10/s lên 50/s.
- [ ] Lệnh trùng/xung đột không nhân bản hành động hoặc làm sai state.
- [ ] Một HQ bị phá chỉ loại đội đó; phe thua khi toàn bộ HQ của phe bị phá.

## Phase R9 — Kinh tế, sản xuất và luật thắng hoàn chỉnh

**Triển khai:** chi phí thử nghiệm cho spawn/build, ưu tiên luật đơn giản; HQ tạo thợ ban đầu, BARRACKS xuất mọi loại theo luật full; tường, tháp canh có hành vi thống nhất; kiểm soát 50 đơn vị/đội kể cả quân đang chờ; chưa cần cân bằng chuyên sâu.

**Nghiệm thu:**
- [ ] Không đủ tài nguyên thì không được xây/xuất quân.
- [ ] Đủ tài nguyên nhưng thiếu chỗ thì spawn vào hàng đợi, không mất/nhân đôi đơn vị.
- [ ] Tài nguyên trừ chính xác một lần khi lệnh được chấp nhận theo chính sách đã chọn.
- [ ] Đội thua không thể sinh quân hoặc sửa state sau khi bị loại.

## Phase R10 — Trải nghiệm, ổn định và cân bằng sau cùng

**Triển khai:** pixel art, animation, replay, log chi tiết theo người gửi, tutorial `mã số → print → for → if`, giám sát CPU/RAM/tick latency, thống kê trận, cân bằng HP/damage/chi phí theo dữ liệu thử nghiệm; chống spam nâng cao.

**Nghiệm thu:**
- [ ] Kịch bản người mới làm được bằng nhập lệnh số, không cần thuật toán.
- [ ] Kịch bản người dùng vòng lặp điều khiển hiệu quả nhiều bot, lợi thế đến từ mã nguồn chứ không từ tốc độ mạng.
- [ ] Load test 10 đội × 50 bot và số tài khoản mục tiêu: không mất state, không vượt quota; độ trễ/tick đạt ngưỡng SLA nhóm tự chốt từ benchmark.
- [ ] Restart/mất kết nối/khôi phục theo chính sách được công bố; kết quả trận và log kiểm chứng được.

---

# C. Ma trận phụ thuộc và mốc bàn giao

| Mốc | Phase | Sản phẩm nhìn thấy | Có thể demo gì? |
|---|---|---|---|
| **M0** | 00–02 | Logic + HTTP API | Gửi mã số và xem state JSON đổi |
| **M1** | 03–05 | Giao diện + đội + bot | Nhìn thấy và điều khiển bot bằng số |
| **M2** | 06–08 | Tài nguyên, xây dựng, chiến đấu | Thu thập, xây nhà, phá HQ |
| **MVP** | 09–11 | Hai đội online và bản cài dựng lại | Hai trình duyệt chơi hết một trận |
| **Luật lưới** | R1–R3 | Va chạm + footprint + tốc độ quân | Không thể xuyên tường; quân khác tốc độ |
| **Chiến đấu đầy đủ** | R4–R5 | Tầm bắn + đạn | Đạn bay/va chạm/xuyên đúng luật |
| **Coding game** | R6–R7 | Quota + vòng lặp thật | Người biết lặp điều khiển nhiều hành động hơn |
| **Trận nhiều đội** | R8–R9 | 10 đội, nhiều tài khoản, kinh tế | 2 phe phối hợp đánh nhau |
| **Release** | R10 | Trải nghiệm & tải | Game hoàn chỉnh, benchmark và cân bằng |

## D. Test xuyên suốt nên duy trì từ sớm

| Nhóm test | Test cốt lõi | Bắt đầu từ |
|---|---|---|
| Parser | Mã hợp lệ/sai, số tham số, chặn nhiều dòng | 02 |
| Ownership | Không điều khiển bot/công trình đội khác | 04 |
| State | Lệnh thành công thay đổi đúng một lần; lỗi không đổi state | 00–02 |
| Unit | Tên duy nhất theo đội, xóa xong không điều khiển được | 05 |
| Economy | Chỉ thợ đứng trên tài nguyên mới khai thác; tài nguyên vô hạn | 06 |
| Building | Chỉ thợ xây được, HQ không xây lại | 07 |
| Combat | Giảm HP, loại đội, chốt phe thắng, cấm lệnh sau kết thúc | 08 |
| Online | Hai client thấy cùng revision; reconnect không reset | 09 |
| Collision | Không đi xuyên/đè ô/qua biên theo luật full | R1 |
| Quota | Tối đa 10 hành động/s/bot, không lách bằng nhiều tài khoản | R6, R8 |
| Sandbox | Timeout, output cap, không file/network, vòng lặp vô hạn | R7 |

## E. Quy trình triển khai từng phase

1. Viết fixture/trạng thái đầu vào tối thiểu và các test Given–When–Then.
2. Triển khai **một** lát cắt nhỏ đủ vượt test; không làm backlog nâng cao.
3. Chạy toàn bộ test cũ, không chỉ test phase mới.
4. Demo 1 kịch bản thành công + 1 kịch bản lỗi; lưu log hoặc video/ảnh.
5. Đánh dấu `VERIFIED` với commit và mô tả các giới hạn còn chủ ý; mới chuyển phase tiếp.

## F. Các quyết định cần chốt khi tới đúng phase (không chặn MVP)

- **R1–R2:** công trình có cản ngay ô tài nguyên không; vị trí thợ khi BUILD; xe 2×2 có được quay khi sát vật cản không; thứ tự xử lý hai bot di chuyển vào cùng một ô.
- **R5:** đạn chạm đồng minh, cây/tài nguyên, công trình và nhiều mục tiêu thì xử lý ra sao; đạn xuyên của SIEGE gây sát thương tối đa bao nhiêu mục tiêu.
- **R6–R7:** thời gian quota tính theo tick cố định hay cửa sổ trượt, max queue, hành vi khi hàng đợi đầy; chọn DSL `REPEAT` hay Python instrumentation.
- **R8:** luật ưu tiên khi nhiều người điều khiển cùng bot, cách cấp/thu hồi 5 slot điều khiển.
- **R9–R10:** chi phí, HP/damage, tháp canh tự bắn hay có lệnh, tài nguyên mỗi lần khai thác, thời hạn trận và cách xử lý hòa.

**Không cần trả lời những câu hỏi này trước Phase 00.** Mục tiêu là tránh biến bài toán MVP nhỏ thành dự án RTS đầy đủ ngay từ đầu.
