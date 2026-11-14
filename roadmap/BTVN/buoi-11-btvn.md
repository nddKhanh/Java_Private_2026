# Bài Tập Sau Buổi 11 — Capstone

## I. Điểm Bắt Đầu Từ Buổi 10

Nhóm đã có kiến thức OOP, JDBC, nhiều bảng, phân tầng, Git/PR và một View console hoặc Swing.
HitShop là đề tài tham khảo, không bắt buộc.

### 1. Các Ca Cũ Phải Còn Đạt

| Ca | Kết quả cần giữ |
|---|---|
| Console/Swing | Dùng chung Service |
| Dependency | View không chứa SQL |
| Git | Có branch, PR, review và commit có nghĩa |

### 2. Cây Thư Mục Đầu Buổi

Dùng cây cuối [BTVN Buổi 10](buoi-10-btvn.md) hoặc cấu trúc tương đương của đề tài nhóm.

## II. Bài Capstone Bắt Buộc

### 1. Đăng Ký Phạm Vi

Nộp một trang trước khi code:

- Tên đề tài, người dùng chính, ba chức năng chính.
- Ít nhất ba entity và ERD dự kiến.
- Một luồng nghiệp vụ ghi ít nhất hai bảng.
- Giao diện chọn console hoặc Swing.
- Thành viên, feature branch và người review chéo.

Không mở rộng phạm vi khi luồng lõi chưa đạt.

### 2. OOP Và Dữ Liệu

- Ít nhất ba entity, field đóng gói, quan hệ object rõ.
- Ít nhất ba bảng và một quan hệ một-nhiều bằng foreign key.
- Service kiểm tra quy tắc nghiệp vụ.
- Giá trị lịch sử cần giữ phải được snapshot, ví dụ `unit_price`.

### 3. JDBC

- CRUD đầy đủ cho một entity chính.
- Tạo và xem chi tiết một giao dịch nhiều bảng.
- Ít nhất một JOIN.
- Ít nhất một báo cáo GROUP BY hoặc phép tổng hợp tương đương.
- Input SQL dùng PreparedStatement.
- Tài nguyên đóng bằng try-with-resources.
- Dữ liệu còn sau khi khởi động lại.

### 4. Transaction

Luồng ghi nhiều bảng phải dùng cùng Connection và cùng transaction.

- Tất cả bước cùng commit hoặc cùng rollback.
- Có một ca cố ý lỗi ở bước giữa.
- Có query chứng minh không còn dữ liệu/trạng thái dở dang.

### 5. Kiến Trúc

```text
View → Controller → Service → Repository interface → JDBC implementation → MySQL
```

- View không chứa SQL/nghiệp vụ.
- Controller chỉ điều phối.
- Service phụ thuộc repository interface.
- JDBC implementation chứa SQL/mapping.
- Composition root nối dependency.

### 6. Git Và Tài Liệu

- Nhánh `main`, `develop`, `feature/<ten-chuc-nang>`.
- Mỗi thành viên có commit và review phần của người khác.
- README ghi JDK 17, MySQL, cấu hình, schema/seed, cách chạy và kịch bản demo.
- Không commit credential, build output hoặc cấu hình IDE cá nhân không cần thiết.

## III. Kịch Bản Demo Bắt Buộc

1. Reset schema/seed.
2. Khởi động ứng dụng và xem dữ liệu seed.
3. Chạy CRUD, gồm một input sai.
4. Tạo giao dịch nhiều bảng hợp lệ.
5. Xem chi tiết và báo cáo.
6. Chạy ca rollback.
7. Khởi động lại để chứng minh dữ liệu còn.
8. Trình bày một PR, một review và một lỗi đã sửa.

Input và output mong đợi phải được ghi trước trong README.
Không dùng dữ liệu ngẫu nhiên làm kết quả không thể lặp lại.

## IV. Nộp Bài

- URL repository và danh sách PR.
- Source, `schema.sql`, `seed.sql`, README.
- ERD và sơ đồ kiến trúc.
- Bảng test case: input, mong đợi, thực tế.
- Tài liệu demo tối đa 8 trang nếu có.

## V. Nghiệm Thu Capstone

### 1. Rubric 100 Điểm

| Nhóm tiêu chí | Điểm |
|---|---:|
| Chức năng và dữ liệu | 30 |
| OOP và kiến trúc | 20 |
| JDBC và transaction | 20 |
| Kiểm thử và xử lý lỗi | 10 |
| Git collaboration | 10 |
| README và demo | 10 |

Chức năng phụ hoặc giao diện đẹp không bù cho dữ liệu sai, transaction sai hoặc project không dựng lại được.

### 2. Test Case Chung

| Mã | Thao tác | Kết quả mong đợi |
|---|---|---|
| `TC11-01` | Reset/seed rồi chạy | Trạng thái chuẩn, chạy trên JDK 17 |
| `TC11-02` | Liệt kê seed | Khớp seed.sql |
| `TC11-03` | CRUD entity chính | Đủ bốn thao tác; input sai bị từ chối |
| `TC11-04` | Giao dịch nhiều bảng hợp lệ | Các bảng cùng cập nhật |
| `TC11-05` | Xem chi tiết | Quan hệ và giá trị lịch sử đúng |
| `TC11-06` | JOIN/báo cáo | Khớp phép tính tay |
| `TC11-07` | Lỗi giữa transaction | Rollback toàn bộ |
| `TC11-08` | Khởi động lại | Dữ liệu đã commit vẫn còn |
| `TC11-09` | Dựng trên máy khác theo README | Không cần sửa source/credential |
| `TC11-10` | Kiểm tra Git | Đủ branch, PR, review, commit từng thành viên |

### 3. Cây Thư Mục Tối Thiểu

```text
project-name/
├── pom.xml
├── README.md
├── .gitignore
├── sql/
│   ├── schema.sql
│   └── seed.sql
├── docs/
│   ├── erd.png
│   ├── architecture.md
│   └── test-cases.md
└── src/main/java/<base-package>/
    ├── app/
    ├── controller/
    ├── dto/
    ├── model/
    ├── repository/
    │   └── jdbc/
    ├── service/
    └── view/
```
