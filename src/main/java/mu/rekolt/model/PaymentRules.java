package mu.rekolt.model;

public final class PaymentRules {

    public static final int CommissionPercentage = 5;

    public static final double LevyPerKg = 2.0;

    public static double commissionRate() {
        return (double) CommissionPercentage / 100;
    }

    public static double baseValue(Produce produce, double massKg) {
        return produce.baseValue(massKg);
    }

    public static double gradedValue(Produce produce, Grade grade, double massKg) {
        return grade.applyTo(produce.baseValue(massKg));
    }

    public static double categoryValue(Produce produce, Grade grade, double massKg) {
        return produce.applyCategory(gradedValue(produce, grade, massKg));
    }

    public static double commission(Produce produce, Grade grade, double massKg) {
        if (!grade.attractsDeductions()) {
            return 0.0;
        }
        return categoryValue(produce, grade, massKg) * commissionRate();
    }

    public static double levy(Grade grade, double massKg) {
        if (!grade.attractsDeductions()) {
            return 0.0;
        }
        return massKg * LevyPerKg;
    }

    public static double netPayable(Produce produce, Grade grade, double massKg) {
        return categoryValue(produce, grade, massKg)
                - commission(produce, grade, massKg)
                - levy(grade, massKg);
    }

//  The overload. Unpacks a Delivery and delegates to the method above
    public static double netPayable(Delivery delivery) {
        return netPayable(delivery.getProduce(), delivery.getGrade(), delivery.getMassKg());
    }
}