package mu.rekolt.model;

import java.util.List;

// Interface Implemented by Delivery and Member in Objective 6. Every reportable object is included.
public interface Reportable {

//  The heading this item appears under
    String reportTitle();

//  The body lines beneath that heading, already formatted for display
    List<String> reportLines();
}