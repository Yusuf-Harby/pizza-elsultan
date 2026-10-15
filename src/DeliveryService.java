import java.io.PrintStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

public class DeliveryService {
    private final PrintStream output;
    private final Set<Order> assignedOrders = Collections.newSetFromMap(new IdentityHashMap<>());

    public DeliveryService() { this(System.out); }

    public DeliveryService(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    public void assignDelivery(Order order) {
        requireDelivery(order);
        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Delivery can only be assigned to a ready order.");
        }
        if (assignedOrders.add(order)) {
            output.printf("Delivery assigned to %s. Estimated arrival: %d minutes.%n",
                    order.getCustomer().getAddress(), estimateDeliveryTime(order));
        }
    }

    public int estimateDeliveryTime(Order order) {
        requireDelivery(order);
        return 30;
    }

    private void requireDelivery(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        if (order.getFulfillmentType() != FulfillmentType.DELIVERY) {
            throw new IllegalArgumentException("This order is for takeaway.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("This order is cancelled.");
        }
    }
}
