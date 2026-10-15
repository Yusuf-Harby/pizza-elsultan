import java.util.Objects;
import java.util.function.BooleanSupplier;

public class CardPayment implements PaymentMethod {
    private final BooleanSupplier authorization;

    public CardPayment() { this(() -> true); }

    public CardPayment(BooleanSupplier authorization) {
        this.authorization = Objects.requireNonNull(authorization);
    }

    @Override
    public PaymentResult pay(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        if (order.getItems().isEmpty() || order.getStatus() != OrderStatus.NEW) {
            return PaymentResult.FAILED;
        }
        return authorizePayment() ? PaymentResult.SUCCESS : PaymentResult.FAILED;
    }

    private boolean authorizePayment() { return authorization.getAsBoolean(); }
}
