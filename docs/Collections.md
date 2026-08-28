# Collections Justification

Every collection in the tracker is justified below, followed by the alternative that was considered and rejected.

## Produce Lists

 private static final String[ ] produceCodes       = { "MZE", "BNS", "POT", "TEA" };
 
 private static final double[ ] basePricesPerKg    = { 30.0, 90.0, 45.0, 25.0 };
 
 private static final double[ ] categoryMultipliers= { 1.00, 1.00, 0.90, 1.10 };
 
 private static final String[ ] categoryNames      = { "Cereal", "Cereal", "Perishable", "Cash crop" };

 Access pattern: they are four parallel arrays, read by index. indexOfProduceCode("BNS") returns 1, and position 1 of every array is about beans. One index answers every question about a produce type.

 Ordering: The order is not incidental, it follows the column order of the weekly grid. Because the index of the product codes in the arrays is the column number in double[][] grid, the two cannot be separated. 

 Lookup cost: it takes O(n) time to look through the four entries and it will always stay constant since the array is fixed at four and cannot grow.(the cooperative buys four crops)

 Rejected alternative: HashMap<String, Double> for the prices. A map gives lookup by code without scanning, which sounds better. It was rejected because it destroys the property that earns the array its place here. A HashMap has no positions, so it cannot supply the grid column number, and a second structure would be needed to map codes to columns. With four entries the scan costs nothing, so the map's only advantage is one that does not apply at this size.

## The deliveries: ArrayList<Delivery>

 Access pattern: a delivery is added at the last position of the Array when it is newly recorded. There is no insertion or removal from the middle of the Array and to access other deliveries, the whole array has to be searched.

 Ordering: Insertion order, which is the order in which the slips were recorded from first to last. That is the season history and it must be preserved. Sorting is always done on a copy ArrayList, so the record of what happened is never rearranged.

 Lookup cost: adding an item to the array takes O(1) time since it is always one operation but searching is linear and will always take O(n) times since you have to go through the whole ArrayList every time.

 Rejected alternative: LinkedList<Delivery>. It is faster for inserting and removing in the middle but this program does not need it. In addition, it is slower to read by position and uses more memory per element. 

## Total payment per member: HashMap<String, Double>

 Access pattern: it is written once per delivery while doing additions for totals and then accessed by member identifier when searching for a specific member or generating a season report(upcoming objective)

 Ordering: None is needed, and a HashMap does not provide a specific order. Where an order is wanted on screen the identifiers are copied into a list and sorted by a comparator.

 Lookup cost: searching for a member takes O(1) time on average since it searches for a unique identifier per member. Thus, even when the number of members increase, the number of operations done stays constant. 

 Rejected alternative: TreeMap<String, Double>. It keeps its keys sorted, which costs more time per lookup instead of constant. Sorted by identifier is not an order this application needs because the totals table is sorted by amount and the search is by exact key.

## Deliveries per member: HashMap<String, List<Delivery>>

 Access pattern: each member's delivery records are stored under their identifier and fetched at once when that member's statement is being printed without the need to go through the whole season list.

 Ordering: The lists stay in the order they were written and if any sorting is needed, it is done on a new ArrayList copy. 

 Lookup cost: it takes O(1) time to find a member and then takes O(n) time to walk through all their delivery records. 

 Rejected alternative: scanning the delivery ArrayList once per member. While it is correct, the lookup cost would be very expensive because each member search would take O(n^2) times and for very large numbers, it would not be efficient. 

## Distinct member identifiers: HashSet<String>

 Access pattern: it stores recorded member ids and compares them to identify if each is unique to a member and how many they are. 

 Ordering: there is no particular ordering rule followed as long as each id stored is unique. Any sorting is done on a new copy ArrayList of the original HashSet.

 Lookup cost: it takes O(1) time to add or search for a member id because each one of them is unique. There is no moving through the whole list 

 Rejected alternative: a List<String> which contains a check before each add. The problem is that searching the whole list would not be efficient because it would have to look for distinct members list and the deliveries list which would take O(n^2) time. 
