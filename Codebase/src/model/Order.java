package model;

public class Order {
    private String id;
    private Customer customer;
    private Product product;
    private int quantity;

    public Order(String id, Customer customer, Product product, int quantity) {
        this.id = id;
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            System.out.println("Mã đơn hàng không được rỗng");
            return;
        }
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            System.out.println("Khách hàng không hợp lệ");
            return;
        }
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        if (product == null) {
            System.out.println("Sản phẩm không hợp lệ");
            return;
        }

        if (quantity > product.getStock()) {
            System.out.println("Số lượng hiện tại vượt tồn kho sản phẩm mới");
            return;
        }

        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            System.out.println("Số lượng phải lớn hơn 0");
            return;
        }

        if (quantity > product.getStock()) {
            System.out.println("Số lượng vượt tồn kho");
            return;
        }

        this.quantity = quantity;
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return id
                + " | Khách: " + customer.getName()
                + " | Sản phẩm: " + product.getName()
                + " | SL: " + quantity
                + " | Tổng: " + getTotal();
    }
}
