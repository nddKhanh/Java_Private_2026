import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//Product: Đại diện cho một sản phẩm mà cửa hàng kinh doanh, chứa thông tin như tên, giá bán và số lượng tồn kho.
//Customer: Đại diện cho khách hàng mua sắm tại cửa hàng, lưu trữ thông tin để xác định người mua.
//Order: Đại diện cho một lần mua hàng, ghi nhận khách hàng nào đã mua sản phẩm gì, với số lượng bao nhiêu.

//HitShop: Đóng vai trò quản lý hoạt động của cửa hàng, cho phép thêm, xem, sửa và xóa các đơn hàng.

public class HitShop {

    public static final Scanner sc = new Scanner(System.in);

    // Danh sách chứa thông tin khách hàng
    public static List<Customer> customers = new ArrayList<>();

    public static ProductService productService = new ProductServiceImpl();
    public static OrderService orderService = new OrderServiceImpl();

    public static void main(String[] args) {

        // hard code data tại đây
        customers.add(new Customer("C01", "Linh Nguyễn", "0900000001"));
        customers.add(new Customer("C02", "An", "0900000002"));

        productService.addProduct(new GeneralProduct("P01", "Áo thun", 150000, 20));
        productService.addProduct(new GeneralProduct("P02", "Bình nước", 50000, 10));

        // menu cần có thêm order (mọi thứ là nhập từ bàn phím)
        while (true) {
            System.out.println();
            System.out.println("===== HITSHOP - QUẢN LÝ ĐƠN HÀNG =====");
            System.out.println("1. Thêm đơn hàng");
            System.out.println("2. Xem danh sách đơn hàng");
            System.out.println("3. Sửa đơn hàng theo ID");
            System.out.println("4. Xóa đơn hàng theo ID");
            System.out.println("5. Kiểm tra sản phẩm có đắt không");
            System.out.println("6. Thanh toán đơn hàng");
            System.out.println("0. Thoát");

            System.out.print("Chọn chức năng: ");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1:
                    addOrder();
                    break;
                case 2:
                    orderService.printOrders();
                    break;
                case 3:
                    updateOrder();
                    break;
                case 4:
                    deleteOrder();
                    break;
                case 5:
                    checkExpensive();
                    break;
                case 6:
                    payOrder();
                    break;
                case 0:
                    System.out.println("Tạm biệt!");
                    return;
                default:
                    System.out.println("Chức năng không hợp lệ");
            }
        }
    }

    private static void addOrder() {
        // Nhập ID của Order
        System.out.print("Nhập mã đơn hàng: ");
        String orderId = sc.nextLine();

        // Nhập thông tin Customer
        System.out.println("=== Nhập thông tin khách hàng ===");
        System.out.print("Nhập mã khách hàng: ");
        String customerId = sc.nextLine();
        System.out.print("Nhập tên khách hàng: ");
        String customerName = sc.nextLine();
        System.out.print("Nhập số điện thoại: ");
        String customerPhone = sc.nextLine();

        // Nhập thông tin Product
        Product product = inputProduct();
        if (product == null) {
            return;
        }

        // Nhập số lượng mua
        System.out.print("Nhập số lượng mua: ");
        int quantity = Integer.parseInt(sc.nextLine());

        Customer customer = new Customer(customerId, customerName, customerPhone);
        Order order = new Order(orderId, customer, product, quantity);

        orderService.addOrder(order);
        customers.add(customer);
        productService.addProduct(product);

        System.out.println("Thêm đơn hàng thành công!");
    }

    private static Product inputProduct() {
        System.out.println("=== Chọn loại sản phẩm ===");
        System.out.println("1. Product - Sản phẩm thông thường");
        System.out.println("2. Book - Sách");
        System.out.println("3. Cake - Bánh");
        System.out.print("Nhập lựa chọn: ");
        int choice = Integer.parseInt(sc.nextLine());

        if (choice < 1 || choice > 3) {
            System.out.println("Lựa chọn không hợp lệ!");
            return null;
        }

        System.out.println("=== Nhập thông tin sản phẩm ===");
        System.out.print("Nhập mã sản phẩm: ");
        String productId = sc.nextLine();
        System.out.print("Nhập tên sản phẩm: ");
        String productName = sc.nextLine();
        System.out.print("Nhập giá sản phẩm: ");
        double productPrice = Double.parseDouble(sc.nextLine());
        System.out.print("Nhập tồn kho: ");
        int productStock = Integer.parseInt(sc.nextLine());

        Product product;
        switch (choice) {
            case 2:
                System.out.print("Nhập tác giả: ");
                String author = sc.nextLine();
                product = new Book(productId, productName, productPrice, productStock, author);
                break;
            case 3:
                System.out.print("Nhập thương hiệu: ");
                String brand = sc.nextLine();
                product = new Cake(productId, productName, productPrice, productStock, brand);
                break;
            default:
                product = new GeneralProduct(productId, productName, productPrice, productStock);
                break;
        }

        return product;
    }

    private static void checkExpensive() {
        System.out.print("Nhập mã sản phẩm: ");
        String productId = sc.nextLine();

        Product product = productService.findById(productId);
        if (product != null) {
            if (product.isExpensive()) {
                System.out.println("Sản phẩm đắt");
            } else {
                System.out.println("Sản phẩm không đắt");
            }
            return;
        }

        System.out.println("Không tìm thấy sản phẩm!");
    }

    private static void deleteOrder() {
        System.out.print("Nhập mã đơn hàng cần xóa: ");
        String orderId = sc.nextLine();
        orderService.deleteOrder(orderId);
    }

    private static void updateOrder() {
        System.out.print("Nhập mã đơn hàng cần sửa: ");
        String orderId = sc.nextLine();
        orderService.updateOrder(orderId);
    }

    public static void updateOrderDetails(Order orderUpdate) {
        System.out.println("=== Chọn thông tin cần sửa ===");
        System.out.println("1. Sửa ID đơn hàng");
        System.out.println("2. Sửa Customer");
        System.out.println("3. Sửa Product");
        System.out.println("4. Sửa số lượng mua");
        System.out.print("Nhập lựa chọn: ");

        int choice = Integer.parseInt(sc.nextLine());

        switch (choice) {
            case 1:
                System.out.print("Nhập ID mới: ");
                String newId = sc.nextLine();
                orderUpdate.setId(newId);
                break;

            case 2:
                System.out.println("=== Nhập thông tin khách hàng ===");
                System.out.print("Nhập mã khách hàng: ");
                String customerId = sc.nextLine();
                System.out.print("Nhập tên khách hàng: ");
                String customerName = sc.nextLine();
                System.out.print("Nhập số điện thoại: ");
                String customerPhone = sc.nextLine();

                Customer customer = new Customer(customerId, customerName, customerPhone);
                orderUpdate.setCustomer(customer);
                break;

            case 3:
                Product product = inputProduct();
                if (product == null) {
                    return;
                }
                orderUpdate.setProduct(product);
                if (orderUpdate.getProduct() == product) {
                    productService.addProduct(product);
                }
                break;

            case 4:
                System.out.print("Nhập số lượng mua mới: ");
                int quantity = Integer.parseInt(sc.nextLine());
                orderUpdate.setQuantity(quantity);
                break;

            default:
                System.out.println("Lựa chọn không hợp lệ!");
                return;
        }

        System.out.println("Sửa đơn hàng thành công!");
    }

    private static void payOrder() {
        System.out.print("Nhập mã đơn hàng cần thanh toán: ");
        String orderId = sc.nextLine();

        System.out.println("=== Chọn hình thức thanh toán ===");
        System.out.println("1. MoMo");
        System.out.println("2. HITBank");
        System.out.print("Nhập lựa chọn: ");
        int choice = Integer.parseInt(sc.nextLine());

        PaymentService paymentService;
        switch (choice) {
            case 1:
                paymentService = new MoMoPaymentService();
                break;
            case 2:
                paymentService = new BankPaymentService();
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ!");
                return;
        }

        paymentService.pay(orderId);
    }
}
