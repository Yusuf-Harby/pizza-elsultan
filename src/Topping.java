public class Topping {
    private final String name;
    private final double price;
    private boolean available;

    public Topping(String name, double price) {
        this.name = Validation.text(name, "Topping name");
        this.price = Validation.price(price);
        this.available = true;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public void setAvailability(boolean available) { this.available = available; }
}
