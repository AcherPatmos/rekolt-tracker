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

}