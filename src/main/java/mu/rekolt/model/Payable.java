package mu.rekolt.model;

// Interface implemented by Delivery and Member
public interface Payable {

//  Net amount in MUR after the commission and the transport levy
    double netPayable();

//  Value in MUR before the commission and the levy, for the breakdown line
    double grossValue();
}