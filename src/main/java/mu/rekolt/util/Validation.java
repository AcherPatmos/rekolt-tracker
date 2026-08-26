package mu.rekolt.util;

public final class Validation {


    public static final double MinMassKg = 0.0;
    public static final double MaxMassKg = 5000.0;
    public static final int    MinScore   = 0;
    public static final int    MaxScore   = 100;
    public static final int    MinWeek    = 1;
    public static final int    MaxWeek   = 20;

    // Stated once, so the two identifier checks cannot disagree.
    private static final String MemberIdPattern   = "M-\\d{4}";
    private static final String DeliveryIdPattern = "D-\\d{4}";

//  Mass is strictly above the minimum but the maximum is inclusive
    public static boolean isValidMass(double massKg) {
        return massKg > MinMassKg && massKg <= MaxMassKg;
    }

    public static boolean isValidScore(int score) {
        return score >= MinScore && score <= MaxScore;
    }

    public static boolean isValidWeek(int week) {
        return week >= MinWeek && week <= MaxWeek;
    }


    public static boolean isValidMemberId(String raw) {
        return raw != null && raw.matches(MemberIdPattern);
    }

    public static boolean isValidDeliveryId(String raw) {
        return raw != null && raw.matches(DeliveryIdPattern);
    }

    public static boolean isValidName(String raw) {
        return raw != null && !raw.trim().isEmpty();
    }
}