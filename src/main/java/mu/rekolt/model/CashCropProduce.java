package mu.rekolt.model;

// Green tea-leaf. Rule 3: cash crop x1.10
public class CashCropProduce extends Produce {

    private static final double CategoryMultiplier = 1.10;

    public CashCropProduce(String code, String displayName, double basePricePerKg) {
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
        return "Cash crop";
    }
}