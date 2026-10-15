import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Pizza extends Sellable {
    private PizzaSize size;
    private final List<Topping> toppings = new ArrayList<>();

    public Pizza(String name, double price, PizzaSize size) {
        super(name, price);
        this.size = Objects.requireNonNull(size, "Pizza size is required.");
    }

    @Override
    public double getPrice() {
        double total = price * size.getPriceMultiplier();
        for (Topping topping : toppings) {
            total += topping.getPrice();
        }
        return PricingService.roundMoney(total);
    }

    public void addTopping(Topping topping) {
        Objects.requireNonNull(topping, "Topping is required.");
        if (!topping.isAvailable()) {
            throw new IllegalArgumentException("Topping is unavailable: " + topping.getName());
        }
        toppings.add(topping);
    }

    public void setSize(PizzaSize size) {
        this.size = Objects.requireNonNull(size, "Pizza size is required.");
    }

    public PizzaSize getSize() { return size; }
    public List<Topping> getToppings() { return Collections.unmodifiableList(toppings); }
}
