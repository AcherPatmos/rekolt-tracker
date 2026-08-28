package mu.rekolt.app;

import java.util.Scanner;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Produce;
import mu.rekolt.service.SeasonReport;
import mu.rekolt.service.Season;

public class RekoltApp {

    private static void printMenu() {
        System.out.println();
        System.out.println("REKOLT PRODUCE TRACKER - season 2026");
        System.out.println("1. Record a delivery          4. Look up a member");
        System.out.println("2. Season figures on screen   5. Exit");
        System.out.println("3. Generate the season report");
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        Season season = Season.seeded();

        boolean running = true;
        while (running) {
            printMenu();

            switch (Main.readMenuOption(in)) {
                case 1: {
                    String  memberId = Main.readMemberId(in);
                    String  name     = Main.readName(in);
                    Produce produce  = Main.readProduce(in);
                    double  massKg   = Main.readMass(in);
                    int     score    = Main.readScore(in);
                    int     week     = Main.readWeek(in);

                    Delivery recorded = season.record(memberId, name, produce, massKg, score, week);
                    SeasonReport.printDeliveryBreakdown(recorded, name);
                    break;
                }
                case 2:
                    SeasonReport.printSeasonFigures(season);
                    break;
                case 3:
                    SeasonReport.printReport(season);
                    break;
                case 4: {
                    String memberId = Main.readMemberIdOrBlank(in);
                    if (!memberId.isEmpty()) {
                        SeasonReport.printMemberLookup(season, memberId);
                    }
                    break;
                }
                case 5:
                    running = false;
                    System.out.println();
                    System.out.println("Goodbye.");
                    break;
                default:
                    // Unreachable: readMenuOption only returns 1 to 5.
                    break;
            }
        }

        in.close();
    }
}