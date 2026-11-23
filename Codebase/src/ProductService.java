public interface ProductService {
    void addProduct(Product product);
    Product findById(String id);
    void printProducts();
}
