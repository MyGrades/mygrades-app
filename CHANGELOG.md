# Changelog

# 1.1.0 (next release)

- Added extensive edit mode to modify grade entries
 - change all properties (grade, cp, weight, ...)
 - move grade entry to another semester
- Appended *attempt* to grade entry hash, because some universities may not provide enough distinct information for each entry
- Added property *weight* to GradeEntry to allow better average calculation

# 1.0.8

- Fix: AverageCalculator adds credit points without grade to overall sum of credit points