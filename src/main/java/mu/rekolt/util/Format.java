package mu.rekolt.util;

public final class Format {

    //  Money for display: thousands separated, two decimals
    public static String money(double amount) {
        return String.format("%,.2f", amount);
    }

    //  A mass for display: one decimal
    public static String kg(double massKg) {
        return String.format("%.1f", massKg);
    }

    //  A multiplier for display: two decimals
    public static String rate(double multiplier) {
        return String.format("%.2f", multiplier);
    }

}
