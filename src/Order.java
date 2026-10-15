import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private OrderStatus status = OrderStatus.NEW;
    private final FulfillmentType fulfillmentType;
    private final List<OrderItem> items = new ArrayList<>();
    private final Customer customer;
    private final PricingService pricingService;
    private final CustomerNotifier notifier;

    public Order(Customer customer, FulfillmentType fulfillmentType) {
        this(customer, fulfillmentType, new PricingService(), new SmsNotifier());
    }

    public Order(Customer customer, FulfillmentType fulfillmentType,
                 PricingService pricingService, CustomerNotifier notifier) {
        this.customer = Objects.requireNonNull(customer, "Customer is required.");
        this.fulfillmentType = Objects.requireNonNull(fulfillmentType, "Fulfillment is required.");
        this.pricingService = Objects.requireNonNull(pricingService);
        this.notifier = Objects.requireNonNull(notifier);
        if (fulfillmentType == FulfillmentType.DELIVERY && customer.getAddress().isEmpty()) {
            throw new IllegalArgumentException("Delivery requires a customer address.");
        }
    }

    public void addItem(Sellable item, int quantity) {
        requireEditable();
        Objects.requireNonNull(item, "Item is required.");
        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Item is unavailable: " + item.getName());
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        for (OrderItem line : items) {
            if (line.getItem() == item) {
                line.updateQuantity(Math.addExact(line.getQuantity(), quantity));
                return;
            }
        }
        items.add(new OrderItem(item, quantity));
    }

    public void removeItem(Sellable item) {
        requireEditable();
        Objects.requireNonNull(item, "Item is required.");
        items.removeIf(line -> line.getItem() == item);
    }

    public void changeStatus(OrderStatus next) {
        Objects.requireNonNull(next, "Status is required.");
        if (next == status) { return; }
        if (next == OrderStatus.CANCELLED) {
            cancel();
            return;
        }
        boolean valid = (status == OrderStatus.NEW && next == OrderStatus.PREPARING)
                || (status == OrderStatus.PREPARING && next == OrderStatus.READY)
                || (status == OrderStatus.READY && next == OrderStatus.COMPLETED);
        if (!valid) {
            throw new IllegalStateException("Cannot change status from " + status + " to " + next);
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot process an empty order.");
        }
        status = next;
        if (status == OrderStatus.READY) { notifier.sendReadyUpdate(this); }
    }

    public double calculateSubtotal() { return pricingService.calculateSubtotal(this); }
    public double calculateTotal() { return pricingService.calculateTotal(this); }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("A completed order cannot be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireEditable() {
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Only new orders can be edited.");
        }
    }

    public OrderStatus getStatus() { return status; }
    public FulfillmentType getFulfillmentType() { return fulfillmentType; }
    public Customer getCustomer() { return customer; }
    public PricingService getPricingService() { return pricingService; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
}
