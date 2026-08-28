package mu.rekolt.model;

// Maize and beans. Rule 3: cereal x1.00
public class CerealProduce extends Produce {

    private static final double CategoryMultiplier = 1.00;

//  Chained through super: the shared fields are set by the parent, once
    public CerealProduce(String code, String displayName, double basePricePerKg) {
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
        return "Cereal";
    }
}