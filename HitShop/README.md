# HitShop — Kết quả sau BTVN buổi 1–3

Dự án console hoàn thiện phần bắt buộc của ba buổi, ở trạng thái cuối buổi 3:

- Buổi 1: tính thành tiền bằng đơn giá × số lượng.
- Buổi 2: menu thêm, xóa, xem giỏ, tính tổng và thoát.
- Buổi 3: thay ba danh sách song song bằng Product, Customer, CartItem và List<CartItem>.

## Cấu trúc

```text
HitShop/
├── README.md
├── .gitignore
└── src/
    ├── HitShop.java
    └── model/
        ├── Product.java
        ├── Customer.java
        └── CartItem.java
```

## Chạy với JDK 17 trở lên

Mở terminal tại thư mục HitShop:

```powershell
javac --release 17 -encoding UTF-8 -d out src/HitShop.java src/model/Product.java src/model/Customer.java src/model/CartItem.java
java -Dfile.encoding=UTF-8 -cp out HitShop
```

Trong IntelliJ IDEA: mở thư mục HitShop, chọn Project SDK 17, đánh dấu `src` là Sources Root rồi chạy `HitShop.main`.

## Dữ liệu mẫu

Giỏ ban đầu rỗng. Hai sản phẩm có sẵn: P01 / Áo thun / 150000 / tồn 20; P02 / Bình nước / 50000 / tồn 10. Khách hiện tại: C01 / Linh Nguyễn / 0900000001.

Nhập lần lượt mỗi giá trị trên một dòng:

```text
1
P01
2
1
P02
1
3
4
0
```

Kết quả phần giỏ và tổng:

```text
Khách hàng: C01 | Linh Nguyễn | 0900000001
1. P01 | Áo thun | 150000.0 | SL: 2 | Thành tiền: 300000.0
2. P02 | Bình nước | 50000.0 | SL: 1 | Thành tiền: 50000.0
Tổng tiền: 350000.0
```

Xóa dòng 1 thì còn Bình nước, tổng 50000. Mỗi lần thêm là một dòng mới; quantity không làm thay đổi stock và chưa bị giới hạn bởi stock. Tổng là tiền hàng, chưa cộng VAT/phí vận chuyển của phần tự chọn.
