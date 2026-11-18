# Codebase — HitShop BTVN buổi 04

Dựa trên `Codebase/src` của nhánh `develop` trong repository `nddKhanh/Java_Private_2026`, hoàn thành đề BTVN buổi 4 ở nhánh `main` (`Lesson4/buoi-04-btvn.md`).

Giữ cách viết code của codebase: các class model, `Scanner`, method `static`, `switch-case`, vòng lặp `for`, thông báo console tiếng Việt, không thêm framework.

## Nội dung cập nhật

- `Product.isExpensive()` với ngưỡng 10000.
- Thêm `Book extends Product` và `Cake extends Product`, override `isExpensive()` với ngưỡng 20000 và 100000.
- `addOrder()` cho chọn Product/Book/Cake, thêm sản phẩm mới vào danh sách `products`.
- Menu số 5 kiểm tra theo ID và gọi đa hình `product.isExpensive()`.
- Khi sửa sản phẩm của đơn hàng, vẫn có thể chọn cả ba loại.
- Sửa lỗi có sẵn: constructor `Order` chưa gán `this.id = id`.

## Chạy chương trình

Mở terminal ở thư mục `src`:

```powershell
javac -encoding UTF-8 HitShop.java model\*.java
java HitShop
```

Nếu PowerShell không mở rộng wildcard cho `javac`, chạy:

```powershell
javac -encoding UTF-8 HitShop.java model\Product.java model\Customer.java model\Order.java model\Book.java model\Cake.java
java HitShop
```

Nhập `1` để thêm đơn hàng, chọn loại sản phẩm. Nhập `5` và ID vừa thêm để kiểm tra giá.
