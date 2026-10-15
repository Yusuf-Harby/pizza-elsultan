import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReceiptPrinter {
    private final PrintStream output;

    public ReceiptPrinter() { this(System.out); }

    public ReceiptPrinter(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    public void printReceipt(Order order, PaymentResult paymentResult) {
        Objects.requireNonNull(order, "Order is required.");
        Objects.requireNonNull(paymentResult, "Payment result is required.");
        output.println("\n========== Pizza El Sultan ==========");
        output.printf("Customer: %s | Phone: %s%n", order.getCustomer().getName(),
                order.getCustomer().getPhone());
        output.printf("Fulfillment: %s | Status: %s%n", order.getFulfillmentType(), order.getStatus());
        if (order.getFulfillmentType() == FulfillmentType.DELIVERY) {
            output.println("Address: " + order.getCustomer().getAddress());
        }
        for (OrderItem line : order.getItems()) {
            Sellable item = line.getItem();
            String description = item.getName();
            if (item instanceof Pizza) {
                Pizza pizza = (Pizza) item;
                description += " (" + pizza.getSize() + ")";
                if (!pizza.getToppings().isEmpty()) {
                    description += " + " + pizza.getToppings().stream()
                            .map(Topping::getName).collect(Collectors.joining(", "));
                }
            }
            output.printf(Locale.ROOT, "%d x %s @ %.2f = %.2f EGP%n",
                    line.getQuantity(), description, item.getPrice(), line.getLineTotal());
        }
        PricingService pricing = order.getPricingService();
        output.printf(Locale.ROOT, "Subtotal: %.2f EGP%nDiscount: %.2f EGP%nDelivery: %.2f EGP%nTotal: %.2f EGP%n",
                order.calculateSubtotal(), pricing.calculateDiscount(order),
                pricing.calculateDeliveryFee(order), order.calculateTotal());
        output.println("Payment: " + paymentResult);
        output.println("=====================================\n");
    }
}
