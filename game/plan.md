# ANT COLONY WARS — Kế hoạch cơ chế và MVP (v0.3)

> **Trạng thái:** Bản thiết kế cập nhật theo yêu cầu ngày 07/11/2026.  
> **Mục tiêu:** Game chiến thuật online 2D pixel art, bản đồ **ma trận ô vuông**, điều khiển từng đơn vị bằng các lệnh cú pháp tối giản. Người mới dùng một lệnh `print()` vẫn chơi được; người biết vòng lặp, điều kiện, hàm và phối hợp nhóm có lợi thế khi các chức năng nâng cao được bật.  
> **Nguyên tắc triển khai:** **Giữ nguyên tầm nhìn gameplay, nhưng MVP chỉ chứng minh hệ thống chạy được với lệnh đơn**. Không tự đưa cơ chế phức tạp vào MVP.

## 1. Tóm tắt quyết định

| Nội dung | Luật chơi mục tiêu (full) | MVP triển khai đầu tiên |
|---|---|---|
| Thế giới | Online, 2D pixel art, lưới ô vuông, 4 hướng | Có |
| Đội & phe | 10 đội, chia **2 phe**, mỗi phe **5 đội** | Có đội và phe; **1 tài khoản/đội** |
| Thành viên điều khiển | Tối đa **5 tài khoản đang điều khiển/đội** | 1 tài khoản/đội |
| Số đơn vị | Tối đa **50 đơn vị/đội**, tính cả thợ | Có kiểm tra giới hạn khi tạo đơn vị nếu tiện; không phải tối ưu tải |
| Lệnh `print()` thủ công | **Mỗi bot chỉ nhận tối đa 1 hành động trong một lượt gửi theo chế độ `print` đơn** | **Có:** một lệnh hành động đơn/lần gửi |
| Vòng `for` | Luồng lệnh nhiều hành động/bot/lượt gửi, có hạn mức 10 hành động/s/bot | **Chưa có**; chỉ chừa hướng phát triển |
| Hạn mức hành động | Tối đa **10 hành động được xử lý/giây/bot**; dùng chung giữa các tài khoản cùng đội | Chưa cần bộ phân luồng/tính tốc độ hoàn chỉnh |
| Hành động | Tạo/xóa, chọn bot, đi 4 hướng, đứng im, đánh, khai thác, xây, xuất quân | **Chỉ cần xử lý từng lệnh đơn**, có thể triển khai các hành động cốt lõi theo từng bước |
| Di chuyển đặc thù | Mọi đơn vị có hướng quay; tốc độ theo số lệnh tích lũy tùy loại | **Mỗi lệnh MOVE đi 1 ô**, không phân tốc độ |
| Va chạm ô | Mỗi ô tối đa 1 đơn vị; ngoại lệ ô tài nguyên được đứng lên để khai thác | **Chưa cần va chạm**; có thể chồng đơn vị, xuyên vật thể và ra ngoài bản đồ |
| Đạn | Tốc độ bay **10 ô/giây**, kiểm tra va chạm; xuyên hay không tùy loại quân | **Đòn đánh tức thời, xuyên mọi vật thể**, không có đường đạn |
| Tài nguyên | Ô tài nguyên vô hạn, chỉ thợ được khai thác | Có thể dùng tài nguyên vô hạn, chưa cân bằng giá trị |
| Công trình | Nhà chính, trại lính, tháp canh, tường; trừ nhà chính, phải có thợ để xây | Có thể biểu diễn trên map; xây bằng lệnh đơn, chưa cần chiếm ô/va chạm |
| Kết thúc | Phá nhà chính của phe đối địch để giành chiến thắng; xác định thắng theo phe | Có thể kiểm tra HP nhà chính = 0; chưa cần hệ thống giải đấu |

**Ranh giới quan trọng:** Các mục ghi ở cột *full* là **luật chơi mong muốn**, không phải yêu cầu lập trình ngay. Các mục *MVP* có quyền tạm bỏ kiểm tra để rút ngắn thời gian thử tính khả thi.

## 2. Mô hình trận đấu, đội và quyền điều khiển

### 2.1. Luật mục tiêu

- Một trận có **10 đội**, chia thành **2 phe**, mỗi phe 5 đội. Các đội cùng phe là đồng minh; mục tiêu thắng/thua được tính theo **phe**.
- Mỗi đội sở hữu một nhà chính, tài nguyên, công trình và **tối đa 50 đơn vị**. Giới hạn này tính theo **đội**, không phải theo tài khoản.
- Mỗi đội cấp quyền điều khiển đồng thời cho **tối đa 5 tài khoản**. Tất cả cùng thao tác trên **cùng danh sách bot, công trình, tài nguyên và hàng đợi lệnh của đội**.
- Các tài khoản cùng đội **không được cộng thêm quota**. Nếu hai người gửi lệnh trùng, cùng nhắm một bot hoặc gửi quá tải thì áp dụng quy tắc y như **một người gửi**.
- Đồng minh không được tấn công nhau theo mặc định (đề xuất để làm rõ ý nghĩa 2 phe); đổi luật này về sau nếu muốn friendly fire.

### 2.2. MVP

- Có thể tạo trận, tạo đội, chỉ định phe `A` hoặc `B`, gắn **đúng 1 tài khoản vào 1 đội**; một tài khoản chỉ thuộc **một đội trong trận**.
- Đủ để chạy thử bằng 2 đội (mỗi phe 1 đội), nhưng dữ liệu **không khóa cứng** vào 2 đội; mở rộng dần lên 10.
- Chưa có mời thành viên, phân quyền 5 người, xử lý concurrent submissions hoặc đăng nhập nhiều người cùng đội.

## 3. Bản đồ và vật thể

- Bản đồ là ma trận ô vuông `W × H`, kích thước tùy cấu hình trận; tọa độ `(x, y)`, `x` tăng sang phải, `y` tăng xuống dưới.
- Bốn hướng: `UP`, `DOWN`, `LEFT`, `RIGHT`. Không đi chéo.
- Map tĩnh về địa hình nhưng trạng thái quân, công trình, tài nguyên và vị trí thay đổi theo lệnh.
- Có lớp hiển thị **địa hình**, **ô tài nguyên**, **công trình**, **đơn vị**, **đạn** (đạn chỉ ở bản full). Không nhất thiết buộc dữ liệu này vào một trường duy nhất trong ô.
- **Full:** một ô tối đa một đơn vị; ngoại lệ ô tài nguyên vẫn cho một đơn vị đứng trên đó. Một đơn vị có thể cùng đứng trên ô tài nguyên vì tài nguyên là lớp nền, không phải một đơn vị thứ hai. Công trình và tường chiếm ô, chặn đường. Xe công thành dùng footprint 2×2 (4 ô) và phải đủ ô trống.
- **MVP:** bỏ qua kiểm tra ô bị chiếm, vật cản, giới hạn map; cho phép nhiều đơn vị cùng tọa độ, đi xuyên công trình, đi tới `x < 0`, `y < 0` hoặc vượt `W−1`, `H−1`. Đây là chủ ý để kiểm chứng luồng lệnh; UI nên hiển thị vị trí ngoài map bằng log/danh sách đơn vị dù sprite nằm ngoài khung.

## 4. Danh mục đơn vị

| Loại | Vai trò | Đặc điểm full | MVP |
|---|---|---|---|
| **Thợ** (`WORKER`) | Khai thác tài nguyên, xây công trình | Chỉ thợ `GATHER` và `BUILD` | Di chuyển 1 ô/lệnh; có thể `GATHER`, `BUILD` |
| **Cận chiến** (`MELEE`) | Chống chịu, đánh kề | Máu cao, sát thương thấp, giá trung bình, tầm 1; **2 lệnh MOVE cùng hướng/ô** | Di chuyển 1 ô/lệnh; tấn công tức thời, xuyên |
| **Tầm xa** (`RANGED`) | Tấn công từ xa | Máu thấp, sát thương cao, giá trung bình, tầm 3; **1 lệnh MOVE/ô**, đạn **không xuyên** | Di chuyển 1 ô/lệnh; tấn công tức thời, xuyên |
| **Xe công thành** (`SIEGE`) | Phá công trình, xuyên vật thể | Máu trung bình, sát thương rất cao, giá cao, chiếm **2×2**, tầm 7; **5 lệnh MOVE cùng hướng/ô**, đạn **xuyên** | Di chuyển 1 ô/lệnh, không xét footprint; tấn công tức thời, xuyên |

**Chưa cân bằng:** HP, sát thương, tốc độ bắn, giá, sức chứa, lượng khai thác, HP công trình. Dùng hằng số tạm nếu cần chạy demo; không xem đó là luật đã chốt.

### 4.1. Quy tắc hướng quay / di chuyển tích lũy (full, chưa làm MVP)

Mỗi đơn vị cần lưu tối thiểu `facing` (hướng hiện tại) và bộ đếm tiến độ di chuyển `move_count`. **Không cần trường `move_direction` riêng** vì hướng đã có trong `facing`; không phải lưu toàn bộ lịch sử di chuyển nếu bộ đếm đủ đại diện.

Khi nhận `MOVE <direction>`:

1. Nếu `direction != facing`, quay mặt sang hướng mới và **đặt lại tiến độ tích lũy** cho hướng mới.
2. Nếu đã cùng hướng, tăng tiến độ. Khi đạt ngưỡng của loại quân (MELEE 2, RANGED 1, SIEGE 5; WORKER mặc định 1), tiến 1 ô theo `facing`, xóa tiến độ.
3. Với đơn vị yêu cầu nhiều lệnh, lần đổi hướng là một lần quay; cần quy ước rõ có tính là lệnh tích lũy thứ nhất hay không. **Đề xuất:** tính luôn là lần thứ nhất để tránh việc cùng một loại quân có tốc độ khác nhau giữa lệnh đầu và các lệnh sau.
4. `WAIT` đứng yên không quay mặt, không di chuyển; đề xuất không xóa tiến độ. `ATTACK`/`GATHER`/`BUILD` có thể xóa tiến độ khi triển khai full để tránh giữ năng lượng di chuyển không hợp lý.

**Lưu ý:** Với quy tắc 'đổi hướng cũng tính là lần thứ nhất', RANGED/WORKER sẽ quay và tiến ngay bằng một lệnh; MELEE/SIEGE mới phải tích thêm lệnh. Cách này khớp nhu cầu 'quay mặt rồi di chuyển' mà không thêm mã lệnh `TURN`.

**MVP:** bỏ hoàn toàn bộ đếm và xử lý hướng đặc thù. `MOVE DIR` luôn cập nhật `facing=DIR`, `x/y` tăng hoặc giảm đúng một ô.

### 4.2. Đứng im (`WAIT`)

- `WAIT` là **một hành động thực sự**: giữ nguyên vị trí và hướng, hữu ích để đồng bộ kịch bản chiến thuật, chờ đồng đội/đạn bay/hồi nhịp.
- **Full:** `WAIT` được tính vào giới hạn 10 hành động/giây/bot, không phải cách lách quota.
- **MVP:** thực hiện `WAIT` bằng cách không thay đổi tọa độ; trả `OK` và ghi sự kiện trong log.

## 5. Khai thác tài nguyên

- Ô tài nguyên có `resource_type` (gợi ý `FOOD`, `WOOD`, `STONE`); ở giai đoạn đầu **trữ lượng vô hạn**.
- **Chỉ `WORKER`** được `GATHER`; thợ phải đứng **đúng trên ô tài nguyên**, không chỉ đứng cạnh.
- Mỗi lệnh `GATHER` hợp lệ cộng một lượng tài nguyên (giá trị mặc định tùy cấu hình thử nghiệm), không cần mang về kho, không cần thời gian chờ riêng ngoài giới hạn hành động.
- Các đơn vị chiến đấu `GATHER` sẽ nhận lỗi `WORKER_REQUIRED`; thợ ngoài ô tài nguyên nhận `NOT_ON_RESOURCE`.
- **MVP:** có thể hiện thực kiểm tra loại thợ + ô tài nguyên và cộng tài nguyên ngay. Tài nguyên vẫn vô hạn; chưa cần cân bằng, không cần mô phỏng chở hàng.

## 6. Công trình và xuất quân

| Công trình | Full | MVP |
|---|---|---|
| **Nhà chính** (`HQ`) | Mỗi đội khởi đầu có một nhà chính; **HQ bị phá → đội bị loại**; thắng thua tính theo phe | Tạo sẵn khi khởi trận; có HP để thử tấn công |
| **Nhà xuất quân** (`BARRACKS`) | Tạo mọi loại quân, xuất quân ở ô trống quanh nhà; không có chỗ thì **giữ yêu cầu chờ** | Có thể xuất quân ngay ở tọa độ quy định mà không kiểm tra chỗ |
| **Tháp canh** (`TOWER`) | 1 ô, tầm 5, sát thương lớn, HP trung bình | Tạo/hiển thị được; chưa cần AI tự bắn |
| **Tường** (`WALL`) | 1 ô, HP rất lớn, chặn mọi đơn vị, kể cả xe 2×2 | Tạo/hiển thị được; chưa chặn di chuyển |

### 6.1. Xây dựng

- **Full:** trừ nhà chính tạo sẵn khi bắt đầu trận, **mọi công trình chỉ có `WORKER` mới được xây**. Một lệnh `BUILD` hợp lệ là **xây xong ngay**, không có thời gian thi công.
- Thợ thực hiện lệnh xây trên ô đích hợp lệ (đề xuất là ô kề thợ, hoặc ô thợ đang đứng nếu muốn xây rồi rời đi — phải chọn một khi bổ sung kiểm tra vị trí); công trình không được chồng lên vật thể chiếm chỗ.
- Nếu thiếu diện tích trống, trả lỗi `NO_SPACE`, không xây; không phát sinh công trình dở dang.
- **MVP:** `BUILD` do thợ gửi, tạo công trình ngay tại tọa độ yêu cầu, bỏ qua mọi kiểm tra footprint, chiếm ô và va chạm. Đối với HQ, không có lệnh xây lại trong MVP.

### 6.2. Tạo, đặt tên và xóa đơn vị

- Tên đơn vị là duy nhất **trong đội**; cùng phe nhưng khác đội vẫn có thể trùng tên. Server giữ ID nội bộ riêng.
- `SPAWN <type> <name>`: tạo đơn vị ở vị trí gần HQ/nhà xuất quân; tối đa 50 đơn vị/đội. Mục tiêu là nhà xuất quân có thể tạo mọi loại, HQ bảo đảm có thể tạo ít nhất thợ ban đầu để tránh bế tắc.
- **Full:** chỉ xuất đơn vị ở ô hợp lệ trống xung quanh công trình. Nếu chưa có chỗ, gửi `SPAWN_QUEUED` và giữ yêu cầu đến khi có chỗ. Để không vượt 50, slot được giữ ngay khi nhận lệnh; hoàn slot và hoàn chi phí nếu yêu cầu bị hủy hoặc nhà bị phá.
- `DELETE <name>`: xóa đơn vị của đội, giải phóng slot, không hoàn chi phí (quy ước tạm); hủy các lệnh đang chờ của bot đó.
- **MVP:** tạo đơn vị ở điểm xuất quân cố định hoặc ô liền kề bất kỳ, **cho phép trùng vị trí**, không cần hàng đợi chờ chỗ trống. `DELETE` biến mất ngay.

## 7. Tấn công và đạn

### 7.1. Luật mục tiêu (full)

- `ATTACK <target>`: phải có mục tiêu hợp lệ còn sống, thuộc phe địch và trong tầm của đơn vị tấn công.
- Tầm tính theo **Manhattan** trên lưới ô vuông, tạm là khoảng cách nhỏ nhất giữa các ô footprint của chủ thể và mục tiêu; MELEE = 1, RANGED = 3, SIEGE = 7, TOWER = 5.
- **Đạn tầm xa bay 10 ô/giây**, không dịch chuyển tức thời. Engine cập nhật vị trí đạn theo đồng hồ mô phỏng, xác định vật cản/đối tượng đầu tiên trúng đạn để phát sinh sự kiện va chạm.
- **RANGED:** đạn không xuyên vật thể; chạm vật cản/đơn vị trước mục tiêu thì bị chặn hoặc gây sát thương vào vật trúng đầu tiên (chọn khi triển khai full). **SIEGE:** đòn tấn công xuyên, không bị vật thể trên đường chặn; có thể giới hạn số mục tiêu trúng đạn sau khi cân bằng.
- **MELEE:** đánh trực tiếp ô kề; không dùng đạn. **TOWER:** có tầm 5 nhưng cách kích hoạt tấn công (tự động hay ra lệnh) để ở backlog vì chưa cần cho MVP.

### 7.2. MVP

- `ATTACK` trừ HP mục tiêu **ngay lập tức** nếu mục tiêu tồn tại; có thể kiểm tra mục tiêu thuộc phe địch và còn HP (kiểm tra rẻ, dễ làm).
- **Mọi đòn đánh đều xuyên, không có đạn bay, không có collision, không có vật chắn và không cần kiểm tra tầm đánh** trong MVP. Nếu muốn thử tầm đánh, bật sau như một bước mở rộng chứ không coi là điều kiện hoàn thành MVP.
- HP giảm tới 0 thì đơn vị/công trình bị phá. Khi HQ bị phá, có thể đánh dấu đội bị loại để chứng minh vòng đời trận.

## 8. Gửi code, `print`, vòng lặp và giới hạn lệnh

### 8.1. Mục đích thiết kế

Người chỉ biết `print()` có thể chọn một bot và làm một hành động. Người biết `for` có thể sinh nhiều hành động, giữ nhịp thực hiện ở nhiều bot hơn, phối hợp chiến thuật mà **không cần giải thuật tìm đường**.

**Quy tắc full quan trọng để không vô hiệu hóa `for`:**

- **Chế độ `print` đơn:** trong **một lượt gửi chương trình**, với **mỗi bot**, chỉ nhận **tối đa một lệnh hành động** (`MOVE`, `WAIT`, `ATTACK`, `GATHER`, `BUILD`). Nhiều dòng `print` điều khiển cùng bot trong cùng lượt **không được cộng dồn**; các lệnh hành động dư bị từ chối `DUPLICATE_ACTION_IN_SUBMISSION`. `SELECT` chỉ đổi bot đang được nhắm tới, không phải hành động di chuyển.
- **Chế độ lặp (phát triển sau):** khi hệ thống hỗ trợ vòng `for`, các lệnh được sinh bởi vòng lặp **được phép xếp chuỗi nhiều hành động cho cùng bot** trong một lượt gửi, vẫn **không quá 10 hành động thực thi/giây/bot**. Cơ chế này phải dựa trên **ngữ cảnh thực thi có kiểm soát**, không thể phân biệt an toàn bằng cách nhìn các chuỗi stdout vì hai chương trình có thể in ra đúng cùng văn bản. Không được chỉ dùng `print` nhiều dòng rồi tự gắn nhãn là lặp.
- **Giới hạn 10 hành động/giây/bot** là giới hạn *server thực thi*, không phải 10 HTTP request vật lý. Có thể gửi một request chứa nhiều lệnh được sinh hợp lệ, engine thực thi theo thời gian. Các tài khoản cùng đội dùng chung quota bot và hàng đợi.
- Các lệnh quản lý như `SPAWN`, `DELETE`, `SELECT` không được dùng để lách hành động; đề xuất có hạn mức riêng theo **đội** ở bản full (giá trị chưa chốt).

> **Quyết định kỹ thuật cần giữ minh bạch:** Nếu chỉ chạy mã Python và đọc `stdout`, server **không thể biết** một dòng do `for` tạo ra hay do người chơi copy `print`. Muốn thực thi đúng ràng buộc đặc biệt này, bản full phải dùng một trong các cơ chế: (A) ngôn ngữ lệnh/DSL hỗ trợ `REPEAT`, hoặc (B) trình chạy Python có instrumentation / API xuất lệnh đánh dấu nguồn gốc hợp lệ. **Đề xuất ưu tiên A** nếu muốn đơn giản và khó lách. MVP chưa cần giải quyết vấn đề này.

### 8.2. MVP: lệnh số, một hành động mỗi lần gửi

- **Không chạy Python trong MVP.** Giao diện có dropdown chọn bot và một ô nhập **mã số lệnh**; nhấn **Gửi** để thực hiện một hành động.
- Các thao tác có tham số dùng chuỗi dạng `mã tham_số_1 tham_số_2 ...`, chẳng hạn `1 worker ant01` để tạo đơn vị hoặc `9 enemy02` để đánh mục tiêu. Các thao tác di chuyển/đứng im/khai thác chỉ cần nhập **một số**.
- Mỗi request chỉ được chứa **một lệnh**, không chấp nhận nhiều dòng. `SELECT` là thay đổi bot đang chọn trên UI, không làm mất lượt hành động. Server luôn nhận `unit_name` riêng, không dùng lựa chọn chung của cả đội.
- Chỉ chấp nhận đúng số lượng và kiểu tham số cho từng mã; mã lạ và dữ liệu sai trả lỗi, không sửa ngầm.
- MVP chưa triển khai trình biên dịch, vòng lặp, hàng đợi nhiều lệnh hay giới hạn tốc độ đầy đủ. Các giới hạn đó thuộc hướng phát triển, không thay đổi cách diễn giải mã hành động.
- Bảo đảm ít nhất hai đội online có thể gửi lệnh đơn, server thay đổi trạng thái và cập nhật bản đồ cho cả hai.

## 9. Bảng mã lệnh số (ID bắt đầu từ 1, tăng dần)

> **Giao thức cho MVP:** Một dòng = một lệnh. Với hành động không có tham số, nhập đúng **một số**. Khi có tham số, nhập **số + tham số**, ngăn cách bằng dấu cách. Bot được chọn từ danh sách trên giao diện; không cần `SELECT` dưới dạng một lệnh game. Các con số chỉ là **mã hành động**, không phải số lần lặp.

| ID | Tên hành động | Người chơi nhập | Ý nghĩa | Giai đoạn |
|---:|---|---|---|---|
| 1 | Tạo bot | `1 <loại> <tên>` | Tạo và đặt tên bot tại nhà chính | MVP |
| 2 | Xóa bot | `2 <tên>` | Xóa bot thuộc đội | MVP |
| 3 | Lên | `3` | Bot được chọn đi lên 1 ô | MVP |
| 4 | Xuống | `4` | Bot được chọn đi xuống 1 ô | MVP |
| 5 | Trái | `5` | Bot được chọn sang trái 1 ô | MVP |
| 6 | Phải | `6` | Bot được chọn sang phải 1 ô | MVP |
| 7 | Đứng im | `7` | Bot được chọn đứng im một hành động | MVP |
| 8 | Khai thác | `8` | Thợ được chọn khai thác tại ô đang đứng | MVP |
| 9 | Tấn công | `9 <tên_mục_tiêu>` | Bot được chọn tấn công mục tiêu | MVP (tức thời, xuyên vật thể) |
| 10 | Xây dựng | `10 <loại> <x> <y>` | Thợ được chọn xây công trình | MVP (bỏ kiểm tra va chạm) |
| 11 | Xuất quân | `11 <loại> <tên>` | Tạo đơn vị từ công trình sản xuất (nếu đã triển khai) | Later / tùy chọn MVP |
| 12 | Xem trạng thái | `12` | Xem thông tin bot/đội đang chọn, không tiêu tốn hành động | Later (MVP có thể xem ở UI) |

**Quy ước thống nhất:** `1` tạo bot trực tiếp tại nhà chính để thử nghiệm; `11` sẽ dùng cho hệ thống sản xuất quân từ nhà xuất quân khi bổ sung gameplay đầy đủ. Nếu MVP chưa có nhà xuất quân thì chỉ cần `1`. Các tham số `<loại>` và `<tên>` là chuỗi; `<x>`, `<y>` là số nguyên. Tên bot phải duy nhất trong đội.

### 9.1. Ví dụ người chơi dùng trong MVP

Chọn bot `ant01` ở dropdown, rồi nhập `6` và nhấn **Gửi**: `ant01` đi sang phải một ô. Nhập `7`: đứng im. Nhập `8`: thử khai thác (chỉ hợp lệ nếu `ant01` là thợ trên ô tài nguyên).

Để tạo bot, nhập `1 worker ant01`; để xóa bot, nhập `2 ant01`. Mỗi lần gửi **chỉ có một dòng và một hành động**.

### 9.2. Hướng phát triển: từ mã số đến `print` và `for`

Sau MVP, giữ nguyên các mã số và cho phép xuất lệnh qua chương trình:

```python
print(6)  # Bot đang chọn: sang phải
```

Nếu hỗ trợ vòng lặp, ví dụ:

```python
for i in range(5):
    print(6)
```

**Lưu ý bắt buộc:** Kết quả `stdout` của 5 lần `print(6)` trong vòng `for` không khác 5 dòng `print(6)` sao chép thủ công. Muốn *thực sự* giới hạn lệnh `print` đơn mà ưu tiên vòng lặp, phải dùng runtime có phân tích cấu trúc mã hoặc DSL hỗ trợ lệnh lặp riêng; không thể xác minh điều này chỉ bằng chuỗi lệnh xuất ra. Đây là việc thuộc **hướng phát triển**, không chặn MVP.

Khi bổ sung giới hạn 10 hành động/giây/bot, áp dụng tại server theo bot, dùng chung cho mọi tài khoản cùng đội và mọi loại lệnh (ngoại trừ thao tác đọc trạng thái); **không** dựa vào số request HTTP thực tế hay tốc độ máy người chơi.

## 10. Giao diện online

```text
┌────────────────────────────── MAP 2D ──────────────────────────────┬──────── CODE / LỆNH ────────┐
│  Ma trận ô vuông, sprite pixel art                                 │ Team: Blue-01 / Phe A      │
│  Nhà chính · Tài nguyên · Thợ · Lính · Công trình                  │ Bot: [ant_01 ▾]            │
│                                                                    │                            │
│  (click một bot -> chọn tên; click mục tiêu -> lấy ID)             │ print("MOVE RIGHT")        │
│                                                                    │                            │
│                                                                    │ [GỬI LỆNH]                 │
│                                                                    │                            │
│                                                                    │ Kết quả: OK / ERR          │
├────────────────────────────────────────────────────────────────────┴────────────────────────────┤
│ Phe · Team · HP HQ · Tài nguyên · Danh sách bot · Nhật ký hành động / lỗi                          │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

- UI có bản đồ bên trái, danh sách chọn bot và ô **nhập mã lệnh số** bên phải, nút **Gửi lệnh**, bảng mã lệnh và log. Editor Python là hướng phát triển, chưa cần trong MVP.
- Click đơn vị trên map để chọn bot; chọn mục tiêu có thể tự điền `target_id`.
- Server là nguồn sự thật; client nhận trạng thái qua WebSocket hoặc polling nhẹ ở MVP.
- **MVP không cần:** syntax highlighting đầy đủ, debugger, sandbox Python đa tài khoản, replay, lobby 50 người, hệ thống anti-cheat tinh vi.

## 11. Lộ trình MVP — ưu tiên **chạy được**

### P0 — Minimal Vertical Slice (bắt buộc)

1. **Tạo trận/đội/phe:** ít nhất 2 đội; mỗi đội 1 tài khoản, có nhà chính được khởi tạo sẵn.
2. **Hiển thị map:** ma trận vuông, đơn vị/công trình/tài nguyên dạng ký hiệu hoặc sprite placeholder.
3. **Tạo và chọn bot:** `SPAWN` đặt tên, hiển thị đơn vị; UI có chọn bot; `DELETE` xóa đúng bot.
4. **Lệnh đơn:** editor + nút Gửi. Đẩy một lệnh, server trả `OK/ERR`, client cập nhật vị trí và trạng thái.
5. **Hành động đơn:** `MOVE` 4 hướng (1 ô/lệnh), `WAIT`, `ATTACK` (trừ HP trực tiếp), `GATHER` (chỉ thợ đứng trên tài nguyên), `BUILD` (thợ xây xong tức thì).
6. **Đồng bộ online cơ bản:** hai trình duyệt nhìn thấy cùng trạng thái trên server.
7. **Kiểm tra vòng đời:** mất HP, phá nhà chính, đánh dấu đội bị loại. Không bắt buộc cân bằng và xếp hạng.

**Các giản lược được chấp nhận có chủ ý:** vượt biên map; đi xuyên vật thể, xuyên tường và đơn vị; nhiều đơn vị chung ô; mọi quân đi 1 ô/lệnh; mọi đòn đánh xuyên tức thì; không có đạn bay và collision; không có kinh tế/giá thành được cân bằng; một tài khoản/đội; không chạy `for`; không có queue cho spawn bị kẹt; chưa cần xử lý đồng thời 5 thành viên.

**Tiêu chí hoàn thành MVP:** Có thể mở 2 client thuộc 2 đội, tạo thợ/quân, dùng một lệnh đơn để di chuyển/đứng yên/khai thác/xây/tấn công/xóa, quan sát thay đổi đồng bộ trên map và phá HQ để kết thúc phần chơi. **Không được coi những giản lược ở trên là lỗi MVP.**

### P1 — Hoàn thiện luật chơi trước khi cân bằng

- Giới hạn **10 hành động/giây/bot** trên server, không lệ thuộc số người điều khiển hay số request HTTP.
- Thêm **vòng lặp hợp lệ** và hàng đợi lệnh, phân biệt được vòng lặp với nhiều dòng `print` copy. Lệnh trùng hoặc quá tải xử lý theo cùng quota đội/bot.
- Bổ sung kiểm tra biên map, mỗi ô một đơn vị, collision với vật thể, footprint xe 2×2 và tường cản đường.
- Thêm di chuyển tích lũy theo hướng quay (2 / 1 / 5 lệnh MOVE tương ứng MELEE / RANGED / SIEGE).
- Giới hạn tầm đánh 1 / 3 / 7 / 5 và thời gian bay đạn 10 ô/giây; tầm xa chặn vật thể, xe bắn xuyên.
- Chốt vị trí xây hợp lệ, xếp hàng spawn khi thiếu ô trống, giới hạn dân số và công trình.
- Hỗ trợ đủ **10 đội chia 2 phe, 5 tài khoản/đội**; shared control, shared limits và xử lý trùng lệnh từ nhiều người.

### P2 — Chất lượng trải nghiệm và cân bằng (chưa cần)

- Định lượng HP/damage/giá, tốc độ khai thác, tốc độ tấn công, số tài nguyên khởi điểm, kích thước map và thời lượng trận.
- UI pixel art hoàn chỉnh, hiệu ứng đi/đánh/khai thác, panel đội/đồng minh, replay và thống kê.
- Matchmaking, xếp hạng, lịch sử trận, bảo vệ chống spam nâng cao, kịch bản/tutorial theo mức kiến thức `print` → `for` → `if` → hàm.

## 12. Điểm cải tiến đáng cân nhắc nhưng **không chặn MVP**

1. **Quy tắc rõ cho `for`:** ưu tiên DSL `REPEAT` hoặc runner theo dõi nguồn lệnh thay vì tin vào chuỗi stdout; nếu không sẽ không thể ngăn việc copy nhiều `print` một cách đúng đắn.
2. **Lệnh `WAIT` có ý nghĩa:** nếu chỉ gửi một lệnh rồi đứng yên, `WAIT` có cùng trạng thái kết quả với không gửi gì; giá trị thật xuất hiện khi chạy chương trình nhiều lệnh theo thời gian, hoặc phối hợp đồng đội. Vẫn đưa `WAIT` vào MVP để giữ giao thức ổn định.
3. **Đặt tên và ID ổn định:** tên dễ gõ cho người mới, ID nội bộ chống đụng độ giữa nhiều đội. Log nên in cả tên và ID khi cần debug.
4. **Hiển thị lệnh bị từ chối:** mã lỗi ngắn `UNKNOWN_UNIT`, `INVALID_DIRECTION`, `WORKER_REQUIRED`, `NOT_ON_RESOURCE`, `INVALID_TARGET`, về sau thêm `NO_SPACE`, `RATE_LIMIT`, `DUPLICATE_ACTION_IN_SUBMISSION`.
5. **Đồng đội chia việc:** về sau có thể gắn nhãn người gửi vào từng sự kiện để dễ phối hợp và chẩn đoán xung đột, nhưng quyền tác động vẫn là quyền chung của đội.
6. **Tách mô phỏng khỏi đồ họa:** logic là trạng thái + lệnh → trạng thái mới; UI chỉ render. Nhờ vậy các phép thử lệnh đơn rất dễ viết trước khi vẽ pixel art.
7. **Giới hạn thực thi code khi mở `for`:** sandbox với timeout, RAM/CPU/output cap, không cho truy cập network/file system; cần thiết để không ai dùng vòng lặp vô hạn làm nghẽn server.

## 13. Những quyết định tạm để nhóm triển khai không tự suy diễn

- Bản đồ **vuông**, 4 hướng; kích thước map cấu hình được.
- Thắng theo **phe**, nhưng thử MVP chỉ cần mỗi phe một đội. Bị mất HQ thì **đội bị loại**; phe thua khi không còn đội nào có HQ.
- `SPAWN` ở HQ được cho phép tối thiểu với `WORKER`; việc xuất quân khác ở HQ hay chỉ từ BARRACKS sẽ chốt khi hoàn thiện luật sản xuất.
- Trong **full**, quyền tạo/xóa bot là quyền của **đội**; các tài khoản cùng đội dùng chung giới hạn 50 đơn vị và 10 hành động/s/bot.
- Trong **MVP**, chỉ đơn vị `WORKER` được khai thác/xây; tài nguyên vô hạn; xây xong chỉ bằng một lệnh; mọi đơn vị đi 1 ô/lệnh; đánh trực tiếp xuyên; bỏ qua collision và biên map theo chủ đích.
- Không thêm BFS/A*, tự di chuyển, tự tìm mục tiêu, tự khai thác hoặc AI điều khiển thay người chơi.
- **Không triển khai hoặc hứa có tính năng `for` trong MVP**. Cơ chế phân biệt `print` thủ công và output từ vòng lặp là nghiên cứu/thiết kế cho P1.

---

**Tinh thần thiết kế:** Một người biết mỗi `print` vẫn ra được lệnh và nhìn thấy tác dụng ngay. Khi hệ thống mở chế độ lặp, người biết `for` có thể khai thác, di chuyển và điều khiển nhiều đơn vị hiệu quả hơn trong cùng quỹ thời gian. Thành công của MVP là **chứng minh pipeline lệnh → xử lý server → cập nhật bản đồ online**, không phải hoàn thiện tất cả luật RTS từ ngày đầu.
