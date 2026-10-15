import java.util.Objects;

public class OrderItem {
    private final Sellable item;
    private int quantity;

    public OrderItem(Sellable item, int quantity) {
        this.item = Objects.requireNonNull(item, "Item is required.");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return PricingService.roundMoney(item.getPrice() * quantity);
    }

    public void updateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.quantity = quantity;
    }

    public Sellable getItem() { return item; }
    public int getQuantity() { return quantity; }
}
