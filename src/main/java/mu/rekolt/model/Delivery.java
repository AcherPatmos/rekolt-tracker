package mu.rekolt.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import mu.rekolt.util.Format;
import mu.rekolt.util.Validation;

public class Delivery implements Payable, Reportable, Comparable<Delivery> {

    private final String  deliveryId;    // D-1001 and upwards
    private final String  memberId;      // M followed by a hyphen and four digits
    private final Produce produce;       // the object, not a code
    private final double  massKg;        // double: kilograms are measured, not counted
    private final int     qualityScore;  // int: a whole number from 0 to 100
    private final int     week;          // int: a whole number from 1 to 20
    private final Grade   grade;         // derived from qualityScore, fixed for life

    public Delivery(String deliveryId, String memberId, Produce produce,
                    double massKg, int qualityScore, int week) {

        // These throw rather than re-prompt because a
        // constructor has no user to ask again: anything reaching here has
        // already passed ConsoleReader, so a failure is a bug in the calling
        // code, not a typo at the keyboard.
        if (!Validation.isValidDeliveryId(deliveryId)) {
            throw new IllegalArgumentException("Delivery id must be D and four digits, was " + deliveryId);
        }
        if (!Validation.isValidMemberId(memberId)) {
            throw new IllegalArgumentException("Member id must be M and four digits, was " + memberId);
        }
        if (produce == null) {
            throw new IllegalArgumentException("A delivery must have a produce type");
        }
        if (!Validation.isValidMass(massKg)) {
            throw new IllegalArgumentException("Mass must be above 0 and not more than 5000, was " + massKg);
        }
        if (!Validation.isValidScore(qualityScore)) {
            throw new IllegalArgumentException("Quality score must be from 0 to 100, was " + qualityScore);
        }
        if (!Validation.isValidWeek(week)) {
            throw new IllegalArgumentException("Week must be from 1 to 20, was " + week);
        }

        this.deliveryId   = deliveryId;
        this.memberId     = memberId;
        this.produce      = produce;
        this.massKg       = massKg;
        this.qualityScore = qualityScore;
        this.week         = week;
        this.grade        = Grade.fromProduceScore(qualityScore);   // asked once, here, and only here
    }

    public String  getDeliveryId()   { return deliveryId; }
    public String  getMemberId()     { return memberId; }
    public Produce getProduce()      { return produce; }
    public double  getMassKg()       { return massKg; }
    public int     getQualityScore() { return qualityScore; }
    public int     getWeek()         { return week; }
    public Grade   getGrade()        { return grade; }

//  Convenience for the weekly grid, which groups by code
    public String getProduceCode()   { return produce.getCode(); }

//   True when this load was rejected. Asked of the grade, never of a string
    public boolean isRejected()      { return !grade.attractsDeductions(); }

    @Override
    public double netPayable() {
        return PaymentRules.netPayable(this);
    }

//  Steps 1 to 3: the value before deductions. Computed by the produce itself
    @Override
    public double grossValue() {
        return produce.valueOf(massKg, grade);
    }

    @Override
    public String reportTitle() {
        return deliveryId + "  " + produce.getCode() + "  " + Format.kg(massKg) + " kg  grade " + grade;
    }

    @Override
    public List<String> reportLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Week " + week + ", quality score " + qualityScore);
        lines.add("Base value  " + Format.kg(massKg) + " kg x " + Format.money(produce.getBasePricePerKg())
                + " = " + Format.money(produce.baseValue(massKg)));
        lines.add("Grade " + grade + "  x " + Format.rate(grade.multiplier()));
        lines.add(produce.categoryName() + "  x " + Format.rate(produce.categoryMultiplier())
                + " = " + Format.money(grossValue()));
        lines.add("Commission  - " + Format.money(PaymentRules.commission(produce, grade, massKg)));
        lines.add("Levy        - " + Format.money(PaymentRules.levy(grade, massKg)));
        lines.add("NET PAYABLE = " + Format.money(netPayable()) + " MUR");
        return lines;
    }

    @Override
    public int compareTo(Delivery other) {
        return this.deliveryId.compareTo(other.deliveryId);
    }

//  Highest value first: the "top five deliveries by value" table
    public static final Comparator<Delivery> BYDescValue =
            Comparator.comparingDouble(Delivery::netPayable).reversed();

    public static final Comparator<Delivery> ByMemberThenWeek =
            Comparator.comparing(Delivery::getMemberId)
                    .thenComparingInt(Delivery::getWeek)
                    .thenComparing(Delivery::getDeliveryId);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Delivery that = (Delivery) other;
        return deliveryId.equals(that.deliveryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s kg score %d week %d grade %s",
                deliveryId, memberId, produce.getCode(), Format.kg(massKg), qualityScore, week, grade);
    }
}