package mu.rekolt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Member;
import mu.rekolt.model.Produce;
import mu.rekolt.util.Validation;

public class Season {

    private final List<Delivery>       deliveries           = new ArrayList<>();
    private final Map<String, Member>  members              = new LinkedHashMap<>();
    private final Map<String, Double>  totalPaymentByMember = new HashMap<>();
    private final Set<String>          memberIds            = new HashSet<>();

//  The seeded slips occupy D-1001 to D-1012, so a recorded one starts at D-1013
    private int nextDeliveryNumber = 1013;

    public Delivery record(String memberId, String memberName, Produce produce,
                           double massKg, int qualityScore, int week) {

        String deliveryId = "D-" + nextDeliveryNumber;
        nextDeliveryNumber++;

        // The constructor enforces the throw error checks, so an invalid slip never
        // reaches the collections.
        Delivery delivery = new Delivery(deliveryId, memberId, produce, massKg, qualityScore, week);

        Member member = members.get(memberId);
        if (member == null) {
            member = new Member(memberId, memberName);
            members.put(memberId, member);
        }
        member.addDelivery(delivery);

        deliveries.add(delivery);
        memberIds.add(memberId);

        // merge: put the value if the key is absent, otherwise combine the old
        // and new values with the given function. One call replaces the
        // get-null-check-put dance and cannot forget the absent case.
        totalPaymentByMember.merge(memberId, delivery.netPayable(), Double::sum);

        return delivery;
    }

    public List<Delivery> getDeliveries() {
        return new ArrayList<>(deliveries);
    }

    public List<Member> getMembers() {
        return new ArrayList<>(members.values());
    }

    public Set<String> getMemberIds() {
        return new HashSet<>(memberIds);
    }

    public int memberCount() {
        return memberIds.size();
    }

    public int deliveryCount() {
        return deliveries.size();
    }

    public Map<String, Double> getTotalPaymentByMember() {
        return new HashMap<>(totalPaymentByMember);
    }

    public Optional<Member> findMember(String memberId) {
        if (!Validation.isValidMemberId(memberId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(members.get(memberId));
    }

    public Map<String, List<Delivery>> deliveriesByMember() {
        Map<String, List<Delivery>> byMember = new LinkedHashMap<>();
        for (Member member : members.values()) {
            byMember.put(member.getMemberId(), new ArrayList<>(member.getDeliveries()));
        }
        return byMember;
    }

//  Highest value first. Uses the Comparator declared on Delivery
    public List<Delivery> topByValue(int howMany) {
        List<Delivery> sorted = new ArrayList<>(deliveries);
        sorted.sort(Delivery.BYDescValue);
        return sorted.subList(0, Math.min(howMany, sorted.size()));
    }

//  Member, then week, then slip number
    public List<Delivery> orderedForReport() {
        List<Delivery> sorted = new ArrayList<>(deliveries);
        sorted.sort(Delivery.ByMemberThenWeek);
        return sorted;
    }

//  Natural order, from Comparable: by delivery identifier
    public List<Delivery> inSlipOrder() {
        List<Delivery> sorted = new ArrayList<>(deliveries);
        java.util.Collections.sort(sorted);   // no Comparator: uses compareTo
        return sorted;
    }

//  Highest earner first. Uses the Comparator declared on Member
    public List<Member> membersByPayment() {
        List<Member> sorted = new ArrayList<>(members.values());
        sorted.sort(Member.BY_PAYMENT_DESC);
        return sorted;
    }


    public List<Delivery> acceptedDeliveries() {
        List<Delivery> accepted = new ArrayList<>(deliveries);

        accepted.removeIf(Delivery::isRejected);
        return accepted;
    }

    public int rejectedCount() {
        return deliveries.size() - acceptedDeliveries().size();
    }

    public double[][] weeklyGrid() {
        String[] codes = ProduceCatalog.codes();
        double[][] grid = new double[Validation.MaxWeek][codes.length];

        for (int week = Validation.MinWeek; week <= Validation.MaxWeek; week++) {
            for (int column = 0; column < codes.length; column++) {

                double total = 0.0;
                for (Delivery delivery : deliveries) {
                    if (delivery.getWeek() == week && delivery.getProduceCode().equals(codes[column])) {
                        total += delivery.getMassKg();
                    }
                }

                // Weeks are numbered from 1 but arrays are indexed from 0, so
                // week 1 is stored in row 0. This is the only place the offset
                // appears; the printer converts back with row + 1.
                grid[week - 1][column] = total;
            }
        }
        return grid;
    }

//  The season total: every member's net payable, added up
    public double seasonNetPayable() {
        double total = 0.0;
        for (Member member : members.values()) {
            total += member.netPayable();
        }
        return total;
    }

    public double seasonMassKg() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {
            total += delivery.getMassKg();
        }
        return total;
    }
//  Whole Season Data
    public static Season seeded() {
        Season season = new Season();
        season.nextDeliveryNumber = 1001;   // the seed occupies D-1001 upwards

        season.record("M-0042", "Devi Ramjaun",    produce("BNS"), 236.0, 91, 1);
        season.record("M-0117", "Jean Ah-Kine",    produce("MZE"), 412.5, 78, 1);
        season.record("M-0088", "Anisha Beeharry", produce("POT"), 150.0, 60, 2);
        season.record("M-0042", "Devi Ramjaun",    produce("TEA"),  88.3, 91, 1);
        season.record("M-0117", "Jean Ah-Kine",    produce("POT"), 200.0, 42, 3);  // REJECT
        season.record("M-0203", "Kavi Soobrayen",  produce("MZE"), 180.0, 66, 2);
        season.record("M-0311", "Marie Lafleur",   produce("BNS"), 390.5, 76, 2);
        season.record("M-0203", "Kavi Soobrayen",  produce("TEA"), 120.0, 85, 3);  // boundary: A
        season.record("M-0311", "Marie Lafleur",   produce("MZE"), 260.0, 70, 3);  // boundary: B
        season.record("M-0256", "Rajesh Gopaul",   produce("POT"), 320.0, 50, 4);  // boundary: C
        season.record("M-0256", "Rajesh Gopaul",   produce("BNS"),  75.5, 49, 4);  // boundary: REJECT
        season.record("M-0203", "Kavi Soobrayen",  produce("MZE"), 500.0, 84, 5);  // boundary: B

        return season;
    }

//  Seed helper: the code is known good, so the absent case cannot happen here
    private static Produce produce(String code) {
        return ProduceCatalog.forCode(code)
                .orElseThrow(() -> new IllegalStateException("Seed data names an unknown produce code: " + code));
    }
}