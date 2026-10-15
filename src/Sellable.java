public abstract class Sellable {
    protected String name;
    protected double price;
    protected boolean available;

    protected Sellable(String name, double price) {
        this.name = Validation.text(name, "Item name");
        this.price = Validation.price(price);
        this.available = true;
    }

    public abstract double getPrice();
    public String getName() { return name; }
    public boolean isAvailable() { return available; }
    public void setAvailability(boolean available) { this.available = available; }
}
