package mu.rekolt.app;

import java.util.Scanner;

import mu.rekolt.model.Produce;
import mu.rekolt.service.ProduceCatalog;
import mu.rekolt.util.Validation;

public final class Main {

//   Menu option: a whole number from 1 to 5.
    public static int readMenuOption(Scanner in) {
        while (true) {
            System.out.print("Choose an option: ");
            String raw = in.nextLine().trim();
            try {
                int option = Integer.parseInt(raw);
                if (option >= 1 && option <= 5) {
                    return option;
                }
                System.out.println("Please choose 1, 2, 3, 4 or 5. Please try again.");
            } catch (NumberFormatException e) {
                // Catches both empty input and text, since neither parses.
                System.out.println("The option must be a whole number from 1 to 5. Please try again.");
            }
        }
    }

//  Member identifier. Upper-cased at the door, so nothing downstream thinks about case
    public static String readMemberId(Scanner in) {
        while (true) {
            System.out.print("Member identifier : ");
            String raw = in.nextLine().trim().toUpperCase();
            if (Validation.isValidMemberId(raw)) {
                return raw;
            }
            System.out.println("The identifier must be M, a hyphen and four digits, for example M-0042. Please try again.");
        }
    }

//  The same prompt, but Enter skips. Normalised identically, so the two cannot disagree.
    public static String readMemberIdOrBlank(Scanner in) {
        while (true) {
            System.out.print("Look up a member (M-0042, or press Enter to skip) : ");
            String raw = in.nextLine().trim().toUpperCase();
            if (raw.isEmpty()) {
                return "";
            }
            if (Validation.isValidMemberId(raw)) {
                return raw;
            }
            System.out.println("Type an identifier such as M-0042, or press Enter to skip. Please try again.");
        }
    }

//  Member name: any non-empty text. trim() covers the whitespace-only case as well
    public static String readName(Scanner in) {
        while (true) {
            System.out.print("Member name : ");
            String raw = in.nextLine().trim();
            if (Validation.isValidName(raw)) {
                return raw;
            }
            System.out.println("The name cannot be empty or only spaces. Please try again.");
        }
    }

    public static Produce readProduce(Scanner in) {
        while (true) {
            System.out.print("Produce code (MZE/BNS/POT/TEA) : ");
            String raw = in.nextLine().trim().toUpperCase();

            java.util.Optional<Produce> found = ProduceCatalog.forCode(raw);
            if (found.isPresent()) {
                return found.get();
            }
            System.out.println("The produce code must be MZE, BNS, POT or TEA. Please try again.");
        }
    }

//  Mass: a decimal number above 0 and not more than 5000
    public static double readMass(Scanner in) {
        while (true) {
            System.out.print("Mass in kg : ");
            String raw = in.nextLine().trim();
            try {
                double massKg = Double.parseDouble(raw);
                if (Validation.isValidMass(massKg)) {
                    return massKg;
                }
                System.out.println("Mass must be above 0 and not more than 5000. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Mass must be a number. Please try again.");
            }
        }
    }

    public static int readScore(Scanner in) {
        while (true) {
            System.out.print("Quality score (0-100) : ");
            String raw = in.nextLine().trim();
            try {
                int score = Integer.parseInt(raw);
                if (Validation.isValidScore(score)) {
                    return score;
                }
                System.out.println("The score must be from 0 to 100. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("The score must be a whole number. Please try again.");
            }
        }
    }

//  Week of delivery: a whole number from 1 to 20
    public static int readWeek(Scanner in) {
        while (true) {
            System.out.print("Week of delivery (1-20) : ");
            String raw = in.nextLine().trim();
            try {
                int week = Integer.parseInt(raw);
                if (Validation.isValidWeek(week)) {
                    return week;
                }
                System.out.println("The week must be from 1 to 20. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("The week must be a whole number. Please try again.");
            }
        }
    }
}