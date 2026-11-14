> **Prompt cho agent:** Chỉ xử lý các mục **CẦN SỬA** dưới đây; đọc đúng file/hàm được dẫn, sửa tối thiểu, thêm test sát lỗi, chạy test liên quan rồi kiểm tra tổng. Không quét/refactor toàn repo, không triển khai lại mục **ĐÃ CÓ**, không giải thích dài; chỉ báo file sửa, test + kết quả và phần chưa xác minh.

# Ant Colony Wars — Rà soát giao diện và vấn đề còn lại

- **Repo:** `nddKhanh/Java_Private_2026` — nhánh `develop`, thư mục `game/`.
- **Commit đã kiểm tra:** [`4889c289`](https://github.com/nddKhanh/Java_Private_2026/commit/4889c28981152fffc318411fabbd188a26e632f2) (14/11/2026).
- **Phương pháp:** Đọc source và test trên GitHub. **Chưa chạy độc lập Maven/npm hoặc quan sát giao diện bằng trình duyệt/IntelliJ thật.**

## A. Giao diện — ĐÃ CÓ TRONG SOURCE, không làm lại

`game-web/src/BattleScene.ts` đã sử dụng hình dạng riêng cho các loại quân và công trình:

| Đối tượng | Hình dạng hiện tại |
| --- | --- |
| `WORKER` | Hình tròn, chữ `W` |
| `MELEE` | Hình tam giác, chữ `M` |
| `RANGED` | Hình vuông xoay 45°, chữ `R` |
| `SIEGE` | Hình vuông lớn theo footprint 2×2, chữ `S` |
| `HQ` | Hình lục giác, chữ `HQ` |
| `BARRACKS` | Hình vuông xoay 45°, chữ `B` |
| `TOWER` | Hình tròn, chữ `T` |
| `WALL` | Hình chữ nhật dẹt, ký hiệu `▰` |

- Có màu theo đội, ký hiệu loại và HP.
- `addIdLabel()` hiển thị **ID thật ngay dưới** biểu tượng của mọi unit/HQ/building. ID dài bị rút gọn `…`, hover hiện toàn bộ.
- **Khác biệt với yêu cầu gốc:** ID hiện **ở dưới** chứ **chưa nằm trên chính biểu tượng**. Xem mục C.3.
- **Chưa kiểm tra trực quan:** bản đồ đông quân, nhãn chồng lấn, độ rõ khi ô nhỏ/zoom, hover, spawn, di chuyển và phá hủy.

## B. Logic đã sửa — KHÔNG LÀM LẠI

Có source và test hồi quy cho bốn vấn đề rà soát trước: `ATTACK` bỏ qua đồng minh để chọn địch phía sau; spawn không có `sourceId` bị từ chối và spawn có nguồn tính hạn mức công trình; `RunGeneration` bỏ qua callback Run cũ; Atomic/Partial báo lỗi đầu tiên đúng thứ tự. Chỉ sửa lại nếu có test chứng minh hồi quy.

## C. CẦN SỬA / HOÀN THIỆN

### P1 — 1. IntelliJ Run tự chọn đội đầu tiên

- **Chứng cứ:** `.env` mới có `MATCH_KEY`, `BATCH_MODE`; `Submit.java` và `StudentSubmission.java` không truyền `teamId`; `CodeStudioApplication.finishPairing()` gọi `teamToken.getSelectionModel().selectFirst()`.
- **Tác hại:** Hai học viên ở các đội khác nhau có thể đều gửi lệnh bằng token đội đầu tiên của trận.
- **Yêu cầu:** Thêm `TEAM_ID` vào `.env`, truyền theo `Submit` → `StudentSubmission` → Desktop; sau pairing tìm đúng `PairingTeam.teamId`, lấy token đội tương ứng. Thiếu hoặc không khớp `TEAM_ID` thì báo lỗi tiếng Việt, **không tự chọn đội đầu tiên** khi Run từ IntelliJ. Giữ khả năng chọn thủ công trong Desktop.
- **Kiểm thử:** Hai IntelliJ submissions cùng trận, `TEAM_ID` khác nhau → hai đội khác nhau thực thi; ID sai/trống → không gửi batch; Run liên tiếp không lẫn token.
- **File:** `student-bot-template/.env`, `student-bot-template/src/main/java/Submit.java`, `game-desktop/.../StudentSubmission.java`, `.../CodeStudioApplication.java`, test Desktop.

### P2 — 2. Overload spawn cũ vẫn biên dịch nhưng chắc chắn bị server từ chối

- **Chứng cứ:** `GameClient.spawn(unitId, displayName, direction)` và overload không có `sourceId` vẫn gửi lệnh thiếu nguồn; `GameEngine.productionSource()` hiện bắt buộc `sourceId`.
- **Tác hại:** Chương trình dùng API cũ vẫn compile, nhưng thất bại khi chạy, gây khó hiểu.
- **Yêu cầu:** Với overload không có nguồn, đánh dấu `@Deprecated` và **từ chối sớm tại client với thông báo rõ thiếu ID công trình**, không gửi request chắc chắn thất bại. Cập nhật ví dụ/docs sang `bot.select(idCongTrinh)` → `bot.spawn(idQuan, type, dir)` hoặc API đầy đủ truyền `sourceId`. Không tự đoán nguồn sản xuất.
- **Kiểm thử:** API cũ báo lỗi ngay trước khi gửi mạng; API mới tạo quân đúng HQ/BARRACKS và vẫn tính hạn mức 10 hành động/công trình.
- **File:** `game-client-java/.../GameClient.java`, SDK/bot facade liên quan nếu cần, docs và test client.

### P2 — 3. ID nằm sát dưới, chưa nằm trên biểu tượng theo đúng câu chữ yêu cầu

- **Chứng cứ:** Trong `BattleScene.ts`, `addIdLabel()` được gọi tại `center.y + ...`, thay vì vẽ đè lên biểu tượng.
- **Yêu cầu:** Đặt ID thực tế **trên biểu tượng** hoặc trên lớp nhãn phủ biểu tượng đủ tương phản, vẫn đọc được loại quân/công trình và HP. ID dài có thể rút gọn nhưng hover/chọn phải hiện đầy đủ; tránh nhãn chồng lấn trên bản đồ đông. Không thay đổi luật game hoặc giao thức.
- **Kiểm thử:** Đủ 4 loại quân, HQ/BARRACKS/TOWER/WALL; mỗi ID đúng đối tượng, thử ID dài, quân 2×2, ô nhỏ, spawn/di chuyển/tiêu diệt.
- **File:** `game-web/src/BattleScene.ts`.
- **Lưu ý:** Đây là sai khác vị trí hiển thị với yêu cầu gốc, **không phải lỗi thiếu hình dạng hoặc thiếu nhãn ID**.

## D. KIỂM CHỨNG BẮT BUỘC

1. Viết/chạy test đúng các mục C.1–C.3; sau đó chạy Maven reactor test và `npm run build` trong `game-web`, ghi lệnh + kết quả thực tế.
2. Chạy E2E thật: hai học viên ở hai đội khác nhau bấm IntelliJ **Run** → Desktop → Server → lỗi/kết quả về console IntelliJ; xác nhận token/match đúng đội.
3. Mở viewer thật để kiểm tra hình dạng, độ rõ ID, hover, vị trí nhãn khi đông quân và khi thay đổi trạng thái.
4. Chạy lại Atomic/Partial, Java exception trước submit, hai lần Run liên tiếp để xác nhận không hồi quy.

**Hoàn tất khi:** Không tự chọn sai đội; API cũ báo lỗi sớm; ID hiển thị đúng yêu cầu; có bằng chứng test/runtime. Không triển khai lại những mục đã đạt ở A/B.
