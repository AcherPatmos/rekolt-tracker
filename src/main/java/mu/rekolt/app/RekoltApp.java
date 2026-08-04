package mu.rekolt.app;

// testing my code against the worked example.
public class RekoltApp {
    private static final double BnsPricePerKg  = 90.0;  // BNS Beans, MUR per kg
    private static final double CerealMultiplier = 1.00;  // beans sit in the cereal category
    private static final int    CommissionPercentage = 5;    // percentage kept by the cooperative
    private static final double LevyPerKg       = 2.0;   // transport levy, MUR per kg


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

//  values the produce delivered and prints the amount made from it
    private static void valueDelivery(String deliveryId, String memberId, String memberName,
                                      double massKg, int qualityScore) {
        String grade = gradeOf(qualityScore);
        double gradeMultiplier = gradeMultiplierOf(qualityScore);
        boolean rejected = qualityScore < 50;
    }
}
