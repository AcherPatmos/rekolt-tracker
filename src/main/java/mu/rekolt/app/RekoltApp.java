package mu.rekolt.app;
import mu.rekolt.model.Delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static mu.rekolt.util.Format.*;

public class RekoltApp {

//   Menu options
    private static void printMenu() {
        System.out.println();
        System.out.println("REKOLT PRODUCE TRACKER - season 2026");
        System.out.println("1. Record a delivery          3. Generate the season report");
        System.out.println("2. Season figures on screen   4. Exit");
    }
//  runs the whole program
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        ArrayList<Delivery> season = seedSeason();

        boolean running = true;
        while (running) {
            printMenu();

            switch (readMenuOption(in)) {
                case 1: {
                    Delivery recorded = readDelivery(in);
                    season.add(recorded);
                    printDeliveryBreakdown(recorded);
                    break;
                }
                case 2:
                    printSeasonFigures(in,season);
                    break;
                case 3:
                    System.out.println();
                    System.out.println("Not implemented yet. The season report arrives in objective 6.");
                    break;
                case 4:
                    running = false;
                    System.out.println();
                    System.out.println("Goodbye.");
                    break;
                default:
                    // Unreachable: readMenuOption only returns 1 to 4.
                    break;
            }
        }

        in.close();
    }

//  Menu option: a whole number from 1 to 4
    private static int readMenuOption(Scanner in) {
        while (true) {
            System.out.print("Choose an option: ");
            String raw = in.nextLine().trim();
            try {
                int option = Integer.parseInt(raw);
                if (option >= 1 && option <= 4) {
                    return option;
                }
                System.out.println("Please choose 1, 2, 3 or 4. Please try again.");
            } catch (NumberFormatException e) {
                // Catches both empty input and text, since neither parses.
                System.out.println("The option must be a whole number from 1 to 4. Please try again.");
            }
        }
    }

//   list holding produce code
    private static final String [] produceCodes={ "MZE", "BNS", "POT", "TEA" };

//   list holding base price of Produce in MUR per kg
    private static final double [] basePricePerKgs={30.0,90.0,45.0,25.0};

//  list holding category multiplier
    private static final double [] categoryMultiplier={1.00, 1.00, 0.90, 1.10};

//  list holding category names
    private static final String [] categoryNames={"Cereal", "Cereal", "Perishable", "Cash Crop"};

//  percentage amount collected by the commission
    private static final int commissionPercentage = 5;

//  transport levy in MUR for every kilogram delivered.
    private static final double levyPerKg = 2.0;

    private static final int      weeksInSeason = 20;

//     Validation bounds, named so the numbers are not scattered through the file.
    private static final double minMassKg = 0.0;
    private static final double maxMassKg = 5000.0;
    private static final int    minScore  = 0;
    private static final int    maxScore  = 100;

//  How many rows the "top deliveries" table shows
    private static final int topDeliveryCount = 5;

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

//  Helper function to help match the produce code index to the right base price and category
    private static int indexOfProduceCode(String produceCode){
        for (int i = 0; i < produceCode.length(); i++) {
            if(produceCodes[i].equals(produceCode)){
                return i;
            }
        }
        return -1;
    }

//  Base price in MUR per kg for a produce code
    private static double basePriceOf(String produceCode){
        int index = indexOfProduceCode(produceCode);
        return (index<0) ? 0.0: basePricePerKgs[index];
    }

//  category multiplier based on produce type
    private static double categoryMultiplierOf(String produceCode){
        int index = indexOfProduceCode(produceCode);
        return (index<0) ? 0.0: categoryMultiplier[index];
    }

//  produce category identifier
    private static String categoryNameOf(String produceCode){
        int index=indexOfProduceCode(produceCode);
        return (index<0) ? "unknown": categoryNames[index];
    }

    private static double netPayable(double massKg, double basePrice,
                                     double gradeMultiplier, double categoryMultiplier) {

        double baseValue     = massKg * basePrice;                  // step 1
        double gradedValue   = baseValue * gradeMultiplier;         // step 2
        double categoryValue = gradedValue * categoryMultiplier;    // step 3

//      The cast is required.
        double commissionRate = (double) commissionPercentage / 100;
        double commission = categoryValue * commissionRate;         // step 4
//      checks if a produce was rejected before applying levy fee
        double levy = (gradeMultiplier > 0.0) ? massKg * levyPerKg : 0.0;   // step 5

        return categoryValue - commission - levy;
    }
//  Method overload
    private static double netPayable(Delivery delivery){
        String grade = gradeOF(delivery.getQualityScore());
        return netPayable(delivery.getMassKg(), basePriceOf(delivery.getProduceCode()),
                gradeMultiplierOf(grade), categoryMultiplierOf(delivery.getProduceCode()));
    }

//  hash map that connects each farmer to the amount they earned from their produce
    private static HashMap<String, Double> buildMemberTotals(List<Delivery> season) {
        HashMap<String, Double> totals = new HashMap<>();

        for (Delivery delivery : season) {
            String memberId = delivery.getMemberId();
            double runningTotal = totals.getOrDefault(memberId, 0.0);
            totals.put(memberId, runningTotal + netPayable(delivery));
        }

        return totals;
    }
//  maps the members to their individual deliveries. if it is the first time, it creates a new arraylist for them
    private static HashMap<String, List<Delivery>> buildDeliveriesByMember(List<Delivery> season) {
        HashMap<String, List<Delivery>> byMember = new HashMap<>();

        for (Delivery delivery : season) {
            String memberId = delivery.getMemberId();

            List<Delivery> theirDeliveries = byMember.computeIfAbsent(memberId, k -> new ArrayList<>());
            theirDeliveries.add(delivery);
        }

        return byMember;
    }
//  The distinct member identifiers(ids) seen this season stored in a hashset
    private static HashSet<String> collectMemberIds(List<Delivery> season) {
        HashSet<String> memberIds = new HashSet<>();

        for (Delivery delivery : season) {
            memberIds.add(delivery.getMemberId());
        }

        return memberIds;
    }
//  checks if a produce delivered was rejected
    private static boolean isRejected(Delivery delivery){
        return gradeOF(delivery.getQualityScore()).equals("Reject");
    }
//  stores a list of produce deliveries that were not rejected
    private static List<Delivery> withoutRejects(List<Delivery> season) {
//         new ArrayList<>(season) copies the list. Both lists then point at
//         the same Delivery objects, which is harmless here because Delivery
//         has final fields and cannot be altered by anyone.
        List<Delivery> payable = new ArrayList<>(season);

        Iterator<Delivery> iterator = payable.iterator();
        while (iterator.hasNext()) {
            Delivery delivery = iterator.next();   // next() must be called before remove()
            if (isRejected(delivery)) {
                iterator.remove();
            }
        }
        return payable;
    }
//  Orders deliveries by net payable, largest first, so a report can print the biggest loads at the top.
    private static class DeliveryByDescendingValue implements Comparator<Delivery> {
        @Override
        public int compare(Delivery a, Delivery b) {
            int byValue = Double.compare(netPayable(b), netPayable(a));
            if (byValue != 0) {
                return byValue;
            }
            // Equal value: fall back to the identifier so two identical loads
            // always print in the same order.
            return a.getDeliveryId().compareTo(b.getDeliveryId());
        }
    }
//  Orders member identifiers by their season total, largest first, ties broken
//  by identifier so the ordering is reproducible.
    private static class MemberByTotalDescending implements Comparator<String> {

        private final Map<String, Double> totals;

        MemberByTotalDescending(Map<String, Double> totals) {
            this.totals = totals;
        }

        @Override
        public int compare(String memberIdA, String memberIdB) {
            double totalA = totals.getOrDefault(memberIdA, 0.0);
            double totalB = totals.getOrDefault(memberIdB, 0.0);

            int byTotal = Double.compare(totalB, totalA);   // b first: descending
            if (byTotal != 0) {
                return byTotal;
            }
            return memberIdA.compareTo(memberIdB);
        }
    }
//  Resolves a member identifier to the name to display for them.
    private static String nameOf(String memberId, Map<String, List<Delivery>> byMember) {
        List<Delivery> theirDeliveries = byMember.get(memberId);
        if (theirDeliveries == null || theirDeliveries.isEmpty()) {
            return "(name not recorded)";
        }
        return theirDeliveries.getLast().getMemberName();
    }
//  Prints the report for a single member: their details if they have delivered
//  this season, or a short "not found" note if not.
private static void printMemberSearch(String memberId,
                                      Map<String, Double> totals,
                                      Map<String, List<Delivery>> byMember,
                                      Set<String> memberIds) {
    System.out.println();

    if (!memberIds.contains(memberId)) {
        System.out.println("  No deliveries recorded for " + memberId + " this season.");
        System.out.println("  " + memberIds.size() + " members have delivered so far.");
        return;
    }

    // A copy, because sorting rearranges the list it is given and the map
    // should keep its deliveries in the order they were recorded.
    List<Delivery> theirDeliveries =
            new ArrayList<>(byMember.getOrDefault(memberId, new ArrayList<>()));

    // Collections.sort with no comparator uses the NATURAL order, which is
    // the compareTo written inside Delivery: by identifier, so by date.
    Collections.sort(theirDeliveries);

    System.out.println("  " + memberId + "  " + nameOf(memberId, byMember)
            + "  -  " + theirDeliveries.size() + " deliveries");
    System.out.printf("    %-8s %-6s %-5s %10s %-7s %14s%n",
            "Slip", "Week", "Code", "Mass kg", "Grade", "Net MUR");

    for (Delivery delivery : theirDeliveries) {
        System.out.printf("    %-8s %-6d %-5s %10s %-7s %14s%n",
                delivery.getDeliveryId(),
                delivery.getWeek(),
                delivery.getProduceCode(),
                kg(delivery.getMassKg()),
                gradeOF(delivery.getQualityScore()),
                money(netPayable(delivery)));
    }

    System.out.printf("    %-8s %-6s %-5s %10s %-7s %14s%n",
            "", "", "", "", "TOTAL", money(totals.getOrDefault(memberId, 0.0)));
}

//  Pattern identifier: the letter M, a hyphen, then exactly four digits
    private static String readMemberId(Scanner in) {
        while (true) {
            System.out.print("Member identifier : ");
            String raw = in.nextLine().trim();
            if (raw.matches("M-\\d{4}")) {
                return raw;
            }
            System.out.println("The identifier must be M, a hyphen and four digits, for example M-0042. Please try again.");
        }
    }

    private static String readMemberIdOrBlank(Scanner in) {
        while (true) {
            System.out.print("Look up a member (ex: M-0042, or press Enter to skip) : ");
            String raw = in.nextLine().trim().toUpperCase();
            if (raw.isEmpty()) {
                return "";
            }
            if (raw.matches("M-\\d{4}")) {
                return raw;
            }
            System.out.println("Type an identifier such as M-0042, or press Enter to skip. Please try again.");
        }
    }

//  Member name: any non-empty text
    private static String readName(Scanner in) {
        while (true) {
            System.out.print("Member name : ");
            String raw = in.nextLine().trim();
            if (!raw.isEmpty()) {
                return raw;
            }
            System.out.println("The name cannot be empty or only spaces. Please try again.");
        }
    }

//    Produce code: MZE, BNS, POT or TEA, typed in either case
    private static String readProduceCode(Scanner in) {
        while (true) {
            System.out.print("Produce code (MZE/BNS/POT/TEA) : ");
            String raw = in.nextLine().trim().toUpperCase();
            for (String code : produceCodes) {
                if (code.equals(raw)) {
                    return raw;
                }
            }
            System.out.println("The produce code must be MZE, BNS, POT or TEA. Please try again.");
        }
    }
//  Mass input : a decimal number above 0 and not more than 5000
    private static double readMass(Scanner in) {
        while (true) {
            System.out.print("Mass in kg : ");
            String raw = in.nextLine().trim();
            try {
                double massKg = Double.parseDouble(raw);
                // Strictly above the minimum, but the maximum is inclusive:
                // 5000 is accepted, 5000.01 is not, and 0 is not.
                if (massKg > minMassKg && massKg <= maxMassKg) {
                    return massKg;
                }
                System.out.println("Mass must be above 0 and not more than 5000. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Mass must be a number. Please try again.");
            }
        }
    }
//  Quality score: a whole number from 0 to 100.
    private static int readScore(Scanner in) {
        while (true) {
            System.out.print("Quality score (0-100) : ");
            String raw = in.nextLine().trim();
            try {
                int score = Integer.parseInt(raw);
                if (score >= minScore && score <= maxScore) {
                    return score;
                }
                System.out.println("The score must be from 0 to 100. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("The score must be a whole number. Please try again.");
            }
        }
    }

//  Week of delivery: a whole number from 1 to 20
    private static int readWeek(Scanner in) {
        while (true) {
            System.out.print("Week of delivery (1-20) : ");
            String raw = in.nextLine().trim();
            try {
                int week = Integer.parseInt(raw);
                if (week >= 1 && week <= weeksInSeason) {
                    return week;
                }
                System.out.println("The week must be from 1 to 20. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("The week must be a whole number. Please try again.");
            }
        }
    }

//  Runs the six prompts in order and assembles the result into a Delivery
    private static Delivery readDelivery(Scanner in) {
        String memberId    = readMemberId(in);
        String memberName  = readName(in);
        String produceCode = readProduceCode(in);
        double massKg      = readMass(in);
        int    qualityScore = readScore(in);
        int    week        = readWeek(in);

        String deliveryId = "D-" + nextDeliveryNumber;
        nextDeliveryNumber++;

        return new Delivery(deliveryId, memberId, memberName,
                produceCode, massKg, qualityScore, week);
    }
//  array list holding the full season's produce. had to be an Arraylist so that it can dynamically grow.
    private static ArrayList<Delivery> seedSeason() {
        ArrayList<Delivery> season = new ArrayList<>();

        //                            id             member                name                          code              mass           score         week
        season.add(new Delivery("D-1001", "M-0042", "Devi Ramjaun",    "BNS", 236.0, 91, 1));
        season.add(new Delivery("D-1002", "M-0117", "Jean Ah-Kine",    "MZE", 412.5, 78, 1));
        season.add(new Delivery("D-1003", "M-0088", "Anisha Beeharry", "POT", 150.0, 60, 2));
        season.add(new Delivery("D-1004", "M-0042", "Devi Ramjaun",    "TEA",  88.3, 91, 1));
        season.add(new Delivery("D-1005", "M-0117", "Jean Ah-Kine",    "POT", 200.0, 42, 3));  // REJECT
        season.add(new Delivery("D-1006", "M-0203", "Kavi Soobrayen",  "MZE", 180.0, 66, 2));
        season.add(new Delivery("D-1007", "M-0311", "Marie Lafleur",   "BNS", 390.5, 76, 2));
        season.add(new Delivery("D-1008", "M-0203", "Kavi Soobrayen",  "TEA", 120.0, 85, 3));  // boundary: A
        season.add(new Delivery("D-1009", "M-0311", "Marie Lafleur",   "MZE", 260.0, 70, 3));  // boundary: B
        season.add(new Delivery("D-1010", "M-0256", "Rajesh Gopaul",   "POT", 320.0, 50, 4));  // boundary: C
        season.add(new Delivery("D-1011", "M-0256", "Rajesh Gopaul",   "BNS",  75.5, 49, 4));  // boundary: REJECT
        season.add(new Delivery("D-1012", "M-0203", "Kavi Soobrayen",  "MZE", 500.0, 84, 5));  // boundary: B

        return season;
    }

//  Prints the full five-step breakdown for one delivery
    private static void printDeliveryBreakdown(Delivery delivery) {

        String grade              = gradeOF(delivery.getQualityScore());
        double gradeMultiplier    = gradeMultiplierOf(grade);
        double basePrice          = basePriceOf(delivery.getProduceCode());
        double categoryMultiplier = categoryMultiplierOf(delivery.getProduceCode());
        double massKg             = delivery.getMassKg();
        boolean rejected          = grade.equals("REJECT");

        double baseValue      = massKg * basePrice;
        double gradedValue    = baseValue * gradeMultiplier;
        double categoryValue  = gradedValue * categoryMultiplier;
        double commissionRate = (double) commissionPercentage / 100;
        double commission     = rejected ? 0.0 : categoryValue * commissionRate;
        double levy           = rejected ? 0.0 : massKg * levyPerKg;

        System.out.println();
        System.out.println("Delivery " + delivery.getDeliveryId() + " recorded. "
                + delivery.getMemberId() + " " + delivery.getMemberName()
                + " - " + delivery.getProduceCode() + " " + kg(massKg) + " kg"
                + " - score " + delivery.getQualityScore()
                + " - week " + delivery.getWeek()
                + " - grade " + grade);

        System.out.printf("  1. %-16s %-38s %14s%n", "Base value",
                kg(massKg) + " kg x " + money(basePrice) + " MUR/kg", money(baseValue));
        System.out.printf("  2. %-16s %-38s %14s%n", "Grade " + grade,
                "x " + rate(gradeMultiplier), money(gradedValue));
        System.out.printf("  3. %-16s %-38s %14s%n", categoryNameOf(delivery.getProduceCode()),
                "x " + rate(categoryMultiplier), money(categoryValue));
        System.out.printf("  4. %-16s %-38s %14s%n", "Commission",
                rejected ? "not charged on a REJECT"
                        : commissionPercentage + "% of the value after step 3",
                "- " + money(commission));
        System.out.printf("  5. %-16s %-38s %14s%n", "Transport levy",
                rejected ? "not charged on a REJECT"
                        : kg(massKg) + " kg x " + money(levyPerKg) + " MUR/kg",
                "- " + money(levy));
        System.out.printf("     %-16s %-38s %14s MUR%n", "NET PAYABLE", "",
                money(netPayable(delivery)));
    }
//  prints how much individual members are making using hashmaps and hashset
    private static void printMemberTotals(Map<String, Double> totals,
                                          Map<String, List<Delivery>> byMember,
                                          Set<String> memberIds) {
        System.out.println();
        System.out.println("Total payment per member (MUR)");

//  a new array to sort the data from our hashset is created
        List<String> ordered = new ArrayList<>(memberIds);
        ordered.sort(new MemberByTotalDescending(totals));

        double seasonTotal = 0.0;

        for (String memberId : ordered) {
            double memberTotal = totals.getOrDefault(memberId, 0.0);
            int    slipCount   = byMember.getOrDefault(memberId, new ArrayList<>()).size();

            seasonTotal += memberTotal;

            System.out.printf("  %-8s %-20s %10s %14s%n",
                    memberId, nameOf(memberId, byMember),
                    slipCount + (slipCount == 1 ? " slip" : " slips"),
                    money(memberTotal));
        }

        System.out.printf("  %-8s %-20s %10s %14s%n",
                "", "SEASON TOTAL", "", money(seasonTotal));
        System.out.println("  " + memberIds.size()
                + " distinct members were recorded, so the season report needs "
                + memberIds.size() + " member sections.");
    }

//  builds grid for holding the weekly produce delivered
    private static double[][] buildWeeklyGrid(List<Delivery> season) {
        double[][] grid = new double[weeksInSeason][produceCodes.length];

        for(Delivery delivery: season){
            int row= delivery.getWeek()-1;
            int column= indexOfProduceCode(delivery.getProduceCode());
            if (row >= 0 && row < weeksInSeason && column >= 0) {
                grid[row][column] += delivery.getMassKg();
            }
        }
        return grid;
    }

//  Prints the grid format built by build weekly grid
    private static void printWeeklyGrid(double[][] grid) {
        System.out.println();
        System.out.println("Weekly volume grid (kg)");

        System.out.printf("  %-6s", "Week");
        for (String code : produceCodes) {
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

            System.out.printf("  %-6d", row + 1);
            for (int column = 0; column < grid[row].length; column++) {
                System.out.printf("%10s", kg(grid[row][column]));
            }
            System.out.printf("%10s%n", kg(rowTotal));

            seasonMass += rowTotal;
        }

        System.out.printf("  %-6s", "All");
        for (int column = 0; column < produceCodes.length; column++) {
            double columnTotal = 0.0;
            for (double[] doubles : grid) {
                columnTotal += doubles[column];
            }
            System.out.printf("%10s", kg(columnTotal));
        }
        System.out.printf("%10s%n", kg(seasonMass));
    }

private static void printTopDeliveries(List<Delivery> season){
    System.out.println();
    System.out.println("Top " + topDeliveryCount + " deliveries by value");

    List<Delivery> payable = withoutRejects(season);
    int removed = season.size() - payable.size();
    payable.sort(new DeliveryByDescendingValue());

    if (payable.isEmpty()) {
        System.out.println("  Nothing payable this season.");
        return;
    }

    int rows = Math.min(topDeliveryCount, payable.size());
    for (int i = 0; i < rows; i++) {
        Delivery delivery = payable.get(i);
        System.out.printf("  %d. %-8s %-8s %-5s %10s kg  %-7s %14s%n",
                i + 1,
                delivery.getDeliveryId(),
                delivery.getMemberId(),
                delivery.getProduceCode(),
                kg(delivery.getMassKg()),
                gradeOF(delivery.getQualityScore()),
                money(netPayable(delivery)));
    }

    System.out.println("  " + removed + " rejected deliveries were removed from this table, "
            + "however they remain in the season report and in the volume grid.");

}

//  Method that prints season figures by calling on the build grid method and print member totals
private static void printSeasonFigures(Scanner in, List<Delivery> season) {
    System.out.println();
    System.out.println("SEASON FIGURES - " + season.size() + " deliveries recorded");

    Map<String, Double>         totals    = buildMemberTotals(season);
    Map<String, List<Delivery>> byMember  = buildDeliveriesByMember(season);
    Set<String>                 memberIds = collectMemberIds(season);

    printMemberTotals(totals, byMember, memberIds);
    printWeeklyGrid(buildWeeklyGrid(season));
    printTopDeliveries(season);

    System.out.println();
    String wanted = readMemberIdOrBlank(in);
    if (!wanted.isEmpty()) {
        printMemberSearch(wanted, totals, byMember, memberIds);
    }
}

}