package mu.rekolt.model;

// Potatoes. Rule 3: perishable x0.90
public class PerishableProduce extends Produce {

    private static final double CategoryMultiplier = 0.90;

    public PerishableProduce(String code, String displayName, double basePricePerKg) {
        super(code, displayName, basePricePerKg);
    }

    @Override
    public double applyCategory(double gradedValue) {
        return gradedValue * CategoryMultiplier;
    }

    @Override
    public double categoryMultiplier() {
        return CategoryMultiplier;
    }

    @Override
    public String categoryName() {
        return "Perishable";
    }
}