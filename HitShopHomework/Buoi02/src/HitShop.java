import java.util.ArrayList;
import java.util.Scanner;

public class HitShop {
    private static final ArrayList<String> names = new ArrayList<>();
    private static final ArrayList<Double> prices = new ArrayList<>();
    private static final ArrayList<Integer> quantities = new ArrayList<>();

    public static void addItem(String name, double price, int quantity) {
        if (name == null || name.isBlank() || price <= 0 || quantity <= 0) {
            return;
        }

        names.add(name.trim());
        prices.add(price);
        quantities.add(quantity);
    }

    public static void removeItem(int index) {
        if (index < 0 || index >= names.size()) {
            System.out.println("Không tìm thấy mặt hàng.");
            return;
        }

        names.remove(index);
        prices.remove(index);
        quantities.remove(index);
    }

    public static void printItems() {
        if (names.isEmpty()) {
            System.out.println("Giỏ hàng đang trống.");
            return;
        }

        for (int i = 0; i < names.size(); i++) {
            double itemTotal = prices.get(i) * quantities.get(i);
            System.out.printf(
                    "%d. %s | Đơn giá: %.1f | SL: %d | Thành tiền: %.1f%n",
                    i + 1,
                    names.get(i),
                    prices.get(i),
                    quantities.get(i),
                    itemTotal
            );
        }
    }

    public static double calculateTotal() {
        double total = 0;
        for (int i = 0; i < names.size(); i++) {
            total += prices.get(i) * quantities.get(i);
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
        System.out.print("Tên mặt hàng: ");
        String name = scanner.nextLine().trim();
        double price = readPositiveDouble(scanner, "Đơn giá: ");
        int quantity = readPositiveInt(scanner, "Số lượng: ");

        if (name.isEmpty()) {
            System.out.println("Tên mặt hàng không được để trống.");
            return;
        }

        addItem(name, price, quantity);
    }

    private static void handleRemoveItem(Scanner scanner) {
        printItems();
        if (names.isEmpty()) {
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

    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // The message below also covers invalid numeric input.
            }
            System.out.println("Đơn giá phải lớn hơn 0.");
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // The message below also covers invalid integer input.
            }
            System.out.println("Số lượng phải lớn hơn 0.");
        }
    }
}

