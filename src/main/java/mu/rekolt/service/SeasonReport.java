package mu.rekolt.service;

import java.util.List;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Member;
import mu.rekolt.model.PaymentRules;
import mu.rekolt.model.Produce;
import mu.rekolt.util.Format;
import mu.rekolt.util.Validation;

public final class SeasonReport {

//     The full five-step breakdown for one delivery.
//     The NET PAYABLE line calls netPayable() rather than adding up the
//     intermediates printed above it, so the figure shown and the figure
//     totalled can never disagree.

    public static void printDeliveryBreakdown(Delivery delivery, String memberName) {
        Produce produce = delivery.getProduce();
        double  massKg  = delivery.getMassKg();
        boolean rejected = delivery.isRejected();

        // Recomputed only so the working can be shown line by line. All
        // unrounded doubles.
        double baseValue     = produce.baseValue(massKg);
        double gradedValue   = delivery.getGrade().applyTo(baseValue);
        double categoryValue = delivery.grossValue();

        System.out.println();
        System.out.println("Delivery " + delivery.getDeliveryId() + " recorded. "
                + delivery.getMemberId() + " " + memberName
                + " - " + produce.getCode() + " " + Format.kg(massKg) + " kg"
                + " - score " + delivery.getQualityScore()
                + " - week " + delivery.getWeek()
                + " - grade " + delivery.getGrade());

        System.out.printf("  1. %-16s %-38s %14s%n", "Base value",
                Format.kg(massKg) + " kg x " + Format.money(produce.getBasePricePerKg()) + " MUR/kg",
                Format.money(baseValue));
        System.out.printf("  2. %-16s %-38s %14s%n", "Grade " + delivery.getGrade(),
                "x " + Format.rate(delivery.getGrade().multiplier()), Format.money(gradedValue));
        System.out.printf("  3. %-16s %-38s %14s%n", produce.categoryName(),
                "x " + Format.rate(produce.categoryMultiplier()), Format.money(categoryValue));
        System.out.printf("  4. %-16s %-38s %14s%n", "Commission",
                rejected ? "not charged on a REJECT"
                        : PaymentRules.CommissionPercentage + "% of the value after step 3",
                "- " + Format.money(PaymentRules.commission(produce, delivery.getGrade(), massKg)));
        System.out.printf("  5. %-16s %-38s %14s%n", "Transport levy",
                rejected ? "not charged on a REJECT"
                        : Format.kg(massKg) + " kg x " + Format.money(PaymentRules.LevyPerKg) + " MUR/kg",
                "- " + Format.money(PaymentRules.levy(delivery.getGrade(), massKg)));
        System.out.printf("     %-16s %-38s %14s MUR%n", "NET PAYABLE", "",
                Format.money(delivery.netPayable()));
    }

//  Total payment per member, highest first. One pass over the members map
    public static void printMemberTotals(Season season) {
        System.out.println();
        System.out.println("Total payment per member (MUR)");

        for (Member member : season.membersByPayment()) {
            System.out.printf("  %-8s %-20s %14s%n",
                    member.getMemberId(), member.getName(), Format.money(member.netPayable()));
        }
        System.out.printf("  %-8s %-20s %14s%n", "", "SEASON TOTAL", Format.money(season.seasonNetPayable()));
    }

    public static void printWeeklyGrid(Season season) {
        double[][] grid  = season.weeklyGrid();
        String[]   codes = ProduceCatalog.codes();

        System.out.println();
        System.out.println("Weekly volume grid (kg)");
        System.out.printf("  %-6s", "Week");
        for (String code : codes) {
            System.out.printf("%10s", code);
        }
        System.out.printf("%10s%n", "Total");

        double seasonMass = 0.0;

        for (int row = 0; row < grid.length; row++) {
            double rowTotal = 0.0;
            for (int column = 0; column < grid[row].length; column++) {
                rowTotal += grid[row][column];
            }
            if (rowTotal == 0.0) {
                continue;
            }

            System.out.printf("  %-6d", row + 1);   // row 0 is week 1
            for (int column = 0; column < grid[row].length; column++) {
                System.out.printf("%10s", Format.kg(grid[row][column]));
            }
            System.out.printf("%10s%n", Format.kg(rowTotal));
            seasonMass += rowTotal;
        }

        System.out.printf("  %-6s", "All");
        for (int column = 0; column < codes.length; column++) {
            double columnTotal = 0.0;
            for (int row = 0; row < grid.length; row++) {
                columnTotal += grid[row][column];
            }
            System.out.printf("%10s", Format.kg(columnTotal));
        }
        System.out.printf("%10s%n", Format.kg(seasonMass));
    }

//  Top deliveries by value. Sorted with the Comparator on Delivery
    public static void printTopDeliveries(Season season, int howMany) {
        System.out.println();
        System.out.println("Top " + howMany + " deliveries by value");

        List<Delivery> top = season.topByValue(howMany);
        int position = 1;
        for (Delivery delivery : top) {
            System.out.printf("  %d. %-8s %-8s %-4s %10s kg  %-6s %14s%n",
                    position, delivery.getDeliveryId(), delivery.getMemberId(),
                    delivery.getProduceCode(), Format.kg(delivery.getMassKg()),
                    delivery.getGrade(), Format.money(delivery.netPayable()));
            position++;
        }
    }

    public static void printPriceList() {
        System.out.println();
        System.out.println("Price list (a 100 kg grade B load of each)");
        System.out.printf("  %-5s %-16s %-12s %10s %14s%n",
                "Code", "Produce", "Category", "MUR/kg", "100 kg, B");

        for (Produce produce : ProduceCatalog.all()) {
            double sample = produce.valueOf(100.0, mu.rekolt.model.Grade.B);
            System.out.printf("  %-5s %-16s %-12s %10s %14s%n",
                    produce.getCode(), produce.getDisplayName(), produce.categoryName(),
                    Format.money(produce.getBasePricePerKg()), Format.money(sample));
        }
    }

//  Menu option 2
    public static void printSeasonFigures(Season season) {
        System.out.println();
        System.out.println("SEASON FIGURES - " + season.deliveryCount() + " deliveries, "
                + season.memberCount() + " members, " + season.rejectedCount() + " rejected");
        printMemberTotals(season);
        printWeeklyGrid(season);
        printTopDeliveries(season, 5);
        printPriceList();
    }

//  The member lookup, showing the absent case handled rather than crashing
    public static void printMemberLookup(Season season, String memberId) {
        System.out.println();
        season.findMember(memberId).ifPresentOrElse(
                member -> {
                    System.out.println(member.reportTitle());
                    for (String line : member.reportLines()) {
                        System.out.println("  " + line);
                    }
                },
                () -> System.out.println("No member " + memberId + " delivered anything this season.")
        );
    }

    public static void printReport(Season season) {
        System.out.println();
        System.out.println("The Word report arrives in objective 6. The season is ready for it: "
                + season.memberCount() + " member sections, "
                + season.deliveryCount() + " deliveries, "
                + Format.money(season.seasonNetPayable()) + " MUR to pay out.");
        System.out.println("Weeks " + Validation.MinWeek + " to " + Validation.MaxWeek + " are covered.");
    }
}