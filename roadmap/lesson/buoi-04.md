# Buổi 4: Encapsulation Chuyên Sâu, Kế Thừa và Đa Hình

---

## Mục tiêu buổi học

- Kiểm soát việc thay đổi dữ liệu và trạng thái qua method.
- Dùng extends, constructor lớp cha và super.
- Override method đúng mục đích; dùng tham chiếu kiểu cha để làm việc với các kiểu con.
- Refactor điều kiện theo loại thành hành vi đa hình.

> **Cầu nối:** Buổi 3 đã gom dữ liệu và hành vi vào object, nhưng field private cùng getter/setter chưa tự bảo đảm dữ liệu luôn hợp lệ. Encapsulation tập trung kiểm soát trạng thái; kế thừa và đa hình giải quyết riêng việc mô tả các biến thể có hành vi chung và khác nhau.

## I. Kiểu Chung Và Biến Thể Cụ Thể

Trong nhiều mô hình, một nhóm đối tượng chia sẻ thuộc tính/hành vi nhưng mỗi loại cụ thể có phần khác nhau. Ví dụ các hình học cùng có phép tính diện tích nhưng công thức riêng; các loại phương tiện cùng có thông tin cơ bản nhưng cách tính phí khác nhau. Nếu dồn mọi biến thể vào một class với field không liên quan và các nhánh kiểm tra kiểu, mỗi lần thêm loại mới phải sửa nhiều nơi.

~~~java
if ("BUS".equals(type)) { /* in số chỗ */ }
else if ("BICYCLE".equals(type)) { /* in số tốc độ */ }
~~~

Cách này dùng chuỗi để biểu diễn loại và buộc mọi nơi phải biết tất cả loại hiện có. Thêm `FOOD` có thể khiến phải sửa nhiều nhánh; gõ sai chuỗi còn có thể bỏ sót hành vi. Nếu các loại thật sự chia sẻ một thao tác như mô tả, kế thừa/override cho phép gọi một method chung thay vì kiểm tra nhãn ở từng nơi.

Kế thừa biểu diễn quan hệ lớp con “là một loại của” lớp cha và tái sử dụng phần chung. Đa hình cho phép code làm việc qua kiểu chung, trong khi method được chọn theo object thực tế lúc chạy. Đây là hai ý liên quan nhưng khác nhau: `extends` tạo quan hệ lớp; overriding cung cấp hành vi thay thế; dispatch động chọn bản cài đặt phù hợp.

## II. Encapsulation Và Kiểm Soát Dữ Liệu

Encapsulation gom trạng thái và hành vi quản lý trạng thái trong class, đồng thời giới hạn cách code bên ngoài truy cập. Mục tiêu không chỉ là “đổi field thành private”, mà là bảo đảm object luôn giữ được các điều kiện hợp lệ (invariant), chẳng hạn giá dương hoặc tồn kho không âm. Nếu mọi nơi có thể gán field tùy ý, class không thể bảo đảm quy tắc của chính nó.

Ví dụ class dưới khởi tạo số dư bằng 0. Bên ngoài không thể sửa `balance` trực tiếp; chỉ các method công khai mới được thay đổi nó:

~~~java
public class BankAccount {
    private double balance;

    public BankAccount() {}

    public double getBalance() { return balance; }

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        return true;
    }
}
~~~

`deposit(200)` chỉ cộng số dương; `withdraw(1500)` không đổi số dư nếu số dư hiện tại thấp hơn yêu cầu. Các method chỉ đổi trạng thái khi yêu cầu hợp lệ, giữ invariant số dư không âm. Trong chương trình lớn hơn có thể dùng exception thay `false` để báo nguyên nhân chi tiết; exception học ở buổi 6.

Không phải mọi thuộc tính đều cần setter. Chỉ cung cấp thao tác cập nhật mà mô hình thật sự cho phép; nếu muốn tạo object hợp lệ một lần rồi giữ nguyên giá trị, có thể chỉ cung cấp constructor và getter.

## III. Kế Thừa Với extends Và super

Lớp con kế thừa thành viên truy cập được của lớp cha, có thể bổ sung field/method riêng và ghi đè method instance phù hợp. Constructor không được kế thừa như method thường: khi khởi tạo object lớp con, constructor lớp con gọi constructor lớp cha bằng `super(...)`. Nếu không viết lời gọi tường minh, Java chỉ chèn `super()` khi lớp cha có constructor không tham số dùng được.

**Tệp `Vehicle.java`:**

~~~java
public class Vehicle {
    private String model;

    public Vehicle(String model) {
        this.model = model;
    }

    public String getModel() { return model; }

    public String getDescription() { return "Phương tiện: " + model; }
}
~~~

**Tệp `Bicycle.java`:**

~~~java
public class Bicycle extends Vehicle {
    private int gearCount;

    public Bicycle(String model, int gearCount) {
        super(model);
        this.gearCount = gearCount;
    }

    @Override
    public String getDescription() {
        return getModel() + " — xe đạp " + gearCount + " tốc độ";
    }
}
~~~

`extends` khai báo lớp con; `super(...)` gọi constructor lớp cha và phải là lệnh đầu tiên trong constructor; `@Override` giúp compiler kiểm tra method được viết lại.

Field `private` của lớp cha không được truy cập trực tiếp từ lớp con; lớp con dùng method public/protected phù hợp do lớp cha cung cấp. Giữ dữ liệu private giúp lớp cha kiểm soát invariant của nó thay vì để subclass sửa trạng thái tùy tiện. Kế thừa chỉ phù hợp khi quan hệ “là một loại” phản ánh đúng mô hình; dùng kế thừa chỉ để tiết kiệm vài dòng code thường tạo ràng buộc không cần thiết.

## IV. Đa Hình Trong Danh Sách Phương Tiện

Trước hết xét ví dụ trung tính, có thể biên dịch độc lập: mọi phương tiện có mô tả chung, còn loại cụ thể có thể mô tả chi tiết hơn.

~~~java
public class PolymorphismDemo {
    static class Vehicle {
        String description() { return "Phương tiện"; }
    }

    static class Bicycle extends Vehicle {
        @Override
        String description() { return "Xe đạp"; }
    }

    public static void main(String[] args) {
        Vehicle vehicle = new Bicycle();
        System.out.println(vehicle.description()); // Xe đạp
    }
}
~~~

Kiểu biến `Vehicle` quyết định những method nào có thể được gọi khi biên dịch; object thực tế `Bicycle` quyết định phiên bản override nào chạy lúc runtime. Đây là dispatch động. Một list kiểu cha có thể chứa các object con nếu chúng là subtype hợp lệ.

~~~java
import java.util.ArrayList;
import java.util.List;

// Đoạn sau đặt trong main; Vehicle và Bicycle là các class phía trên.
List<Vehicle> vehicles = new ArrayList<>();
vehicles.add(new Vehicle("Xe buýt điện"));
vehicles.add(new Vehicle("Tàu điện"));
vehicles.add(new Bicycle("Touring", 18));

for (Vehicle vehicle : vehicles) {
    System.out.println(vehicle.getDescription());
}
~~~

Khi compiler đọc `vehicle.getDescription()`, nó chỉ cần biết biến có kiểu `Vehicle` và Vehicle khai báo method này. Khi chạy, Java chọn phần cài đặt theo object thực tế: object Vehicle dùng bản cha, Bicycle dùng bản override. Đa hình giúp thêm kiểu mới mà không cần viết nhánh kiểm tra loại ở mọi nơi. Dùng khi các biến thể thật sự chia sẻ một hành vi nhưng cần cách thực hiện riêng; không dùng kế thừa chỉ để gom các field tình cờ giống nhau.

List có kiểu Vehicle nhưng phần tử vẫn giữ object cụ thể như Bicycle. Khi gọi `getDescription()`, Java chạy method override của object đó.

~~~text
Biến có kiểu khai báo: Vehicle
          |
          v
Object thực tế: Bicycle
          |
vehicle.description() -> Bicycle.description()
~~~

## V. Trạng Thái Có Quy Tắc Chuyển

Phần này bổ sung một ví dụ độc lập về kiểm soát trạng thái; trọng tâm vẫn là encapsulation, kế thừa và đa hình.

Một lượt đăng ký có trạng thái và chỉ được chuyển theo các quy tắc nhất định. Buổi này dùng String để biểu diễn status; enum thuộc buổi 8.

Đóng gói trạng thái nghĩa là không để nơi gọi tùy ý gán `status`. Thay vào đó, method như `approve()` kiểm tra trạng thái hiện tại rồi chỉ cho phép chuyển từ `PENDING` sang `APPROVED`. Cách này hữu ích khi có quy tắc chuyển trạng thái; với dữ liệu không có invariant, getter/setter đơn giản có thể đủ.

~~~java
public class Registration {
    private String status = "PENDING";

    public void approve() {
        if (status.equals("PENDING")) status = "APPROVED";
    }
    public void reject() {
        if (status.equals("PENDING")) status = "CANCELLED";
    }
    public String getStatus() { return status; }
}
~~~

Điều kiện trong `approve()` và `reject()` minh họa invariant: chỉ đăng ký đang chờ mới được xử lý. Một lượt đã duyệt không thể bị ghi đè thành từ chối. Khi tập trạng thái cố định, enum ở buổi 8 giúp tránh lỗi gõ String.

## Phần Nâng Cao — Tìm Hiểu Thêm

- Overloading là nhiều method cùng tên nhưng khác danh sách tham số; compiler chọn overload dựa trên kiểu và số lượng đối số. Overriding là lớp con thay phần cài đặt method kế thừa; runtime dispatch chọn bản cài đặt theo object thực tế.
- HAS-A biểu diễn một object chứa/sử dụng object khác; composition nhấn mạnh vòng đời/thành phần thuộc về đối tượng chứa. IS-A biểu diễn quan hệ subtype và chỉ nên dùng khi lớp con thay thế được lớp cha.
- Upcasting chuyển tham chiếu lớp con sang lớp cha, thường tự động và an toàn; downcasting cần ép kiểu tường minh, chỉ hợp lệ nếu object thực sự thuộc kiểu đích. `instanceof` có thể kiểm tra trước khi ép, nhưng không nên dùng để quay lại chuỗi if/else theo loại.

**Bài tập tự chọn:** Tạo `Shape` với `Circle` và `Rectangle`, viết overload tính diện tích từ các bộ tham số khác nhau; dự đoán khi nào chọn overload và khi nào gọi override. Thử upcast rồi kiểm tra một downcast hợp lệ/không hợp lệ. Bài này chỉ giúp đào sâu OOP, không thêm chức năng cốt lõi của ứng dụng.

## Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| Constructor lớp con báo lỗi | Chưa gọi constructor cha phù hợp | Gọi super(...) đầu constructor |
| Override không chạy | Sai tên/tham số method | Dùng @Override để phát hiện |
| Field private lớp cha không truy cập được | private chỉ dùng trong class khai báo | Dùng getter hoặc method nghiệp vụ |
| Vẫn có if/else cho từng subtype | Dùng type để rẽ nhánh thay vì hành vi chung | Gọi method override qua Vehicle |

## Tổng Kết Buổi 4

- Encapsulation bảo vệ invariant; kế thừa chia sẻ/đặc tả hành vi có quan hệ IS-A; overriding và đa hình chọn hành vi theo object thực tế.
- Registration minh họa việc bảo vệ trạng thái qua method; Vehicle/Bicycle minh họa kế thừa và đa hình.
- Interface sẽ giải quyết trường hợp nhiều kiểu không cùng lớp cha nhưng vẫn cần chung một hợp đồng.

### Câu Hỏi Ôn Tập

1. Lớp con kế thừa gì từ lớp cha?
2. super trong constructor dùng để làm gì?
3. @Override giúp phát hiện lỗi nào?
4. Đa hình thể hiện ra sao trong List<Vehicle>?
5. Vì sao không nên để nơi gọi thay đổi balance hoặc status tùy ý?

## Bài Tập Về Nhà

Yêu cầu bài tập bắt buộc và phần tự chọn được tách riêng tại [Bài tập về nhà Buổi 04](../BTVN/buoi-04-btvn.md).
## Tài Liệu Tham Khảo

- [Oracle — Inheritance](https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html)
- [Oracle — Overriding and Hiding Methods](https://docs.oracle.com/javase/tutorial/java/IandI/override.html)
- [Oracle — Polymorphism](https://docs.oracle.com/javase/tutorial/java/IandI/polymorphism.html)
