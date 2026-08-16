package mu.rekolt.app;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

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
        List<Delivery> season = seedSeason();

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
                    printSeasonFigures(season);
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

//    array holding produce code
    private static final String [] produceCodes={ "MZE", "BNS", "POT", "TEA" };

//   array holding base price of Produce in MUR per kg
    private static final double [] basePricePerKgs={30.0,90.0,45.0,25.0};

//  array holding category multiplier
    private static final double [] categoryMultiplier={1.00, 1.00, 0.90, 1.10};

//   array holding category names
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

//  Money for display: thousands separated, two decimals
    private static String money(double amount) {
        return String.format("%,.2f", amount);
    }

//  A mass for display: one decimal
    private static String kg(double massKg) {
        return String.format("%.1f", massKg);
    }

//  A multiplier for display: two decimals
    private static String rate(double multiplier) {
        return String.format("%.2f", multiplier);
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
//  list holding the full season's produce. had to be an Arraylist so that it can dynamically grow.
    private static List<Delivery> seedSeason() {
        List<Delivery> season = new ArrayList<>();

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
//  prints how much individual members are making
    private static void printMemberTotals(List<Delivery> season) {
        System.out.println();
        System.out.println("Total payment per member (MUR)");

        double seasonTotal = 0.0;

        for (int i = 0; i < season.size(); i++) {
            Delivery current = season.get(i);

            boolean alreadySeen = false;
            for (int j = 0; j < i; j++) {
                if (season.get(j).getMemberId().equals(current.getMemberId())) {
                    alreadySeen = true;
                    break;
                }
            }
            if (alreadySeen) {
                continue;
            }

            double memberTotal = 0.0;
            for (Delivery delivery : season) {
                if (delivery.getMemberId().equals(current.getMemberId())) {
                    // The Delivery overload of netPayable. A REJECT returns
                    // 0.0, so rejected loads add nothing without needing a
                    // special case here.
                    memberTotal += netPayable(delivery);
                }
            }

            seasonTotal += memberTotal;
            System.out.printf("  %-8s %-20s %14s%n",
                    current.getMemberId(), current.getMemberName(), money(memberTotal));
        }

        System.out.printf("  %-8s %-20s %14s%n", "", "SEASON TOTAL", money(seasonTotal));
    }

//  builds grid for holding the weekly produce using nested for loops
    private static double[][] buildWeeklyGrid(List<Delivery> season) {
        double[][] grid = new double[weeksInSeason][produceCodes.length];

        for (int week = 1; week <= weeksInSeason; week++) {
            for (int column = 0; column < produceCodes.length; column++) {

                double total = 0.0;
//               nested for loop;
                for (Delivery delivery : season) {
                    if (delivery.getWeek() == week
                            && delivery.getProduceCode().equals(produceCodes[column])) {
                        total += delivery.getMassKg();
                    }
                }

//                 Weeks are numbered from 1 but arrays are indexed from 0,
//                 so week 1 is stored in row 0. This is the only place the
//                 offset appears; the printer converts back with row + 1.
                grid[week - 1][column] = total;
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
//  Method that prints season figures by calling on the build grid method and print member totals
    private static void printSeasonFigures(List<Delivery> season) {
        System.out.println();
        System.out.println("SEASON FIGURES:  " + season.size() + " deliveries recorded");
        printMemberTotals(season);
        printWeeklyGrid(buildWeeklyGrid(season));
    }

}