public class Drink extends Sellable {
    public Drink(String name, double price) { super(name, price); }

    @Override
    public double getPrice() { return price; }
}
