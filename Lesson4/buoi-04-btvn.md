# BÀI TẬP VỀ NHÀ — BUỔI 04

## HITSHOP — MỞ RỘNG QUẢN LÝ SẢN PHẨM VÀ ĐƠN HÀNG

**Ngôn ngữ:** Java (JDK 17)  
**Hình thức:** Tiếp tục làm trên repository `HitShop` của buổi 03, không tạo dự án mới.

## I. Nhìn lại buổi 03

Ở buổi trước, các bạn đã xây dựng một chương trình quản lý giỏ hàng với các lớp `Product`, `Customer`, `CartItem` và danh sách `List<CartItem>`. Chương trình đã có thể thêm sản phẩm vào giỏ, xóa một dòng hàng, xem giỏ và tính tổng tiền hàng.

Những chức năng cũ phải tiếp tục hoạt động đúng sau khi mở rộng:

| Tình huống | Kết quả cần giữ |
|---|---|
| Giỏ hàng trống | Thông báo giỏ trống, tổng tiền bằng `0` |
| Thêm `P01 × 2` và `P02 × 1` | Có hai dòng hàng, tổng tiền hàng bằng `350000` |
| Xóa vị trí không tồn tại | Có thông báo phù hợp, giỏ hàng không thay đổi |

**Cây thư mục ở đầu buổi:**

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Customer.java
    └── CartItem.java
```

## II. Bài tập lớn: HitShop

### Bối cảnh

HitShop đang bán các sản phẩm thông thường và chuẩn bị kinh doanh thêm **sách** và **đồ điện tử**. Mỗi loại đều là sản phẩm, nhưng cách giới thiệu và phí vận chuyển lại khác nhau.

Cửa hàng cũng muốn quản lý **đơn hàng** của khách. Khi đơn còn chờ xử lý, khách có thể thêm hoặc bỏ sản phẩm. Khi đơn đã được xác nhận hoặc đã hủy, các thao tác làm thay đổi đơn không còn được phép.

Nhiệm vụ của các bạn là mở rộng chương trình đang có để giải quyết những yêu cầu này. **Không chia thành ba chương trình riêng.**

### A. Thuộc tính cần bổ sung

Chỉ liệt kê những thuộc tính **mới so với buổi 03**. Các thuộc tính cũ của `Product`, `Customer` và `CartItem` được giữ lại.

#### `Book extends Product` — lớp mới

```java
private String author;
```

Một cuốn sách có thêm tên tác giả. Những thông tin chung như mã, tên, giá và tồn kho vẫn dùng từ `Product`.

#### `ElectronicProduct extends Product` — lớp mới

```java
private int warrantyMonths;
```

Đồ điện tử có thêm thời hạn bảo hành, tính bằng tháng.

#### `Order` — lớp mới

```java
private String id;
private Customer customer;
private List<CartItem> items;
private String status;
```

Mỗi đơn thuộc về một khách hàng và có thể chứa nhiều dòng hàng. Đơn mới có danh sách hàng rỗng và trạng thái `PENDING` (đang chờ). Hai trạng thái còn lại là `CONFIRMED` (đã xác nhận) và `CANCELLED` (đã hủy).

### B. Phương thức và phần xử lý cần bổ sung

Phần khởi tạo đối tượng và các phương thức đọc, cập nhật thông tin thông thường, các bạn tự viết theo kiến thức đã học. Dưới đây chỉ có **phương thức mới** hoặc **quy tắc xử lý mới**, được nhóm theo từng lớp.

#### 1. `Product`

```java
String getDescription()
```

Trả về tên sản phẩm. Đây là cách giới thiệu mặc định dành cho sản phẩm thông thường.

```java
double shippingFee(int quantity)
```

Tính phí vận chuyển bằng `10000 × quantity`. Nếu số lượng không lớn hơn `0`, hãy báo cho người dùng biết yêu cầu không hợp lệ thay vì trả về một kết quả sai.

```java
void decreaseStock(int quantity)
```

Giảm tồn kho khi số lượng giảm là số dương và không vượt quá lượng hàng đang có. Nếu không đủ hàng hoặc nhập số lượng sai, tồn kho phải giữ nguyên.

```java
void increaseStock(int quantity)
```

Tăng tồn kho khi số lượng nhập là số dương. Không cho phép nhập một giá trị khiến số lượng trong kho bị sai do vượt giới hạn của kiểu `int`; nếu xảy ra, lượng hàng cũ vẫn phải được giữ nguyên.

**Logic cần bổ sung vào những phần đã có:** Mã và tên sản phẩm không được để trống; giá phải là số dương hợp lệ; tồn kho không được âm. Mã sản phẩm không được tùy ý thay đổi sau khi tạo. Giá bán có thể cập nhật, nhưng nếu người dùng thử nhập giá không hợp lệ thì phải giữ lại giá trước đó. Tồn kho chỉ được thay đổi thông qua các thao tác nhập thêm hoặc giảm hàng, không cho phép gán trực tiếp một con số bất kỳ.

#### 2. `Book`

```java
String getDescription()
```

Giới thiệu sách theo mẫu `Tên sách — tác giả: Tên tác giả`. Ví dụ: `Java cơ bản — tác giả: An Nguyen`. Tên tác giả phải có nội dung, không được để trống.

```java
double shippingFee(int quantity)
```

Sách có phí vận chuyển `5000 × quantity`. Số lượng không hợp lệ vẫn phải được xử lý như ở `Product`.

#### 3. `ElectronicProduct`

```java
String getDescription()
```

Giới thiệu theo mẫu `Tên sản phẩm — bảo hành: N tháng`. Ví dụ: `Bàn phím — bảo hành: 12 tháng`. Thời hạn bảo hành phải lớn hơn `0`.

```java
double shippingFee(int quantity)
```

Đồ điện tử có phí vận chuyển `30000 × quantity`. Không chấp nhận số lượng bằng `0` hoặc âm.

**Điểm cần chú ý khi làm ba lớp sản phẩm:** Một danh sách `List<Product>` phải chứa được cả sản phẩm thông thường, sách và đồ điện tử. Khi duyệt danh sách, hãy gọi cùng tên phương thức `getDescription()` hoặc `shippingFee(...)` trên từng phần tử, nhưng kết quả phải đúng theo loại sản phẩm thật. Đừng viết một loạt câu lệnh kiểm tra “nếu là sách”, “nếu là đồ điện tử” trong phần hiển thị để tự quyết định cách xử lý.

#### 4. `CartItem`

```java
double getShippingFee()
```

Tính phí vận chuyển của dòng hàng bằng cách sử dụng số lượng mua và cách tính phí của sản phẩm đang được chứa trong dòng hàng. Chẳng hạn, hai cuốn sách có tổng phí vận chuyển là `10000`.

**Logic cần bổ sung:** Một dòng hàng chỉ hợp lệ khi có sản phẩm và số lượng mua lớn hơn `0`. Những phương thức tính tiền hàng từ buổi 03 vẫn phải giữ nguyên kết quả cũ.

#### 5. `Order`

```java
void addItem(CartItem item)
```

Chỉ thêm hàng khi đơn còn `PENDING`. Không nhận dòng hàng bị thiếu thông tin, số lượng lớn hơn tồn kho hoặc sản phẩm có mã đã xuất hiện trong đơn. Nếu không thể thêm, các dòng hàng trước đó phải giữ nguyên.

```java
void removeItem(String productId)
```

Xóa dòng hàng có mã sản phẩm tương ứng khi đơn còn `PENDING`. Nếu không tìm thấy sản phẩm, hãy thông báo rõ và không làm thay đổi đơn.

```java
double getSubtotal()
```

Cộng tiền hàng của tất cả dòng trong đơn, **chưa tính vận chuyển**. Đơn chưa có sản phẩm thì kết quả là `0`.

```java
double getShippingFee()
```

Cộng phí vận chuyển của các dòng hàng. Mỗi dòng tự tính dựa trên loại sản phẩm của mình, nên `Order` không cần phân loại sách hay đồ điện tử. Đơn rỗng có phí vận chuyển bằng `0`.

```java
double getTotal()
```

Trả về số tiền khách phải thanh toán: `getSubtotal() + getShippingFee()`.

```java
void confirm()
```

Chỉ xác nhận khi đơn đang `PENDING` và có ít nhất một dòng hàng. Ngay lúc xác nhận, phải kiểm tra lại xem kho còn đủ cho **tất cả** các dòng hàng hay không, vì tồn kho có thể đã thay đổi sau khi thêm hàng vào đơn. Nếu có bất kỳ dòng nào không đủ hàng, đơn vẫn ở trạng thái `PENDING` và không bị mất dòng hàng nào. Nếu mọi thứ hợp lệ, chuyển sang `CONFIRMED`.

```java
void cancel()
```

Chỉ hủy đơn đang `PENDING`; đơn chưa có hàng vẫn có thể hủy. Một đơn đã `CONFIRMED` hoặc `CANCELLED` thì không được đổi sang trạng thái khác.

**Logic cần bổ sung khi bảo vệ đơn hàng:** Sau khi đơn được xác nhận hoặc hủy, không cho thêm, xóa hay sửa các dòng hàng trong đơn nữa. Trạng thái đơn cũng không được thay đổi bằng cách gán trực tiếp từ bên ngoài. Hãy chú ý cả trường hợp người dùng lấy danh sách hoặc một dòng hàng ra ngoài rồi chỉnh sửa chúng: việc đó không được âm thầm làm thay đổi dữ liệu mà `Order` đang quản lý. Khi một thao tác bị từ chối, nội dung và trạng thái hợp lệ trước đó vẫn phải được giữ nguyên.

Ở giai đoạn này, thêm hàng vào đơn và xác nhận đơn **chỉ kiểm tra tồn kho, chưa tự trừ số lượng trong kho**.

### C. Dữ liệu mẫu

| Mã | Loại | Tên sản phẩm | Giá | Tồn kho | Thông tin riêng |
|---|---|---|---:|---:|---|
| `P01` | `Product` | Áo thun | 150000 | 20 | — |
| `P02` | `Product` | Bình nước | 50000 | 10 | — |
| `P03` | `Book` | Java cơ bản | 120000 | 5 | Tác giả: An Nguyen |
| `P04` | `ElectronicProduct` | Bàn phím | 400000 | 3 | Bảo hành: 12 tháng |

Khách hàng mẫu: `C01 / Linh Nguyễn / 0900000001`.

### D. Phần giao diện cần bổ sung

Giữ lại menu và những chức năng đã có ở buổi 03. Chỉ cần bổ sung các thao tác sau vào chương trình console:

- Khi xem sản phẩm, hiển thị được mô tả riêng của sách và đồ điện tử bên cạnh giá và số lượng còn trong kho.
- Cho phép tạo đơn cho khách hàng, thêm sản phẩm vào đơn và xóa sản phẩm khỏi đơn khi còn chờ xác nhận.
- Cho phép xem nội dung đơn, gồm tiền hàng, phí vận chuyển, tổng thanh toán và trạng thái.
- Cho phép xác nhận hoặc hủy đơn; nếu thao tác không hợp lệ thì thông báo bằng lời dễ hiểu và giữ nguyên dữ liệu.

Những trường hợp khó nhập từ menu có thể được thử bằng một đoạn mã ngắn trong `main`.

## III. Năm tình huống kiểm tra

Năm tình huống sau dùng để kiểm tra bài tập lớn. Hãy chạy từng tình huống với dữ liệu mẫu được tạo mới, để kết quả của bài thử này không ảnh hưởng đến bài thử khác.

| STT | Tình huống | Kết quả mong đợi |
|---|---|---|
| **1** | Đưa bốn sản phẩm mẫu vào `List<Product>`, sau đó lần lượt gọi mô tả và tính vận chuyển cho số lượng `2`. | Mô tả đúng từng sản phẩm; phí vận chuyển tương ứng là `20000`, `20000`, `10000`, `60000`. |
| **2** | Đổi giá P01 thành `160000`, rồi thử đổi thành `0`. Giảm kho P01 đi `2`, sau đó thử giảm nhiều hơn lượng còn lại. | Giá cuối là `160000`, tồn kho cuối là `18`; thao tác sai không làm mất dữ liệu hợp lệ. |
| **3** | Tạo đơn O01 cho C01, thêm `P01 × 2` và `P02 × 1`; sau đó thử thêm P01 lần nữa và thử thêm `P04 × 4`. | Đơn chỉ có hai dòng hợp lệ; tiền hàng `350000`, phí vận chuyển `30000`, tổng thanh toán `380000`. |
| **4** | Tạo O02 với `P04 × 3`; sau đó giảm tồn kho P04 xuống `2` rồi mới xác nhận. | Không xác nhận được, trạng thái vẫn `PENDING`, nội dung đơn không thay đổi. |
| **5** | Xác nhận một đơn hợp lệ rồi thử thêm hàng và hủy đơn. Với một đơn khác, hủy ngay từ đầu rồi thử xác nhận. | Đơn đầu vẫn `CONFIRMED`, đơn sau vẫn `CANCELLED`; thao tác bị từ chối không làm thay đổi dữ liệu. |

## IV. Lưu ý khi làm bài

Ở buổi học này, điều quan trọng không chỉ là chương trình chạy đúng khi người dùng nhập dữ liệu đẹp. Hãy thử suy nghĩ xem chuyện gì xảy ra nếu họ nhập sai số lượng, thêm cùng một sản phẩm hai lần, thay đổi kho sau khi tạo đơn, hoặc cố sửa một đơn đã xác nhận.

Mỗi lớp nên chịu trách nhiệm với dữ liệu và công việc của chính nó. Sản phẩm tự biết cách giới thiệu và tính phí vận chuyển; đơn hàng tự biết khi nào được thêm, xóa, xác nhận hay hủy. Đừng dồn toàn bộ cách xử lý vào `main` hoặc viết lại những phần đã làm đúng ở buổi trước.

Khi chương trình từ chối một thao tác, không chỉ cần có thông báo mà còn phải giữ được dữ liệu hợp lệ trước đó. Đây là điểm các bạn nên tự kiểm tra kỹ khi hoàn thiện bài.

## V. Tổng kết buổi 04

Sau bài tập, HitShop sẽ quản lý được nhiều loại sản phẩm trong cùng một danh sách. Những loại sản phẩm đó có thể dùng chung các thông tin cơ bản nhưng vẫn có cách mô tả và tính vận chuyển khác nhau. Chương trình cũng có thêm đơn hàng với các quy tắc rõ ràng để hạn chế việc thay đổi dữ liệu không đúng lúc.

Qua quá trình làm bài, các bạn cần hiểu được **vì sao dữ liệu nên được bảo vệ**, **vì sao các lớp có điểm chung nên tận dụng kế thừa** và **vì sao cùng một lời gọi phương thức lại có thể dẫn đến cách xử lý khác nhau**. Đây là những ý tưởng sẽ tiếp tục được sử dụng khi HitShop mở rộng trong các buổi sau.

**Cây thư mục sau khi hoàn thành:**

```text
src/
├── HitShop.java
└── model/
    ├── Product.java
    ├── Book.java
    ├── ElectronicProduct.java
    ├── Customer.java
    ├── CartItem.java
    └── Order.java
```

**Nộp bài:** Đẩy mã nguồn lên repository `HitShop` trên GitHub và bình luận đường dẫn bài làm dưới bài đăng giao bài.
