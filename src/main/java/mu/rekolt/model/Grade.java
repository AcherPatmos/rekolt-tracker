package mu.rekolt.model;

import mu.rekolt.util.Validation;

public enum Grade {

    // Declared from the highest band down. The multipliers are rule 2; the
    // minimum scores are the lower bound of each band, the upper bound being
    // implied by the band above it.
    A     (85, 1.15),
    B     (70, 1.00),
    C     (50, 0.85),
    REJECT( 0, 0.00);

    private final int    minimumScore;   // int: a score is counted, not measured
    private final double multiplier;     // double: a scaling factor with decimals

//      Enum constructors are implicitly private and run once per constant when
//      the class is loaded. Nothing outside this file can create a Grade, which
//      is why there can only ever be these four.

    Grade(int minimumScore, double multiplier) {
        this.minimumScore = minimumScore;
        this.multiplier   = multiplier;
    }

    public int minimumScore() {
        return minimumScore;
    }

    public double multiplier() {
        return multiplier;
    }

    public static Grade fromProduceScore(int qualityScore) {
        if (!Validation.isValidScore(qualityScore)) {
            throw new IllegalArgumentException(
                    "Quality score must be from 0 to 100, was " + qualityScore);
        }

        if (qualityScore >= A.minimumScore) {
            return A;
        } else if (qualityScore >= B.minimumScore) {
            return B;
        } else if (qualityScore >= C.minimumScore) {
            return C;
        } else {
            return REJECT;
        }
    }

    public double applyTo(double baseValue) {
        return baseValue * multiplier;
    }

    public boolean attractsDeductions() {
        return this != REJECT;
    }
}