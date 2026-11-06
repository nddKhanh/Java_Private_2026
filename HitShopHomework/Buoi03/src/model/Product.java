package model;

public class Product {
    private static int count;

    private final String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        this.id = id;
        setName(name);
        setPrice(price);
        setStock(stock);
        count++;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        this.name = name.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0.");
        }
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Tồn kho không được âm.");
        }
        this.stock = stock;
    }

    public static int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + price + " | tồn: " + stock;
    }
}

