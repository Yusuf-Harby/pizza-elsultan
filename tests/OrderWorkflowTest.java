import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Dependency-free regression tests. Run with java -cp out OrderWorkflowTest. */
public class OrderWorkflowTest {
    private static int checks;

    public static void main(String[] args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer);
        Customer customer = new Customer("Ali", "01012345678", "Cairo");
        PricingService pricing = new PricingService(20, 0.1);
        Order order = new Order(customer, FulfillmentType.DELIVERY, pricing, new SmsNotifier(output));
        Pizza pizza = new Pizza("Chicken", 80, PizzaSize.MEDIUM);
        pizza.addTopping(new Topping("Cheese", 15));
        order.addItem(pizza, 2);
        Drink water = new Drink("Water", 10);
        order.addItem(water, 1);
        order.addItem(water, 1);
        equal(290, order.calculateSubtotal());
        equal(29, pricing.calculateDiscount(order));
        equal(281, order.calculateTotal());
        check(order.getItems().size() == 2, "Repeated item merges quantity");
        order.removeItem(water);
        equal(263, order.calculateTotal());
        check(new CardPayment(() -> false).pay(order) == PaymentResult.FAILED, "Declined card");
        check(order.getStatus() == OrderStatus.NEW, "Failed payment leaves order editable");
        check(new CardPayment().pay(order) == PaymentResult.SUCCESS, "Approved card");
        fails(() -> order.changeStatus(OrderStatus.COMPLETED));
        order.changeStatus(OrderStatus.PREPARING);
        fails(() -> order.addItem(water, 1));
        fails(() -> order.removeItem(pizza));
        check(new CashPayment().pay(order) == PaymentResult.FAILED, "Cannot repay processed order");
        order.changeStatus(OrderStatus.READY);
        String notification = buffer.toString();
        check(notification.contains(customer.getPhone()), "Ready notification includes phone");
        order.changeStatus(OrderStatus.READY);
        check(buffer.toString().equals(notification), "Ready notification sent once");
        DeliveryService delivery = new DeliveryService(output);
        delivery.assignDelivery(order);
        delivery.assignDelivery(order);
        check(buffer.toString().split("Delivery assigned", -1).length == 2, "Delivery assigned once");
        check(delivery.estimateDeliveryTime(order) == 30, "Delivery estimate");
        order.changeStatus(OrderStatus.COMPLETED);
        fails(order::cancel);
        new ReceiptPrinter(output).printReceipt(order, PaymentResult.SUCCESS);
        check(buffer.toString().contains("263.00 EGP"), "Receipt includes total");
        check(buffer.toString().contains("Cheese"), "Receipt includes toppings");

        Order empty = new Order(customer, FulfillmentType.TAKEAWAY);
        equal(0, empty.calculateTotal());
        check(new CashPayment().pay(empty) == PaymentResult.FAILED, "Empty payment rejected");
        fails(() -> empty.changeStatus(OrderStatus.PREPARING));
        fails(() -> empty.addItem(water, 0));
        water.setAvailability(false);
        fails(() -> empty.addItem(water, 1));
        Topping unavailable = new Topping("Olives", 10);
        unavailable.setAvailability(false);
        fails(() -> pizza.addTopping(unavailable));
        fails(() -> new Drink("Invalid", Double.NaN));
        fails(() -> new PricingService(20, 1.1));
        fails(() -> new Order(new Customer("Ali", "123", ""), FulfillmentType.DELIVERY));
        fails(() -> delivery.assignDelivery(empty));
        empty.cancel();
        fails(() -> empty.changeStatus(OrderStatus.PREPARING));
        fails(() -> empty.addItem(pizza, 1));
        check(new CardPayment().pay(empty) == PaymentResult.FAILED, "Cancelled payment rejected");
        Order takeaway = new Order(customer, FulfillmentType.TAKEAWAY);
        takeaway.addItem(new Drink("Cola", 25), 2);
        equal(50, takeaway.calculateTotal());
        check(new CashPayment().pay(takeaway) == PaymentResult.SUCCESS, "Cash payment succeeds");
        pizza.setSize(PizzaSize.LARGE);
        equal(175, pizza.getPrice());
        System.out.println("Passed " + checks + " checks.");
    }

    private static void equal(double expected, double actual) {
        check(Math.abs(expected - actual) < 0.000001, "Expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }

    private static void fails(Runnable action) {
        checks++;
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            return;
        }
        throw new AssertionError("Expected validation failure");
    }
}
