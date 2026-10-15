public class Customer {
    private final String name;
    private final String phone;
    private final String address;

    public Customer(String name, String phone, String address) {
        this.name = Validation.text(name, "Customer name");
        this.phone = Validation.text(phone, "Phone");
        this.address = address == null ? "" : address.trim();
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}
