package mu.rekolt.app;

// testing my code against the worked example.
public class RekoltApp {
    private static final double bnsPricePerKg  = 90.0;  // BNS Beans, MUR per kg
    private static final double cerealMultiplier = 1.00;  // beans sit in the cereal category
    private static final int    commissionPercentage = 5;    // percentage kept by the cooperative
    private static final double levyPerKg       = 2.0;   // transport levy, MUR per kg


//  Grade letter for a quality score.
    private static String gradeOf(int qualityScore) {
        if (qualityScore >= 85) return "A";
        if (qualityScore >= 70) return "B";
        if (qualityScore >= 50) return "C";
        return "REJECT";
    }

//  Grade multiplier for a quality score.
    private static double gradeMultiplierOf(int qualityScore) {
        if (qualityScore >= 85) return 1.15;
        if (qualityScore >= 70) return 1.00;
        if (qualityScore >= 50) return 0.85;
        return 0.00;
    }
//    Money for display: two decimals kept
    private static String money(double amount) {
        return String.format("%,.2f", amount);
    }

//  A mass for display: one decimal kept
    private static String kg(double massKg) {
        return String.format("%.1f", massKg);
    }

//  A multiplier for display: two decimals kept
    private static String rate(double multiplier) {
        return String.format("%.2f", multiplier);
    }

//  values the produce delivered and prints the amount made from it
    private static void valueDelivery(String deliveryId, String memberId, String memberName,
                                      double massKg, int qualityScore) {

//      stores the values for the grade scored and quality of the produce
        String grade = gradeOf(qualityScore);
        double gradeMultiplier = gradeMultiplierOf(qualityScore);
        boolean rejected = qualityScore < 50;

//      calculates the base value of the produce
        double baseValue= massKg * bnsPricePerKg;

//      calculates new value after a grade has been assigned to the produce
        double gradeScore= baseValue * gradeMultiplier;

//      calculates the new value of the produce depending on its category
        double categoryScore= gradeScore * cerealMultiplier;

//      performing explicit cast on the commission percentage to allow integer division
        double commissionRate = (double) commissionPercentage / 100;

//      checks if a produce has been rejected before assigning a commission fee
        double commission;
        if(rejected){
             commission = 0;
        } else{
            commission= commissionRate * categoryScore;
        }

//      checks if a produce has been rejected before assigning a Levy fee
        double levy;
        if(rejected){
            levy =0;
        } else {
            levy = massKg * levyPerKg;
        }

//      calculates the net payable amount a farmer will receive
        double netPayable = categoryScore - commission - levy;

        // Display. This is the only place any rounding happens.
        System.out.println("Delivery: " + deliveryId + " recorded. " + memberId + " " + memberName
                + " Produce: BNS " + kg(massKg) + "kg - score= " + qualityScore + " - grade= " + grade);

//      column and row print formats
        System.out.printf("  1. %-16s %-28s %14s%n", "Base value",
                kg(massKg) + " kg x " + money(bnsPricePerKg) + " MUR/kg", money(baseValue));
        System.out.printf("  2. %-16s %-28s %14s%n", "Grade " + grade,
                "x " + rate(gradeMultiplier), money(gradeScore));
        System.out.printf("  3. %-16s %-28s %14s%n", "Cereal",
                "x " + rate(cerealMultiplier), money(categoryScore));
        System.out.printf("  4. %-16s %-28s %14s%n", "Commission",
                commissionPercentage + "% of Produce value after category score", "= " + money(commission));
        System.out.printf("  5. %-16s %-28s %14s%n", "Transport levy",
                rejected ? "not charged on Produce REJECT" : kg(massKg) + " kg x " + money(levyPerKg) + " MUR/kg",
                "= " + money(levy));
        System.out.printf("     %-16s %-28s %14s MUR%n", "NET PAYABLE", "", money(netPayable));
    }

    public static void main(String[] args) {

        // The worked example from the specification. Expected net: 22,732.70 MUR.
        valueDelivery("D-1001", "M-0042", "Devi Ramjaun", 236.0, 91);

        System.out.println();

        // The same load with a quality score below 50. Expected: paid nothing because Produce was rejected
        valueDelivery("D-1002", "M-0042", "Devi Ramjaun", 236.0, 42);
    }
}
