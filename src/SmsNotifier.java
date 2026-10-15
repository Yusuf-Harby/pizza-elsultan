import java.io.PrintStream;
import java.util.Objects;

public class SmsNotifier implements CustomerNotifier {
    private final PrintStream output;

    public SmsNotifier() { this(System.out); }

    public SmsNotifier(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void sendReadyUpdate(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        output.printf("[Simulated SMS to %s] Hello %s, your order is ready for %s.%n",
                order.getCustomer().getPhone(), order.getCustomer().getName(),
                order.getFulfillmentType() == FulfillmentType.DELIVERY ? "delivery" : "pickup");
    }
}
