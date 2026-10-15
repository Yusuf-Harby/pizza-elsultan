public class Main {
    public static void main(String[] args) {
        PricingService pricing = new PricingService(20, 0.10);
        CustomerNotifier notifier = new SmsNotifier();
        ReceiptPrinter printer = new ReceiptPrinter();
        DeliveryService deliveryService = new DeliveryService();

        System.out.println("Pizza El Sultan demo - prices in EGP, 10% discount on items.");
        System.out.println("Payments and SMS messages are simulated.");


        System.out.println("\n--- Takeaway order ---");
        Customer ali = new Customer("Ali", "01012345678", "");
        Order takeaway = new Order(ali, FulfillmentType.TAKEAWAY, pricing, notifier);
        Pizza margherita = new Pizza("Margherita", 60, PizzaSize.MEDIUM);
        margherita.addTopping(new Topping("Extra cheese", 15));
        takeaway.addItem(margherita, 2);
        takeaway.addItem(new Drink("Cola", 25), 2);

        PaymentMethod cash = new CashPayment();
        PaymentResult cashResult = cash.pay(takeaway);
        System.out.println("Cash payment: " + cashResult);
        if (cashResult == PaymentResult.SUCCESS) {
            takeaway.changeStatus(OrderStatus.PREPARING);
            takeaway.changeStatus(OrderStatus.READY);
            System.out.println("Customer collects the takeaway order. No delivery fee.");
            takeaway.changeStatus(OrderStatus.COMPLETED);
        }
        printer.printReceipt(takeaway, cashResult);


        System.out.println("\n--- Delivery order ---");
        Customer mona = new Customer("Mona", "01234567890", "10 Nile Street, Cairo");
        Order delivery = new Order(mona, FulfillmentType.DELIVERY, pricing, notifier);
        delivery.addItem(new Pizza("Chicken", 80, PizzaSize.LARGE), 1);
        delivery.addItem(new Drink("Water", 10), 3);

        PaymentMethod declinedCard = new CardPayment(() -> false);
        PaymentResult cardResult = declinedCard.pay(delivery);
        System.out.println("First card attempt: " + cardResult);
        System.out.println("Order status after failed payment: " + delivery.getStatus());
        printer.printReceipt(delivery, cardResult);

        PaymentMethod approvedCard = new CardPayment(() -> true);
        cardResult = approvedCard.pay(delivery);
        System.out.println("Card retry: " + cardResult);
        if (cardResult == PaymentResult.SUCCESS) {
            delivery.changeStatus(OrderStatus.PREPARING);
            delivery.changeStatus(OrderStatus.READY);
            deliveryService.assignDelivery(delivery);
            System.out.println("Order delivered to the customer.");
            delivery.changeStatus(OrderStatus.COMPLETED);
        }
        printer.printReceipt(delivery, cardResult);

        System.out.println("\n--- Edge cases ---");
        Order empty = new Order(ali, FulfillmentType.TAKEAWAY, pricing, notifier);
        System.out.println("1. Paying for an empty order: " + cash.pay(empty));

        try {
            empty.addItem(new Drink("Water", 10), 0);
        } catch (IllegalArgumentException exception) {
            System.out.println("2. Zero quantity: " + exception.getMessage());
        }

        Drink unavailable = new Drink("Cola", 25);
        unavailable.setAvailability(false);
        try {
            empty.addItem(unavailable, 1);
        } catch (IllegalArgumentException exception) {
            System.out.println("3. Unavailable item: " + exception.getMessage());
        }

        try {
            takeaway.cancel();
        } catch (IllegalStateException exception) {
            System.out.println("4. Cancelling a completed order: " + exception.getMessage());
        }
    }
}
