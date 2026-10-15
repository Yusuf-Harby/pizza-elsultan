import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner input = new Scanner(System.in);
    private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();
    private final DeliveryService deliveryService = new DeliveryService();
    private final List<Topping> toppings = List.of(
            new Topping("Extra cheese", 15), new Topping("Olives", 10),
            new Topping("Mushrooms", 12));

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("Welcome to Pizza El Sultan! Prices are in EGP.");
        System.out.println("Card payments and SMS messages are console simulations.");
        try {
            boolean running = true;
            while (running) {
                System.out.println("\n1. Create order\n0. Exit");
                switch (readInt("Choose: ", 0, 1)) {
                    case 1: manageOrder(createOrder()); break;
                    case 0: running = false; break;
                    default: break;
                }
            }
        } catch (EndOfInput ignored) {
            System.out.println("\nInput closed. Goodbye!");
        }
    }

    private Order createOrder() {
        String name = readText("Customer name: ");
        String phone = readText("Phone: ");
        System.out.println("1. Takeaway\n2. Delivery (20 EGP)");
        FulfillmentType type = readInt("Choose: ", 1, 2) == 1
                ? FulfillmentType.TAKEAWAY : FulfillmentType.DELIVERY;
        String address = type == FulfillmentType.DELIVERY ? readText("Address: ") : "";
        return new Order(new Customer(name, phone, address), type);
    }

    private void manageOrder(Order order) {
        PaymentResult payment = PaymentResult.FAILED;
        while (true) {
            System.out.printf("\nOrder: %s | Total: %.2f EGP%n", order.getStatus(), order.calculateTotal());
            System.out.println("1. Add pizza\n2. Add drink\n3. Remove item\n4. Show order"
                    + "\n5. Pay and start preparing\n6. Mark ready\n7. Complete order"
                    + "\n8. Cancel order\n0. Back");
            try {
                switch (readInt("Choose: ", 0, 8)) {
                    case 1:
                        requireNew(order);
                        addPizza(order);
                        break;
                    case 2:
                        requireNew(order);
                        addDrink(order);
                        break;
                    case 3:
                        requireNew(order);
                        removeItem(order);
                        break;
                    case 4:
                        receiptPrinter.printReceipt(order, payment);
                        break;
                    case 5:
                        requireNew(order);
                        if (order.getItems().isEmpty()) {
                            throw new IllegalStateException("Add at least one item before payment.");
                        }
                        System.out.println("1. Cash\n2. Card (simulation)");
                        PaymentMethod method;
                        if (readInt("Choose: ", 1, 2) == 1) {
                            method = new CashPayment();
                        } else {
                            System.out.println("1. Approve simulated payment\n2. Decline simulated payment");
                            boolean approved = readInt("Choose: ", 1, 2) == 1;
                            method = new CardPayment(() -> approved);
                        }
                        payment = method.pay(order);
                        if (payment == PaymentResult.SUCCESS) {
                            order.changeStatus(OrderStatus.PREPARING);
                        } else {
                            System.out.println("Payment failed. You can retry or choose cash.");
                        }
                        receiptPrinter.printReceipt(order, payment);
                        break;
                    case 6:
                        order.changeStatus(OrderStatus.READY);
                        if (order.getFulfillmentType() == FulfillmentType.DELIVERY) {
                            deliveryService.assignDelivery(order);
                        }
                        break;
                    case 7:
                        order.changeStatus(OrderStatus.COMPLETED);
                        receiptPrinter.printReceipt(order, payment);
                        return;
                    case 8:
                        if (payment == PaymentResult.SUCCESS) {
                            System.out.println("This simulated payment is voided on cancellation.");
                        }
                        order.cancel();
                        payment = PaymentResult.FAILED;
                        receiptPrinter.printReceipt(order, payment);
                        return;
                    case 0:
                        if (order.getStatus() == OrderStatus.NEW) {
                            order.cancel();
                            return;
                        }
                        System.out.println("Complete or cancel the current order before going back.");
                        break;
                    default: break;
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private void addPizza(Order order) {
        System.out.println("1. Margherita (60 EGP base)\n2. Chicken (80 EGP base)\n3. Sultan Special (100 EGP base)");
        int choice = readInt("Pizza: ", 1, 3);
        String[] names = {"Margherita", "Chicken", "Sultan Special"};
        double[] prices = {60, 80, 100};
        System.out.println("1. Small (base x1)\n2. Medium (base x1.5)\n3. Large (base x2)");
        PizzaSize size = PizzaSize.values()[readInt("Size: ", 1, 3) - 1];
        Pizza pizza = new Pizza(names[choice - 1], prices[choice - 1], size);
        while (true) {
            for (int i = 0; i < toppings.size(); i++) {
                System.out.printf("%d. %s (+%.2f EGP)%n", i + 1, toppings.get(i).getName(),
                        toppings.get(i).getPrice());
            }
            int topping = readInt("Add topping (0 to finish): ", 0, toppings.size());
            if (topping == 0) { break; }
            pizza.addTopping(toppings.get(topping - 1));
        }
        order.addItem(pizza, readInt("Quantity: ", 1, 100));
    }

    private void addDrink(Order order) {
        System.out.println("1. Water (10 EGP)\n2. Cola (25 EGP)");
        int choice = readInt("Drink: ", 1, 2);
        Drink drink = choice == 1 ? new Drink("Water", 10) : new Drink("Cola", 25);
        order.addItem(drink, readInt("Quantity: ", 1, 100));
    }

    private void removeItem(Order order) {
        List<OrderItem> lines = order.getItems();
        if (lines.isEmpty()) {
            System.out.println("The order is empty.");
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            System.out.printf("%d. %s x%d%n", i + 1, lines.get(i).getItem().getName(),
                    lines.get(i).getQuantity());
        }
        int index = readInt("Remove item (0 to go back): ", 0, lines.size());
        if (index != 0) { order.removeItem(lines.get(index - 1).getItem()); }
    }

    private void requireNew(Order order) {
        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Only new orders can be edited or paid.");
        }
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) { throw new EndOfInput(); }
            String value = input.nextLine().trim();
            if (!value.isEmpty()) { return value; }
            System.out.println("Please enter a value.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int number = Integer.parseInt(readText(prompt));
                if (number >= min && number <= max) { return number; }
            } catch (NumberFormatException ignored) {
                // Invalid input is handled by the same range message below.
            }
            System.out.printf("Please enter a whole number between %d and %d.%n", min, max);
        }
    }

    private static class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
