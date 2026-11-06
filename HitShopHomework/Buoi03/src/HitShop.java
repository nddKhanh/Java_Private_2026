import model.CartItem;
import model.Customer;
import model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HitShop {
    private static final Customer currentCustomer =
            new Customer("C01", "Linh Nguyễn", "0900000001");
    private static final List<CartItem> cart = new ArrayList<>();
    private static final Product product1 =
            new Product("P01", "Áo thun", 150_000, 20);
    private static final Product product2 =
            new Product("P02", "Bình nước", 50_000, 10);

    public static void addItem(Product product, int quantity) {
        cart.add(new CartItem(product, quantity));
    }

    public static void removeItem(int index) {
        if (index < 0 || index >= cart.size()) {
            System.out.println("Không tìm thấy mặt hàng.");
            return;
        }
        cart.remove(index);
    }

    public static void printItems() {
        System.out.println("Khách hàng: " + currentCustomer);
        if (cart.isEmpty()) {
            System.out.println("Giỏ hàng đang trống.");
            return;
        }

        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            Product product = item.getProduct();
            System.out.printf(
                    "%d. %s | %s | %.1f | SL: %d | Thành tiền: %.1f%n",
                    i + 1,
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    item.getQuantity(),
                    item.getTotal()
            );
        }
    }

    public static double calculateTotal() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotal();
        }
        return total;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleAddItem(scanner);
                case "2" -> handleRemoveItem(scanner);
                case "3" -> printItems();
                case "4" -> System.out.printf("Tổng tiền: %.1f%n", calculateTotal());
                case "0" -> running = false;
                default -> System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Thêm mặt hàng");
        System.out.println("2. Xóa mặt hàng");
        System.out.println("3. Xem giỏ hàng");
        System.out.println("4. Xem tổng tiền");
        System.out.println("0. Thoát");
        System.out.print("Lựa chọn: ");
    }

    private static void handleAddItem(Scanner scanner) {
        System.out.println(product1.getId() + " - " + product1.getName());
        System.out.println(product2.getId() + " - " + product2.getName());
        System.out.print("Nhập mã sản phẩm: ");
        String productId = scanner.nextLine().trim();

        Product selectedProduct;
        if (product1.getId().equalsIgnoreCase(productId)) {
            selectedProduct = product1;
        } else if (product2.getId().equalsIgnoreCase(productId)) {
            selectedProduct = product2;
        } else {
            System.out.println("Không tìm thấy sản phẩm.");
            return;
        }

        System.out.print("Nhập số lượng: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException exception) {
            System.out.println("Số lượng phải lớn hơn 0.");
            return;
        }

        if (quantity <= 0) {
            System.out.println("Số lượng phải lớn hơn 0.");
            return;
        }

        addItem(selectedProduct, quantity);
    }

    private static void handleRemoveItem(Scanner scanner) {
        printItems();
        if (cart.isEmpty()) {
            return;
        }

        System.out.print("Nhập số thứ tự cần xóa: ");
        try {
            int displayNumber = Integer.parseInt(scanner.nextLine().trim());
            removeItem(displayNumber - 1);
        } catch (NumberFormatException exception) {
            System.out.println("Không tìm thấy mặt hàng.");
        }
    }
}

