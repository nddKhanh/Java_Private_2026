import model.CartItem;
import model.Customer;
import model.Product;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class HitShop {
    private static final Customer currentCustomer =
            new Customer("C01", "Linh Nguyễn", "0900000001");
    private static final List<CartItem> cart = new ArrayList<>();

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        // Tạo một lần và tái sử dụng khi thêm các dòng vào giỏ.
        Product shirt = new Product("P01", "Áo thun", 150000, 20);
        Product bottle = new Product("P02", "Bình nước", 50000, 10);

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            int choice;
            do {
                printMenu();
                choice = readInt(scanner, "Lựa chọn: ");

                switch (choice) {
                    case 1 -> readAndAddItem(scanner, shirt, bottle);
                    case 2 -> {
                        printItems();
                        int displayNumber = readInt(scanner, "Số thứ tự cần xóa: ");
                        removeItem(displayNumber - 1);
                    }
                    case 3 -> printItems();
                    case 4 -> System.out.printf(Locale.US,
                            "Tổng tiền: %.1f%n", calculateTotal());
                    case 0 -> System.out.println("Kết thúc.");
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } while (choice != 0);
        }
    }

    static void addItem(Product product, int quantity) {
        if (quantity <= 0) {
            System.out.println("Số lượng phải lớn hơn 0.");
            return;
        }

        // Buổi 3 chưa gộp dòng, kiểm tra hoặc trừ tồn kho.
        cart.add(new CartItem(product, quantity));
    }

    static void removeItem(int index) {
        if (index < 0 || index >= cart.size()) {
            System.out.println("Không tìm thấy mặt hàng.");
            return;
        }
        cart.remove(index);
    }

    static void printItems() {
        System.out.println("Khách hàng: " + currentCustomer);
        if (cart.isEmpty()) {
            System.out.println("Giỏ hàng đang trống.");
            return;
        }

        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            Product product = item.getProduct();
            System.out.printf(Locale.US,
                    "%d. %s | %s | %.1f | SL: %d | Thành tiền: %.1f%n",
                    i + 1, product.getId(), product.getName(), product.getPrice(),
                    item.getQuantity(), item.getTotal());
        }
    }

    static double calculateTotal() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotal();
        }
        return total;
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== HIT SHOP ===");
        System.out.println("1. Thêm mặt hàng");
        System.out.println("2. Xóa mặt hàng");
        System.out.println("3. Xem giỏ");
        System.out.println("4. Xem tổng tiền");
        System.out.println("0. Thoát");
    }

    private static void readAndAddItem(Scanner scanner, Product shirt, Product bottle) {
        System.out.println(shirt);
        System.out.println(bottle);
        System.out.print("Mã sản phẩm: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String productId = scanner.nextLine().trim();

        Product product;
        if (productId.equals(shirt.getId())) {
            product = shirt;
        } else if (productId.equals(bottle.getId())) {
            product = bottle;
        } else {
            System.out.println("Không tìm thấy sản phẩm.");
            return;
        }

        int quantity = readInt(scanner, "Số lượng: ");
        addItem(product, quantity);
    }

    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            if (!scanner.hasNextLine()) {
                return 0;
            }
            scanner.nextLine();
            System.out.println("Vui lòng nhập số nguyên.");
            System.out.print(prompt);
        }
        int value = scanner.nextInt();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        return value;
    }
}
