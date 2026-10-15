public interface PaymentMethod {
    PaymentResult pay(Order order);
}
