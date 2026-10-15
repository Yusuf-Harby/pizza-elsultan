# Pizza El Sultan

## 1. Architecture

`Main` runs a console demonstration. `Order` contains `OrderItem` objects and belongs to a `Customer`. Each order item references a `Sellable`, implemented by `Pizza` and `Drink`. Separate classes handle pricing, payment, delivery, receipts, and notifications.

## 2. Single Responsibility Principle (SRP)

`PricingService` calculates subtotals, discounts, delivery fees, and totals. `ReceiptPrinter` formats and prints those values. Changing receipt formatting does not require changing pricing rules.

## 3. Adding a new feature or type

A new payment type, such as `WalletPayment`, can implement `PaymentMethod.pay(Order)`. `Main` can use it through a `PaymentMethod` variable without changing `Order`, `CashPayment`, or `CardPayment`.

## 4. Inheritance and substitution

`Pizza` and `Drink` extend `Sellable`. Both provide `getPrice()` and share a name and availability. `Order.addItem(Sellable, int)` and `OrderItem.getLineTotal()` work with either type without requiring a specific subclass.

## 5. Interface methods

Yes, every implementation needs every method in its interface. `PaymentMethod` contains only `pay(Order)`, used by both payment classes. `CustomerNotifier` contains only `sendReadyUpdate(Order)`, implemented by `SmsNotifier`. No implementation needs an empty or unsupported method.

## 6. Dependencies on abstractions

`Order` receives a `CustomerNotifier` through its constructor and calls it when the order becomes ready. `OrderItem` references the abstract `Sellable`. `Main` invokes payments through `PaymentMethod` variables. However, `Order` still depends on the concrete `PricingService`, and its convenience constructor creates an `SmsNotifier`.

## 7. Composition instead of inheritance

`Order` contains order items, and `Pizza` contains toppings. A topping is part of a pizza configuration, so `Pizza` stores a list of `Topping` objects instead of extending `Topping`. This also allows multiple toppings on one pizza.

## 8. Largest change associated with Phase 2

Based on the supplied requirements, supporting different payment methods and successful or failed payment attempts drove a major change. The Initial UML has one `Payment` class with a string method. The Final UML uses `PaymentMethod`, `CashPayment`, `CardPayment`, and `PaymentResult`. `Main` demonstrates a declined card followed by a successful retry.

## 9. Initial UML versus Final UML

The Final UML replaces `MenuItem` with abstract `Sellable`, replaces string sizes and extras with `PizzaSize` and `Topping`, and adds `OrderItem` for quantities. It replaces string order status and type with `OrderStatus` and `FulfillmentType`. Payment becomes an interface with separate implementations. Pricing, receipt printing, and customer notifications become separate components, while `Delivery` becomes `DeliveryService` with a time estimate. The code follows these changes, and `Order.calculateTotal()` delegates to `PricingService` rather than storing a total price.

## 10. Five more payment methods or notification channels

For five additions in total, approximately five new implementation classes and a few wiring lines in `Main` would be needed. Payment types implement `PaymentMethod`; notification channels implement `CustomerNotifier` and are passed to `Order`. Existing domain and implementation classes would need no changes for choosing one channel per order. Sending through multiple channels together would require an additional notifier that delegates to several channels.

## 11. Least satisfactory part

Payment state is separate from `Order`: `pay()` returns a result, and `Main` decides whether to start preparation. `Order.changeStatus()` does not itself check payment. Also, exposed order items and pizzas remain mutable, so external code could change quantities or prices after preparation. These rules could be enforced more strongly.

## 12. What I would design differently

I would add a small checkout service to coordinate payment and order transitions, freeze order lines at checkout, and use `BigDecimal` throughout money calculations instead of `double`. These changes would keep payment rules and charged totals consistent outside the demonstration too.
