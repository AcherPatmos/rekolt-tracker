# Deviations from design-v1 (objective 4)

this is to document where I deviated from the design-v1 I made while building the system and why 
I made changes. 

## 1. `Produce` became abstract with three subclasses
Objective 4's design had produce as a code plus a category field. That would
have meant a `switch` on the category to pick the multiplier, which objective 5 instructs to change. 
The category is now the subclass,
so the object supplies the multiplier and no branch chooses it.

## 2. `Member` is a new class
Objective 2 stored `memberName` on every `Delivery`, so the same fact was
copied once per slip. `Member` now owns the name and the list of slips.

## 3. `PaymentRules` sits in `model`, not `service`
I had first put payment rule under `service/`. It is in
`model/` instead because `Delivery.netPayable()` must call it, and a service
class calling back down into the model while the model calls up into the
service is a circular package dependency. Keeping the rule in `model` makes the
arrows point one way: `app` → `service` → `model` → `util`.

## 4. Grading logic moved into the `Grade` enum
`gradeOf` and `gradeMultiplierOf` were two separate methods in `RekoltApp`. Both
now live on the enum constant.

## 5. `Validation` holds predicates, `ConsoleReader` holds prompts
The rules were previously written inside the prompting loops, which meant the
model constructors could not reuse them and would have restated the bounds.
Split so one rule has two callers.

## 6. Member identifiers are accepted in either case
`m-0042` is upper-cased at the prompt and accepted. The specification is
explicit that produce codes may be typed in either case but says nothing about
identifiers. Normalising both at the door keeps the two identifier prompts from
disagreeing with each other. 

## 7. Menu gained a fifth option
"Look up a member" was added to demonstrate the objective-3 search with the
absent case. Exit moved from 4 to 5.
