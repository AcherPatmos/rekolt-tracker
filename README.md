# REKOLT Planters' Cooperative Produce Tracker

Java console application that records deliveries, applies the cooperative's
payment rules, and writes one Microsoft Word season report.

## Build and run from a clean clone

```
git clone <this repo>
cd rekolt-tracker
mvn -q compile exec:java
```

Maven downloads Apache POI on the first run.

Alternatively, `mvn -q package` produces `target/rekolt-tracker-1.0.0.jar`
with POI bundled in, runnable with `java -jar target/rekolt-tracker-1.0.0.jar`.

Requires JDK 21 or later and Maven 3.8+. Run from the project root, because
the report is written to `output/` relative to the working directory.

## The menu

| Option | What it does |
|---|---|
| 1 | Record a delivery, then print its five-step breakdown |
| 2 | Season figures: member totals, weekly grid, top five, price list |
| 3 | Write `output/season-report.docx` and append to `output/run-log.txt` |
| 4 | Look up one member's statement |
| 5 | Exit |

The season starts with twelve deliveries held in code. The first five reproduce
the worked example and the sample run from the specification, so the program
checks itself on every start.

## Types and precision

`double` for mass and money, because kilograms are measured and money has
decimals. `int` for the quality score, the week and the commission percentage,
because those are counted. The cast in `PaymentRules` is required: without it,
`5 / 100` is integer division and every commission would be zero.

Intermediate values are never rounded. `Format` rounds at display time and is
the only place in the program that does except for one deliberate exception,
described below.

### The one rounding decision

Each **member's** net payable is rounded to the cent (`Money.toCents`) before it
is printed or totalled, because that is the amount actually handed over. The
season total is the sum of those cent-exact payments.

## Layout

```
src/main/java/mu/rekolt/
├── app/      RekoltApp (menu), MainConsoleReader (prompts)
├── model/    Produce + 3 subclasses, Delivery, Member, Grade, PaymentRules,
│             Payable, Reportable
├── service/  ProduceCatalog, SeasonService, SeasonReport, ReportService,
│             DocxReportWriter
└── util/     Format, Validation, Money, RunLog
```

Dependencies flow one way: `app` → `service` → `model` → `util`.
See `docs/Designv1` for every class and its callers.

## Attribution

Apache POI 5.5.1 (Apache License 2.0) is used to write the .docx.
No other third-party code is included.
