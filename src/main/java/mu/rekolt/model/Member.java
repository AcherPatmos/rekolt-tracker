package mu.rekolt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import mu.rekolt.util.Format;
import mu.rekolt.util.Validation;

public class Member implements Payable, Reportable, Comparable<Member> {

    private final String memberId;
    private final String name;
    private final List<Delivery> deliveries = new ArrayList<>();

    public Member(String memberId, String name) {
        if (!Validation.isValidMemberId(memberId)) {
            throw new IllegalArgumentException("Member id must be M and four digits, was " + memberId);
        }
        if (!Validation.isValidName(name)) {
            throw new IllegalArgumentException("Member name cannot be empty or only spaces");
        }
        this.memberId = memberId;
        this.name     = name.trim();
    }

    public String getMemberId() { return memberId; }
    public String getName()     { return name; }

    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public void addDelivery(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Cannot add a null delivery");
        }
        if (!delivery.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException(
                    "Delivery " + delivery.getDeliveryId() + " belongs to " + delivery.getMemberId()
                            + ", not to " + memberId);
        }
        deliveries.add(delivery);
    }

    public int deliveryCount() {
        return deliveries.size();
    }

//  Rejected loads are included: a REJECT still counts towards volume
    public double totalMassKg() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {
            total += delivery.getMassKg();
        }
        return total;
    }


    @Override
    public double netPayable() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {

            total += delivery.netPayable();
        }
        return total;
    }

    @Override
    public double grossValue() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {
            total += delivery.grossValue();
        }
        return total;
    }

    @Override
    public String reportTitle() {
        return memberId + "  " + name;
    }

    @Override
    public List<String> reportLines() {
        List<String> lines = new ArrayList<>();
        List<Delivery> ordered = new ArrayList<>(deliveries);
        ordered.sort(Delivery.ByMemberThenWeek);

        for (Delivery delivery : ordered) {
            lines.add(String.format("%-8s wk %-3d %-4s %10s kg  %-6s %14s",
                    delivery.getDeliveryId(), delivery.getWeek(), delivery.getProduceCode(),
                    Format.kg(delivery.getMassKg()), delivery.getGrade(),
                    Format.money(delivery.netPayable())));
        }
        lines.add(String.format("%d deliveries, %s kg, NET PAYABLE %s MUR",
                deliveryCount(), Format.kg(totalMassKg()), Format.money(netPayable())));
        return lines;
    }

    public double totalCommission() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {
            total += PaymentRules.commission(delivery.getProduce(), delivery.getGrade(), delivery.getMassKg());
        }
        return total;
    }

//  The transport levy this member paid across the season
    public double totalLevy() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {
            total += PaymentRules.levy(delivery.getGrade(), delivery.getMassKg());
        }
        return total;
    }

//  Natural order: by identifier, so member sections in the report are stable
    @Override
    public int compareTo(Member other) {
        return this.memberId.compareTo(other.memberId);
    }

//  Highest earner first, for the on-screen totals
    public static final Comparator<Member> BY_PAYMENT_DESC =
            Comparator.comparingDouble(Member::netPayable).reversed();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Member that = (Member) other;
        return memberId.equals(that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return memberId + " " + name + " (" + deliveryCount() + " deliveries)";
    }
}