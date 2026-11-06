import java.util.Scanner;

public class HitShop {
    private static final double VAT_RATE = 0.10;
    private static final double SHIPPING_FEE = 20_000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String name = readName(scanner);
        double price = readPositiveDouble(scanner, "Nhập đơn giá: ");
        int quantity = readPositiveInt(scanner, "Nhập số lượng: ");

        double subtotal = price * quantity;
        double total = subtotal + subtotal * VAT_RATE + SHIPPING_FEE;

        System.out.println("\n--- HÓA ĐƠN TẠM TÍNH ---");
        System.out.println("Tên mặt hàng: " + name);
        System.out.printf("Đơn giá: %.0f%n", price);
        System.out.println("Số lượng: " + quantity);
        System.out.printf("Thành tiền: %.0f%n", subtotal);
        System.out.printf("VAT (10%%): %.0f%n", subtotal * VAT_RATE);
        System.out.printf("Phí vận chuyển: %.0f%n", SHIPPING_FEE);
        System.out.printf("Tổng thanh toán: %.0f%n", total);
    }

    private static String readName(Scanner scanner) {
        while (true) {
            System.out.print("Nhập tên mặt hàng: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                return name;
            }
            System.out.println("Tên mặt hàng không được để trống.");
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
            System.out.println("Đơn giá phải là số lớn hơn 0.");
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
            System.out.println("Số lượng phải là số nguyên dương.");
        }
    }
}

