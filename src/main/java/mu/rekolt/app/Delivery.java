package mu.rekolt.app;

public abstract class Delivery {
    private final String deliveryId;
    private final String memberId;
    private final String memberName;
    private final String produceCode;
    private final double massKg;
    private final int qualityScore;
    private final int week;

    public Delivery(String deliveryId, String memberId, String memberName,
                    String produceCode, double massKg, int qualityScore, int week) {
        this.deliveryId = deliveryId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.produceCode = produceCode;
        this.massKg = massKg;
        this.qualityScore = qualityScore;
        this.week = week;
    }

    public String getDeliveryId()  { return deliveryId; }
    public String getMemberId()    { return memberId; }
    public String getMemberName()  { return memberName; }
    public String getProduceCode() { return produceCode; }
    public double getMassKg()      { return massKg; }
    public int    getQualityScore(){ return qualityScore; }
    public int    getWeek()        { return week; }
}
