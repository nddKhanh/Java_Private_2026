public class GeneralProduct extends Product {

    public GeneralProduct(String id, String name, double price, int stock) {
        super(id, name, price, stock);
    }

    @Override
    public boolean isExpensive() {
        return getPrice() > 10000;
    }
}
