package mu.rekolt.app;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class RekoltApp {

//  base price of Produce in MUR per kg
    private static final double mzePricePerKg = 30.0;   // MZE Maize          (cereal)
    private static final double bnsPricePerKg = 90.0;   // BNS Beans          (cereal)
    private static final double potPricePerKg = 45.0;   // POT Potatoes       (perishable)
    private static final double teaPricePerKg = 25.0;   // TEA Green tea-leaf (cash crop)

//  category multiplier
    private static final double cerealMultiplier     = 1.00;
    private static final double perishableMultiplier = 0.90;
    private static final double cashCropMultiplier   = 1.10;

//  percentage amount collected by the commission
    private static final int commissionPercentage = 5;

//  transport levy in MUR for every kilogram delivered.
    private static final double levyPerKg = 2.0;

    private static final int      weeksInSeason = 20;
    private static final String[] produceCodes  = { "MZE", "BNS", "POT", "TEA" };

//     Validation bounds, named so the numbers are not scattered through the file.
    private static final double minMassKg = 0.0;
    private static final double maxMassKg = 5000.0;
    private static final int    minScore  = 0;
    private static final int    maxScore  = 100;

//     The seeded deliveries occupy D-1001 to D-1012, so anything the user
//     records during this run starts at D-1013.
    private static int nextDeliveryNumber = 1013;

//   if statement to check for the produce quality score
    private static String gradeOF(int qualityScore){
        if(qualityScore>=85){
            return "A";
        } else if (qualityScore>=70) {
            return "B";
        } else if (qualityScore>=50) {
            return "C";
        }
        return "Reject";
    }

//   Switch case for assigning grade value to the produce
    private static double gradeMultiplierOf(String grade) {
        double gradeMultiplier;
        gradeMultiplier = switch (grade) {
            case "A" -> 1.15;
            case "B" -> 1.00;
            case "C" -> 0.85;
            default -> 0.00;  // REJECT
        };
        return gradeMultiplier;
    }
}