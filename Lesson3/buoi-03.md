# Buổi 3: Cơ Sở Lập Trình Hướng Đối Tượng Trong Java

## Mục tiêu buổi học

Sau buổi học, học viên có thể:

- Giải thích class dùng để làm gì; nhận diện và viết thuộc tính, phương thức, constructor, getter/setter.
- Giải thích ý nghĩa của `this`; phân biệt class, object, instance và biến tham chiếu.
- Biểu diễn quan hệ HAS-A bằng một thuộc tính có kiểu class khác.
- Nhận biết `toString()`, thành viên static và các mức truy cập cơ bản.

**Kiến thức cần có:** biến, kiểu dữ liệu, phép gán, method, tham số và giá trị trả về của buổi 1–2. Ví dụ dùng cú pháp tương thích JDK 17.

**Mạch bài:** từ mô tả một người học, xác định những gì class Student cần có; sau đó dùng chính Student để giải thích this, object và quan hệ với Address. Mỗi đoạn code bổ sung một ý cho cùng ví dụ. Những đoạn sử dụng object được đặt trong main của một class Demo; mỗi class public nằm trong file cùng tên. Chỉ bài cuối buổi yêu cầu học viên tự viết lời giải tổng hợp.

## I. Vì Sao Cần Lập Trình Hướng Đối Tượng?

### 1. Từ dữ liệu rời đến đối tượng

Ở buổi 2, các danh sách song song giữ những phần dữ liệu liên quan. Xóa ở một danh sách nhưng quên xóa ở danh sách khác làm dữ liệu cùng vị trí không còn thuộc về cùng một đối tượng. Đó là khó khăn của cách tổ chức dữ liệu.

Xét một ví dụ nhỏ khác:

```java
String studentName = "An";
int studentAge = 18;
```

Hai biến cùng mô tả một người học nhưng kiểu dữ liệu của chúng chưa diễn đạt mối liên hệ ấy. Khi có nhiều người học, ta cần biểu diễn “mỗi người có tên và tuổi riêng”.

**Lập trình hướng đối tượng (Object-Oriented Programming — OOP)** là cách tổ chức chương trình quanh các đối tượng. Mỗi đối tượng kết hợp:

- **Trạng thái (state):** dữ liệu mô tả đối tượng ở một thời điểm, chẳng hạn tên và tuổi.
- **Hành vi (behavior):** thao tác đối tượng cung cấp, chẳng hạn giới thiệu tên hoặc cập nhật tuổi.

Gom đúng dữ liệu và hành vi giúp code diễn đạt mối liên hệ rõ hơn, giảm việc truyền các dữ liệu rời ở nhiều nơi. Có class vẫn chưa tự bảo đảm chương trình đúng; ta cần viết quy tắc xử lý phù hợp.

### 2. Thuật ngữ nền tảng

| Thuật ngữ | Ý nghĩa | Ví dụ |
|---|---|---|
| Class — lớp | Khai báo một kiểu, mô tả trạng thái và hành vi của các object thuộc kiểu đó | `Student` |
| Object — đối tượng | Đối tượng cụ thể được tạo khi chương trình chạy | Người học tên An, 18 tuổi |
| Instance — thể hiện | Cách gọi object khi nhấn mạnh nó thuộc class nào | Object An là một instance của Student |
| Field — trường | Biến khai báo trong thân class, ngoài method/constructor | `name`, `age` |
| Method — phương thức | Khối lệnh có tên, mô tả một thao tác | `introduce()` |

Class là mô tả chung; object có dữ liệu cụ thể. Hai object cùng class có cùng cấu trúc nhưng có thể mang trạng thái khác nhau. Object và instance không phải hai loại thực thể riêng.

**Kiểm tra nhanh:** “Student”, “người học An” và “tuổi của An” lần lượt tương ứng với khái niệm nào?

## II. Class Và Những Thành Phần Của Một Class

### 1. Class là gì? Bắt đầu từ một mô tả thực tế

Khi nói “một người học có tên, tuổi và có thể giới thiệu bản thân”, ta đang mô tả hai nhóm thông tin:

| Câu hỏi | Mô tả thực tế | Thành phần trong Java |
|---|---|---|
| Người học có những thông tin gì? | Tên, tuổi | Thuộc tính — field |
| Người học thực hiện được thao tác gì? | Giới thiệu bản thân | Phương thức — method |

**Class** là nơi khai báo mô tả chung đó thành một kiểu dữ liệu do ta định nghĩa. Từ kiểu Student, chương trình có thể biểu diễn nhiều người học khác nhau nhưng cùng có cấu trúc tên, tuổi và hành vi giới thiệu.

Khung khai báo ban đầu:

```java
public class Student {
    // Khai báo các thành phần của Student tại đây.
}
```

`class` là từ khóa, Student là tên class. Theo quy ước, tên class viết PascalCase. Với class public, tên file phải là `Student.java`. Cặp ngoặc nhọn bao quanh thân class.

Một class có thể chứa thuộc tính, phương thức và constructor. Getter/setter cũng là phương thức. Không phải class nào cũng bắt buộc khai báo đủ tất cả; ta chọn thành phần cần thiết theo điều muốn mô tả.

**Tổ chức class bằng package:** package là không gian tên để nhóm các class liên quan. Ví dụ Student thuộc package school thì file nằm ở `src/school/Student.java` và bắt đầu bằng `package school;`. File ngoài package đó dùng `import school.Student;` để gọi tên ngắn Student. Import không tạo đối tượng hoặc thay đổi quyền truy cập. Trong các ví dụ tiếp theo, tạm bỏ package để tập trung đọc cấu trúc class.

### 2. Thuộc tính — đối tượng cần lưu những gì?

**Thuộc tính (field)** là biến được khai báo trong class, bên ngoài phương thức và constructor, dùng để giữ trạng thái của đối tượng.

Với người học, tên và tuổi cần được giữ lại để các thao tác sau có thể sử dụng:

```java
public class Student {
    private String name;
    private int age;
}
```

- String phù hợp với tên; int phù hợp với tuổi nguyên.
- name và age là tên thuộc tính.
- private giới hạn việc truy cập trực tiếp từ bên ngoài. Trước mắt hiểu rằng class khác sẽ dùng các phương thức được Student cho phép; các mức truy cập được giới thiệu ở phần nâng cao.
- Mỗi người học sẽ có tên và tuổi riêng. Không đặt dữ liệu riêng của mọi người vào một biến dùng chung.

Nếu chưa có phép gán khác, field int có giá trị mặc định 0, double là 0.0, boolean là false, kiểu tham chiếu như String là null. Null nghĩa là chưa tham chiếu đến đối tượng nào; không phải chuỗi rỗng `""`.

Giá trị mặc định là quy tắc của Java, chưa chắc phù hợp với ý nghĩa thực tế. Chẳng hạn tên null và tuổi 0 thường chưa đủ để mô tả người học cần dùng. Ta sẽ giải quyết việc đặt giá trị ban đầu bằng constructor.

### 3. Phương thức — đối tượng làm được gì với dữ liệu?

**Phương thức (method)** là khối lệnh có tên mô tả một thao tác. Method có thể đọc dữ liệu, thay đổi dữ liệu hoặc tính và trả kết quả.

Người học đã có tên và tuổi; hành vi giới thiệu sẽ sử dụng chính hai thuộc tính ấy:

```java
public String introduce() {
    String message = "Tôi là " + name + ", " + age + " tuổi";
    return message;
}
```

Đoạn này được đặt trong thân Student, sau các field.

| Thành phần | Ý nghĩa trong ví dụ |
|---|---|
| public | Cho phép bên ngoài gọi phương thức khi class cũng truy cập được |
| String | Kiểu dữ liệu của kết quả trả về |
| introduce | Tên phương thức, đặt theo thao tác |
| () | Không cần nhận tham số |
| Thân phương thức | Tạo chuỗi từ trạng thái hiện có |
| return message | Trả kết quả cho nơi gọi |

message là **biến cục bộ**, chỉ dùng trong lần gọi method này. name và age là **field**, lưu trạng thái gắn với người học và được các method của Student sử dụng.

Khác field, biến cục bộ không được tự cấp giá trị mặc định để đọc: nếu viết `String message;` rồi return ngay khi chưa gán, code không biên dịch.

Phương thức introduce trả chuỗi, không tự in. Việc hiển thị kết quả được thực hiện ở nơi gọi bằng println. Method không trả kết quả thì khai báo void; method cần dữ liệu đầu vào thì khai báo tham số, giống kiến thức buổi 2.

### 4. Constructor — đặt trạng thái ban đầu như thế nào?

Khi tiếp nhận thông tin một người học, ta cần biết tên và tuổi ngay từ đầu. **Constructor (hàm khởi tạo)** thiết lập trạng thái ban đầu khi tạo đối tượng.

Thêm constructor có tham số vào Student:

```java
public Student(String initialName, int initialAge) {
    name = initialName;
    age = initialAge;
}
```

initialName và initialAge là tham số nhận dữ liệu; name và age là field được gán. Dùng tên tham số khác field ở bước này để dễ phân biệt hai vai trò.

Khi viết:

```java
Student student = new Student("An", 18);
```

Java tạo một Student và gọi constructor với các đối số "An", 18. Constructor nhận hai giá trị đó qua tham số rồi gán vào thuộc tính. Cách tạo và biến student sẽ được phân tích kỹ ở mục IV.

Constructor khác method thông thường ở các điểm:

| Tiêu chí | Constructor | Method thông thường |
|---|---|---|
| Tên | Trùng tên class: Student | Theo hành vi: introduce |
| Kiểu trả về | Không khai báo, kể cả void | Khai báo kiểu kết quả hoặc void |
| Khi sử dụng trong bài | Khi tạo đối tượng bằng new | Khi gọi thao tác trên đối tượng đã có |
| Ý nghĩa | Thiết lập trạng thái ban đầu | Đọc, tính toán hoặc thay đổi trạng thái |

`public void Student()` là method, không phải constructor.

**Constructor không tham số:** có thể cung cấp giá trị ban đầu khi chưa nhận dữ liệu từ bên ngoài:

```java
public Student() {
    name = "Chưa đặt tên";
    age = 18;
}
```

Student có thể khai báo cả hai constructor trên. `new Student()` dùng bản không tham số; `new Student("An", 18)` dùng bản hai tham số. Số lượng, thứ tự và kiểu đối số phải phù hợp.

**Constructor mặc định:** nếu không khai báo constructor nào, compiler cung cấp constructor mặc định không tham số. Khi đã tự viết bất kỳ constructor nào, compiler không tự thêm bản không tham số. Bản không tham số do ta viết ở trên không phải constructor mặc định do compiler sinh.

### 5. Getter/setter — cho phép đọc và cập nhật những gì?

Constructor giải quyết việc đặt trạng thái ban đầu. Sau đó, code bên ngoài có thể cần đọc tên hoặc cập nhật tuổi. Vì field đang private, ta cung cấp những thao tác cụ thể.

**Getter** là method trả về giá trị thuộc tính:

```java
public String getName() {
    return name;
}

public int getAge() {
    return age;
}
```

Getter thường không nhận tham số, có kiểu trả về phù hợp với dữ liệu cần đọc. Getter boolean thường có tên dạng isActive().

**Setter** là method nhận giá trị mới để cập nhật thuộc tính:

```java
public void setAge(int newAge) {
    age = newAge;
}
```

setAge nhận tuổi mới qua tham số, thay đổi field và không trả kết quả nên dùng void. get/set là quy ước đặt tên, không phải từ khóa đặc biệt.

Liên hệ thực tế: một hồ sơ có thể cho phép xem tên và tuổi, nhưng chỉ cho phép cập nhật một số thông tin. Vì vậy **không bắt buộc mọi field đều có cả getter và setter**. Student trong bài cho đọc tên nhưng chưa cung cấp thao tác sửa tên.

Để field private và cho phép thao tác qua method là bước đầu của **đóng gói**: class giữ dữ liệu cùng hành vi và giới hạn cách bên ngoài sử dụng dữ liệu ấy. Tuy nhiên, setter trên vẫn nhận tuổi âm. Private cộng setter không điều kiện chưa bảo đảm dữ liệu hợp lệ; việc đặt quy tắc bảo vệ trạng thái sẽ học sâu ở buổi 4.

### 6. Nhìn lại các thành phần trong cùng một class

Đây là phiên bản Student đã có đủ những thành phần vừa học:

```java
public class Student {
    private String name;
    private int age;

    public Student() {
        name = "Chưa đặt tên";
        age = 18;
    }

    public Student(String initialName, int initialAge) {
        name = initialName;
        age = initialAge;
    }

    public String introduce() {
        return "Tôi là " + name + ", " + age + " tuổi";
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int newAge) {
        age = newAge;
    }
}
```

Field trả lời “cần lưu gì”, constructor trả lời “bắt đầu với dữ liệu nào”, method trả lời “làm được gì”. Getter/setter là những method cho phép đọc hoặc cập nhật có chọn lọc.

## III. Từ Khóa this

Trong Student ở trên, ta cố ý đặt tên tham số khác field. Nhưng cả hai cùng diễn đạt “tên” và “tuổi”, nên thường muốn đặt tên giống nhau:

```java
public Student(String name, int age) {
    this.name = name;
    this.age = age;
}

public void setAge(int age) {
    this.age = age;
}
```

**this là tham chiếu đến đối tượng hiện tại** trong constructor hoặc method của đối tượng.

Trong `this.age = age;`:

- this.age là field của đối tượng đang được xử lý.
- age bên phải là tham số, đang che tên field.
- Phép gán đưa dữ liệu từ tham số vào field.

Nếu chỉ viết `age = age;`, hai vế đều là tham số. Câu lệnh vẫn có thể biên dịch nhưng không cập nhật field.

Liên hệ thực tế: khi cập nhật tuổi cho hồ sơ của An, “tuổi của hồ sơ này” khác với “tuổi mới vừa nhận”. this giúp chỉ rõ “của đối tượng này”. Khi xử lý hồ sơ Bình, this chỉ đối tượng Bình, không chỉ mọi Student cùng lúc.

Khi tên field không bị che, viết age hoặc this.age trong method của đối tượng đều có thể chỉ cùng field. This không phải tên class và không phải một bản sao đối tượng.

Từ đây, dùng phiên bản constructor và setter có this để tiếp tục phân tích Student.

## IV. Phân Biệt Class, Object Và Instance

### 1. Từ mô tả chung đến những người học cụ thể

Student đã mô tả cấu trúc chung; để biểu diễn An và Bình, ta tạo các đối tượng:

```java
Student an = new Student("An", 18);
Student binh = new Student("Bình", 20);

System.out.println(an.introduce());   // Tôi là An, 18 tuổi
System.out.println(binh.introduce()); // Tôi là Bình, 20 tuổi
```

| Khái niệm | Trong ví dụ | Ý nghĩa |
|---|---|---|
| Class | Student | Kiểu mô tả cấu trúc và hành vi chung |
| Object | Student mang tên An, tuổi 18 | Đối tượng cụ thể có trạng thái riêng |
| Instance | Object An là instance của Student | Nhấn mạnh object thuộc class nào |
| Biến tham chiếu | an, binh | Giữ giá trị tham chiếu để sử dụng object |

Object và instance nói về cùng đối tượng theo hai cách diễn đạt. Class Student chỉ khai báo một lần, nhưng có thể tạo nhiều instance. Mỗi object có field riêng; `an.setAge(19)` không đổi tuổi của Bình.

### 2. Đọc một câu lệnh tạo đối tượng

```java
Student an = new Student("An", 18);
```

- Student bên trái là kiểu của biến.
- an là tên biến tham chiếu.
- new tạo object mới; Student("An", 18) gọi constructor phù hợp.
- Dấu bằng gán tham chiếu của object mới cho biến an.
- `an.getAge()` gọi method trên object mà an đang tham chiếu.

Chỉ viết `Student an;` là khai báo biến, chưa tạo Student. Nếu là biến cục bộ, phải gán trước khi dùng. Với `Student an = null;`, biến đã có giá trị nhưng không trỏ đến object nào; gọi method qua nó gây NullPointerException khi chạy.

### 3. Gán biến tham chiếu không sao chép object

```java
Student an = new Student("An", 18);
Student sameStudent = an;
Student anotherStudent = new Student("An", 18);

sameStudent.setAge(19);
System.out.println(an.getAge());             // 19
System.out.println(anotherStudent.getAge()); // 18
System.out.println(an == sameStudent);       // true
System.out.println(an == anotherStudent);    // false
```

```text
an ──────────┐
             ├──> Student A: name = "An", age = 19
sameStudent ─┘

anotherStudent ──> Student B: name = "An", age = 18
```

Có ba biến nhưng chỉ hai object. Phép gán sameStudent = an sao chép giá trị tham chiếu, không gọi constructor và không tạo bản sao Student. Hai biến cùng nhìn thấy trạng thái của một object.

Với kiểu tham chiếu, == kiểm tra có cùng tham chiếu hay không; không tự so sánh từng thuộc tính. Hai object có cùng dữ liệu ban đầu vẫn có thể là hai object khác nhau.

Điều này khác phép gán `int b = a;`: giá trị được sao chép lúc đó là một số nguyên, không phải tham chiếu đến object.

## V. HAS-A — Một Đối Tượng Có Một Đối Tượng Khác

### 1. Khi thông tin cũng có cấu trúc riêng

Student hiện có tên và tuổi. Nếu muốn mô tả nơi ở gồm thành phố và đường, có thể gom hai thông tin liên quan ấy thành class Address.

```java
public class Address {
    private String city;
    private String street;

    public Address(String city, String street) {
        this.city = city;
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
```

Address cũng dùng đúng các thành phần đã học: field, constructor, getter và setter. Điểm mới nằm ở cách Student sử dụng Address.

### 2. Biểu diễn “Student có một Address”

Phiên bản dưới thay thế Student trước đó để minh họa riêng quan hệ:

```java
public class Student {
    private String name;
    private int age;
    private Address address;

    public Student(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    public String introduce() {
        return "Tôi là " + name + ", " + age
                + " tuổi, ở " + address.getCity();
    }
}
```

**HAS-A** nghĩa là “có một”: Student có một Address. Trong code, quan hệ được thể hiện bằng field `private Address address;`.

Một thuộc tính không chỉ có thể mang kiểu int hay String mà còn có thể mang kiểu class tự định nghĩa. Student không phải Address; Student giữ tham chiếu đến Address và dùng method của Address để đọc thông tin.

### 3. Kết nối với kiến thức tham chiếu

```java
Address home = new Address("Hà Nội", "Đường A");
Student an = new Student("An", 18, home);
System.out.println(an.introduce());
// Tôi là An, 18 tuổi, ở Hà Nội

home.setCity("Huế");
System.out.println(an.introduce());
// Tôi là An, 18 tuổi, ở Huế
```

```text
an ──> Student ── address ──> Address
                                ↑
home ───────────────────────────┘
```

Constructor nhận bản sao giá trị tham chiếu của home rồi gán vào field address. Java luôn truyền tham số theo giá trị; ở đây giá trị đó là tham chiếu. Field address và biến home cùng trỏ đến một Address, nên cập nhật thành phố được nhìn thấy từ cả hai phía.

Nếu gán `home = new Address("Đà Nẵng", "Đường B");`, chỉ home trỏ sang object mới; address trong Student vẫn trỏ đến Address cũ.

Trong ví dụ, Address truyền vào khác null. Khai báo HAS-A không tự bảo đảm field khác null và không tự sao chép object. Buổi này chỉ cần nắm quan hệ “có một”; phân loại quan hệ và so sánh với kế thừa sẽ học sau.

## VI. Giới Thiệu toString()

Sau khi có object, đôi lúc ta muốn xem nhanh nó chứa thông tin gì. Nếu in Student chưa định nghĩa lại toString(), kết quả mặc định thường có dạng `Student@1a2b3c`, chưa thể hiện tên và tuổi. Phần sau @ là giá trị hashCode ở hệ thập lục phân, không nên gọi là địa chỉ bộ nhớ.

`toString()` trả biểu diễn chuỗi của object. Có thể thêm method sau vào Student:

```java
@Override
public String toString() {
    return name + " | " + age;
}
```

Với Student tên An, tuổi 18, `System.out.println(an);` sẽ in `An | 18`. Method trả String, không tự in và không nên thay đổi trạng thái.

@Override giúp compiler kiểm tra rằng ta đang định nghĩa lại đúng method đã có. Cơ chế ghi đè sẽ học sau; ở đây chỉ cần nhận biết công dụng của toString().

## VII. Nâng Cao — static Và Giới Thiệu Access Modifier

### 1. static: thành viên thuộc class

Cho đến đây, name và age thuộc riêng từng Student. Nếu muốn biết đã tạo bao nhiêu Student, số đếm đó cần được dùng chung thay vì mỗi người giữ một số đếm riêng.

Ví dụ rút gọn sau minh họa sự khác nhau:

```java
public class Student {
    private String name;
    private static int count;

    public Student(String name) {
        this.name = name;
        count++;
    }

    public String getName() {
        return name;
    }

    public static int getCount() {
        return count;
    }
}
```

```java
Student an = new Student("An");
Student binh = new Student("Bình");
System.out.println(an.getName());      // An
System.out.println(binh.getName());    // Bình
System.out.println(Student.getCount()); // 2
```

| Thành viên | Thuộc về | Cách sử dụng |
|---|---|---|
| name, getName() | Từng object | an.getName() |
| count, getCount() | Class | Student.getCount() |

Mỗi lần new gọi constructor thì count tăng. `Student other = an;` chỉ gán tham chiếu nên không tăng count. Count đếm số lần tạo trong ví dụ, không phải số object còn được sử dụng.

Method static không có this, vì không gắn với một object hiện tại. Muốn dùng field/method của object từ method static, cần một tham chiếu đến object cụ thể.

Static không có nghĩa bất biến. Final mới giới hạn gán lại biến. Với biến tham chiếu final, ta không gán nó sang object khác nhưng object vẫn có thể đổi trạng thái qua method. Không thêm static vào mọi field để gọi thuận tiện từ main, vì dễ làm dữ liệu riêng của các object thành dữ liệu dùng chung.

### 2. Access modifier: nơi nào được phép truy cập?

Ở phần cơ bản, ta dùng private cho field, public cho constructor và method cần gọi từ bên ngoài. Đây là các mức truy cập, không quyết định dữ liệu thuộc class hay object.

| Mức truy cập của thành viên | Cùng class | Class khác cùng package | Class con khác package | Class khác, khác package |
|---|---|---|---|---|
| private | Có | Không | Không | Không |
| Không ghi modifier — package-private | Có | Có | Không | Không |
| protected | Có | Có | Có điều kiện trong ngữ cảnh kế thừa | Không |
| public | Có | Có | Có | Có |

Bảng xét các class thông thường; class chứa thành viên cũng phải truy cập được. “Default access” nghĩa là không ghi modifier, không phải viết từ khóa default trước field.

Buổi này chỉ nhận biết protected; điều kiện truy cập qua kế thừa sẽ học ở buổi 4. Package con vẫn là package khác. Class top-level chỉ khai báo public hoặc không ghi modifier, không dùng private/protected như field.

## VIII. Bài Tập Trên Lớp — Danh Sách Sinh Viên Và Ô Tô

### 1. Các class cần có

**Class Car**

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| carId | String | Mã ô tô |
| brand | String | Hãng xe, ví dụ Lamborghini, Toyota |
| licensePlate | String | Biển số xe |

Cần có constructor nhận đủ thuộc tính, getter và setter cho các thuộc tính.

**Class Student**

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| studentId | String | Mã sinh viên |
| name | String | Tên sinh viên, ví dụ Khánh |
| age | int | Tuổi sinh viên |
| car | Car | Ô tô của sinh viên |

Student HAS-A Car: mỗi sinh viên giữ một đối tượng Car qua thuộc tính car.

Cần có constructor nhận đủ thuộc tính, getter và setter cho các thuộc tính. Ngoài ra, có phương thức:

| Phương thức | Ý nghĩa |
|---|---|
| `String introduce()` | Không nhận tham số; trả về chuỗi giới thiệu tên, tuổi, hãng xe và biển số của sinh viên |

Ví dụ kết quả introduce():

```text
Mình tên là Khánh, 20 tuổi, đi xe Lamborghini, biển số 30A-12345.
```

**Class Main**

| Phương thức | Ý nghĩa |
|---|---|
| `public static void main(String[] args)` | Nhận mảng String là các đối số khi chạy chương trình, không trả kết quả; tạo danh sách sinh viên, đọc dữ liệu từ bàn phím và thực hiện menu |

### 2. Yêu cầu

1. Tạo một `List<Student>` bằng ArrayList, có sẵn hai sinh viên:

   | Mã sinh viên | Tên | Tuổi | Mã ô tô | Hãng xe | Biển số |
   |---|---|---|---|---|---|
   | SV01 | Khánh | 20 | CAR01 | Lamborghini | 30A-12345 |
   | SV02 | An | 19 | CAR02 | Toyota | 30B-67890 |

2. Nhập từ bàn phím thông tin của một sinh viên khác gồm mã sinh viên, tên, tuổi, mã ô tô, hãng xe và biển số. Tạo Car, tạo Student rồi thêm vào danh sách. Sau bước này danh sách có ba sinh viên.

3. Triển khai menu lặp lại cho đến khi chọn thoát:

   ```text
   1. Thêm sinh viên
   2. Xóa sinh viên
   3. Sửa sinh viên
   4. Giới thiệu sinh viên tên Khánh
   5. Tìm sinh viên có ô tô hãng Lamborghini
   0. Thoát
   ```

   - **Thêm:** nhập thông tin một sinh viên và ô tô từ bàn phím, tạo đối tượng rồi thêm vào danh sách.
   - **Xóa:** hiển thị danh sách kèm số thứ tự, nhập số thứ tự sinh viên cần xóa và xóa sinh viên đó.
   - **Sửa:** hiển thị danh sách kèm số thứ tự, nhập số thứ tự sinh viên cần sửa; nhập thông tin mới cho mã sinh viên, tên, tuổi, mã ô tô, hãng xe và biển số rồi cập nhật qua setter.
   - **Giới thiệu Khánh:** tìm các sinh viên có name là Khánh và hiển thị chuỗi do introduce() trả về. Nếu không có, in đúng câu `Anh Khánh đz`.
   - **Tìm xe Lamborghini:** kiểm tra thuộc tính brand của Car mà mỗi sinh viên đang giữ. Nếu có, hiển thị phần giới thiệu của các sinh viên đó; nếu không có, in `Không có sinh viên nào có ô tô hãng Lamborghini.`.

### 3. Nâng cao — Mã bảo mật và mật khẩu dùng chung

**Yêu cầu 1: Bảo vệ việc đọc và sửa mã sinh viên**

Chuyển toàn bộ thuộc tính của Student sang private. Thêm bước kiểm tra mật khẩu trong getter/setter của studentId: chỉ khi người dùng nhập đúng `Em yêu HIT` mới được lấy hoặc sửa mã sinh viên.

Getter/setter của studentId nhận thêm tham số `String password`. Mỗi lần cần gọi, Main yêu cầu nhập mật khẩu từ bàn phím và truyền vào. Hai phương thức tự kiểm tra mật khẩu: nếu sai, in `Sai mật khẩu.`; getter trả null, setter giữ nguyên mã. Các getter/setter khác giữ nguyên.

**Yêu cầu 2: Một mật khẩu thuộc class, dùng chung cho mọi sinh viên**

Tạo một biến static trong Student để lưu mật khẩu dùng chung, có giá trị ban đầu là `Em yêu HIT`. Getter/setter của studentId tự kiểm tra mật khẩu người dùng nhập với mật khẩu này trước khi cho phép đọc hoặc sửa mã sinh viên. Bổ sung chức năng đổi mật khẩu chung: chỉ đổi khi nhập đúng mật khẩu hiện tại.

Thực hiện các bước kiểm tra sau:

1. Nhập mật khẩu để kiểm tra việc đọc hoặc sửa mã của một sinh viên.
2. Đổi mật khẩu, sau đó nhập lại mật khẩu cũ để xem còn truy cập được không.
3. Thử mật khẩu cũ với các sinh viên khác nhau trong danh sách, quan sát xem còn dùng được không.
4. Tạo một sinh viên mới rồi thử dùng mật khẩu cũ để đọc hoặc sửa mã của sinh viên đó.

Phần vận dụng tổng hợp ở nhà được giao riêng tại [BTVN buổi 03](buoi-03-btvn.md).

## IX. Câu hỏi củng cố

1. Class, object và instance khác nhau như thế nào? Vì sao khai báo một biến có kiểu là class chưa đồng nghĩa với việc tạo ra một object?

2. Constructor khác phương thức thông thường ở mục đích và cách sử dụng như thế nào? Từ khóa this đại diện cho điều gì trong constructor và phương thức của đối tượng?

3. Vì sao cần getter/setter khi thuộc tính đã được khai báo private? Có getter/setter cho mọi thuộc tính có đồng nghĩa với việc dữ liệu đã được bảo vệ hợp lệ không? Vì sao?

4. Quan hệ HAS-A diễn đạt điều gì và được thể hiện như thế nào trong một class? Vì sao giữ tham chiếu đến một object khác không đồng nghĩa với việc giữ một bản sao của object đó?

5. Thành viên static khác thành viên của đối tượng như thế nào về nơi sở hữu và việc dùng chung dữ liệu? Vì sao phương thức static không sử dụng được this?
