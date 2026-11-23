public interface OrderService {
    void addOrder(Order order);
    void printOrders();
    void updateOrder(String id);
    void deleteOrder(String id);
}
