package mu.rekolt.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Member;
import mu.rekolt.model.PaymentRules;
import mu.rekolt.model.Produce;
import mu.rekolt.util.Format;
import mu.rekolt.util.Validation;
import mu.rekolt.util.RoundMoney;
import mu.rekolt.util.WriteRunLog;

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
            for (double[] doubles : grid) {
                columnTotal += doubles[column];
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

//  Polymorphism Loop
    public static void printPriceList() {
        System.out.println();
        System.out.println("Price list (a 100 kg grade B delivery. No commission or Levy fee applied)");
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


    //  Objective 6: the Word report
    public static final Path ReportFile = Path.of("output", "season-report.docx");
    public static final Path LogFile    = Path.of("output", "run-log.txt");

    //  Menu option 3. Builds the figures, hands them to the Word writer, and turns
//  any failure into a sentence the treasurer can act on.
//  The catch blocks run most specific first. Every exception below is a
//  subclass of IOException, so putting IOException at the top would make the
//  rest unreachable and the compiler would reject the method.
    public static boolean writeSeasonReport(Season season) {

        if (season.deliveryCount() == 0) {
            System.out.println();
            System.out.println("There are no deliveries yet, so there is nothing to report.");
            System.out.println("Record at least one delivery with option 1 first.");
            return false;
        }

        DocxReportFigures figures = new DocxReportFigures(season);

        System.out.println();
        System.out.print("Writing " + ReportFile + " ... ");

        try {
            WriteDocxReport.write(figures, ReportFile);

        } catch (NoSuchFileException e) {
            failed("The folder " + ReportFile.toAbsolutePath().getParent() + " does not exist and could not be created.",
                    "Create it yourself, or run the program from the project root where output/ lives.");
            return false;

        } catch (AccessDeniedException e) {
            failed("Permission was refused for " + ReportFile.toAbsolutePath() + ".",
                    "The file is most likely open in Word. Close it and choose option 3 again.");
            return false;

        } catch (FileNotFoundException e) {
            failed("The path " + ReportFile.toAbsolutePath() + " could not be opened for writing.",
                    "Check that output/ is a folder and not a file, and that the name is not in use.");
            return false;

        } catch (FileSystemException e) {
//          Covers a full disk, a read-only volume and a locked file on Windows
            failed("The file system refused the write: " + e.getReason() + ".",
                    "Check that output/ holds no folder named season-report.docx, and that the drive "
                            + "is not read-only or full.");
            return false;

        } catch (IOException e) {
            failed("The report could not be written: " + e.getMessage(),
                    "Check the output/ folder and try again.");
            return false;
        }

        System.out.println(figures.memberCount() + " member sections, done.");
        System.out.println("  " + ReportFile.toAbsolutePath());
        System.out.println("  Season net payable " + Format.money(figures.seasonNet()) + " MUR across "
                + figures.deliveryCount() + " deliveries.");

        if (figures.reconciles()) {
            System.out.println("  Reconciled: the member sections add up to the closing total.");
        } else {
            System.out.println("  WARNING: sections and closing total differ by "
                    + Format.money(figures.reconciliationGap()) + " MUR. Do not hand this out.");
        }

        WriteRunLog.append(LogFile, "season-report.docx written - "
                + figures.memberCount() + " members, "
                + figures.deliveryCount() + " deliveries, "
                + Format.money(figures.seasonNet()) + " MUR, "
                + (figures.reconciles() ? "reconciled" : "NOT RECONCILED"));

        return true;
    }

    //  One shape for every failure: what went wrong, then what to do about it
    private static void failed(String whatHappened, String whatToDo) {
        System.out.println("failed.");
        System.out.println("  " + whatHappened);
        System.out.println("  " + whatToDo);
        WriteRunLog.append(LogFile, "FAILED - " + whatHappened);
    }

}