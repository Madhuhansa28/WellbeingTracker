# Analysis plan (written before analysing real data)

## Research questions
1. Within a person, are days with more social + video time than usual
   associated with lower mood the same day?
2. Does yesterday's social + video time predict today's mood (lagged effect)?
3. Does yesterday's mood predict today's social + video time (reverse direction)?

## Data rules (decided in advance)
- Several check-ins on the same day → use the **mean** of that day.
- Days without a check-in are **dropped**, not filled in.
- Lagged values are used only when the previous day is exactly one day earlier.
- Main exposure: social + video minutes per day, in hours ("entertainment").

## Methods
- Separate **within-person** (daily deviation from own average) from
  **between-person** (person's average) effects by person-mean centring.
- Linear mixed-effects model with a random intercept per participant,
  controlling for sleep and weekend.
- The method is first validated on synthetic data with known true effects.

## Pilot data
- n = 1, ~1 week: used only to check **feasibility** (does the app collect
  usable data?), not to draw conclusions.
## Pilot data
- Real data was collected with the app (n = 1, ~1 week) and exported
  successfully, but was not analysed in this version (future work).