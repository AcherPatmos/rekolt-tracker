package mu.rekolt.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Member;
import mu.rekolt.model.PaymentRules;
import mu.rekolt.util.RoundMoney;

//  Every figure the Word report prints, and no POI code at all.

public final class DocxReportFigures {

    private static final DateTimeFormatter Stamp =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Season season;
    private final LocalDateTime generatedAt;
    private final List<Member> members;

    public DocxReportFigures(Season season) {
        if (season == null) {
            throw new IllegalArgumentException("A report needs a season");
        }
        this.season      = season;
        this.generatedAt = LocalDateTime.now();

//      Sorted by identifier through Member.compareTo, so the sections come out
//      in the same order on every run and the treasurer can find a name
        this.members = new ArrayList<>(season.getMembers());
        this.members.sort(null);
    }

    public List<Member> members()     { return List.copyOf(members); }
    public int    memberCount()       { return members.size(); }
    public int    deliveryCount()     { return season.deliveryCount(); }
    public int    rejectedCount()     { return season.rejectedCount(); }
    public String generatedAtStamp()  { return generatedAt.format(Stamp); }
    public int    commissionPercentage() { return PaymentRules.CommissionPercentage; }
    public double levyPerKg()         { return PaymentRules.LevyPerKg; }

    //  A member's slips in report order: week, then slip number
    public List<Delivery> deliveriesOf(Member member) {
        List<Delivery> ordered = new ArrayList<>(member.getDeliveries());
        ordered.sort(Delivery.ByMemberThenWeek);
        return ordered;
    }

    //  Per-member figures, rounded to the cent because these are what is paid
    public double netOf(Member member)        { return RoundMoney.toCents(member.netPayable()); }
    public double grossOf(Member member)      { return RoundMoney.toCents(member.grossValue()); }
    public double commissionOf(Member member) { return RoundMoney.toCents(member.totalCommission()); }
    public double levyOf(Member member)       { return RoundMoney.toCents(member.totalLevy()); }

    //  Closing totals: the sum of what the member pages actually show, so the last
//  page cannot disagree with the pages before it
    public double seasonGross() {
        double total = 0.0;
        for (Member member : members) {
            total += grossOf(member);
        }
        return total;
    }

    public double seasonCommission() {
        double total = 0.0;
        for (Member member : members) {
            total += commissionOf(member);
        }
        return total;
    }

    public double seasonLevy() {
        double total = 0.0;
        for (Member member : members) {
            total += levyOf(member);
        }
        return total;
    }

    public double seasonNet() {
        double total = 0.0;
        for (Member member : members) {
            total += netOf(member);
        }
        return total;
    }

    public double seasonMassKg() {
        double total = 0.0;
        for (Member member : members) {
            total += member.totalMassKg();
        }
        return total;
    }

    //  Residue left by rounding each member separately. Each can be up to half a
//  cent out and the halves do not cancel. Shown as its own line, not hidden.
    public double roundingAdjustment() {
        return seasonNet() - (seasonGross() - seasonCommission() - seasonLevy());
    }

    //  The reconciliation objective 6 asks for: do the printed member pages add up
//  to the printed closing total? Comparing unrounded doubles here would return
//  true while the printed pages were a cent apart.
    public boolean reconciles() {
        double sumOfPages = 0.0;
        for (Member member : members) {
            sumOfPages += netOf(member);
        }
        boolean pagesAgree = RoundMoney.sameToTheCent(sumOfPages, seasonNet());

//      Sanity bound, not a rounding check: the printed total should never drift
//      from the season's own running total by more than a cent per member. A
//      larger gap means a delivery counted twice or missed, which rounding
//      cannot cause.
        double drift = Math.abs(seasonNet() - season.seasonNetPayable());
        boolean withinRounding = drift <= (0.01 * memberCount()) + RoundMoney.CentTolerance;

        return pagesAgree && withinRounding;
    }

    public double reconciliationGap() {
        return seasonNet() - RoundMoney.toCents(season.seasonNetPayable());
    }
}