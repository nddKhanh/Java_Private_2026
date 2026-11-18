package model;

public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;

    private static int count;

    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        count++;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            System.out.println("Mã sản phẩm không được rỗng");
            return;
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            System.out.println("Tên sản phẩm không được rỗng");
            return;
        }
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (!Double.isFinite(price) || price <= 0) {
            System.out.println("Giá phải lớn hơn 0");
            return;
        }
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            System.out.println("Tồn kho không được âm");
            return;
        }
        this.stock = stock;
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        if (count < 0) {
            System.out.println("Số lượng sản phẩm không được âm");
            return;
        }
        Product.count = count;
    }

    public boolean isExpensive() {
        return price > 10000;
    }

    @Override
    public String toString() {
        return id + " | " + name
                + " | Giá: " + price
                + " | Tồn: " + stock;
    }
}
