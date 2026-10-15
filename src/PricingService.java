import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class PricingService {
    private final double deliveryFee;
    private final double discountRate;

    public PricingService() { this(20.0, 0.0); }

    /** discountRate is a fraction between 0 and 1; discount applies to items only. */
    public PricingService(double deliveryFee, double discountRate) {
        this.deliveryFee = Validation.price(deliveryFee);
        if (!Double.isFinite(discountRate) || discountRate < 0 || discountRate > 1) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 1.");
        }
        this.discountRate = discountRate;
    }

    public double calculateSubtotal(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        double subtotal = 0;
        for (OrderItem item : order.getItems()) {
            subtotal += item.getLineTotal();
        }
        return roundMoney(subtotal);
    }

    public double calculateDiscount(Order order) {
        return roundMoney(calculateSubtotal(order) * discountRate);
    }

    public double calculateDeliveryFee(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        return order.getFulfillmentType() == FulfillmentType.DELIVERY
                && !order.getItems().isEmpty() ? roundMoney(deliveryFee) : 0;
    }

    public double calculateTotal(Order order) {
        return roundMoney(calculateSubtotal(order) - calculateDiscount(order)
                + calculateDeliveryFee(order));
    }

    static double roundMoney(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
