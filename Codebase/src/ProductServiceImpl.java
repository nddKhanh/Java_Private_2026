import java.util.ArrayList;
import java.util.List;

public class ProductServiceImpl implements ProductService {
    private List<Product> products = new ArrayList<>();

    @Override
    public void addProduct(Product product) {
        products.add(product);
    }

    @Override
    public Product findById(String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    @Override
    public void printProducts() {
        if (products.isEmpty()) {
            System.out.println("Danh sách sản phẩm trống!");
            return;
        }
        for (Product product : products) {
            System.out.println(product.toString());
        }
    }
}
