import java.util.Objects;

public class CashPayment implements PaymentMethod {
    @Override
    public PaymentResult pay(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        return order.getItems().isEmpty() || order.getStatus() != OrderStatus.NEW
                ? PaymentResult.FAILED : PaymentResult.SUCCESS;
    }
}
