package mu.rekolt.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

//  Rounding money to the cent. This allows for easier report printing since rounding
//  is only done at the last step of net payment
public final class RoundMoney {

    private RoundMoney() { }

    //  Two figures agree if they differ by less than half a cent
    public static final double CentTolerance = 0.005;

    //  HALF_UP matches String.format, so a figure rounded here can never disagree
//  with the same figure rounded by Format.money
    public static double toCents(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    //  Never compare money with ==; a chain of double arithmetic lands near, not on
    public static boolean sameToTheCent(double a, double b) {
        return Math.abs(a - b) < CentTolerance;
    }
}