public class BankPaymentService implements PaymentService {

    @Override
    public void pay(String orderId) {
        System.out.println("Đơn hàng " + orderId + " của bạn đã được thanh toán thành công qua HITBank");
    }
}
