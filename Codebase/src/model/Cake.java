package model;

public class Cake extends Product {
    private String brand;

    public Cake(String id, String name, double price, int stock, String brand) {
        super(id, name, price, stock);
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @Override
    public boolean isExpensive() {
        return getPrice() > 100000;
    }
}
