package mu.rekolt.model;

import java.util.Objects;

public abstract class Produce {

    private final String code;            // MZE, BNS, POT, TEA; always in upper case
    private final String displayName;     // Maize, Beans, Potatoes, Green tea-leaf
    private final double basePricePerKg;  // rule 1, MUR per kg


    protected Produce(String code, String displayName, double basePricePerKg) {
        if (code == null || !code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Produce code must be three upper-case letters, was " + code);
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Produce name cannot be empty");
        }
        if (basePricePerKg <= 0.0) {
            throw new IllegalArgumentException("Base price must be above 0, was " + basePricePerKg);
        }
        this.code           = code;
        this.displayName    = displayName;
        this.basePricePerKg = basePricePerKg;
    }

    public String getCode()          { return code; }
    public String getDisplayName()   { return displayName; }
    public double getBasePricePerKg(){ return basePricePerKg; }

    public final double baseValue(double massKg) {
        return massKg * basePricePerKg;
    }


    public abstract double applyCategory(double gradedValue);

    public abstract double categoryMultiplier();

    public abstract String categoryName();


    public final double valueOf(double massKg, Grade grade) {
        return applyCategory(grade.applyTo(baseValue(massKg)));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Produce that = (Produce) other;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " " + displayName + " (" + categoryName() + ")";
    }
}