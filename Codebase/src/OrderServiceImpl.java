import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    private List<Order> orders = new ArrayList<>();

    @Override
    public void addOrder(Order order) {
        orders.add(order);
    }

    @Override
    public void printOrders() {
        if (orders.isEmpty()) {
            System.out.println("Danh sách đơn hàng trống!");
            return;
        }

        for (Order order : orders) {
            System.out.println(order.toString());
        }
    }

    @Override
    public void updateOrder(String id) {
        for (Order order : orders) {
            if (order.getId().equals(id)) {
                HitShop.updateOrderDetails(order);
                return;
            }
        }
        System.out.println("Không tìm thấy đơn hàng!");
    }

    @Override
    public void deleteOrder(String id) {
        for (Order order : orders) {
            if (order.getId().equals(id)) {
                orders.remove(order);
                System.out.println("Xóa đơn hàng thành công!");
                return;
            }
        }
        System.out.println("Không tìm thấy đơn hàng!");
    }
}
