final class Validation {
    private Validation() { }

    static String text(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    static double price(double value) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("Price must be a finite, non-negative number.");
        }
        return value;
    }
}
